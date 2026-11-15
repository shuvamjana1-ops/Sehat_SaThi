package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class OfflineTaskKind(val title: String) {
  METER_PHOTO("Meter photo scan"),
  PRESCRIPTION_PHOTO("Prescription scan"),
  VOICE_PHRASE("Voice reading phrase"),
}

data class OfflineQueuedTask(
  val id: String,
  val kind: OfflineTaskKind,
  val summary: String,
  val createdAtLabel: String,
  val bitmap: Bitmap? = null,
  val transcript: String? = null,
)

object OfflineQueueHelper {
  private const val MAX_QUEUE_SIZE = 12

  fun enqueue(
    current: List<OfflineQueuedTask>,
    kind: OfflineTaskKind,
    summary: String,
    bitmap: Bitmap? = null,
    transcript: String? = null,
  ): List<OfflineQueuedTask> {
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    val next =
      OfflineQueuedTask(
        id = "oq-${System.currentTimeMillis()}-${current.size}",
        kind = kind,
        summary = summary,
        createdAtLabel = time,
        bitmap = bitmap,
        transcript = transcript,
      )
    return (current + next).takeLast(MAX_QUEUE_SIZE)
  }

  fun remove(current: List<OfflineQueuedTask>, taskId: String): List<OfflineQueuedTask> =
    current.filterNot { it.id == taskId }
}

class ConnectivityObserver(context: Context) {
  private val cm =
    context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

  fun isCurrentlyOnline(): Boolean {
    val manager = cm ?: return true
    val network = manager.activeNetwork ?: return false
    val caps = manager.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  val onlineFlow: Flow<Boolean> = callbackFlow {
    val manager = cm
    if (manager == null) {
      trySend(true)
      close()
      return@callbackFlow
    }

    trySend(isCurrentlyOnline())

    val callback =
      object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          trySend(true)
        }

        override fun onLost(network: Network) {
          trySend(isCurrentlyOnline())
        }

        override fun onCapabilitiesChanged(
          network: Network,
          networkCapabilities: NetworkCapabilities,
        ) {
          val hasInternet =
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          trySend(hasInternet)
        }
      }

    val registered =
      runCatching {
        val request =
          NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        manager.registerNetworkCallback(request, callback)
        true
      }.getOrDefault(false)

    awaitClose {
      if (registered) {
        runCatching { manager.unregisterNetworkCallback(callback) }
      }
    }
  }.distinctUntilChanged()
}

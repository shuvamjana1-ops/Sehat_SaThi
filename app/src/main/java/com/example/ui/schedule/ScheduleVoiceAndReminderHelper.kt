package com.example.ui.schedule

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.DaySlot
import com.example.data.SeededDoseEvent
import java.util.Locale

class ScheduleSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
  private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
  private var ready = false
  private var pendingText: String? = null

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val indianEnglish = Locale.Builder().setLanguage("en").setRegion("IN").build()
      val res = tts?.setLanguage(indianEnglish)
      if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setLanguage(Locale.ENGLISH)
      }
      tts?.setSpeechRate(0.90f)
      ready = true
      pendingText?.let {
        speak(it)
        pendingText = null
      }
    }
  }

  fun speak(text: String) {
    if (!ready) {
      pendingText = text
      return
    }
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sehat_schedule_tts")
  }

  fun stop() {
    tts?.stop()
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
    ready = false
  }

  companion object {
    fun buildScheduleSpeechText(doses: List<SeededDoseEvent>): String {
      if (doses.isEmpty()) return "You have no medicines scheduled today."
      val grouped = doses.groupBy { it.slot }
      val parts = mutableListOf("Here is your daily medicine schedule.")
      DaySlot.entries.forEach { slot ->
        val items = grouped[slot].orEmpty()
        if (items.isNotEmpty()) {
          val listText = items.joinToString(", and ") { "${it.medicineLabel}, ${it.instruction}" }
          parts += "${slot.title} around ${slot.timeWindow}: $listText."
        }
      }
      return parts.joinToString(" ")
    }
  }
}

object ScheduleNotificationHelper {
  private const val CHANNEL_ID = "sehatsaathi_dose_reminders"

  fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
      PackageManager.PERMISSION_GRANTED
  }

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(
          CHANNEL_ID,
          "Medicine Reminders",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Daily medicine schedule reminders"
        }
      val manager = context.getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  @SuppressLint("MissingPermission")
  fun showDoseReminderNotification(context: Context, dose: SeededDoseEvent): Boolean {
    if (!hasNotificationPermission(context)) return false
    ensureChannel(context)

    val intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        101,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Medicine reminder: ${dose.slot.title}")
        .setContentText("${dose.medicineLabel}: ${dose.instruction}")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(dose.id.hashCode(), notification)
    return true
  }
}

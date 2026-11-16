package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

sealed interface ImageLoadResult {
  data class Success(val bitmap: Bitmap, val compressedKb: Int) : ImageLoadResult
  data class Error(val message: String) : ImageLoadResult
}

object ImageUploadHelper {
  const val MAX_UPLOAD_BYTES: Long = 15L * 1024L * 1024L
  const val MAX_DIMENSION_PX: Int = 1600
  const val JPEG_QUALITY: Int = 80

  fun computeInSampleSize(
    rawWidth: Int,
    rawHeight: Int,
    reqWidth: Int = MAX_DIMENSION_PX,
    reqHeight: Int = MAX_DIMENSION_PX,
  ): Int {
    var inSampleSize = 1
    if (rawHeight > reqHeight || rawWidth > reqWidth) {
      val halfHeight = rawHeight / 2
      val halfWidth = rawWidth / 2
      while (
        (halfHeight / inSampleSize) >= reqHeight &&
          (halfWidth / inSampleSize) >= reqWidth
      ) {
        inSampleSize *= 2
      }
    }
    return inSampleSize.coerceAtLeast(1)
  }

  fun scaleAndCompressBitmap(
    source: Bitmap,
    maxDimensionPx: Int = MAX_DIMENSION_PX,
    jpegQuality: Int = JPEG_QUALITY,
  ): Pair<Bitmap, Int> {
    val longest = maxOf(source.width, source.height).coerceAtLeast(1)
    val scaled =
      if (longest <= maxDimensionPx) {
        source
      } else {
        val ratio = maxDimensionPx.toFloat() / longest.toFloat()
        val targetW = (source.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (source.height * ratio).roundToInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(source, targetW, targetH, true)
      }

    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, jpegQuality.coerceIn(50, 95), stream)
    val bytes = stream.toByteArray()
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: scaled
    val kb = ((bytes.size + 1023) / 1024).coerceAtLeast(1)
    return decoded to kb
  }

  fun loadAndCompressImage(
    context: Context,
    uri: Uri,
    maxDimensionPx: Int = MAX_DIMENSION_PX,
    maxBytes: Long = MAX_UPLOAD_BYTES,
    jpegQuality: Int = JPEG_QUALITY,
  ): ImageLoadResult {
    return try {
      val resolver = context.contentResolver
      val mime = resolver.getType(uri)
      if (mime != null && !mime.startsWith("image/", ignoreCase = true)) {
        return ImageLoadResult.Error("Please choose an image file (JPG, PNG, or WebP).")
      }

      val reportedSize = queryFileSizeBytes(context, uri)
      if (reportedSize != null && reportedSize > maxBytes) {
        return ImageLoadResult.Error(
          "This image is too large (max 15 MB). Please choose a smaller photo.",
        )
      }

      val rawBitmap: Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          val source = ImageDecoder.createSource(resolver, uri)
          ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.isMutableRequired = true
            val w = info.size.width
            val h = info.size.height
            val longest = maxOf(w, h)
            if (longest > maxDimensionPx && longest > 0) {
              val ratio = maxDimensionPx.toFloat() / longest.toFloat()
              decoder.setTargetSize(
                (w * ratio).roundToInt().coerceAtLeast(1),
                (h * ratio).roundToInt().coerceAtLeast(1),
              )
            }
          }
        } else {
          val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
          resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
          }
          val sample =
            if (boundsOpts.outWidth > 0 && boundsOpts.outHeight > 0) {
              computeInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, maxDimensionPx, maxDimensionPx)
            } else {
              1
            }
          val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
          resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
          }
        }

      if (rawBitmap == null) {
        return ImageLoadResult.Error("Could not read that image. Please try another photo.")
      }

      val (compressed, kb) = scaleAndCompressBitmap(rawBitmap, maxDimensionPx, jpegQuality)
      ImageLoadResult.Success(bitmap = compressed, compressedKb = kb)
    } catch (_: OutOfMemoryError) {
      ImageLoadResult.Error("That image is too large to open safely. Please pick a smaller photo.")
    } catch (_: Throwable) {
      ImageLoadResult.Error("Could not open that photo. Please try another or enter details yourself.")
    }
  }

  private fun queryFileSizeBytes(context: Context, uri: Uri): Long? {
    return runCatching {
      context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        } else {
          null
        }
      }
    }.getOrNull()
  }
}

fun sanitizeDialNumber(raw: String): String {
  val trimmed = raw.trim()
  if (trimmed.isEmpty()) return ""
  val hasPlus = trimmed.startsWith("+")
  val digitsOnly = trimmed.filter { it.isDigit() }
  if (digitsOnly.isEmpty()) return ""
  return if (hasPlus) "+$digitsOnly" else digitsOnly
}

fun launchSafeDialer(
  context: Context,
  phone: String,
  onError: ((String) -> Unit)? = null,
) {
  val cleaned = sanitizeDialNumber(phone)
  if (cleaned.isEmpty()) {
    onError?.invoke("Phone number is empty.")
    return
  }
  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned")).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  runCatching { context.startActivity(intent) }.onFailure {
    onError?.invoke("No phone app is available on this device.")
  }
}

fun launchSafeEmail(
  context: Context,
  email: String,
  subject: String = "",
  onError: ((String) -> Unit)? = null,
) {
  val cleaned = email.trim()
  if (cleaned.isEmpty() || !cleaned.contains("@")) {
    onError?.invoke("Email address is not valid.")
    return
  }
  val uriBuilder = StringBuilder("mailto:").append(Uri.encode(cleaned))
  if (subject.isNotBlank()) {
    uriBuilder.append("?subject=").append(Uri.encode(subject))
  }
  val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriBuilder.toString())).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  runCatching { context.startActivity(intent) }.onFailure {
    onError?.invoke("No email app is configured on this device.")
  }
}

fun launchGoogleMapsSearch(
  context: Context,
  query: String = "hospital emergency near me",
  onError: ((String) -> Unit)? = null,
) {
  val encoded = Uri.encode(query.ifBlank { "hospital emergency near me" })
  val gmmIntentUri = Uri.parse("geo:0,0?q=$encoded")
  val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
    setPackage("com.google.android.apps.maps")
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  val resolvedIntent = if (mapIntent.resolveActivity(context.packageManager) != null) {
    mapIntent
  } else {
    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }
  runCatching { context.startActivity(resolvedIntent) }.onFailure {
    onError?.invoke("Could not open Google Maps.")
  }
}


package com.example.ui.components

import android.provider.Settings
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

@Composable
fun rememberPrefersReducedMotion(): Boolean {
  val context = LocalContext.current
  return remember(context) {
    try {
      val scale =
        Settings.Global.getFloat(
          context.contentResolver,
          Settings.Global.ANIMATOR_DURATION_SCALE,
          1f,
        )
      scale == 0f
    } catch (_: Throwable) {
      false
    }
  }
}

/**
 * Applies smooth, zero-recomposition 3D perspective depth (3D rotationX/Y, cylinder scale,
 * parallax lift, and edge fade) to LazyColumn cards as they enter and leave the viewport.
 */
fun Modifier.sehatScroll3DKeyed(
  listState: LazyListState,
  itemKey: Any,
  reducedMotion: Boolean,
  maxTiltDegrees: Float = 12.5f,
): Modifier {
  if (reducedMotion) return this
  return this.graphicsLayer {
    val layoutInfo = listState.layoutInfo
    val viewportHeight = layoutInfo.viewportSize.height.toFloat()
    if (viewportHeight <= 0f) return@graphicsLayer

    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey } ?: return@graphicsLayer
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
    val itemCenter = itemInfo.offset + itemInfo.size / 2f
    val normalized = ((itemCenter - viewportCenter) / (viewportHeight * 0.55f)).coerceIn(-1f, 1f)
    val absNorm = abs(normalized)
    val curved = absNorm * absNorm

    cameraDistance = 12f * density
    rotationX = -normalized * maxTiltDegrees
    rotationY = normalized * 2.0f
    val scale = 1f - (curved * 0.07f)
    scaleX = scale
    scaleY = scale
    alpha = (1f - (curved * 0.16f)).coerceIn(0.82f, 1f)
    translationY = normalized * 14f
  }
}

/**
 * Applies smooth 3D perspective depth to items inside a vertically scrolling Column (`ScrollState`)
 * without triggering recomposition on scroll frames.
 */
fun Modifier.sehatColumnScroll3D(
  scrollState: ScrollState,
  viewportHeightPx: Float,
  reducedMotion: Boolean,
  maxTiltDegrees: Float = 11.5f,
): Modifier {
  if (reducedMotion) return this
  return composed {
    val metrics = remember { floatArrayOf(0f, 0f) } // [windowY, heightPx]
    this
      .onGloballyPositioned { coords ->
        metrics[0] = coords.positionInWindow().y
        metrics[1] = coords.size.height.toFloat()
      }
      .graphicsLayer {
        // Read scrollState.value so the graphics layer updates cleanly on every scroll pixel.
        @Suppress("UNUSED_VARIABLE")
        val scrollTick = scrollState.value
        if (viewportHeightPx <= 0f || metrics[1] <= 0f) return@graphicsLayer

        val viewportCenter = viewportHeightPx * 0.5f
        val itemCenter = metrics[0] + metrics[1] * 0.5f
        val normalized = ((itemCenter - viewportCenter) / (viewportHeightPx * 0.58f)).coerceIn(-1f, 1f)
        val absNorm = abs(normalized)
        val curved = absNorm * absNorm

        cameraDistance = 12f * density
        rotationX = -normalized * maxTiltDegrees
        rotationY = normalized * 1.8f
        val scale = 1f - (curved * 0.065f)
        scaleX = scale
        scaleY = scale
        alpha = (1f - (curved * 0.14f)).coerceIn(0.84f, 1f)
        translationY = normalized * 12f
      }
  }
}

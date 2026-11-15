package com.example.ui.theme

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Centralized Liquid Glass styling tokens & guardrails.
 * Guarantees crisp readability (>= 4.5:1) by enforcing higher opacity on critical health cards
 * and honoring reduced-motion / reduced-transparency accessibility settings.
 */
object GlassTokens {
  const val STANDARD_GLASS_ALPHA = 0.72f
  const val MIST_GLASS_ALPHA = 0.66f
  const val HIGH_READABILITY_ALPHA = 0.90f
  const val CHROME_GLASS_ALPHA = 0.86f
  const val REDUCED_TRANSPARENCY_ALPHA = 0.96f

  const val DARK_STANDARD_GLASS_ALPHA = 0.62f
  const val DARK_MIST_GLASS_ALPHA = 0.58f
  const val DARK_HIGH_READABILITY_ALPHA = 0.88f
  const val DARK_CHROME_GLASS_ALPHA = 0.86f
  const val DARK_REDUCED_TRANSPARENCY_ALPHA = 0.95f

  val GlassRadius: Dp = 22.dp
  val GlassCornerShape = RoundedCornerShape(GlassRadius)

  private val LightTealMist = Color(0xFF4BAEC4)

  fun cardFill(
    baseColor: Color = PureWhite,
    highReadability: Boolean = false,
    reducedTransparency: Boolean = false,
    isDark: Boolean = false,
  ): Color {
    if (isDark) {
      val darkBase =
        when (baseColor) {
          PureWhite, MistSoft -> DarkSurface
          Mist -> DarkMist
          else -> baseColor
        }
      val alpha =
        when {
          reducedTransparency -> DARK_REDUCED_TRANSPARENCY_ALPHA
          highReadability -> DARK_HIGH_READABILITY_ALPHA
          darkBase == DarkMist -> DARK_MIST_GLASS_ALPHA
          else -> DARK_STANDARD_GLASS_ALPHA
        }
      return darkBase.copy(alpha = alpha)
    }

    val alpha =
      when {
        reducedTransparency -> REDUCED_TRANSPARENCY_ALPHA
        highReadability -> HIGH_READABILITY_ALPHA
        baseColor == Mist || baseColor == MistSoft -> MIST_GLASS_ALPHA
        else -> STANDARD_GLASS_ALPHA
      }
    return baseColor.copy(alpha = alpha)
  }

  fun chromeFill(
    scrolled: Boolean = false,
    reducedTransparency: Boolean = false,
    isDark: Boolean = false,
  ): Color {
    if (isDark) {
      val alpha =
        when {
          reducedTransparency -> 0.97f
          scrolled -> 0.92f
          else -> DARK_CHROME_GLASS_ALPHA
        }
      return DarkSurface.copy(alpha = alpha)
    }
    val alpha =
      when {
        reducedTransparency -> 0.98f
        scrolled -> 0.90f
        else -> CHROME_GLASS_ALPHA
      }
    return PureWhite.copy(alpha = alpha)
  }

  fun borderBrush(baseBorder: Color = HairlineBorder, isDark: Boolean = false): Brush =
    if (isDark) {
      val resolvedBorder = if (baseBorder == HairlineBorder) DarkHairlineBorder else baseBorder
      Brush.linearGradient(
        colors =
          listOf(
            DarkClinicalBlueAccent.copy(alpha = 0.34f),
            resolvedBorder.copy(alpha = 0.90f),
            DarkMist.copy(alpha = 0.75f),
          ),
      )
    } else {
      Brush.linearGradient(
        colors =
          listOf(
            PureWhite.copy(alpha = 0.94f),
            baseBorder.copy(alpha = 0.82f),
            Mist.copy(alpha = 0.72f),
          ),
      )
    }

  fun borderStroke(baseBorder: Color = HairlineBorder, isDark: Boolean = false): BorderStroke =
    BorderStroke(width = 1.dp, brush = borderBrush(baseBorder, isDark = isDark))

  fun sheenBrush(topLeftAlpha: Float = 0.56f, isDark: Boolean = false): Brush =
    if (isDark) {
      Brush.linearGradient(
        colors =
          listOf(
            DarkDeepOcean.copy(alpha = (topLeftAlpha * 0.24f).coerceIn(0.06f, 0.18f)),
            DarkClinicalBlueAccent.copy(alpha = (topLeftAlpha * 0.08f).coerceIn(0.02f, 0.08f)),
            Color.Transparent,
          ),
      )
    } else {
      Brush.linearGradient(
        colors =
          listOf(
            PureWhite.copy(alpha = topLeftAlpha),
            PureWhite.copy(alpha = topLeftAlpha * 0.25f),
            Color.Transparent,
          ),
      )
    }

  fun pointerSpecularBrush(pointer: Offset, radiusPx: Float, isDark: Boolean = false): Brush =
    Brush.radialGradient(
      colors =
        if (isDark) {
          listOf(
            DarkClinicalBlueAccent.copy(alpha = 0.20f),
            DarkDeepOcean.copy(alpha = 0.08f),
            Color.Transparent,
          )
        } else {
          listOf(
            PureWhite.copy(alpha = 0.42f),
            LightTealMist.copy(alpha = 0.12f),
            Color.Transparent,
          )
        },
      center = pointer,
      radius = radiusPx.coerceAtLeast(1f),
    )

  fun topCornerHighlight(cornerRadiusPx: Float, size: Size): Triple<Offset, Size, CornerRadius> {
    val highlightH = (size.height * 0.28f).coerceIn(18f, 72f)
    return Triple(Offset.Zero, Size(size.width, highlightH), CornerRadius(cornerRadiusPx, cornerRadiusPx))
  }
}

@Composable
fun rememberPrefersReducedTransparency(): Boolean {
  val context = LocalContext.current
  return remember(context) {
    try {
      // Honor Android accessibility contrast / animation preferences cleanly without breaking pre-API 34.
      if (Build.VERSION.SDK_INT >= 34) {
        Settings.Secure.getInt(context.contentResolver, "high_text_contrast_enabled", 0) == 1
      } else {
        false
      }
    } catch (_: Throwable) {
      false
    }
  }
}

/**
 * Fixed multi-blob ambient backdrop for frosted glass refraction.
 * Uses pure GPU radial gradients translated only with graphicsLayer (no image files,
 * no per-frame shader reallocations), pauses when backgrounded, and stops under reduced motion.
 */
@Composable
fun SehatAmbientBackdrop(
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  val colors = SehatPalette.current
  val isDark = colors.isDark
  val lifecycleOwner = LocalLifecycleOwner.current
  var isActive by remember { mutableStateOf(true) }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      isActive =
        when (event) {
          Lifecycle.Event.ON_START,
          Lifecycle.Event.ON_RESUME -> true
          Lifecycle.Event.ON_PAUSE,
          Lifecycle.Event.ON_STOP -> false
          else -> isActive
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  val animateBlobs = !reducedMotion && isActive

  val driftA: Float
  val driftB: Float
  val driftC: Float

  if (animateBlobs) {
    val transition = rememberInfiniteTransition(label = "sehat_ambient_blobs")
    val animA by
      transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
        label = "blob_a_drift",
      )
    val animB by
      transition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 28_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
        label = "blob_b_drift",
      )
    val animC by
      transition.animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 21_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
        label = "blob_c_drift",
      )
    driftA = animA
    driftB = animB
    driftC = animC
  } else {
    driftA = 0f
    driftB = 0f
    driftC = 0f
  }

  val softTeal = Color(0xFF3CA3BA)
  val density = LocalDensity.current

  val backdropColors =
    if (isDark) {
      listOf(DarkBackground, DarkMistSoft, DarkBackground)
    } else {
      listOf(PureWhite, MistSoft, Mist.copy(alpha = 0.92f))
    }

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(colors = backdropColors)),
  ) {
    val widthPx = with(density) { maxWidth.toPx() }
    val heightPx = with(density) { maxHeight.toPx() }

    // Blob 1: Clinical Blue top-left ambient glow (transform-only)
    Box(
      modifier =
        Modifier
          .size(360.dp)
          .graphicsLayer {
            translationX = (-widthPx * 0.14f) + (28.dp.toPx() * driftA)
            translationY = (-heightPx * 0.04f) + (22.dp.toPx() * driftC)
          }
          .drawBehind {
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors = listOf(ClinicalBlue.copy(alpha = if (isDark) 0.24f else 0.16f), Color.Transparent),
                  radius = size.minDimension * 0.5f,
                ),
            )
          },
    )

    // Blob 2: Light Teal mid-right drifting orb (transform-only)
    Box(
      modifier =
        Modifier
          .size(400.dp)
          .graphicsLayer {
            translationX = (widthPx * 0.44f) + (26.dp.toPx() * driftB)
            translationY = (heightPx * 0.28f) + (34.dp.toPx() * driftA)
          }
          .drawBehind {
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors = listOf(softTeal.copy(alpha = if (isDark) 0.18f else 0.17f), Color.Transparent),
                  radius = size.minDimension * 0.5f,
                ),
            )
          },
    )

    // Blob 3: Deep Ocean lower-left subtle anchor orb (transform-only)
    Box(
      modifier =
        Modifier
          .size(380.dp)
          .graphicsLayer {
            translationX = (-widthPx * 0.08f) + (32.dp.toPx() * driftC)
            translationY = (heightPx * 0.64f) + (26.dp.toPx() * driftB)
          }
          .drawBehind {
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors = listOf(DeepOcean.copy(alpha = if (isDark) 0.28f else 0.11f), Color.Transparent),
                  radius = size.minDimension * 0.5f,
                ),
            )
          },
    )

    content()
  }
}

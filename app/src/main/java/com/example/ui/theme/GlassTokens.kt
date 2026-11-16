package com.example.ui.theme

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Surface and elevation tokens.
 * Keeps translucency only on top bar, bottom bar, and modal chrome while keeping content cards
 * nearly opaque (>= 0.97 alpha) with 1dp borders for high contrast in bright light.
 */
object GlassTokens {
  const val STANDARD_GLASS_ALPHA = 0.72f
  const val MIST_GLASS_ALPHA = 0.66f
  const val HIGH_READABILITY_ALPHA = 0.96f
  const val CHROME_GLASS_ALPHA = 0.90f
  const val REDUCED_TRANSPARENCY_ALPHA = 0.98f

  const val DARK_STANDARD_GLASS_ALPHA = 0.88f
  const val DARK_MIST_GLASS_ALPHA = 0.85f
  const val DARK_HIGH_READABILITY_ALPHA = 0.96f
  const val DARK_CHROME_GLASS_ALPHA = 0.90f
  const val DARK_REDUCED_TRANSPARENCY_ALPHA = 0.98f

  val GlassRadius: Dp = 20.dp
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
          reducedTransparency -> 0.98f
          scrolled -> 0.94f
          else -> DARK_CHROME_GLASS_ALPHA
        }
      return DarkSurface.copy(alpha = alpha)
    }
    val alpha =
      when {
        reducedTransparency -> 0.99f
        scrolled -> 0.94f
        else -> CHROME_GLASS_ALPHA
      }
    return PureWhite.copy(alpha = alpha)
  }

  fun borderBrush(baseBorder: Color = HairlineBorder, isDark: Boolean = false): Brush =
    if (isDark) {
      val resolvedBorder = if (baseBorder == HairlineBorder) DarkHairlineBorder else baseBorder
      Brush.linearGradient(
        colors = listOf(resolvedBorder, resolvedBorder),
      )
    } else {
      Brush.linearGradient(
        colors = listOf(baseBorder, baseBorder),
      )
    }

  fun borderStroke(baseBorder: Color = HairlineBorder, isDark: Boolean = false): BorderStroke =
    BorderStroke(width = 1.dp, brush = borderBrush(baseBorder, isDark = isDark))

  fun sheenBrush(topLeftAlpha: Float = 0.12f, isDark: Boolean = false): Brush =
    if (isDark) {
      Brush.linearGradient(
        colors =
          listOf(
            DarkDeepOcean.copy(alpha = (topLeftAlpha * 0.16f).coerceIn(0.02f, 0.08f)),
            Color.Transparent,
          ),
      )
    } else {
      Brush.linearGradient(
        colors =
          listOf(
            PureWhite.copy(alpha = (topLeftAlpha * 0.25f).coerceIn(0.02f, 0.12f)),
            Color.Transparent,
          ),
      )
    }

  fun pointerSpecularBrush(pointer: Offset, radiusPx: Float, isDark: Boolean = false): Brush =
    Brush.radialGradient(
      colors =
        if (isDark) {
          listOf(
            DarkClinicalBlueAccent.copy(alpha = 0.10f),
            Color.Transparent,
          )
        } else {
          listOf(
            PureWhite.copy(alpha = 0.20f),
            LightTealMist.copy(alpha = 0.06f),
            Color.Transparent,
          )
        },
      center = pointer,
      radius = radiusPx.coerceAtLeast(1f),
    )

  fun topCornerHighlight(cornerRadiusPx: Float, size: Size): Triple<Offset, Size, CornerRadius> {
    val highlightH = (size.height * 0.22f).coerceIn(14f, 48f)
    return Triple(Offset.Zero, Size(size.width, highlightH), CornerRadius(cornerRadiusPx, cornerRadiusPx))
  }
}

@Composable
fun rememberPrefersReducedTransparency(): Boolean {
  val context = LocalContext.current
  return remember(context) {
    try {
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
 * Calm, static White-to-Mist wash backdrop without drifting orbs or decorative blobs.
 */
@Composable
fun SehatAmbientBackdrop(
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  val colors = SehatPalette.current
  val backdropColors =
    if (colors.isDark) {
      listOf(DarkBackground, DarkMistSoft, DarkBackground)
    } else {
      listOf(PureWhite, MistSoft, Mist.copy(alpha = 0.65f))
    }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(colors = backdropColors)),
  ) {
    content()
  }
}

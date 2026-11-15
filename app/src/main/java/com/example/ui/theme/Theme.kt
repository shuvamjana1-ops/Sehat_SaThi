package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemePreference(val label: String) {
  SYSTEM("System"),
  LIGHT("Light"),
  DARK("Dark"),
}

@Immutable
data class SehatColors(
  val isDark: Boolean,
  val background: Color,
  val surface: Color,
  val mist: Color,
  val mistSoft: Color,
  val ink: Color,
  val inkSecondary: Color,
  val deepOcean: Color,
  val clinicalBlue: Color,
  val clinicalBlueAccent: Color,
  val hairlineBorder: Color,
  val subtleDivider: Color,
  val statusNormalFg: Color,
  val statusNormalBg: Color,
  val statusNormalBorder: Color,
  val statusCautionFg: Color,
  val statusCautionBg: Color,
  val statusCautionBorder: Color,
  val statusUrgentFg: Color,
  val statusUrgentBg: Color,
  val statusUrgentBorder: Color,
  val statusInfoFg: Color,
  val statusInfoBg: Color,
  val statusInfoBorder: Color,
)

val LightSehatColors =
  SehatColors(
    isDark = false,
    background = PureWhite,
    surface = PureWhite,
    mist = Mist,
    mistSoft = MistSoft,
    ink = Ink,
    inkSecondary = InkSecondary,
    deepOcean = DeepOcean,
    clinicalBlue = ClinicalBlue,
    clinicalBlueAccent = ClinicalBlue,
    hairlineBorder = HairlineBorder,
    subtleDivider = SubtleDivider,
    statusNormalFg = StatusNormalFg,
    statusNormalBg = StatusNormalBg,
    statusNormalBorder = StatusNormalBorder,
    statusCautionFg = StatusCautionFg,
    statusCautionBg = StatusCautionBg,
    statusCautionBorder = StatusCautionBorder,
    statusUrgentFg = StatusUrgentFg,
    statusUrgentBg = StatusUrgentBg,
    statusUrgentBorder = StatusUrgentBorder,
    statusInfoFg = StatusInfoFg,
    statusInfoBg = StatusInfoBg,
    statusInfoBorder = StatusInfoBorder,
  )

val DarkSehatColors =
  SehatColors(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    mist = DarkMist,
    mistSoft = DarkMistSoft,
    ink = DarkInk,
    inkSecondary = DarkInkSecondary,
    deepOcean = DarkDeepOcean,
    clinicalBlue = DarkClinicalBlue,
    clinicalBlueAccent = DarkClinicalBlueAccent,
    hairlineBorder = DarkHairlineBorder,
    subtleDivider = DarkSubtleDivider,
    statusNormalFg = DarkStatusNormalFg,
    statusNormalBg = DarkStatusNormalBg,
    statusNormalBorder = DarkStatusNormalBorder,
    statusCautionFg = DarkStatusCautionFg,
    statusCautionBg = DarkStatusCautionBg,
    statusCautionBorder = DarkStatusCautionBorder,
    statusUrgentFg = DarkStatusUrgentFg,
    statusUrgentBg = DarkStatusUrgentBg,
    statusUrgentBorder = DarkStatusUrgentBorder,
    statusInfoFg = DarkStatusInfoFg,
    statusInfoBg = DarkStatusInfoBg,
    statusInfoBorder = DarkStatusInfoBorder,
  )

val LocalSehatColors = staticCompositionLocalOf { LightSehatColors }

object SehatPalette {
  val current: SehatColors
    @Composable get() = LocalSehatColors.current
}

private val SehatLightColorScheme =
  lightColorScheme(
    primary = ClinicalBlue,
    onPrimary = PureWhite,
    primaryContainer = Mist,
    onPrimaryContainer = DeepOcean,
    secondary = DeepOcean,
    onSecondary = PureWhite,
    secondaryContainer = MistSoft,
    onSecondaryContainer = DeepOcean,
    tertiary = ClinicalBlueHover,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = Ink,
    surface = PureWhite,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = InkSecondary,
    outline = HairlineBorder,
    outlineVariant = SubtleDivider,
    error = StatusUrgentFg,
    onError = PureWhite,
    errorContainer = StatusUrgentBg,
    onErrorContainer = StatusUrgentFg,
  )

private val SehatDarkColorScheme =
  darkColorScheme(
    primary = DarkClinicalBlue,
    onPrimary = PureWhite,
    primaryContainer = DarkMist,
    onPrimaryContainer = DarkDeepOcean,
    secondary = DarkDeepOcean,
    onSecondary = DarkBackground,
    secondaryContainer = DarkMistSoft,
    onSecondaryContainer = DarkInk,
    tertiary = DarkClinicalBlueAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkMist,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkHairlineBorder,
    outlineVariant = DarkSubtleDivider,
    error = DarkStatusUrgentFg,
    onError = DarkBackground,
    errorContainer = DarkStatusUrgentBg,
    onErrorContainer = DarkStatusUrgentFg,
  )

@Composable
fun SehatSaathiTheme(
  themePreference: ThemePreference = ThemePreference.SYSTEM,
  content: @Composable () -> Unit,
) {
  val isDark =
    when (themePreference) {
      ThemePreference.SYSTEM -> isSystemInDarkTheme()
      ThemePreference.LIGHT -> false
      ThemePreference.DARK -> true
    }
  val palette = if (isDark) DarkSehatColors else LightSehatColors
  val colorScheme = if (isDark) SehatDarkColorScheme else SehatLightColorScheme
  val typography =
    if (isDark) {
      buildSehatTypography(
        headingColor = DarkDeepOcean,
        bodyColor = DarkInk,
        secondaryColor = DarkInkSecondary,
      )
    } else {
      SehatTypography
    }

  CompositionLocalProvider(LocalSehatColors provides palette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typography,
      shapes = SehatShapes,
      content = content,
    )
  }
}

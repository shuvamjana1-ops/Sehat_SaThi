package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Core SehatSaathi Design System Palette
val DeepOcean = Color(0xFF0B4F6C)
val DeepOceanDark = Color(0xFF07384D)
val ClinicalBlue = Color(0xFF0E6E8C)
val ClinicalBlueHover = Color(0xFF0A5870)
val ClinicalBluePressed = Color(0xFF084457)
val Mist = Color(0xFFE8F1F5)
val MistSoft = Color(0xFFF4F9FB)
val PureWhite = Color(0xFFFFFFFF)
val Ink = Color(0xFF16262E)
val InkSecondary = Color(0xFF475C66)
val HairlineBorder = Color(0xFFCDE0E8)
val SubtleDivider = Color(0xFFDFECF2)

// Health status semantic tokens (always paired with icon + text label)
val StatusNormalFg = Color(0xFF0E6034)
val StatusNormalBg = Color(0xFFE6F4EC)
val StatusNormalBorder = Color(0xFFA7D7BC)

val StatusCautionFg = Color(0xFF8A4B00)
val StatusCautionBg = Color(0xFFFFF4E0)
val StatusCautionBorder = Color(0xFFF3C677)

val StatusUrgentFg = Color(0xFFB31B1B)
val StatusUrgentBg = Color(0xFFFDECEC)
val StatusUrgentBorder = Color(0xFFF1A9A9)

val StatusInfoFg = DeepOcean
val StatusInfoBg = Mist
val StatusInfoBorder = HairlineBorder

// Dark Mode SehatSaathi Palette (WCAG AA compliant against #0A1A21 and #10262F)
val DarkBackground = Color(0xFF0A1A21)
val DarkSurface = Color(0xFF10262F)
val DarkMist = Color(0xFF14303B)
val DarkMistSoft = Color(0xFF0D2029)
val DarkInk = Color(0xFFE8F1F5)
val DarkInkSecondary = Color(0xFFA8C4CF)
val DarkDeepOcean = Color(0xFFBFE3F0)
val DarkClinicalBlue = Color(0xFF127B9C) // >= 4.9:1 with PureWhite CTA labels
val DarkClinicalBlueHover = Color(0xFF178CB0)
val DarkClinicalBlueAccent = Color(0xFF5FC6E3) // >= 7.7:1 for links/ghost labels on dark surfaces
val DarkHairlineBorder = Color(0xFF1D3B47)
val DarkSubtleDivider = Color(0xFF19323D)

// Dark health status semantic tokens (always paired with icon + text label)
val DarkStatusNormalFg = Color(0xFF7AE0A8)
val DarkStatusNormalBg = Color(0xFF0E3524)
val DarkStatusNormalBorder = Color(0xFF215B3E)

val DarkStatusCautionFg = Color(0xFFFFD17A)
val DarkStatusCautionBg = Color(0xFF3A270B)
val DarkStatusCautionBorder = Color(0xFF6F4B15)

val DarkStatusUrgentFg = Color(0xFFFF9A9A)
val DarkStatusUrgentBg = Color(0xFF3F1518)
val DarkStatusUrgentBorder = Color(0xFF7C2A30)

val DarkStatusInfoFg = DarkDeepOcean
val DarkStatusInfoBg = DarkMist
val DarkStatusInfoBorder = DarkHairlineBorder


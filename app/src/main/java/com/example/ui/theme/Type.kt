package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import com.example.R

val ManropeFontFamily = FontFamily(Font(R.font.manrope))

/**
 * Senior-friendly accessible Manrope scale:
 * - Display / large titles: line-height 1.15 - 1.19 (within 1.1 - 1.2)
 * - Section & card headings: line-height 1.25 - 1.28 (within 1.2 - 1.3), LineBreak.Heading (balance)
 * - Body text (bodyLarge & bodyMedium): >= 18sp, line-height 1.56 (within 1.55 - 1.60), LineBreak.Paragraph (pretty)
 * - Captions & labels (labelLarge, labelMedium, labelSmall): >= 14sp, line-height 1.40
 */
fun buildSehatTypography(
  headingColor: Color = DeepOcean,
  bodyColor: Color = Ink,
  secondaryColor: Color = InkSecondary,
): Typography =
  Typography(
    displayLarge =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 46.sp, // 1.15x
        letterSpacing = (-0.5).sp,
        lineBreak = LineBreak.Heading,
        color = headingColor,
      ),
    displayMedium =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp, // 1.1875x
        letterSpacing = (-0.25).sp,
        lineBreak = LineBreak.Heading,
        color = headingColor,
      ),
    headlineLarge =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 33.sp, // 1.27x
        lineBreak = LineBreak.Heading,
        color = headingColor,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp, // 1.27x
        lineBreak = LineBreak.Heading,
        color = headingColor,
      ),
    titleLarge =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp, // 1.25x
        lineBreak = LineBreak.Heading,
        color = bodyColor,
      ),
    titleMedium =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 23.sp, // 1.28x
        lineBreak = LineBreak.Heading,
        color = bodyColor,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp, // 1.56x
        lineBreak = LineBreak.Paragraph,
        color = bodyColor,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp, // 1.56x (enforces >= 18sp body rule everywhere)
        lineBreak = LineBreak.Paragraph,
        color = bodyColor,
      ),
    labelLarge =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.4.sp, // 1.40x
        color = bodyColor,
      ),
    labelMedium =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.6.sp, // 1.40x
        color = secondaryColor,
      ),
    labelSmall =
      TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // >= 14sp minimum for all captions & small labels
        lineHeight = 19.6.sp, // 1.40x
        color = secondaryColor,
      ),
  )

val SehatTypography = buildSehatTypography()

fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

/**
 * Joins a numeric reading/dose value and its clinical unit with a non-breaking space (\u00A0)
 * so the number and its unit (e.g., "62\u00A0mg/dL", "128/82\u00A0mmHg", "500\u00A0mg") never split across lines.
 */
fun nonBreakingUnit(value: Any, unit: String): String = "$value\u00A0$unit"

private val UNIT_SPLIT_REGEX =
  Regex("(\\d+)\\s+(mg/dL|mmHg|mg|mcg|mL|IU|days|day|tablets|tablet|units|unit|kg|lb|cm|ft)\\b")

/**
 * Replaces regular spaces between numbers and clinical units with non-breaking spaces (\u00A0).
 */
fun String.withNonBreakingUnits(): String =
  UNIT_SPLIT_REGEX.replace(this, "$1\u00A0$2")

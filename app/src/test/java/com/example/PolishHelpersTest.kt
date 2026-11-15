package com.example

import android.graphics.Bitmap
import com.example.data.SiteConfig
import com.example.export.DoctorSummaryAndDataExporter
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.ImageUploadHelper
import com.example.ui.components.sanitizeDialNumber
import com.example.ui.screens.TrendWindow
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.Mist
import com.example.ui.theme.PureWhite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PolishHelpersTest {

  @Test
  fun sanitizeDialNumber_stripsSpacesAndDashesWhileKeepingPlus() {
    assertEquals("+919820111222", sanitizeDialNumber("+91 9820-111 222"))
    assertEquals("112", sanitizeDialNumber(" 112 "))
    assertEquals("", sanitizeDialNumber("   ---  "))
  }

  @Test
  fun imageUploadHelper_computesPowerOfTwoSampleSizeAndScalesLargeBitmap() {
    assertEquals(15L * 1024L * 1024L, ImageUploadHelper.MAX_UPLOAD_BYTES)
    assertEquals(1600, ImageUploadHelper.MAX_DIMENSION_PX)
    assertEquals(80, ImageUploadHelper.JPEG_QUALITY)

    val sample = ImageUploadHelper.computeInSampleSize(rawWidth = 6400, rawHeight = 4800)
    assertTrue("Expected inSampleSize >= 2 for 6400px image, got $sample", sample >= 2)

    val large = Bitmap.createBitmap(2400, 1200, Bitmap.Config.ARGB_8888)
    val (compressed, kb) = ImageUploadHelper.scaleAndCompressBitmap(large)
    assertTrue("Longest side must be <= 1600px, was ${maxOf(compressed.width, compressed.height)}", maxOf(compressed.width, compressed.height) <= 1600)
    assertTrue("Expected compressed KB >= 1, got $kb", kb >= 1)
  }

  @Test
  fun siteConfig_formatsCurrentYearCopyrightAndLeavesOptionalContactsBlank() {
    val notice = SiteConfig.copyrightNotice(2026)
    assertEquals("© 2026 SehatSaathi. All rights reserved.", notice)
    assertEquals("112", SiteConfig.EMERGENCY_NUMBER)
    assertEquals("", SiteConfig.SUPPORT_PHONE)
    assertEquals("", SiteConfig.SUPPORT_EMAIL)
  }

  @Test
  fun trendWindow_exposesAllFourFilterOptionsAndComputesSafeSummaries() {
    assertEquals(
      listOf(7 to "7 days", 30 to "30 days", 60 to "60 days", 365 to "1 year"),
      TrendWindow.entries.map { it.days to it.label },
    )
    val state = SehatSaathiUiState()
    val summary60 = DoctorSummaryAndDataExporter.computeVisitSummary(state, windowDays = 60)
    val summaryYear = DoctorSummaryAndDataExporter.computeVisitSummary(state, windowDays = 365)
    assertEquals(state.readings.size.coerceAtMost(60), summary60.windowDays)
    assertEquals(state.readings.size.coerceAtMost(365), summaryYear.windowDays)
    assertTrue(summaryYear.avgGlucose > 0)
  }

  @Test
  fun glassTokens_enforcesReadabilityAndReducedTransparencyGuardrails() {
    val standard = GlassTokens.cardFill(PureWhite, highReadability = false, reducedTransparency = false)
    val mist = GlassTokens.cardFill(Mist, highReadability = false, reducedTransparency = false)
    val critical = GlassTokens.cardFill(PureWhite, highReadability = true, reducedTransparency = false)
    val fallback = GlassTokens.cardFill(PureWhite, highReadability = false, reducedTransparency = true)

    assertTrue(standard.alpha in 0.55f..0.75f)
    assertTrue(mist.alpha in 0.55f..0.70f)
    assertTrue("Critical health data must remain high-opacity", critical.alpha >= 0.88f)
    assertTrue("Reduced transparency fallback must be nearly opaque", fallback.alpha >= 0.95f)
  }
}

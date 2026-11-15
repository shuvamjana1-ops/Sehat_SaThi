package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.ai.PrescriptionAiParser
import com.example.data.DrugKnowledgeRepository
import com.example.export.DoctorSummaryAndDataExporter
import com.example.ui.SehatSaathiUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SehatSaathi", appName)
  }

  @Test
  fun `load sample drug dataset from assets`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dataset = DrugKnowledgeRepository.loadFromAssets(context)
    assertTrue(dataset.medicines.size >= 55)
    assertTrue(dataset.interactions.isNotEmpty())
  }

  @Test
  fun `image quality rejects tiny or dark bitmaps`() {
    val tiny = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    assertFalse(PrescriptionAiParser.assessImageQuality(tiny).isGood)

    val dark = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    dark.eraseColor(Color.BLACK)
    assertFalse(PrescriptionAiParser.assessImageQuality(dark).isGood)
  }

  @Test
  fun `doctor summary pdf and json export create valid output`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val state = SehatSaathiUiState()
    val pdfFile = DoctorSummaryAndDataExporter.generateDoctorSummaryPdf(context, state, windowDays = 30)
    assertTrue(pdfFile.exists())
    assertTrue(pdfFile.length() > 500L)
    val header = pdfFile.readBytes().take(5).toByteArray().decodeToString()
    assertEquals("%PDF-", header)

    val jsonText = DoctorSummaryAndDataExporter.buildJsonExport(state)
    assertTrue(jsonText.contains("Sunita Devi"))
    assertTrue(jsonText.contains("medicines"))
    val jsonFile = DoctorSummaryAndDataExporter.writeJsonExportFile(context, jsonText)
    assertTrue(jsonFile.exists())
  }
}

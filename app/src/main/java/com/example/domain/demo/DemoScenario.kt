package com.example.domain.demo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.ui.components.AppRoutes

enum class DemoScenario(
  val id: String,
  val title: String,
  val caption: String,
) {
  LOW_SUGAR_2AM(
    id = "low_sugar_2am",
    title = "Low sugar at 2 AM",
    caption = "Demo: Logs 55 mg/dL, opens the emergency screen, shows safe steps, and triggers a simulated family alert.",
  ),
  HIGH_BLOOD_PRESSURE(
    id = "high_bp",
    title = "High blood pressure",
    caption = "Demo: Logs 185/125 mmHg, shows blood pressure crisis guidance and a simulated family alert.",
  ),
  SCAN_SAMPLE_RX(
    id = "scan_sample_rx",
    title = "Scan a sample prescription",
    caption = "Demo: Loads a bundled sample prescription with fixture output, interaction warning, generic option, and schedule.",
  ),
  MEDICINE_RUNNING_OUT(
    id = "medicine_running_out",
    title = "Medicine running out",
    caption = "Demo: Shows a medicine with 3 days left, refill warning, and quick refill/WhatsApp actions.",
  ),
  MISSED_DOSE(
    id = "missed_dose",
    title = "Missed dose",
    caption = "Demo: Simulates an overdue morning dose nudge and escalates a calm notice to family.",
  ),
}

data class GuidedTourStep(
  val stepNumber: Int,
  val title: String,
  val body: String,
  val areaLabel: String,
  val targetRoute: String,
)

object GuidedTourCatalog {
  val steps: List<GuidedTourStep> =
    listOf(
      GuidedTourStep(
        stepNumber = 1,
        title = "Scan a prescription",
        body = "Photograph a printed or handwritten slip. Review every medicine name and check for interactions or cheaper generics before saving.",
        areaLabel = "Scan area",
        targetRoute = AppRoutes.SCAN_RX,
      ),
      GuidedTourStep(
        stepNumber = 2,
        title = "Log sugar or blood pressure",
        body = "Enter readings using the large keypad, voice, or a meter photo. Simple clinical rules highlight danger and safe next steps.",
        areaLabel = "Log reading",
        targetRoute = AppRoutes.LOG_READING,
      ),
      GuidedTourStep(
        stepNumber = 3,
        title = "Track refills before running out",
        body = "See how many days of stock remain for each medicine, mark refills, or prepare a pharmacy order message.",
        areaLabel = "Medicines & refills",
        targetRoute = AppRoutes.MEDICINES,
      ),
      GuidedTourStep(
        stepNumber = 4,
        title = "Keep family informed safely",
        body = "Only consented family contacts receive urgent alerts. Test the Demo escalation timer or preview messages here.",
        areaLabel = "Family & alerts",
        targetRoute = AppRoutes.FAMILY,
      ),
    )
}

/**
 * Generates a lightweight bundled sample prescription image for the Demo scenario so the
 * scanner review step always displays a realistic preview without bundling large binary assets.
 */
fun createSamplePrescriptionBitmap(
  patientName: String = "Sunita Devi",
  patientAge: Int = 66,
): Bitmap {
  val width = 840
  val height = 980
  val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bmp)
  canvas.drawColor(Color.rgb(252, 253, 255))

  val headerPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(11, 79, 108)
      textSize = 38f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
  val subPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(71, 92, 102)
      textSize = 24f
    }
  val rxPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(22, 38, 46)
      textSize = 30f
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }
  val borderPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(14, 110, 140)
      strokeWidth = 4f
      style = Paint.Style.STROKE
    }

  canvas.drawRect(24f, 24f, width - 24f, height - 24f, borderPaint)
  canvas.drawText("Care Clinic (Sample Rx - Demo)", 56f, 96f, headerPaint)
  canvas.drawText("Dr. R. Mehta  ·  Date: 15 Nov 2026", 56f, 142f, subPaint)
  canvas.drawLine(56f, 170f, width - 56f, 170f, borderPaint)

  canvas.drawText("Patient: $patientName (Age $patientAge)", 56f, 224f, rxPaint)
  canvas.drawText("Rx:", 56f, 290f, headerPaint)
  canvas.drawText("1. Tab Brufen 400 mg   .   BD (After food) x 5d", 68f, 360f, rxPaint)
  canvas.drawText("2. Tab Telma 40 mg     .   OD (Morning) x 30d", 68f, 440f, rxPaint)
  canvas.drawText("3. Cap Pan-D / Dolo 650? . SOS (After food)", 68f, 520f, rxPaint)

  canvas.drawLine(56f, 640f, width - 56f, 640f, borderPaint)
  canvas.drawText("Fictional sample slip for Demo mode only.", 56f, 700f, subPaint)
  canvas.drawText("Confirm every medicine with your doctor.", 56f, 742f, subPaint)
  return bmp
}

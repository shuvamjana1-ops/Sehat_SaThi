package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.RefillStatus
import com.example.domain.refill.RefillEngine
import com.example.ui.SehatSaathiUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class DoctorVisitSummaryData(
  val generatedAt: String,
  val windowDays: Int,
  val avgGlucose: Int,
  val minGlucose: Int,
  val maxGlucose: Int,
  val avgSys: Int,
  val avgDia: Int,
  val highBpCount: Int,
  val lowSugarCount: Int,
  val adherencePercent: Int,
  val refillGaps: List<String>,
  val suggestedQuestions: List<String>,
)

object DoctorSummaryAndDataExporter {

  fun computeVisitSummary(
    state: SehatSaathiUiState,
    windowDays: Int = 30,
  ): DoctorVisitSummaryData {
    val window = state.readings.takeLast(windowDays.coerceAtLeast(1))
    val avgGlucose = if (window.isEmpty()) 0 else window.map { it.glucoseMgDl }.average().toInt()
    val minGlucose = window.minOfOrNull { it.glucoseMgDl } ?: 0
    val maxGlucose = window.maxOfOrNull { it.glucoseMgDl } ?: 0
    val avgSys = if (window.isEmpty()) 0 else window.map { it.systolic }.average().toInt()
    val avgDia = if (window.isEmpty()) 0 else window.map { it.diastolic }.average().toInt()
    val highBpCount =
      window.count {
        it.systolic >= state.thresholdsConfig.bpElevatedSystolic ||
          it.diastolic >= state.thresholdsConfig.bpElevatedDiastolic
      }
    val lowSugarCount = window.count { it.glucoseMgDl < state.thresholdsConfig.glucoseLowMgDl }

    val taken = state.dosesToday.count { it.taken }
    val total = state.dosesToday.size
    val adherencePercent = if (total == 0) 100 else ((taken.toFloat() / total.toFloat()) * 100f).toInt()

    val refillGaps =
      state.medicines
        .filter { it.refillStatus != RefillStatus.OK }
        .map { med ->
          val daysInfo =
            med.daysLeftOrNull?.let { "$it days left" } ?: "stock unknown — ask pharmacist"
          "${med.brandName} (${med.refillStatus.label}: $daysInfo)"
        } + RefillEngine.prescriptionEndingNotices(state.medicines)

    val questions = buildList {
      if (lowSugarCount > 0) {
        add("My sugar dropped below ${state.thresholdsConfig.glucoseLowMgDl} mg/dL $lowSugarCount time(s). Should meal or dose timing be reviewed?")
      }
      if (maxGlucose > 220) {
        add("I had post-meal readings up to $maxGlucose mg/dL. What target range should I aim for after dinner?")
      }
      if (highBpCount > 0) {
        add("Blood pressure was at or above 140/90 on $highBpCount reading(s). Should I keep a morning-and-evening log?")
      }
      if (state.medicines.any { it.prescriptionDaysRemaining != null && it.prescriptionDaysRemaining <= 7 }) {
        add("Can you renew the prescription that is ending within the next 7 days?")
      }
      add("Are exact-strength generic equivalents appropriate for my current medicines?")
    }

    val generatedAt = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date())
    return DoctorVisitSummaryData(
      generatedAt = generatedAt,
      windowDays = window.size,
      avgGlucose = avgGlucose,
      minGlucose = minGlucose,
      maxGlucose = maxGlucose,
      avgSys = avgSys,
      avgDia = avgDia,
      highBpCount = highBpCount,
      lowSugarCount = lowSugarCount,
      adherencePercent = adherencePercent,
      refillGaps = refillGaps,
      suggestedQuestions = questions,
    )
  }

  fun buildWhatsAppDoctorSummary(
    state: SehatSaathiUiState,
    windowDays: Int = 30,
  ): String {
    val summary = computeVisitSummary(state, windowDays)
    val medsText =
      state.medicines.joinToString("\n") {
        "- ${it.brandName} (${it.genericName} ${it.strength}) · ${it.frequency} · ${it.foodTiming}"
      }
    val flagsText =
      if (state.trendFlags.isEmpty()) "None flagged"
      else state.trendFlags.joinToString("\n") { "- ${it.title} (${it.label})" }
    return buildString {
      appendLine("SehatSaathi Doctor Visit Summary (${summary.generatedAt})")
      appendLine("Patient: ${state.patient.name}, Age ${state.patient.age} (${state.patient.conditions.joinToString(", ")})")
      appendLine()
      appendLine("Last ${summary.windowDays} readings:")
      appendLine("- Blood sugar: avg ${summary.avgGlucose} mg/dL (range ${summary.minGlucose}-${summary.maxGlucose} mg/dL, ${summary.lowSugarCount} low)")
      appendLine("- Blood pressure: avg ${summary.avgSys}/${summary.avgDia} mmHg (${summary.highBpCount} elevated)")
      appendLine("- Today's dose adherence: ${summary.adherencePercent}%")
      appendLine()
      appendLine("Current medicines:")
      appendLine(medsText)
      appendLine()
      appendLine("Pattern notes (not a diagnosis):")
      appendLine(flagsText)
      appendLine()
      append("SehatSaathi is a support tool, not a doctor. Verify all values with your clinician.")
    }
  }

  fun generateDoctorSummaryPdf(
    context: Context,
    state: SehatSaathiUiState,
    windowDays: Int = 30,
  ): File {
    val summary = computeVisitSummary(state, windowDays)
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(dir, "SehatSaathi_Doctor_Summary.pdf")

    val lines = buildDoctorSummaryLines(state, summary)
    val nativeSuccess =
      try {
        renderWithAndroidPdfDocument(outFile, state, summary)
        true
      } catch (_: Throwable) {
        false
      }

    if (!nativeSuccess) {
      writeMinimalValidPdf(outFile, lines)
    }
    return outFile
  }

  private fun buildDoctorSummaryLines(
    state: SehatSaathiUiState,
    summary: DoctorVisitSummaryData,
  ): List<String> = buildList {
    add("SehatSaathi - Doctor Visit Summary")
    add("Generated: ${summary.generatedAt} | Support tool, not a doctor.")
    add("Patient: ${state.patient.name} | Age: ${state.patient.age} | Conditions: ${state.patient.conditions.joinToString(", ")}")
    add("Blood Sugar (${summary.windowDays} readings): Avg ${summary.avgGlucose} mg/dL (Min ${summary.minGlucose}, Max ${summary.maxGlucose}), Low: ${summary.lowSugarCount}")
    add("Blood Pressure: Avg ${summary.avgSys}/${summary.avgDia} mmHg, Elevated: ${summary.highBpCount}")
    add("Adherence today: ${summary.adherencePercent}%")
    add("Medicines:")
    state.medicines.take(8).forEach { med ->
      val stockText = med.daysLeftOrNull?.let { "${med.stockCount} ${med.unit} (~$it days)" } ?: "Stock unknown"
      add("- ${med.brandName} [${med.genericName} ${med.strength}] ${med.frequency}, ${med.foodTiming} | $stockText")
    }
    if (summary.refillGaps.isNotEmpty()) {
      add("Refill & Prescription Notices:")
      summary.refillGaps.take(5).forEach { add("- $it") }
    }
    add("Pattern Notes (Trend warning, not a diagnosis):")
    if (state.trendFlags.isEmpty()) add("- None flagged")
    else state.trendFlags.take(4).forEach { add("- ${it.title}: ${it.detail}") }
    add("Suggested Questions for Your Doctor:")
    summary.suggestedQuestions.take(5).forEachIndexed { idx, q -> add("${idx + 1}. $q") }
    add("Disclaimer: SehatSaathi is an informational support tool, not a doctor.")
  }

  private fun renderWithAndroidPdfDocument(
    outFile: File,
    state: SehatSaathiUiState,
    summary: DoctorVisitSummaryData,
  ) {
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint =
      Paint().apply {
        color = Color.rgb(11, 79, 108)
        textSize = 18f
        isFakeBoldText = true
      }
    val sectionPaint =
      Paint().apply {
        color = Color.rgb(14, 110, 140)
        textSize = 12f
        isFakeBoldText = true
      }
    val bodyPaint =
      Paint().apply {
        color = Color.rgb(22, 38, 46)
        textSize = 10f
      }
    val mutedPaint =
      Paint().apply {
        color = Color.rgb(80, 98, 107)
        textSize = 8.5f
      }
    val linePaint =
      Paint().apply {
        color = Color.rgb(208, 224, 232)
        strokeWidth = 1f
      }

    var y = 42f
    val left = 36f
    val right = 559f

    fun drawWrapped(text: String, paint: Paint, lineHeight: Float = 13f) {
      val maxChars = 84
      var remaining = text
      while (remaining.isNotEmpty() && y < 790f) {
        val chunk =
          if (remaining.length <= maxChars) remaining
          else {
            val splitAt = remaining.lastIndexOf(' ', maxChars).let { if (it <= 20) maxChars else it }
            remaining.substring(0, splitAt)
          }
        canvas.drawText(chunk, left, y, paint)
        y += lineHeight
        remaining = remaining.removePrefix(chunk).trimStart()
      }
    }

    canvas.drawText("SehatSaathi — Doctor Visit Summary", left, y, titlePaint)
    y += 16f
    canvas.drawText("Generated: ${summary.generatedAt} | Support tool, not a doctor.", left, y, mutedPaint)
    y += 10f
    canvas.drawLine(left, y, right, y, linePaint)
    y += 18f

    canvas.drawText("PATIENT PROFILE", left, y, sectionPaint)
    y += 14f
    drawWrapped(
      "Name: ${state.patient.name} | Age: ${state.patient.age} | Conditions: ${state.patient.conditions.joinToString(", ")}",
      bodyPaint,
    )
    drawWrapped(
      "Emergency: ${state.patient.emergencyNumber} | Primary family contact: ${state.caregivers.firstOrNull()?.let { "${it.name} (${it.phone})" } ?: "None"}",
      bodyPaint,
    )
    y += 8f

    canvas.drawText("VITALS SUMMARY (LAST ${summary.windowDays} READINGS) & ADHERENCE", left, y, sectionPaint)
    y += 14f
    drawWrapped(
      "Blood Sugar: Avg ${summary.avgGlucose} mg/dL (Min ${summary.minGlucose}, Max ${summary.maxGlucose}) | Low readings (<70): ${summary.lowSugarCount}",
      bodyPaint,
    )
    drawWrapped(
      "Blood Pressure: Avg ${summary.avgSys}/${summary.avgDia} mmHg | Elevated readings (>=140/90): ${summary.highBpCount}",
      bodyPaint,
    )
    drawWrapped("Today's Medicine Adherence: ${summary.adherencePercent}% (${state.dosesToday.count { it.taken }}/${state.dosesToday.size} doses taken)", bodyPaint)
    y += 8f

    canvas.drawText("CURRENT MEDICINES & STOCK", left, y, sectionPaint)
    y += 14f
    state.medicines.take(8).forEach { med ->
      val stockText = med.daysLeftOrNull?.let { "${med.stockCount} ${med.unit} (~$it days)" } ?: "Stock unknown"
      drawWrapped(
        "• ${med.brandName} [${med.genericName} ${med.strength}] — ${med.frequency}, ${med.foodTiming} | $stockText",
        bodyPaint,
      )
    }
    y += 8f

    if (summary.refillGaps.isNotEmpty()) {
      canvas.drawText("REFILL & PRESCRIPTION NOTICES", left, y, sectionPaint)
      y += 14f
      summary.refillGaps.take(5).forEach { gap -> drawWrapped("• $gap", bodyPaint) }
      y += 8f
    }

    canvas.drawText("PATTERN NOTES (TREND WARNING, NOT A DIAGNOSIS)", left, y, sectionPaint)
    y += 14f
    if (state.trendFlags.isEmpty()) {
      drawWrapped("No multi-day threshold patterns flagged in the current window.", bodyPaint)
    } else {
      state.trendFlags.take(4).forEach { flag ->
        drawWrapped("• ${flag.title}: ${flag.detail}", bodyPaint)
      }
    }
    y += 8f

    if (state.alerts.isNotEmpty()) {
      canvas.drawText("RECENT ALERTS", left, y, sectionPaint)
      y += 14f
      state.alerts.take(3).forEach { alert ->
        drawWrapped("• [${alert.level.label}] ${alert.title} (${alert.timestampLabel})", bodyPaint)
      }
      y += 8f
    }

    canvas.drawText("SUGGESTED QUESTIONS TO ASK YOUR DOCTOR", left, y, sectionPaint)
    y += 14f
    summary.suggestedQuestions.take(5).forEachIndexed { idx, q ->
      drawWrapped("${idx + 1}. $q", bodyPaint)
    }

    canvas.drawLine(left, 804f, right, 804f, linePaint)
    canvas.drawText(
      "SehatSaathi is an informational support tool, not a doctor. Do not change doses without medical advice.",
      left,
      820f,
      mutedPaint,
    )

    pdf.finishPage(page)
    try {
      FileOutputStream(outFile).use { fos -> pdf.writeTo(fos) }
    } finally {
      pdf.close()
    }
  }

  private fun writeMinimalValidPdf(outFile: File, lines: List<String>) {
    fun esc(s: String): String =
      s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
    val streamContent = buildString {
      appendLine("BT")
      appendLine("/F1 10 Tf")
      appendLine("40 800 Td")
      appendLine("14 TL")
      lines.take(48).forEachIndexed { i, line ->
        if (i == 0) appendLine("(${esc(line.take(92))}) Tj")
        else appendLine("T* (${esc(line.take(92))}) Tj")
      }
      appendLine("ET")
    }
    val objects = listOf(
      "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n",
      "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n",
      "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n",
      "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n",
      "5 0 obj << /Length ${streamContent.toByteArray().size} >>\nstream\n${streamContent}endstream\nendobj\n",
    )
    val sb = StringBuilder("%PDF-1.4\n")
    val offsets = mutableListOf(0)
    objects.forEach { obj ->
      offsets += sb.length
      sb.append(obj)
    }
    val xrefStart = sb.length
    sb.append("xref\n0 ${objects.size + 1}\n0000000000 65535 f \n")
    offsets.drop(1).forEach { off -> sb.append(String.format(Locale.US, "%010d 00000 n \n", off)) }
    sb.append("trailer << /Size ${objects.size + 1} /Root 1 0 R >>\nstartxref\n$xrefStart\n%%EOF\n")
    outFile.writeText(sb.toString())
  }

  fun buildJsonExport(state: SehatSaathiUiState): String {
    val root = JSONObject()
    root.put("exported_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))
    root.put("notice", "SehatSaathi user data export. Informational support tool, not a medical record.")

    val patientObj = JSONObject().apply {
      put("id", state.patient.id)
      put("name", state.patient.name)
      put("age", state.patient.age)
      put("conditions", JSONArray(state.patient.conditions))
      put("emergency_number", state.patient.emergencyNumber)
      put("pharmacy_name", state.patient.pharmacyName)
      put("pharmacy_phone", state.patient.pharmacyPhone)
    }
    root.put("patient", patientObj)

    root.put(
      "reminder_preferences",
      JSONObject().apply {
        put("morning_time", state.reminderPrefs.morningTime)
        put("afternoon_time", state.reminderPrefs.afternoonTime)
        put("night_time", state.reminderPrefs.nightTime)
        put("quiet_hours_enabled", state.reminderPrefs.quietHoursEnabled)
        put("quiet_start", state.reminderPrefs.quietStart)
        put("quiet_end", state.reminderPrefs.quietEnd)
      },
    )

    val medsArr = JSONArray()
    state.medicines.forEach { med ->
      medsArr.put(
        JSONObject().apply {
          put("id", med.id)
          put("brand_name", med.brandName)
          put("generic_name", med.genericName)
          put("strength", med.strength)
          put("frequency", med.frequency)
          put("food_timing", med.foodTiming)
          put("stock_count", med.stockCount)
          put("unknown_stock", med.unknownStock)
          put("refill_status", med.refillStatus.label)
        },
      )
    }
    root.put("medicines", medsArr)

    val readingsArr = JSONArray()
    state.readings.forEach { r ->
      readingsArr.put(
        JSONObject().apply {
          put("id", r.id)
          put("day_label", r.dayLabel)
          put("glucose_mg_dl", r.glucoseMgDl)
          put("systolic", r.systolic)
          put("diastolic", r.diastolic)
          put("context", r.contextLabel)
          put("status", r.status.label)
        },
      )
    }
    root.put("readings", readingsArr)

    val caregiversArr = JSONArray()
    state.caregivers.forEach { c ->
      caregiversArr.put(
        JSONObject().apply {
          put("id", c.id)
          put("name", c.name)
          put("relationship", c.relationship)
          put("phone", c.phone)
          put("consent_status", c.consentStatus)
          put("priority", c.priority)
        },
      )
    }
    root.put("caregivers", caregiversArr)

    val refillArr = JSONArray()
    state.refillHistory.forEach { entry ->
      refillArr.put(
        JSONObject().apply {
          put("id", entry.id)
          put("medicine_name", entry.medicineName)
          put("action", entry.actionLabel)
          put("timestamp", entry.timestampLabel)
        },
      )
    }
    root.put("refill_history", refillArr)

    return root.toString(2)
  }

  fun writeJsonExportFile(context: Context, jsonText: String): File {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(dir, "sehatsaathi_data_export.json")
    outFile.writeText(jsonText)
    return outFile
  }

  fun clearExportCache(context: Context) {
    runCatching {
      File(context.cacheDir, "exports").deleteRecursively()
      File(context.cacheDir, "captures").deleteRecursively()
    }
  }

  fun shareFileWithIntent(
    context: Context,
    file: File,
    mimeType: String,
    chooserTitle: String,
  ): Boolean {
    return try {
      val uri: Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = mimeType
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
      val chooser = Intent.createChooser(intent, chooserTitle).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
      true
    } catch (_: Throwable) {
      false
    }
  }

  fun openPdfWithIntent(context: Context, file: File): Boolean {
    return try {
      val uri: Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent =
        Intent(Intent.ACTION_VIEW).apply {
          setDataAndType(uri, "application/pdf")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
      true
    } catch (_: Throwable) {
      shareFileWithIntent(context, file, "application/pdf", "Share Doctor Summary PDF")
    }
  }
}

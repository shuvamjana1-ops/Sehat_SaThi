package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.HealthStatusLevel
import com.example.data.RefillStatus
import com.example.data.SeededReading
import com.example.export.DoctorSummaryAndDataExporter
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatInlineLoadingBanner
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatRingProgress
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.StatusChip
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.components.sehatScroll3DKeyed
import com.example.ui.theme.CardShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular

enum class TrendWindow(val days: Int, val label: String) {
  SEVEN_DAYS(7, "7 days"),
  THIRTY_DAYS(30, "30 days"),
  SIXTY_DAYS(60, "60 days"),
  ONE_YEAR(365, "1 year"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendsScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onShowToast: (String) -> Unit,
  onOpenMedicines: () -> Unit,
  onBackHome: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var selectedWindow by remember { mutableStateOf(TrendWindow.SEVEN_DAYS) }
  var filterMenuExpanded by remember { mutableStateOf(false) }
  var showInlineSummaryPreview by remember { mutableStateOf(false) }
  var isExportingPdf by remember { mutableStateOf(false) }

  val windowDays = selectedWindow.days
  val filteredReadings = remember(uiState.readings, windowDays) {
    uiState.readings.takeLast(windowDays)
  }
  val visitSummary = remember(uiState, windowDays) {
    DoctorSummaryAndDataExporter.computeVisitSummary(uiState, windowDays)
  }

  val takenCount = uiState.dosesToday.count { it.taken }
  val totalDoses = uiState.dosesToday.size
  val dueRefillCount = uiState.medicines.count { it.refillStatus != RefillStatus.OK }
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val listState = rememberLazyListState()

  LazyColumn(
    state = listState,
    contentPadding =
      PaddingValues(16.dp, 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier.fillMaxSize().testTag("trends_scroll"),
  ) {
    item(key = "trends_header") {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = c.deepOcean)
          Text("Trends & Summary", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text =
              if (filteredReadings.size < windowDays) {
                "Showing ${filteredReadings.size} saved reading(s)"
              } else {
                "Last ${selectedWindow.label} (${filteredReadings.size} readings)"
              },
            style = MaterialTheme.typography.labelMedium,
            color = c.inkSecondary,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
          )
          Box {
            Surface(
              onClick = { filterMenuExpanded = true },
              shape = PillShape,
              color = c.clinicalBlue,
              contentColor = PureWhite,
              border = BorderStroke(1.dp, c.hairlineBorder),
              modifier =
                Modifier
                  .defaultMinSize(minHeight = 48.dp)
                  .semantics {
                    contentDescription = "Filter time range, currently ${selectedWindow.label}"
                  }
                  .testTag("trends_filter_btn"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.FilterList,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Filter: ${selectedWindow.label}",
                  style = MaterialTheme.typography.labelLarge,
                  color = PureWhite,
                  maxLines = 1,
                  softWrap = false,
                  overflow = TextOverflow.Ellipsis,
                )
                Icon(
                  imageVector = Icons.Filled.ArrowDropDown,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
            DropdownMenu(
              expanded = filterMenuExpanded,
              onDismissRequest = { filterMenuExpanded = false },
              modifier = Modifier.background(c.surface).testTag("trends_filter_menu"),
            ) {
              TrendWindow.entries.forEach { option ->
                val isSelected = selectedWindow == option
                DropdownMenuItem(
                  text = {
                    Text(
                      text = option.label,
                      style = MaterialTheme.typography.bodyMedium,
                      color = if (isSelected) c.clinicalBlueAccent else c.ink,
                    )
                  },
                  trailingIcon = {
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = c.clinicalBlueAccent,
                      )
                    }
                  },
                  onClick = {
                    selectedWindow = option
                    filterMenuExpanded = false
                  },
                  modifier =
                    Modifier
                      .background(if (isSelected) c.mistSoft else c.surface)
                      .defaultMinSize(minHeight = 48.dp)
                      .testTag("trends_filter_option_${option.days}"),
                )
              }
            }
          }
        }
      }
    }

    // 1. Adherence & Refill Snapshot
    item(key = "trends_adherence") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "trends_adherence", reducedMotion),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          SehatRingProgress(completed = takenCount, total = totalDoses, size = 96.dp, strokeWidth = 10.dp)
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Adherence & refills", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
            Text(
              "Today's doses: ${visitSummary.adherencePercent}% taken ($takenCount of $totalDoses)",
              style = MaterialTheme.typography.bodyMedium.tabular(),
              color = c.ink,
            )
            Text(
              text =
                if (dueRefillCount == 0) "All active medicines have safe stock."
                else "$dueRefillCount medicine(s) need refill review or renewal.",
              style = MaterialTheme.typography.labelMedium,
              color = if (dueRefillCount > 0) c.statusCautionFg else c.statusNormalFg,
            )
            if (dueRefillCount > 0) {
              SehatGhostButton(text = "Review refill alerts", onClick = onOpenMedicines)
            }
          }
        }
      }
    }

    // 2. Blood Sugar Trend Chart with Target Band
    item(key = "trends_glucose") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "trends_glucose", reducedMotion),
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Blood sugar (${selectedWindow.label})", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          Text("Target 70–180 mg/dL", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
        }
        Spacer(modifier = Modifier.height(10.dp))
        GlucoseTrendChart(readings = filteredReadings)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          StatPill("Avg", "${visitSummary.avgGlucose} mg/dL")
          StatPill("Low–High", "${visitSummary.minGlucose}–${visitSummary.maxGlucose}")
          StatPill("Low (<70)", "${visitSummary.lowSugarCount}")
        }
      }
    }

    // 3. Blood Pressure Trend Chart (Systolic + Diastolic)
    item(key = "trends_bp") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "trends_bp", reducedMotion),
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Blood pressure (${selectedWindow.label})", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          Text("Target <140/90 mmHg", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
        }
        Spacer(modifier = Modifier.height(10.dp))
        BloodPressureTrendChart(readings = filteredReadings)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          StatPill("Avg BP", "${visitSummary.avgSys}/${visitSummary.avgDia}")
          StatPill("Elevated (>=140/90)", "${visitSummary.highBpCount}")
          StatPill("Readings", "${filteredReadings.size}")
        }
      }
    }

    // 4. Doctor Summary PDF & WhatsApp Share
    item(key = "trends_pdf") {
      SehatCard(
        containerColor = c.mistSoft,
        borderColor = c.clinicalBlueAccent,
        modifier = Modifier.sehatScroll3DKeyed(listState, "trends_pdf", reducedMotion),
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = c.clinicalBlueAccent)
            Text("Doctor Summary PDF", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          }
          DemoPillBadge(text = "1-Page Visit Sheet")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "Exports a single-page summary with your current medicines, ${selectedWindow.label} sugar & BP ranges, adherence %, pattern notes, and questions for your doctor.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (isExportingPdf) {
          SehatInlineLoadingBanner(
            title = "Preparing 1-page Doctor Summary PDF...",
            subtitle = "Formatting medicines, sugar & BP charts, and doctor questions.",
          )
          Spacer(modifier = Modifier.height(10.dp))
        }
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          SehatPrimaryButton(
            text = if (isExportingPdf) "Preparing PDF..." else "Export PDF",
            icon = Icons.Filled.PictureAsPdf,
            enabled = !isExportingPdf,
            isLoading = isExportingPdf,
            onClick = {
              isExportingPdf = true
              runCatching {
                val file = DoctorSummaryAndDataExporter.generateDoctorSummaryPdf(context, uiState, windowDays)
                DoctorSummaryAndDataExporter.openPdfWithIntent(context, file)
                onShowToast("Generated 1-page Doctor Summary PDF (${file.name}).")
              }.onFailure {
                onShowToast("Could not open PDF viewer; try Share PDF instead.")
              }
              isExportingPdf = false
            },
            testTag = "export_doctor_pdf_btn",
          )
          SehatSecondaryButton(
            text = "Share PDF",
            icon = Icons.Filled.Share,
            minHeight = 56.dp,
            enabled = !isExportingPdf,
            onClick = {
              isExportingPdf = true
              runCatching {
                val file = DoctorSummaryAndDataExporter.generateDoctorSummaryPdf(context, uiState, windowDays)
                DoctorSummaryAndDataExporter.shareFileWithIntent(
                  context = context,
                  file = file,
                  mimeType = "application/pdf",
                  chooserTitle = "Share SehatSaathi Doctor Summary PDF",
                )
              }.onFailure {
                onShowToast("Could not share Doctor Summary PDF right now.")
              }
              isExportingPdf = false
            },
            testTag = "share_doctor_pdf_btn",
          )
          SehatSecondaryButton(
            text = "WhatsApp summary (Demo)",
            icon = Icons.AutoMirrored.Filled.Chat,
            minHeight = 56.dp,
            onClick = {
              val msg = DoctorSummaryAndDataExporter.buildWhatsAppDoctorSummary(uiState, windowDays)
              val uri = Uri.parse("https://wa.me/?text=${Uri.encode(msg)}")
              val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              runCatching { context.startActivity(intent) }
                .onFailure { onShowToast("Could not open WhatsApp; you can share the PDF instead.") }
            },
            testTag = "share_doctor_whatsapp_btn",
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SehatGhostButton(
          text = if (showInlineSummaryPreview) "Hide summary preview" else "Preview visit sheet & doctor questions",
          icon = Icons.Filled.Visibility,
          onClick = { showInlineSummaryPreview = !showInlineSummaryPreview },
          testTag = "preview_doctor_summary_btn",
        )

        if (showInlineSummaryPreview) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = CardShape,
            color = c.surface,
            border = BorderStroke(1.dp, c.hairlineBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Suggested questions to ask your doctor", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
              visitSummary.suggestedQuestions.forEachIndexed { i, q ->
                Text("${i + 1}. $q", style = MaterialTheme.typography.bodyMedium, color = c.ink)
              }
              if (visitSummary.refillGaps.isNotEmpty()) {
                HorizontalDivider(color = c.hairlineBorder)
                Text("Refill & prescription gaps", style = MaterialTheme.typography.labelLarge, color = c.statusCautionFg)
                visitSummary.refillGaps.forEach { g ->
                  Text("• $g", style = MaterialTheme.typography.bodyMedium, color = c.ink)
                }
              }
            }
          }
        }
      }
    }

    // 5. Rule-Based Trend Pattern Notes
    if (uiState.trendFlags.isNotEmpty()) {
      item(key = "trends_flags_header") {
        Text("Rule-based pattern notes", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      items(uiState.trendFlags, key = { "flag_${it.label}_${it.title}" }) { flag ->
        val flagKey = "flag_${flag.label}_${flag.title}"
        SehatCard(
          containerColor = c.statusCautionBg,
          borderColor = c.statusCautionBorder,
          modifier = Modifier.sehatScroll3DKeyed(listState, flagKey, reducedMotion),
        ) {
          StatusChip(level = HealthStatusLevel.CAUTION, label = flag.label)
          Spacer(modifier = Modifier.height(6.dp))
          Text(flag.title, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
          Spacer(modifier = Modifier.height(4.dp))
          Text(flag.detail, style = MaterialTheme.typography.bodyMedium, color = c.ink)
        }
      }
    }

    // 6. Recent Readings List
    item(key = "trends_readings_header") {
      Text("Readings in selected period", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    items(filteredReadings.reversed(), key = { it.id }) { reading ->
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, reading.id, reducedMotion),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "${reading.dayLabel} · ${reading.contextLabel}",
              style = MaterialTheme.typography.labelLarge,
              color = c.deepOcean,
            )
            Text(
              "Sugar ${reading.glucoseMgDl} mg/dL · BP ${reading.systolic}/${reading.diastolic} mmHg",
              style = MaterialTheme.typography.bodyMedium.tabular(),
              color = c.ink,
            )
          }
          StatusChip(level = reading.status)
        }
      }
    }

    item {
      SehatSecondaryButton(text = "Back to Dashboard", onClick = onBackHome, modifier = Modifier.fillMaxWidth())
    }
  }
}

@Composable
private fun StatPill(label: String, value: String) {
  val c = SehatPalette.current
  Surface(
    shape = CardShape,
    color = c.mistSoft,
    border = BorderStroke(1.dp, c.hairlineBorder),
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
      Text(value, style = MaterialTheme.typography.titleMedium.tabular(), color = c.deepOcean)
    }
  }
}

@Composable
private fun GlucoseTrendChart(readings: List<SeededReading>) {
  val c = SehatPalette.current
  val bandColor = c.mist.copy(alpha = if (c.isDark) 0.55f else 0.78f)
  val highThreshColor = c.clinicalBlueAccent.copy(alpha = 0.55f)
  val lowThreshColor = c.statusUrgentFg.copy(alpha = 0.55f)
  val lineColor = c.deepOcean
  val dotHaloColor = c.surface
  val urgentDotColor = c.statusUrgentFg
  val cautionDotColor = c.statusCautionFg
  val normalDotColor = c.statusNormalFg
  val reducedMotion = rememberPrefersReducedMotion()
  var started by remember { mutableStateOf(false) }
  LaunchedEffect(readings) { started = true }
  val progress by
    animateFloatAsState(
      targetValue = if (started) 1f else 0f,
      animationSpec = tween(if (reducedMotion) 0 else 700, easing = FastOutSlowInEasing),
      label = "glucose_chart_anim",
    )

  Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
    if (readings.size < 2) return@Canvas
    val values = readings.map { it.glucoseMgDl.toFloat() }
    val minY = (values.minOrNull() ?: 60f).coerceAtMost(50f)
    val maxY = (values.maxOrNull() ?: 220f).coerceAtLeast(240f)
    val range = (maxY - minY).coerceAtLeast(1f)
    val padV = 14.dp.toPx()
    val h = size.height - padV * 2f

    fun yFor(v: Float): Float = size.height - padV - (((v - minY) / range).coerceIn(0f, 1f) * h)

    // Safe Target Band (70 - 180)
    val yHigh = yFor(180f)
    val yLow = yFor(70f)
    drawRect(
      color = bandColor,
      topLeft = Offset(0f, yHigh),
      size = Size(size.width, (yLow - yHigh).coerceAtLeast(2f)),
    )

    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
    drawLine(highThreshColor, Offset(0f, yHigh), Offset(size.width, yHigh), strokeWidth = 1.5.dp.toPx(), pathEffect = dash)
    drawLine(lowThreshColor, Offset(0f, yLow), Offset(size.width, yLow), strokeWidth = 1.5.dp.toPx(), pathEffect = dash)

    val stepX = size.width / (readings.size - 1).toFloat()
    val visibleMaxIdx = ((readings.size - 1) * progress).toInt().coerceAtLeast(1)

    val path = Path()
    readings.forEachIndexed { idx, r ->
      if (idx <= visibleMaxIdx) {
        val pt = Offset(idx * stepX, yFor(r.glucoseMgDl.toFloat()))
        if (idx == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
      }
    }
    drawPath(
      path = path,
      color = lineColor,
      style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    readings.forEachIndexed { idx, r ->
      if (idx <= visibleMaxIdx) {
        val pt = Offset(idx * stepX, yFor(r.glucoseMgDl.toFloat()))
        val dotColor =
          when {
            r.glucoseMgDl < 70 || r.glucoseMgDl > 300 -> urgentDotColor
            r.glucoseMgDl > 180 -> cautionDotColor
            else -> normalDotColor
          }
        drawCircle(color = dotHaloColor, radius = 5.dp.toPx(), center = pt)
        drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = pt)
      }
    }
  }
}

@Composable
private fun BloodPressureTrendChart(readings: List<SeededReading>) {
  val c = SehatPalette.current
  val bandColor = c.mist.copy(alpha = if (c.isDark) 0.50f else 0.72f)
  val urgentThreshColor = c.statusUrgentFg.copy(alpha = 0.55f)
  val sysColor = c.deepOcean
  val diaColor = c.clinicalBlueAccent
  val reducedMotion = rememberPrefersReducedMotion()
  var started by remember { mutableStateOf(false) }
  LaunchedEffect(readings) { started = true }
  val progress by
    animateFloatAsState(
      targetValue = if (started) 1f else 0f,
      animationSpec = tween(if (reducedMotion) 0 else 700, easing = FastOutSlowInEasing),
      label = "bp_chart_anim",
    )

  Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
    if (readings.size < 2) return@Canvas
    val minY = 60f
    val maxY = (readings.maxOfOrNull { it.systolic.toFloat() } ?: 180f).coerceAtLeast(185f)
    val range = (maxY - minY).coerceAtLeast(1f)
    val padV = 14.dp.toPx()
    val h = size.height - padV * 2f

    fun yFor(v: Float): Float = size.height - padV - (((v - minY) / range).coerceIn(0f, 1f) * h)

    // Target band 90 - 140
    val y140 = yFor(140f)
    val y90 = yFor(90f)
    drawRect(
      color = bandColor,
      topLeft = Offset(0f, y140),
      size = Size(size.width, (y90 - y140).coerceAtLeast(2f)),
    )

    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
    drawLine(urgentThreshColor, Offset(0f, yFor(180f)), Offset(size.width, yFor(180f)), strokeWidth = 1.5.dp.toPx(), pathEffect = dash)

    val stepX = size.width / (readings.size - 1).toFloat()
    val visibleMaxIdx = ((readings.size - 1) * progress).toInt().coerceAtLeast(1)

    val sysPath = Path()
    val diaPath = Path()
    readings.forEachIndexed { idx, r ->
      if (idx <= visibleMaxIdx) {
        val x = idx * stepX
        val sy = yFor(r.systolic.toFloat())
        val dy = yFor(r.diastolic.toFloat())
        if (idx == 0) {
          sysPath.moveTo(x, sy)
          diaPath.moveTo(x, dy)
        } else {
          sysPath.lineTo(x, sy)
          diaPath.lineTo(x, dy)
        }
      }
    }
    drawPath(sysPath, color = sysColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    drawPath(diaPath, color = diaColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

    readings.forEachIndexed { idx, r ->
      if (idx <= visibleMaxIdx) {
        val x = idx * stepX
        drawCircle(color = sysColor, radius = 3.2.dp.toPx(), center = Offset(x, yFor(r.systolic.toFloat())))
        drawCircle(color = diaColor, radius = 2.8.dp.toPx(), center = Offset(x, yFor(r.diastolic.toFloat())))
      }
    }
  }
}

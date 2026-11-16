package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.ai.ExtractedRxMedicine
import com.example.data.HealthStatusLevel
import com.example.domain.drugs.InteractionWarning
import com.example.domain.drugs.MedicineSafetyCheckReport
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.ImageLoadResult
import com.example.ui.components.ImageUploadHelper
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatCardSkeleton
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatIconButton
import com.example.ui.components.SehatInlineLoadingBanner
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.schedule.ScheduleSpeechHelper
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanPrescriptionScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onProcessBitmap: (Bitmap) -> Unit,
  onLoadDemoRx: () -> Unit,
  onStartManualEntry: () -> Unit,
  onUpdateMedicine: (ExtractedRxMedicine) -> Unit,
  onAddBlankMedicine: () -> Unit,
  onRemoveMedicine: (String) -> Unit,
  onRunSafetyChecks: () -> Unit,
  onExplainInteractionAi: (InteractionWarning) -> Unit,
  onSaveToSchedule: () -> Unit,
  onSetStep: (Int) -> Unit,
  onResetScanner: () -> Unit,
  onGoToMedicines: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  val rx = uiState.rxScanner
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
  var showPharmacistCard by remember { mutableStateOf(false) }

  val speechHelper = remember(context) { ScheduleSpeechHelper(context) }
  DisposableEffect(Unit) { onDispose { speechHelper.shutdown() } }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        when (val result = ImageUploadHelper.loadAndCompressImage(context, uri)) {
          is ImageLoadResult.Success -> onProcessBitmap(result.bitmap)
          is ImageLoadResult.Error -> onShowToast(result.message)
        }
      }
    }

  val cameraLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
      val uri = pendingCameraUri
      if (saved && uri != null) {
        when (val result = ImageUploadHelper.loadAndCompressImage(context, uri)) {
          is ImageLoadResult.Success -> onProcessBitmap(result.bitmap)
          is ImageLoadResult.Error -> onShowToast(result.message)
        }
      }
    }

  LazyColumn(
    contentPadding =
      PaddingValues(16.dp, 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier.fillMaxSize().testTag("scan_rx_scroll"),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.DocumentScanner, contentDescription = null, tint = c.deepOcean)
            Text("Scan Prescription", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
          }
          if (rx.step > 0) {
            SehatGhostButton(text = "New scan", icon = Icons.Filled.Refresh, onClick = onResetScanner)
          }
        }

        // Sample dataset notice always visible
        Surface(
          shape = CardShape,
          color = c.mist,
          border = BorderStroke(1.dp, c.hairlineBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = uiState.drugDataset?.datasetNotice ?: "Sample data: verify with an official source before real use",
            style = MaterialTheme.typography.labelMedium,
            color = c.deepOcean,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }

        RxStepperBar(currentStep = rx.step, onStepClick = { target -> if (target <= rx.step) onSetStep(target) })
      }
    }

    when (rx.step) {
      0 -> {
        item {
          SehatCard(containerColor = c.surface) {
            Text("1. Capture prescription photo", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Hold your phone steady in bright light. We check for blur or darkness first, then Gemini extracts the medicine list for you to verify.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              SehatPrimaryButton(
                text = "Take photo",
                icon = Icons.Filled.CameraAlt,
                onClick = {
                  val uri = createTempRxPhotoUri(context)
                  pendingCameraUri = uri
                  cameraLauncher.launch(uri)
                },
                testTag = "rx_camera_btn",
              )
              SehatSecondaryButton(
                text = "Choose from gallery",
                icon = Icons.Filled.PhotoLibrary,
                minHeight = 56.dp,
                onClick = {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                  )
                },
                testTag = "rx_gallery_btn",
              )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = c.hairlineBorder)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              SehatSecondaryButton(
                text = "Try Sample Rx (Demo)",
                onClick = onLoadDemoRx,
                testTag = "rx_demo_sample_btn",
              )
              SehatGhostButton(
                text = "Enter medicines manually",
                icon = Icons.Filled.Edit,
                onClick = onStartManualEntry,
                testTag = "rx_manual_btn",
              )
            }
            rx.capturedBitmap?.let { bmp ->
              Spacer(modifier = Modifier.height(14.dp))
              Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Selected prescription photo preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(150.dp).clip(CardShape),
              )
            }
          }
        }

        if (rx.isScanning) {
          item {
            SehatInlineLoadingBanner(
              title = "Reading your prescription...",
              subtitle = "Extracting medicine names, strengths, and timings. You will review every line.",
            )
          }
        }

        rx.qualityIssue?.let { issue ->
          item {
            SehatCard(containerColor = c.statusCautionBg, borderColor = c.statusCautionBorder) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = c.statusCautionFg)
                Text(issue.issueTitle ?: "Please retake photo", style = MaterialTheme.typography.titleMedium, color = c.statusCautionFg)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(issue.issueGuidance.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = c.ink)
              Spacer(modifier = Modifier.height(10.dp))
              rx.capturedBitmap?.let { bmp ->
                Image(
                  bitmap = bmp.asImageBitmap(),
                  contentDescription = "Prescription photo needing retake",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxWidth().height(150.dp).clip(CardShape),
                )
              }
            }
          }
        }

        rx.scanError?.let { err ->
          item {
            SehatCard(containerColor = c.statusUrgentBg, borderColor = c.statusUrgentBorder) {
              Text(err, style = MaterialTheme.typography.bodyMedium, color = c.statusUrgentFg)
              Spacer(modifier = Modifier.height(8.dp))
              SehatSecondaryButton(text = "Enter medicines manually", onClick = onStartManualEntry)
            }
          }
        }
      }

      1 -> {
        item {
          BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val sideBySide = maxWidth >= 720.dp && rx.capturedBitmap != null
            if (sideBySide) {
              Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OriginalRxPhotoCard(
                  bitmap = rx.capturedBitmap,
                  doctorName = rx.doctorName,
                  rxDate = rx.rxDate,
                  isSampleResult = rx.isSampleResult,
                  modifier = Modifier.weight(0.9f),
                )
                ReviewHeaderCard(
                  warnings = rx.warnings,
                  isSampleResult = rx.isSampleResult,
                  onAddBlank = onAddBlankMedicine,
                  onProceedChecks = onRunSafetyChecks,
                  modifier = Modifier.weight(1.1f),
                )
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OriginalRxPhotoCard(
                  bitmap = rx.capturedBitmap,
                  doctorName = rx.doctorName,
                  rxDate = rx.rxDate,
                  isSampleResult = rx.isSampleResult,
                  modifier = Modifier.fillMaxWidth(),
                )
                ReviewHeaderCard(
                  warnings = rx.warnings,
                  isSampleResult = rx.isSampleResult,
                  onAddBlank = onAddBlankMedicine,
                  onProceedChecks = onRunSafetyChecks,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
        }

        items(rx.extractedMedicines, key = { it.id }) { item ->
          EditableRxMedicineCard(
            item = item,
            onUpdate = onUpdateMedicine,
            onRemove = { onRemoveMedicine(item.id) },
          )
        }

        item {
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SehatSecondaryButton(
              text = "Add medicine",
              icon = Icons.Filled.Add,
              minHeight = 56.dp,
              onClick = onAddBlankMedicine,
              modifier = Modifier.weight(0.45f),
            )
            SehatPrimaryButton(
              text = "Confirm & Check Safety",
              icon = Icons.Filled.CheckCircle,
              onClick = onRunSafetyChecks,
              modifier = Modifier.weight(0.55f),
              testTag = "rx_run_checks_btn",
            )
          }
        }
      }

      2 -> {
        rx.checkReport?.let { report ->
          item {
            SafetyAndGenericsReportSection(
              report = report,
              isExplainingAi = rx.isExplainingInteraction,
              aiExplanation = rx.interactionExplanation,
              onExplainInteraction = onExplainInteractionAi,
              onShowPharmacistCard = { showPharmacistCard = true },
            )
          }
        }

        item {
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SehatSecondaryButton(
              text = "Back to edit",
              minHeight = 56.dp,
              onClick = { onSetStep(1) },
              modifier = Modifier.weight(0.4f),
            )
            SehatPrimaryButton(
              text = "Save to Daily Schedule",
              icon = Icons.Filled.CheckCircle,
              onClick = onSaveToSchedule,
              modifier = Modifier.weight(0.6f),
              testTag = "rx_save_schedule_btn",
            )
          }
        }
      }

      3 -> {
        item {
          SehatCard(containerColor = c.surface) {
            StatusChip(level = HealthStatusLevel.NORMAL, label = "Saved to your schedule")
            Spacer(modifier = Modifier.height(10.dp))
            Text("4. Your daily schedule is ready", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Tap Listen to hear your full daily schedule read aloud in clear English.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              SehatPrimaryButton(
                text = "Listen to schedule",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                onClick = {
                  speechHelper.speak(ScheduleSpeechHelper.buildScheduleSpeechText(uiState.dosesToday))
                },
                testTag = "rx_listen_schedule_btn",
              )
              SehatSecondaryButton(
                text = "Open Schedule & Reminders",
                minHeight = 56.dp,
                onClick = onGoToMedicines,
                testTag = "rx_open_schedule_btn",
              )
            }
          }
        }
      }
    }
  }

  val activeReport = rx.checkReport
  if (showPharmacistCard && activeReport != null) {
    ShowPharmacistDialog(
      report = activeReport,
      onDismiss = { showPharmacistCard = false },
    )
  }
}

@Composable
private fun RxStepperBar(currentStep: Int, onStepClick: (Int) -> Unit) {
  val c = SehatPalette.current
  val labels = listOf("1. Capture", "2. Review", "3. Checks", "4. Schedule")
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
    labels.forEachIndexed { idx, label ->
      val active = idx == currentStep
      val done = idx < currentStep
      Surface(
        onClick = { onStepClick(idx) },
        shape = ButtonShape,
        color = if (active) c.clinicalBlue else if (done) c.mist else c.surface,
        border = BorderStroke(1.dp, if (active) c.clinicalBlue else c.hairlineBorder),
        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = if (active) PureWhite else c.deepOcean,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
        )
      }
    }
  }
}

@Composable
private fun OriginalRxPhotoCard(
  bitmap: Bitmap?,
  doctorName: String?,
  rxDate: String?,
  isSampleResult: Boolean,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  SehatCard(containerColor = c.surface, modifier = modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      Text("Original prescription", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
      if (isSampleResult) {
        DemoPillBadge(text = "Sample result")
      } else if (bitmap == null) {
        DemoPillBadge(text = "Demo / Manual")
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Doctor: ${doctorName ?: "Not readable"} · Date: ${rxDate ?: "Not readable"}",
      style = MaterialTheme.typography.labelMedium,
      color = c.inkSecondary,
    )
    if (bitmap != null) {
      Spacer(modifier = Modifier.height(10.dp))
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Captured prescription kept beside extracted medicines",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardShape),
      )
    }
  }
}

@Composable
private fun ReviewHeaderCard(
  warnings: List<String>,
  isSampleResult: Boolean,
  onAddBlank: () -> Unit,
  onProceedChecks: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  SehatCard(containerColor = c.mistSoft, modifier = modifier) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = "2. Review & confirm every line",
        style = MaterialTheme.typography.titleLarge,
        color = c.deepOcean,
        modifier = Modifier.weight(1f).padding(end = 8.dp),
      )
      if (isSampleResult) {
        DemoPillBadge(text = "Sample result")
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      "Edit any medicine name, strength, or timing below. Amber items have low confidence and must be checked carefully.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.ink,
    )
    warnings.forEach { w ->
      Spacer(modifier = Modifier.height(6.dp))
      Text("• $w", style = MaterialTheme.typography.labelMedium, color = c.statusCautionFg)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditableRxMedicineCard(
  item: ExtractedRxMedicine,
  onUpdate: (ExtractedRxMedicine) -> Unit,
  onRemove: () -> Unit,
) {
  val c = SehatPalette.current
  val lowConfidence = item.needsVerification || item.confidence < 0.7
  val bg = if (lowConfidence) c.statusCautionBg else c.surface
  val border = if (lowConfidence) c.statusCautionBorder else c.hairlineBorder

  SehatCard(containerColor = bg, borderColor = border) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      val pct = (item.confidence * 100).toInt()
      StatusChip(
        level = if (lowConfidence) HealthStatusLevel.CAUTION else HealthStatusLevel.NORMAL,
        label = if (lowConfidence) "Please verify ($pct%)" else "Extracted ($pct%)",
      )
      SehatIconButton(
        icon = Icons.Filled.Delete,
        contentDescription = "Remove medicine line",
        onClick = onRemove,
        tint = c.statusUrgentFg,
      )
    }

    if (lowConfidence) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Please verify with your doctor or pharmacist before saving.",
        style = MaterialTheme.typography.labelLarge,
        color = c.statusCautionFg,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))
    SehatTextField(
      label = "Medicine name",
      value = item.nameAsWritten,
      onValueChange = { onUpdate(item.copy(nameAsWritten = it, normalizedName = it)) },
      placeholder = "e.g. Glycomet 500",
    )
    Spacer(modifier = Modifier.height(10.dp))
    SehatTextField(
      label = "Strength",
      value = item.strength.orEmpty(),
      onValueChange = { onUpdate(item.copy(strength = it.ifBlank { null })) },
      placeholder = "e.g. 500 mg",
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text("How often & when", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
    Spacer(modifier = Modifier.height(6.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      listOf("OD", "BD", "TDS", "QID", "SOS").forEach { freq ->
        FilterChip(
          selected = item.frequency.equals(freq, ignoreCase = true),
          onClick = { onUpdate(item.copy(frequency = freq)) },
          label = { Text(freq) },
          colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.mist, selectedLabelColor = c.deepOcean),
        )
      }
      listOf("before_food" to "Before food", "after_food" to "After food", "bedtime" to "Bedtime").forEach { (key, label) ->
        FilterChip(
          selected = item.timing == key,
          onClick = { onUpdate(item.copy(timing = key)) },
          label = { Text(label) },
          colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.mist, selectedLabelColor = c.deepOcean),
        )
      }
    }
  }
}

@Composable
fun SafetyAndGenericsReportSection(
  report: MedicineSafetyCheckReport,
  isExplainingAi: Boolean,
  aiExplanation: String?,
  onExplainInteraction: (InteractionWarning) -> Unit,
  onShowPharmacistCard: () -> Unit,
) {
  val c = SehatPalette.current
  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // 1. Interaction Warnings
    SehatCard(containerColor = c.surface) {
      Text("Interaction check", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      Spacer(modifier = Modifier.height(8.dp))
      if (report.interactions.isEmpty() && report.unknownPairNotices.isEmpty()) {
        StatusChip(level = HealthStatusLevel.NORMAL, label = "No known pair flags in sample database")
      } else {
        report.interactions.forEach { warning ->
          Surface(
            shape = CardShape,
            color = if (warning.statusLevel == HealthStatusLevel.URGENT) c.statusUrgentBg else c.statusCautionBg,
            border = BorderStroke(1.dp, if (warning.statusLevel == HealthStatusLevel.URGENT) c.statusUrgentBorder else c.statusCautionBorder),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              StatusChip(level = warning.statusLevel, label = warning.severity.label)
              Text(
                "${warning.medicineA} + ${warning.medicineB} (${warning.ingredientA} + ${warning.ingredientB})",
                style = MaterialTheme.typography.titleMedium,
                color = c.deepOcean,
              )
              Text(warning.reason, style = MaterialTheme.typography.bodyMedium, color = c.ink)
              SehatGhostButton(
                text = if (isExplainingAi) "Explaining..." else "Explain simply with AI",
                icon = Icons.Filled.AutoAwesome,
                enabled = !isExplainingAi,
                onClick = { onExplainInteraction(warning) },
              )
            }
          }
        }
        if (isExplainingAi) {
          Spacer(modifier = Modifier.height(8.dp))
          SehatCardSkeleton(label = "Explaining interaction in plain English...", lines = 3)
        }
        aiExplanation?.let { exp ->
          Surface(shape = CardShape, color = c.mistSoft, border = BorderStroke(1.dp, c.hairlineBorder), modifier = Modifier.fillMaxWidth()) {
            Text(exp, style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.padding(12.dp))
          }
        }
        report.unknownPairNotices.forEach { msg ->
          Surface(shape = CardShape, color = c.statusCautionBg, border = BorderStroke(1.dp, c.statusCautionBorder), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(msg, style = MaterialTheme.typography.bodyMedium, color = c.statusCautionFg, modifier = Modifier.padding(12.dp))
          }
        }
      }
    }

    // 2. Duplicate Ingredients
    if (report.duplicates.isNotEmpty()) {
      SehatCard(containerColor = c.statusCautionBg, borderColor = c.statusCautionBorder) {
        Text("Duplicate ingredient alert", style = MaterialTheme.typography.titleLarge, color = c.statusCautionFg)
        Spacer(modifier = Modifier.height(6.dp))
        report.duplicates.forEach { dup ->
          Text("• ${dup.message}", style = MaterialTheme.typography.bodyMedium, color = c.ink)
        }
      }
    }

    // 3. Generics & Unknown Status per Medicine
    SehatCard(containerColor = c.surface) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Generic options (exact match)", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        Icon(Icons.Filled.LocalPharmacy, contentDescription = null, tint = c.clinicalBlueAccent)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        "Generic matches are shown only when both active ingredient and strength match. Prices are approximate; verify at the store.",
        style = MaterialTheme.typography.labelMedium,
        color = c.inkSecondary,
      )
      Spacer(modifier = Modifier.height(10.dp))

      report.resolvedMedicines.forEach { med ->
        Surface(
          shape = CardShape,
          color = if (med.isUnknown) c.statusCautionBg else c.mistSoft,
          border = BorderStroke(1.dp, if (med.isUnknown) c.statusCautionBorder else c.hairlineBorder),
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(med.inputName, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            if (med.isUnknown) {
              StatusChip(level = HealthStatusLevel.CAUTION, label = med.unknownMessage ?: "Not in our database. Please ask your pharmacist.")
            } else if (med.genericEquivalent != null) {
              val g = med.genericEquivalent
              Text("Active ingredient: ${g.ingredient} ${g.strength}", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
              Text("Generic option: ${g.genericName}", style = MaterialTheme.typography.labelLarge, color = c.clinicalBlueAccent)
              Text(
                "Approx. brand ${g.approxBrandPrice} vs generic ${g.approxGenericPrice} (${g.priceDisclaimer})",
                style = MaterialTheme.typography.labelMedium,
                color = c.ink,
              )
            } else {
              Text(
                "Matched ${med.resolvedIngredient}, but strength (${med.inputStrength ?: "unknown"}) did not match a generic entry. Ask your pharmacist.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.statusCautionFg,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      SehatSecondaryButton(
        text = "Show this to your pharmacist",
        icon = Icons.Filled.LocalPharmacy,
        onClick = onShowPharmacistCard,
        modifier = Modifier.fillMaxWidth(),
        testTag = "show_pharmacist_btn",
      )
    }
  }
}

@Composable
fun ShowPharmacistDialog(
  report: MedicineSafetyCheckReport,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.5.dp, c.clinicalBlueAccent),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Show this to your pharmacist", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
        Text(
          "Please verify same active ingredient and strength before dispensing:",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        HorizontalDivider(color = c.hairlineBorder)
        report.resolvedMedicines.forEach { item ->
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.inputName, style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            Text(
              text =
                item.genericEquivalent?.let { "→ Ask for: ${it.genericName}" }
                  ?: item.unknownMessage
                  ?: "→ Verify strength with pharmacist",
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(report.datasetNotice, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
        SehatPrimaryButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
      }
    }
  }
}

private fun createTempRxPhotoUri(context: Context): Uri {
  val dir = File(context.cacheDir, "captures").apply { mkdirs() }
  val file = File(dir, "rx_${System.currentTimeMillis()}.jpg")
  return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

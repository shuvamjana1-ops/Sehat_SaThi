package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.DaySlot
import com.example.data.HealthStatusLevel
import com.example.data.RefillStatus
import com.example.data.SeededMedicine
import com.example.domain.drugs.InteractionWarning
import com.example.domain.refill.RefillEngine
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatProgressMeter
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.components.sehatScroll3DKeyed
import com.example.ui.schedule.ScheduleNotificationHelper
import com.example.ui.schedule.ScheduleSpeechHelper
import com.example.ui.theme.CardShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicinesAndScheduleScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onToggleDoseTaken: (String) -> Unit,
  onSkipDose: (String) -> Unit,
  onCheckExtraMedicine: (String) -> Unit,
  onExplainInteraction: (InteractionWarning) -> Unit,
  onRestockMedicine: (medicineId: String, amount: Int) -> Unit,
  onAdjustStock: (medicineId: String, newCount: Int, markUnknown: Boolean) -> Unit,
  onSnoozeRefill: (medicineId: String) -> Unit,
  onRecordPharmacyOrder: (medicineName: String) -> Unit,
  onOpenScanner: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var isSpeaking by remember { mutableStateOf(false) }
  var extraMedicineInput by remember { mutableStateOf(uiState.extraCheckMedicineName) }
  var showPharmacistDialog by remember { mutableStateOf(false) }
  var adjustTargetMed by remember { mutableStateOf<SeededMedicine?>(null) }

  val speechHelper = remember(context) { ScheduleSpeechHelper(context) }
  DisposableEffect(Unit) { onDispose { speechHelper.shutdown() } }

  val nextPendingDose = uiState.dosesToday.firstOrNull { !it.taken && it.id !in uiState.skippedDoseIds }

  val notifPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted && nextPendingDose != null) {
        ScheduleNotificationHelper.showDoseReminderNotification(context, nextPendingDose)
        onShowToast("Reminder notification posted.")
      } else {
        onShowToast("Notifications blocked; reminders will still show inside SehatSaathi.")
      }
    }

  fun slotTimeLabel(slot: DaySlot): String =
    when (slot) {
      DaySlot.MORNING -> uiState.reminderPrefs.morningTime
      DaySlot.AFTERNOON -> uiState.reminderPrefs.afternoonTime
      DaySlot.NIGHT -> uiState.reminderPrefs.nightTime
    }

  val endingNotices = remember(uiState.medicines) {
    RefillEngine.prescriptionEndingNotices(uiState.medicines)
  }
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val listState = rememberLazyListState()

  Column(modifier = modifier.fillMaxSize()) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = c.surface,
      contentColor = c.deepOcean,
      indicator = { tabPositions ->
        if (selectedTab < tabPositions.size) {
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = c.clinicalBlueAccent,
          )
        }
      },
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        selectedContentColor = c.clinicalBlueAccent,
        unselectedContentColor = c.inkSecondary,
        text = { Text("Schedule", maxLines = 1, overflow = TextOverflow.Ellipsis) },
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        selectedContentColor = c.clinicalBlueAccent,
        unselectedContentColor = c.inkSecondary,
        text = { Text("Safety & Generics", maxLines = 2, overflow = TextOverflow.Ellipsis) },
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        selectedContentColor = c.clinicalBlueAccent,
        unselectedContentColor = c.inkSecondary,
        text = { Text("Refills (${uiState.medicines.size})", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.testTag("medicines_refills_tab"),
      )
    }

    LazyColumn(
      state = listState,
      contentPadding =
        PaddingValues(16.dp, 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      item(key = "med_notice") {
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
      }

      when (selectedTab) {
        0 -> {
          item(key = "sched_spoken") {
            SehatCard(
              containerColor = c.surface,
              modifier = Modifier.sehatScroll3DKeyed(listState, "sched_spoken", reducedMotion),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Daily spoken schedule", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
                DemoPillBadge(text = "en-IN Voice")
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text =
                  if (uiState.reminderPrefs.quietHoursEnabled) {
                    "Quiet hours active (${uiState.reminderPrefs.quietStart} – ${uiState.reminderPrefs.quietEnd}). Urgent alerts always show."
                  } else {
                    "Background notifications may be limited by your device; reminders always show when SehatSaathi is open."
                  },
                style = MaterialTheme.typography.labelMedium,
                color = c.inkSecondary,
              )
              Spacer(modifier = Modifier.height(12.dp))
              FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SehatPrimaryButton(
                  text = if (isSpeaking) "Stop listening" else "Listen",
                  icon = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                  onClick = {
                    if (isSpeaking) {
                      speechHelper.stop()
                      isSpeaking = false
                    } else {
                      isSpeaking = true
                      speechHelper.speak(ScheduleSpeechHelper.buildScheduleSpeechText(uiState.dosesToday))
                    }
                  },
                  testTag = "schedule_listen_btn",
                )
                SehatSecondaryButton(
                  text = "Test reminder alert",
                  icon = Icons.Filled.NotificationsActive,
                  minHeight = 56.dp,
                  onClick = {
                    val target = nextPendingDose ?: uiState.dosesToday.firstOrNull()
                    if (target == null) {
                      onShowToast("No scheduled doses found.")
                    } else if (ScheduleNotificationHelper.hasNotificationPermission(context)) {
                      ScheduleNotificationHelper.showDoseReminderNotification(context, target)
                      onShowToast("Reminder notification sent for ${target.medicineLabel}.")
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                      onShowToast("Notifications are disabled in system settings; reminders still show inside SehatSaathi.")
                    }
                  },
                  testTag = "schedule_test_notif_btn",
                )
              }
            }
          }

          if (nextPendingDose != null) {
            item(key = "sched_up_next") {
              SehatCard(
                containerColor = c.mistSoft,
                borderColor = c.clinicalBlueAccent,
                modifier = Modifier.sehatScroll3DKeyed(listState, "sched_up_next", reducedMotion),
              ) {
                StatusChip(
                  level = HealthStatusLevel.INFO,
                  label = "Up next · ${nextPendingDose.slot.title} (${slotTimeLabel(nextPendingDose.slot)})",
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(nextPendingDose.medicineLabel, style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
                Text(nextPendingDose.instruction, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                  SehatPrimaryButton(
                    text = "Taken",
                    icon = Icons.Filled.Check,
                    onClick = { onToggleDoseTaken(nextPendingDose.id) },
                    modifier = Modifier.weight(1f),
                    testTag = "reminder_taken_btn",
                  )
                  SehatSecondaryButton(
                    text = "Skip",
                    icon = Icons.Filled.SkipNext,
                    minHeight = 56.dp,
                    onClick = { onSkipDose(nextPendingDose.id) },
                    modifier = Modifier.weight(1f),
                    testTag = "reminder_skip_btn",
                  )
                }
              }
            }
          }

          DaySlot.entries.forEach { slot ->
            val slotDoses = uiState.dosesToday.filter { it.slot == slot }
            if (slotDoses.isNotEmpty()) {
              item(key = "slot_hdr_${slot.name}") {
                Text("${slot.title} (${slotTimeLabel(slot)})", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              }
              items(slotDoses, key = { it.id }) { dose ->
                val skipped = dose.id in uiState.skippedDoseIds
                SehatCard(
                  containerColor = c.surface,
                  modifier = Modifier.sehatScroll3DKeyed(listState, dose.id, reducedMotion),
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                      Text(dose.medicineLabel, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                      Text(dose.instruction, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
                    }
                    StatusChip(
                      level =
                        when {
                          dose.taken -> HealthStatusLevel.NORMAL
                          skipped -> HealthStatusLevel.CAUTION
                          else -> HealthStatusLevel.INFO
                        },
                      label =
                        when {
                          dose.taken -> "Taken"
                          skipped -> "Skipped"
                          else -> "Pending"
                        },
                    )
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SehatSecondaryButton(
                      text = if (dose.taken) "Undo taken" else "Mark Taken",
                      onClick = { onToggleDoseTaken(dose.id) },
                      modifier = Modifier.weight(1f),
                    )
                    if (!dose.taken && !skipped) {
                      SehatSecondaryButton(
                        text = "Skip",
                        onClick = { onSkipDose(dose.id) },
                        modifier = Modifier.weight(0.6f),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          item(key = "safety_check_input") {
            SehatCard(
              containerColor = c.surface,
              modifier = Modifier.sehatScroll3DKeyed(listState, "safety_check_input", reducedMotion),
            ) {
              Text("Check another medicine", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "Type any brand or generic name (e.g. Brufen 400, Forcan 150, Dolo 650) to check interactions and generic pricing against your active list.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.inkSecondary,
              )
              Spacer(modifier = Modifier.height(10.dp))
              SehatTextField(
                label = "Medicine name to check",
                value = extraMedicineInput,
                onValueChange = { extraMedicineInput = it },
                placeholder = "e.g. Brufen 400",
                testTag = "extra_med_check_input",
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SehatPrimaryButton(
                  text = "Run safety check",
                  onClick = { onCheckExtraMedicine(extraMedicineInput) },
                  modifier = Modifier.weight(1f),
                  testTag = "run_extra_med_check_btn",
                )
                SehatSecondaryButton(
                  text = "Scan Rx",
                  icon = Icons.Filled.DocumentScanner,
                  minHeight = 56.dp,
                  onClick = onOpenScanner,
                )
              }
            }
          }

          uiState.activeSafetyReport?.let { report ->
            item {
              SafetyAndGenericsReportSection(
                report = report,
                isExplainingAi = uiState.rxScanner.isExplainingInteraction,
                aiExplanation = uiState.rxScanner.interactionExplanation,
                onExplainInteraction = onExplainInteraction,
                onShowPharmacistCard = { showPharmacistDialog = true },
              )
            }
          }
        }

        2 -> {
          item(key = "refill_hdr") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Filled.Medication, contentDescription = null, tint = c.deepOcean)
              Text("Refill & Stock Tracker", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
            }
          }

          if (endingNotices.isNotEmpty()) {
            item(key = "refill_ending") {
              SehatCard(
                containerColor = c.statusCautionBg,
                borderColor = c.statusCautionBorder,
                modifier = Modifier.sehatScroll3DKeyed(listState, "refill_ending", reducedMotion),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Filled.Warning, contentDescription = null, tint = c.statusCautionFg)
                  Text("Prescription ending notice", style = MaterialTheme.typography.titleMedium, color = c.statusCautionFg)
                }
                Spacer(modifier = Modifier.height(6.dp))
                endingNotices.forEach { note ->
                  Text("• $note", style = MaterialTheme.typography.bodyMedium, color = c.ink)
                }
              }
            }
          }

          // Pharmacy contact card
          item(key = "refill_pharmacy") {
            SehatCard(
              containerColor = c.mistSoft,
              borderColor = c.clinicalBlueAccent,
              modifier = Modifier.sehatScroll3DKeyed(listState, "refill_pharmacy", reducedMotion),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Filled.LocalPharmacy, contentDescription = null, tint = c.clinicalBlueAccent)
                  Text(uiState.patient.pharmacyName, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                }
                DemoPillBadge(text = "Demo")
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Phone: ${uiState.patient.pharmacyPhone}",
                style = MaterialTheme.typography.labelLarge.tabular().copy(textDecoration = TextDecoration.Underline),
                color = c.clinicalBlueAccent,
                modifier =
                  Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(role = Role.Button) {
                      launchSafeDialer(context, uiState.patient.pharmacyPhone, onShowToast)
                    }
                    .padding(vertical = 10.dp),
              )
              Spacer(modifier = Modifier.height(6.dp))
              FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SehatPrimaryButton(
                  text = "WhatsApp pharmacy (Demo)",
                  icon = Icons.AutoMirrored.Filled.Chat,
                  onClick = {
                    val msg = RefillEngine.buildPharmacyWhatsAppText(uiState.patient, uiState.medicines)
                    launchWhatsAppAlert(context, uiState.patient.pharmacyPhone, msg)
                    onRecordPharmacyOrder("Pharmacy refill list")
                  },
                  testTag = "pharmacy_whatsapp_btn",
                )
                SehatSecondaryButton(
                  text = "Call pharmacy",
                  icon = Icons.Filled.Call,
                  minHeight = 56.dp,
                  onClick = { launchSafeDialer(context, uiState.patient.pharmacyPhone, onShowToast) },
                  testTag = "pharmacy_call_btn",
                )
              }
            }
          }

          items(uiState.medicines.sortedBy { it.daysLeftOrNull ?: -1 }, key = { it.id }) { med ->
            val progressColor =
              when (med.refillStatus) {
                RefillStatus.OUT, RefillStatus.DUE -> c.statusUrgentFg
                RefillStatus.SOON, RefillStatus.UNKNOWN -> c.statusCautionFg
                RefillStatus.OK -> c.clinicalBlueAccent
              }
            SehatCard(
              containerColor = if (med.refillStatus == RefillStatus.UNKNOWN) c.statusCautionBg else c.surface,
              borderColor = if (med.refillStatus == RefillStatus.UNKNOWN) c.statusCautionBorder else c.hairlineBorder,
              modifier = Modifier.sehatScroll3DKeyed(listState, med.id, reducedMotion),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                  Text(med.brandName, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                  Text(
                    "${med.genericName} ${med.strength} · ${med.frequency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.inkSecondary,
                  )
                }
                StatusChip(level = med.refillStatus.badgeLevel, label = med.refillStatus.label)
              }
              Spacer(modifier = Modifier.height(8.dp))
              if (med.refillStatus == RefillStatus.UNKNOWN) {
                Text(
                  RefillEngine.UNKNOWN_STOCK_MESSAGE,
                  style = MaterialTheme.typography.bodyMedium,
                  color = c.statusCautionFg,
                )
              } else {
                Text(
                  "Stock: ${med.stockCount} ${med.unit} · ${med.daysLeft} day(s) left" +
                    (med.snoozedUntilLabel?.let { " · $it" } ?: ""),
                  style = MaterialTheme.typography.labelLarge.tabular(),
                  color = c.ink,
                )
                Spacer(modifier = Modifier.height(6.dp))
                SehatProgressMeter(
                  progress = (med.daysLeft / 15f).coerceIn(0.05f, 1f),
                  color = progressColor,
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SehatPrimaryButton(
                  text = "Restock +15",
                  icon = Icons.Filled.AddCircleOutline,
                  minHeight = 48.dp,
                  onClick = { onRestockMedicine(med.id, 15) },
                  testTag = "restock_15_${med.id}",
                )
                SehatSecondaryButton(
                  text = "Adjust stock",
                  icon = Icons.Filled.Edit,
                  onClick = { adjustTargetMed = med },
                  testTag = "adjust_stock_${med.id}",
                )
                SehatGhostButton(
                  text = "Snooze 2d",
                  icon = Icons.Filled.Snooze,
                  onClick = { onSnoozeRefill(med.id) },
                )
              }
            }
          }

          if (uiState.refillHistory.isNotEmpty()) {
            item(key = "refill_history_hdr") {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.History, contentDescription = null, tint = c.deepOcean)
                Text("Refill history", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              }
            }
            items(uiState.refillHistory.take(8), key = { it.id }) { entry ->
              SehatCard(
                containerColor = c.surface,
                modifier = Modifier.sehatScroll3DKeyed(listState, entry.id, reducedMotion),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(entry.medicineName, style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
                    Text(entry.actionLabel, style = MaterialTheme.typography.bodyMedium, color = c.ink)
                  }
                  Text(entry.timestampLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
                }
              }
            }
          }
        }
      }
    }
  }

  val activeReport = uiState.activeSafetyReport
  if (showPharmacistDialog && activeReport != null) {
    ShowPharmacistDialog(
      report = activeReport,
      onDismiss = { showPharmacistDialog = false },
    )
  }

  adjustTargetMed?.let { target ->
    AdjustStockDialog(
      medicine = target,
      onSave = { count, unknown ->
        onAdjustStock(target.id, count, unknown)
        adjustTargetMed = null
      },
      onDismiss = { adjustTargetMed = null },
    )
  }
}

@Composable
private fun AdjustStockDialog(
  medicine: SeededMedicine,
  onSave: (count: Int, markUnknown: Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  var countText by remember(medicine) { mutableStateOf(medicine.stockCount.toString()) }
  var markUnknown by remember(medicine) { mutableStateOf(medicine.unknownStock) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.5.dp, c.clinicalBlueAccent),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Adjust ${medicine.brandName}", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
        SehatTextField(
          label = "Exact count (${medicine.unit})",
          value = countText,
          onValueChange = { countText = it.filter { ch -> ch.isDigit() }.take(4) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          enabled = !markUnknown,
          testTag = "adjust_stock_input",
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 48.dp)
              .clickable { markUnknown = !markUnknown },
        ) {
          Checkbox(
            checked = markUnknown,
            onCheckedChange = { markUnknown = it },
            colors =
              CheckboxDefaults.colors(
                checkedColor = c.clinicalBlue,
                uncheckedColor = c.inkSecondary,
                checkmarkColor = PureWhite,
              ),
          )
          Text(
            "Stock unknown (never show as plentiful)",
            style = MaterialTheme.typography.bodyMedium,
            color = c.ink,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(0.45f))
          SehatPrimaryButton(
            text = "Save stock",
            onClick = { onSave(countText.toIntOrNull() ?: 0, markUnknown) },
            modifier = Modifier.weight(0.55f),
            testTag = "save_adjusted_stock_btn",
          )
        }
      }
    }
  }
}

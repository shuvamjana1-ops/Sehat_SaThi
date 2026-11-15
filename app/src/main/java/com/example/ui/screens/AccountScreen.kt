package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.HealthStatusLevel
import com.example.data.ReminderPreferences
import com.example.domain.risk.RiskThresholdsConfig
import com.example.export.DoctorSummaryAndDataExporter
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatDestructiveButton
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.launchSafeEmail
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.components.sehatScroll3DKeyed
import com.example.ui.theme.CardShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.ThemePreference

private enum class SensitiveActionType {
  EXPORT_JSON,
  DELETE_ACCOUNT,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onSetThemePreference: (ThemePreference) -> Unit = {},
  onSaveProfile: (name: String, age: Int, conditions: String, pharmacyName: String, pharmacyPhone: String) -> Unit,
  onSaveReminderPrefs: (ReminderPreferences) -> Unit,
  onUpdateThresholds: (RiskThresholdsConfig) -> Unit,
  onAddCaregiverInvite: (name: String, relationship: String, phone: String) -> String?,
  onAcceptCaregiverDemo: (String) -> Unit,
  onRevokeCaregiver: (String) -> Unit,
  onVerifyReauth: (email: String, secret: String) -> Boolean,
  onDeleteAccount: (confirmationText: String) -> Boolean,
  onResetDemoData: () -> Unit,
  onLogout: () -> Unit,
  onToggleOfflineSimulation: (Boolean) -> Unit,
  onSyncOfflineQueue: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val context = LocalContext.current

  var nameInput by remember(uiState.patient.name) { mutableStateOf(uiState.patient.name) }
  var ageInput by remember(uiState.patient.age) { mutableStateOf(uiState.patient.age.toString()) }
  var conditionsInput by remember(uiState.patient.conditions) {
    mutableStateOf(uiState.patient.conditions.joinToString(", "))
  }
  var pharmacyNameInput by remember(uiState.patient.pharmacyName) {
    mutableStateOf(uiState.patient.pharmacyName)
  }
  var pharmacyPhoneInput by remember(uiState.patient.pharmacyPhone) {
    mutableStateOf(uiState.patient.pharmacyPhone)
  }

  var morningTime by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.morningTime) }
  var afternoonTime by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.afternoonTime) }
  var nightTime by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.nightTime) }
  var quietEnabled by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.quietHoursEnabled) }
  var quietStart by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.quietStart) }
  var quietEnd by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.quietEnd) }

  var lowSugarInput by remember(uiState.thresholdsConfig) {
    mutableStateOf(uiState.thresholdsConfig.glucoseLowMgDl.toString())
  }
  var highSugarUrgentInput by remember(uiState.thresholdsConfig) {
    mutableStateOf(uiState.thresholdsConfig.glucoseHighUrgentMgDl.toString())
  }
  var bpCrisisSysInput by remember(uiState.thresholdsConfig) {
    mutableStateOf(uiState.thresholdsConfig.bpCrisisSystolic.toString())
  }

  var cgName by remember { mutableStateOf("") }
  var cgRelation by remember { mutableStateOf("Family") }
  var cgPhone by remember { mutableStateOf("") }
  var cgError by remember { mutableStateOf<String?>(null) }

  var pendingSensitiveAction by remember { mutableStateOf<SensitiveActionType?>(null) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }
  var jsonPreviewText by remember { mutableStateOf<String?>(null) }
  val reducedMotion = rememberPrefersReducedMotion()
  val listState = rememberLazyListState()

  LazyColumn(
    state = listState,
    contentPadding =
      PaddingValues(16.dp, 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier.fillMaxSize().imePadding().testTag("account_scroll"),
  ) {
    item(key = "acc_header") {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Filled.Person, contentDescription = null, tint = c.deepOcean)
        Text("Account & Security", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        if (uiState.session.isDemoMode) DemoPillBadge(text = "Demo mode")
      }
      val displayEmail = uiState.session.email.ifBlank { "sunita.demo@sehatsaathi.local" }
      val isRealEmail = !uiState.session.isDemoMode && !displayEmail.endsWith(".local", ignoreCase = true)
      if (isRealEmail) {
        Text(
          text = "Email: $displayEmail",
          style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
          color = c.clinicalBlueAccent,
          modifier =
            Modifier
              .defaultMinSize(minHeight = 48.dp)
              .clickable(role = Role.Button) {
                launchSafeEmail(context, displayEmail, "SehatSaathi account", onShowToast)
              }
              .padding(vertical = 8.dp),
        )
      } else {
        Text(
          text = "Email: $displayEmail",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
      }
    }

    // 0. Appearance & Theme Selector (System / Light / Dark)
    item(key = "acc_theme") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_theme", reducedMotion),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = if (c.isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = null,
            tint = c.clinicalBlueAccent,
          )
          Text("Appearance & theme", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Choose System default, crisp Light mode, or high-contrast Dark mode (WCAG AA).",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          ThemePreference.entries.forEach { pref ->
            val selected = uiState.themePreference == pref
            val prefIcon =
              when (pref) {
                ThemePreference.SYSTEM -> Icons.Filled.SettingsBrightness
                ThemePreference.LIGHT -> Icons.Filled.LightMode
                ThemePreference.DARK -> Icons.Filled.DarkMode
              }
            Surface(
              onClick = { onSetThemePreference(pref) },
              shape = PillShape,
              color = if (selected) c.clinicalBlue else c.mistSoft,
              border = BorderStroke(1.5.dp, if (selected) c.clinicalBlue else c.hairlineBorder),
              modifier =
                Modifier
                  .weight(1f)
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("theme_pref_${pref.name.lowercase()}"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
              ) {
                Icon(
                  imageVector = prefIcon,
                  contentDescription = null,
                  tint = if (selected) PureWhite else c.deepOcean,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = pref.label,
                  style = MaterialTheme.typography.labelLarge,
                  color = if (selected) PureWhite else c.deepOcean,
                  maxLines = 1,
                )
              }
            }
          }
        }
      }
    }

    // 1. Edit Profile & Pharmacy
    item(key = "acc_profile") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_profile", reducedMotion),
      ) {
        Text("Patient & pharmacy details", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        Spacer(modifier = Modifier.height(10.dp))
        SehatTextField(
          label = "Patient name",
          value = nameInput,
          onValueChange = { nameInput = it },
          testTag = "account_name_input",
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatTextField(
            label = "Age",
            value = ageInput,
            onValueChange = { ageInput = it.filter { ch -> ch.isDigit() }.take(3) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.32f),
          )
          SehatTextField(
            label = "Conditions (comma-separated)",
            value = conditionsInput,
            onValueChange = { conditionsInput = it },
            modifier = Modifier.weight(0.68f),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        SehatTextField(
          label = "Preferred pharmacy name",
          value = pharmacyNameInput,
          onValueChange = { pharmacyNameInput = it },
        )
        Spacer(modifier = Modifier.height(10.dp))
        SehatTextField(
          label = "Pharmacy phone / WhatsApp",
          value = pharmacyPhoneInput,
          onValueChange = { pharmacyPhoneInput = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(modifier = Modifier.height(12.dp))
        SehatPrimaryButton(
          text = "Save profile & pharmacy",
          icon = Icons.Filled.Save,
          onClick = {
            onSaveProfile(
              nameInput,
              ageInput.toIntOrNull() ?: uiState.patient.age,
              conditionsInput,
              pharmacyNameInput,
              pharmacyPhoneInput,
            )
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "save_profile_btn",
        )
      }
    }

    // 2. Reminder Times & Quiet Hours
    item(key = "acc_reminders") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_reminders", reducedMotion),
      ) {
        Text("Reminder times & quiet hours", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          SehatTextField(
            label = "Morning",
            value = morningTime,
            onValueChange = { morningTime = it },
            modifier = Modifier.weight(1f),
          )
          SehatTextField(
            label = "Afternoon",
            value = afternoonTime,
            onValueChange = { afternoonTime = it },
            modifier = Modifier.weight(1f),
          )
          SehatTextField(
            label = "Night",
            value = nightTime,
            onValueChange = { nightTime = it },
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text("Quiet hours", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            Text(
              "Pauses routine reminders during sleep; urgent low-sugar / BP emergency alerts still ring.",
              style = MaterialTheme.typography.labelMedium,
              color = c.inkSecondary,
            )
          }
          Switch(
            checked = quietEnabled,
            onCheckedChange = { quietEnabled = it },
            colors =
              SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = c.clinicalBlue,
                uncheckedThumbColor = c.inkSecondary,
                uncheckedTrackColor = c.mistSoft,
                uncheckedBorderColor = c.hairlineBorder,
              ),
          )
        }
        if (quietEnabled) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SehatTextField(
              label = "Quiet start",
              value = quietStart,
              onValueChange = { quietStart = it },
              modifier = Modifier.weight(1f),
            )
            SehatTextField(
              label = "Quiet end",
              value = quietEnd,
              onValueChange = { quietEnd = it },
              modifier = Modifier.weight(1f),
            )
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SehatSecondaryButton(
          text = "Save reminder settings",
          minHeight = 52.dp,
          onClick = {
            onSaveReminderPrefs(
              ReminderPreferences(
                morningTime = morningTime.ifBlank { "8:00 AM" },
                afternoonTime = afternoonTime.ifBlank { "1:30 PM" },
                nightTime = nightTime.ifBlank { "9:00 PM" },
                quietHoursEnabled = quietEnabled,
                quietStart = quietStart.ifBlank { "10:00 PM" },
                quietEnd = quietEnd.ifBlank { "6:30 AM" },
              ),
            )
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "save_reminders_btn",
        )
      }
    }

    // 3. Caregiver Consent Lifecycle
    item(key = "acc_caregiver_invite") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_caregiver_invite", reducedMotion),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = c.clinicalBlueAccent)
          Text("Caregiver consent & contacts", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          DemoPillBadge(text = "Demo")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "Family contacts only receive urgent health alerts after accepting consent.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(10.dp))
        SehatTextField(
          label = "Add family contact name",
          value = cgName,
          onValueChange = { cgName = it; cgError = null },
          placeholder = "e.g. Dr. Nair or Anita",
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          SehatTextField(
            label = "Relationship",
            value = cgRelation,
            onValueChange = { cgRelation = it },
            modifier = Modifier.weight(0.45f),
          )
          SehatTextField(
            label = "Phone number",
            value = cgPhone,
            onValueChange = { cgPhone = it; cgError = null },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = "+91 98...",
            modifier = Modifier.weight(0.55f),
          )
        }
        cgError?.let { err ->
          Spacer(modifier = Modifier.height(6.dp))
          Text(err, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg)
        }
        Spacer(modifier = Modifier.height(10.dp))
        SehatSecondaryButton(
          text = "Send consent invite (Demo)",
          minHeight = 52.dp,
          onClick = {
            val err = onAddCaregiverInvite(cgName, cgRelation, cgPhone)
            if (err != null) {
              cgError = err
            } else {
              cgName = ""
              cgPhone = ""
            }
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "add_caregiver_invite_btn",
        )
      }
    }

    items(uiState.caregivers, key = { it.id }) { cg ->
      val isPending = cg.consentStatus.contains("Pending", ignoreCase = true)
      SehatCard(
        containerColor = c.mistSoft,
        modifier = Modifier.sehatScroll3DKeyed(listState, cg.id, reducedMotion),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(cg.name, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            Text(cg.relationship, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
            Text(
              text = cg.phone,
              style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
              color = c.clinicalBlueAccent,
              modifier =
                Modifier
                  .defaultMinSize(minHeight = 48.dp)
                  .clickable(role = Role.Button) {
                    launchSafeDialer(context, cg.phone, onShowToast)
                  }
                  .padding(vertical = 8.dp),
            )
          }
          StatusChip(
            level = if (isPending) HealthStatusLevel.CAUTION else HealthStatusLevel.NORMAL,
            label = cg.consentStatus,
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (isPending) {
            SehatPrimaryButton(
              text = "Simulate accept (Demo)",
              icon = Icons.Filled.Check,
              minHeight = 48.dp,
              onClick = { onAcceptCaregiverDemo(cg.id) },
            )
          }
          SehatGhostButton(
            text = "Revoke & remove",
            onClick = { onRevokeCaregiver(cg.id) },
          )
        }
      }
    }

    // 4. Configurable Clinician-Reviewable Thresholds
    item(key = "acc_thresholds") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_thresholds", reducedMotion),
      ) {
        Text("Safety rule thresholds", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        Text(
          "Deterministic rules use these cutoffs (review with your doctor before changing).",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          SehatTextField(
            label = "Low sugar (<)",
            value = lowSugarInput,
            onValueChange = { lowSugarInput = it.filter { ch -> ch.isDigit() }.take(3) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
          )
          SehatTextField(
            label = "Urgent high (>)",
            value = highSugarUrgentInput,
            onValueChange = { highSugarUrgentInput = it.filter { ch -> ch.isDigit() }.take(3) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
          )
          SehatTextField(
            label = "BP crisis (>=)",
            value = bpCrisisSysInput,
            onValueChange = { bpCrisisSysInput = it.filter { ch -> ch.isDigit() }.take(3) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          SehatSecondaryButton(
            text = "Apply thresholds",
            onClick = {
              val next =
                uiState.thresholdsConfig.copy(
                  glucoseLowMgDl = (lowSugarInput.toIntOrNull() ?: 70).coerceIn(55, 90),
                  glucoseHighUrgentMgDl = (highSugarUrgentInput.toIntOrNull() ?: 300).coerceIn(220, 450),
                  bpCrisisSystolic = (bpCrisisSysInput.toIntOrNull() ?: 180).coerceIn(150, 220),
                )
              onUpdateThresholds(next)
            },
            modifier = Modifier.weight(1f),
          )
          SehatGhostButton(
            text = "Defaults",
            onClick = { onUpdateThresholds(RiskThresholdsConfig()) },
          )
        }
      }
    }

    // 4b. Offline Mode & Sync Queue (Demo)
    item(key = "acc_offline") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_offline", reducedMotion),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(Icons.Filled.CloudOff, contentDescription = null, tint = c.deepOcean)
          Text(
            "Offline mode & sync queue",
            style = MaterialTheme.typography.titleLarge,
            color = c.deepOcean,
          )
          DemoPillBadge(text = "Demo")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
              text =
                if (uiState.effectiveOnline) "Connection: Online"
                else "Connection: Offline (local keypad & rules active)",
              style = MaterialTheme.typography.titleMedium,
              color = c.deepOcean,
            )
            Text(
              text =
                "Simulate offline mode to test queueing AI scans and calling family directly without internet.",
              style = MaterialTheme.typography.labelMedium,
              color = c.inkSecondary,
            )
          }
          Switch(
            checked = uiState.isOfflineSimulationEnabled,
            onCheckedChange = onToggleOfflineSimulation,
            colors =
              SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = c.clinicalBlue,
                uncheckedThumbColor = c.inkSecondary,
                uncheckedTrackColor = c.mistSoft,
                uncheckedBorderColor = c.hairlineBorder,
              ),
            modifier = Modifier.testTag("offline_sim_switch"),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        StatusChip(
          level = if (uiState.offlineQueue.isEmpty()) HealthStatusLevel.NORMAL else HealthStatusLevel.CAUTION,
          label =
            if (uiState.offlineQueue.isEmpty()) "0 queued AI scans"
            else "${uiState.offlineQueue.size} scan(s) queued for AI sync",
        )
        if (uiState.offlineQueue.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          uiState.offlineQueue.forEach { task ->
            Text(
              text = "• ${task.kind.title}: ${task.summary} (${task.createdAtLabel})",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          SehatPrimaryButton(
            text =
              if (uiState.isSyncingOfflineQueue) "Syncing queued scans..."
              else "Sync queued scans (${uiState.offlineQueue.size})",
            icon = Icons.Filled.Sync,
            enabled = uiState.effectiveOnline && !uiState.isSyncingOfflineQueue,
            onClick = onSyncOfflineQueue,
            modifier = Modifier.fillMaxWidth(),
            testTag = "account_sync_queue_btn",
          )
        }
      }
    }

    // 5. Privacy, JSON Data Download, Reset & Account Deletion
    item(key = "acc_privacy") {
      SehatCard(
        containerColor = c.surface,
        modifier = Modifier.sehatScroll3DKeyed(listState, "acc_privacy", reducedMotion),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Filled.Security, contentDescription = null, tint = c.deepOcean)
          Text("Privacy & your data", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Downloading all data or deleting your account requires confirming your identity first.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SehatPrimaryButton(
            text = "Download all data (JSON)",
            icon = Icons.Filled.Download,
            onClick = { pendingSensitiveAction = SensitiveActionType.EXPORT_JSON },
            testTag = "download_json_btn",
          )
          SehatSecondaryButton(
            text = "Reset demo data",
            icon = Icons.Filled.Refresh,
            minHeight = 56.dp,
            onClick = onResetDemoData,
            testTag = "reset_demo_data_btn",
          )
          SehatSecondaryButton(
            text = "Log out",
            icon = Icons.AutoMirrored.Filled.Logout,
            minHeight = 56.dp,
            onClick = onLogout,
            testTag = "logout_btn",
          )
        }

        jsonPreviewText?.let { preview ->
          Spacer(modifier = Modifier.height(12.dp))
          Surface(shape = CardShape, color = c.mist, border = BorderStroke(1.dp, c.hairlineBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("JSON Export Ready (${preview.length} bytes)", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
              Text(preview.take(280) + "...", style = MaterialTheme.typography.labelSmall, color = c.ink)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = c.hairlineBorder)
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
          shape = CardShape,
          color = c.statusUrgentBg,
          border = BorderStroke(1.dp, c.statusUrgentBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Danger zone", style = MaterialTheme.typography.titleMedium, color = c.statusUrgentFg)
            Text(
              "Permanently deletes your local SehatSaathi session, cached PDF/JSON exports, and resets all records.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
            )
            SehatDestructiveButton(
              text = "Delete account & all data",
              icon = Icons.Filled.DeleteForever,
              onClick = { pendingSensitiveAction = SensitiveActionType.DELETE_ACCOUNT },
              modifier = Modifier.fillMaxWidth(),
              testTag = "delete_account_btn",
            )
          }
        }
      }
    }
  }

  pendingSensitiveAction?.let { action ->
    ReauthDialog(
      defaultEmail = uiState.session.email,
      isDemoMode = uiState.session.isDemoMode,
      onVerify = onVerifyReauth,
      onDismiss = { pendingSensitiveAction = null },
      onVerified = {
        pendingSensitiveAction = null
        when (action) {
          SensitiveActionType.EXPORT_JSON -> {
            val json = DoctorSummaryAndDataExporter.buildJsonExport(uiState)
            jsonPreviewText = json
            val file = DoctorSummaryAndDataExporter.writeJsonExportFile(context, json)
            DoctorSummaryAndDataExporter.shareFileWithIntent(
              context = context,
              file = file,
              mimeType = "application/json",
              chooserTitle = "Save or Share SehatSaathi Data JSON",
            )
            onShowToast("Exported your data to ${file.name}.")
          }
          SensitiveActionType.DELETE_ACCOUNT -> showDeleteConfirmDialog = true
        }
      },
    )
  }

  if (showDeleteConfirmDialog) {
    DeleteAccountConfirmationDialog(
      onConfirmDelete = { typed ->
        val ok = onDeleteAccount(typed)
        if (ok) showDeleteConfirmDialog = false
        ok
      },
      onDismiss = { showDeleteConfirmDialog = false },
    )
  }
}

@Composable
private fun ReauthDialog(
  defaultEmail: String,
  isDemoMode: Boolean,
  onVerify: (String, String) -> Boolean,
  onDismiss: () -> Unit,
  onVerified: () -> Unit,
) {
  val c = SehatPalette.current
  var email by remember { mutableStateOf(defaultEmail) }
  var secret by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.5.dp, c.clinicalBlueAccent),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Confirm it is you", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
        Text(
          text =
            if (isDemoMode) "Demo re-authentication: type CONFIRM (or your 6+ char passcode) to continue."
            else "Enter your account email and password to proceed.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        SehatTextField(
          label = "Account email",
          value = email,
          onValueChange = { email = it; error = null },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        SehatTextField(
          label = if (isDemoMode) "Type CONFIRM (Demo)" else "Password",
          value = secret,
          onValueChange = { secret = it; error = null },
          isPassword = !isDemoMode,
          placeholder = if (isDemoMode) "CONFIRM" else "Password",
          testTag = "reauth_secret_input",
        )
        error?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(0.45f))
          SehatPrimaryButton(
            text = "Continue",
            onClick = {
              if (onVerify(email, secret)) onVerified()
              else error = "Verification did not match. Try typing CONFIRM."
            },
            modifier = Modifier.weight(0.55f),
            testTag = "reauth_continue_btn",
          )
        }
      }
    }
  }
}

@Composable
private fun DeleteAccountConfirmationDialog(
  onConfirmDelete: (String) -> Boolean,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  var typed by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(2.dp, c.statusUrgentFg),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Delete all data?", style = MaterialTheme.typography.headlineMedium, color = c.statusUrgentFg)
        Text(
          "Type DELETE in capital letters to permanently erase your local account, readings, medicines, and exports.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
        SehatTextField(
          label = "Type DELETE to confirm",
          value = typed,
          onValueChange = { typed = it },
          placeholder = "DELETE",
          testTag = "delete_confirm_input",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(0.45f))
          SehatDestructiveButton(
            text = "Delete forever",
            enabled = typed.trim() == "DELETE",
            onClick = { onConfirmDelete(typed) },
            modifier = Modifier.weight(0.55f),
            testTag = "confirm_delete_forever_btn",
          )
        }
      }
    }
  }
}

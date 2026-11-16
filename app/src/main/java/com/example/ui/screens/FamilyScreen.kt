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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.HealthStatusLevel
import com.example.domain.caregiver.CaregiverDomain
import com.example.domain.caregiver.CaregiverPatientLink
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.components.sehatScroll3DKeyed
import com.example.ui.theme.CardShape
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FamilyAlertsScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onTriggerDemoEscalation: () -> Unit,
  onAcknowledgeAlert: () -> Unit,
  onAddCaregiverInvite: (name: String, relationship: String, phone: String) -> String?,
  onAcceptCaregiverDemo: (String) -> Unit,
  onRevokeCaregiver: (String) -> Unit,
  onSetCaregiverViewMode: (Boolean) -> Unit,
  onToggleWeeklyDigestOptIn: (Boolean) -> Unit,
  onBackHome: () -> Unit,
) {
  val context = LocalContext.current
  var inviteName by remember { mutableStateOf("") }
  var inviteRelation by remember { mutableStateOf("Family") }
  var invitePhone by remember { mutableStateOf("") }
  var inviteError by remember { mutableStateOf<String?>(null) }
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val listState = rememberLazyListState()

  LazyColumn(
    state = listState,
    contentPadding =
      PaddingValues(16.dp, 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = Modifier.fillMaxSize().imePadding(),
  ) {
    item(key = "family_header") {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Filled.Groups, contentDescription = null, tint = c.deepOcean)
        Text("Family & Caregiver Hub", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        DemoPillBadge(text = "Demo")
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        "Manage family consent links, emergency escalations, and the Caregiver Dashboard view.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.inkSecondary,
      )
    }

    // Role switcher toggle
    item(key = "role_switcher") {
      Surface(
        shape = CardShape,
        color = c.surface,
        border = BorderStroke(1.dp, c.hairlineBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(14.dp).fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              if (uiState.caregiverViewMode) Icons.Filled.SupervisorAccount else Icons.Filled.Person,
              contentDescription = null,
              tint = c.clinicalBlueAccent,
            )
            Column {
              Text(
                if (uiState.caregiverViewMode) "Caregiver Dashboard Role" else "Patient View Role",
                style = MaterialTheme.typography.titleMedium,
                color = c.deepOcean,
              )
              Text(
                if (uiState.caregiverViewMode) "Viewing linked patient summaries & alerts" else "Switch to Caregiver view to review linked patient cards",
                style = MaterialTheme.typography.bodySmall,
                color = c.inkSecondary,
              )
            }
          }
          Switch(
            checked = uiState.caregiverViewMode,
            onCheckedChange = onSetCaregiverViewMode,
            modifier = Modifier.testTag("caregiver_view_toggle"),
          )
        }
      }
    }

    if (uiState.caregiverViewMode) {
      // CAREGIVER DASHBOARD VIEW (Group 3A & 3C)
      item(key = "caregiver_dashboard_hdr") {
        Text("Linked Patients (Caregiver Role)", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }

      item(key = "linked_patient_card") {
        val lastReading = uiState.readings.lastOrNull()
        val takenCount = uiState.dosesToday.count { it.taken }
        val totalDoses = uiState.dosesToday.size.coerceAtLeast(1)
        val refillGaps = uiState.medicines.count { it.daysLeft <= 5 }
        val statusLevel = if ((lastReading?.glucoseMgDl ?: 130) < 70 || (lastReading?.glucoseMgDl ?: 130) > 200) HealthStatusLevel.URGENT else HealthStatusLevel.NORMAL

        SehatCard(containerColor = c.surface) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(uiState.patient.name, style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              Text("Age ${uiState.patient.age} · ${uiState.patient.conditions.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
            }
            StatusChip(level = statusLevel, label = if (statusLevel == HealthStatusLevel.URGENT) "Urgent" else "Normal")
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Last Reading", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
              Text(
                text = lastReading?.let { "${it.glucoseMgDl} mg/dL (${it.contextLabel})" } ?: "No reading today",
                style = MaterialTheme.typography.bodyMedium.tabular(),
                color = c.ink,
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("Today's Doses", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
              Text("$takenCount / $totalDoses taken", style = MaterialTheme.typography.bodyMedium.tabular(), color = c.ink)
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Refill Status", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
              Text(if (refillGaps > 0) "$refillGaps medicine(s) due soon" else "All stocks OK", style = MaterialTheme.typography.bodyMedium, color = if (refillGaps > 0) c.statusCautionFg else c.ink)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("Open Alerts", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
              Text("${uiState.alerts.size} open alert(s)", style = MaterialTheme.typography.bodyMedium, color = c.ink)
            }
          }

          if (uiState.alerts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SehatPrimaryButton(
              text = "Acknowledge latest open alert",
              onClick = onAcknowledgeAlert,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }

      // Weekly Caregiver Digest Preview (Group 3C)
      item(key = "weekly_digest_preview") {
        val digest = CaregiverDomain.generateWeeklyDigest(
          patientId = uiState.patient.id,
          patientName = uiState.patient.name,
          readings = uiState.readings,
          doses = uiState.dosesToday,
          medicines = uiState.medicines,
          alerts = uiState.alerts,
          optedIn = uiState.weeklyDigestOptedIn,
        )

        SehatCard(containerColor = c.mistSoft) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Weekly Caregiver Digest (Demo)", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            DemoPillBadge(text = "Demo")
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(digest.generatedSummaryText, style = MaterialTheme.typography.bodyMedium, color = c.ink)
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Receive weekly summary via SMS/WhatsApp", style = MaterialTheme.typography.bodySmall, color = c.inkSecondary)
            Switch(
              checked = uiState.weeklyDigestOptedIn,
              onCheckedChange = onToggleWeeklyDigestOptIn,
              modifier = Modifier.testTag("digest_opt_in_switch"),
            )
          }
        }
      }
    } else {
      // PATIENT VIEW: Caregivers & Consent Hub
      if (uiState.escalationDemo.active) {
        item(key = "family_escalation") {
          Surface(
            shape = CardShape,
            color = c.surface,
            border = BorderStroke(1.5.dp, c.statusUrgentBorder),
            modifier =
              Modifier
                .fillMaxWidth()
                .sehatScroll3DKeyed(listState, "family_escalation", reducedMotion),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Escalation Timer (Demo)", style = MaterialTheme.typography.titleMedium, color = c.statusUrgentFg)
                DemoPillBadge(text = "Demo")
              }
              Text(
                text =
                  if (uiState.escalationDemo.escalated) {
                    "Escalated to ${uiState.escalationDemo.secondaryCaregiverName}."
                  } else {
                    "Simulating 5-min escalation to ${uiState.escalationDemo.secondaryCaregiverName} in ${uiState.escalationDemo.secondsRemaining}s..."
                  },
                style = MaterialTheme.typography.bodyMedium.tabular(),
                color = c.ink,
              )
              if (!uiState.escalationDemo.escalated) {
                SehatPrimaryButton(
                  text = "Acknowledge alert",
                  onClick = onAcknowledgeAlert,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
        }
      }

      items(uiState.caregivers, key = { it.id }) { caregiver ->
        val consented = !caregiver.consentStatus.contains("Pending", ignoreCase = true)
        SehatCard(
          containerColor = c.surface,
          modifier = Modifier.sehatScroll3DKeyed(listState, caregiver.id, reducedMotion),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(caregiver.name, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
              Text(caregiver.relationship, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
              Text(
                text = caregiver.phone,
                style = MaterialTheme.typography.labelLarge.tabular().copy(textDecoration = TextDecoration.Underline),
                color = c.clinicalBlueAccent,
                modifier =
                  Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(role = Role.Button) { launchSafeDialer(context, caregiver.phone) }
                    .padding(vertical = 8.dp),
              )
            }
            StatusChip(
              level = if (consented) HealthStatusLevel.NORMAL else HealthStatusLevel.CAUTION,
              label = caregiver.consentStatus,
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (consented) {
              SehatPrimaryButton(
                text = "WhatsApp alert (Demo)",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                  launchWhatsAppAlert(
                    context,
                    caregiver.phone,
                    "Alert: ${uiState.patient.name}'s blood sugar is 62 mg/dL (low). Please call her now.",
                  )
                },
                testTag = "family_whatsapp_${caregiver.id}",
              )
            } else {
              SehatPrimaryButton(
                text = "Accept invite (Demo)",
                icon = Icons.Filled.Check,
                onClick = { onAcceptCaregiverDemo(caregiver.id) },
              )
            }
            SehatSecondaryButton(
              text = "Call",
              icon = Icons.Filled.Call,
              minHeight = 56.dp,
              onClick = { launchSafeDialer(context, caregiver.phone) },
              testTag = "family_call_${caregiver.id}",
            )
            SehatGhostButton(
              text = "Revoke",
              onClick = { onRevokeCaregiver(caregiver.id) },
            )
          }
        }
      }

      item(key = "family_invite_card") {
        SehatCard(
          containerColor = c.surface,
          modifier = Modifier.sehatScroll3DKeyed(listState, "family_invite_card", reducedMotion),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = c.clinicalBlueAccent)
            Text("Invite another family contact", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
          }
          Spacer(modifier = Modifier.height(8.dp))
          SehatTextField(
            label = "Name",
            value = inviteName,
            onValueChange = { inviteName = it; inviteError = null },
            placeholder = "e.g. Meera Sharma",
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SehatTextField(
              label = "Relationship",
              value = inviteRelation,
              onValueChange = { inviteRelation = it },
              modifier = Modifier.weight(0.45f),
            )
            SehatTextField(
              label = "Phone",
              value = invitePhone,
              onValueChange = { invitePhone = it; inviteError = null },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              placeholder = "+91 98...",
              modifier = Modifier.weight(0.55f),
            )
          }
          inviteError?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(it, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg)
          }
          Spacer(modifier = Modifier.height(10.dp))
          SehatSecondaryButton(
            text = "Create consent invite (Demo)",
            onClick = {
              val err = onAddCaregiverInvite(inviteName, inviteRelation, invitePhone)
              if (err != null) inviteError = err
              else {
                inviteName = ""
                invitePhone = ""
              }
            },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      item(key = "family_timer_test") {
        SehatCard(
          containerColor = c.mistSoft,
          modifier = Modifier.sehatScroll3DKeyed(listState, "family_timer_test", reducedMotion),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = c.statusUrgentFg)
            Text("Test 5-minute escalation timer", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            DemoPillBadge(text = "Demo")
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "Simulates sending an urgent alert to ${uiState.caregivers.firstOrNull()?.name ?: "Primary contact"} and escalating after 5 minutes (20 seconds in Demo).",
            style = MaterialTheme.typography.bodyMedium,
            color = c.inkSecondary,
          )
          Spacer(modifier = Modifier.height(10.dp))
          SehatSecondaryButton(
            text = "Start Demo Escalation Timer",
            onClick = onTriggerDemoEscalation,
            modifier = Modifier.fillMaxWidth(),
            testTag = "test_escalation_btn",
          )
        }
      }

      item(key = "family_alerts_hdr") {
        Text("Recent alerts", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }

      items(uiState.alerts, key = { it.id }) { alert ->
        SehatCard(
          containerColor = c.surface,
          modifier = Modifier.sehatScroll3DKeyed(listState, alert.id, reducedMotion),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            StatusChip(level = alert.level)
            Text(alert.timestampLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(alert.title, style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = c.ink)
        }
      }
    }

    item {
      SehatSecondaryButton(text = "Back to Dashboard", onClick = onBackHome, modifier = Modifier.fillMaxWidth())
    }
  }
}

package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.HealthStatusLevel
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.StatusChip
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.components.sehatScroll3DKeyed
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular

data class LinkedPatientSummary(
  val id: String,
  val name: String,
  val age: Int,
  val conditions: List<String>,
  val statusLevel: HealthStatusLevel,
  val statusLabel: String,
  val lastReadingText: String,
  val dosesText: String,
  val refillText: String,
  val openAlertsCount: Int,
)

@Composable
fun CaregiverDashboardScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onAcknowledgeAlert: () -> Unit,
  onBackHome: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val listState = rememberLazyListState()

  val linkedPatients = remember(uiState) {
    listOf(
      LinkedPatientSummary(
        id = uiState.patient.id,
        name = uiState.patient.name,
        age = uiState.patient.age,
        conditions = uiState.patient.conditions,
        statusLevel = if ((uiState.readings.lastOrNull()?.glucoseMgDl ?: 130) < 70) HealthStatusLevel.URGENT else HealthStatusLevel.NORMAL,
        statusLabel = if ((uiState.readings.lastOrNull()?.glucoseMgDl ?: 130) < 70) "Urgent" else "Normal",
        lastReadingText = uiState.readings.lastOrNull()?.let { "${it.glucoseMgDl} mg/dL (${it.contextLabel})" } ?: "No reading today",
        dosesText = "${uiState.dosesToday.count { it.taken }} / ${uiState.dosesToday.size} doses taken",
        refillText = "All stocks adequate",
        openAlertsCount = uiState.alerts.size,
      ),
      LinkedPatientSummary(
        id = "patient-rajesh",
        name = "Rajesh Sharma",
        age = 71,
        conditions = listOf("Hypertension"),
        statusLevel = HealthStatusLevel.CAUTION,
        statusLabel = "Caution",
        lastReadingText = "158/96 mmHg · 4h ago",
        dosesText = "2 / 2 doses taken",
        refillText = "Refill due in 3 days",
        openAlertsCount = 1,
      ),
    )
  }

  LazyColumn(
    state = listState,
    contentPadding =
      PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp,
        bottom = contentPadding.calculateBottomPadding() + 16.dp,
      ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier.fillMaxSize().testTag("caregiver_dashboard_screen"),
  ) {
    item(key = "cg_header") {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Filled.SupervisorAccount, contentDescription = null, tint = c.deepOcean)
        Text("Caregiver Dashboard", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        DemoPillBadge(text = "Demo")
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        "Monitor linked patients, view vital summaries, and acknowledge open safety alerts.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.inkSecondary,
      )
    }

    items(linkedPatients, key = { it.id }) { patient ->
      SehatCard(
        containerColor = c.surface,
        useGlass = true,
        highReadabilityGlass = true,
        modifier = Modifier.sehatScroll3DKeyed(listState, patient.id, reducedMotion),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(patient.name, style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
            Text(
              "Age ${patient.age} · ${patient.conditions.joinToString(", ")}",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
          }
          StatusChip(level = patient.statusLevel, label = patient.statusLabel)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Last Reading", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            Text(patient.lastReadingText, style = MaterialTheme.typography.bodyMedium.tabular(), color = c.ink)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("Today's Doses", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            Text(patient.dosesText, style = MaterialTheme.typography.bodyMedium.tabular(), color = c.ink)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Refill Status", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            Text(patient.refillText, style = MaterialTheme.typography.bodyMedium, color = c.ink)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("Open Alerts", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            Text("${patient.openAlertsCount} open alert(s)", style = MaterialTheme.typography.bodyMedium, color = c.ink)
          }
        }

        if (patient.openAlertsCount > 0) {
          Spacer(modifier = Modifier.height(14.dp))
          SehatPrimaryButton(
            text = "Acknowledge alert (${patient.name})",
            onClick = onAcknowledgeAlert,
            modifier = Modifier.fillMaxWidth(),
            testTag = "ack_alert_${patient.id}",
          )
        }
      }
    }

    item {
      SehatPrimaryButton(
        text = "Back to Dashboard",
        onClick = onBackHome,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.domain.demo.DemoScenario
import com.example.domain.demo.GuidedTourCatalog
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DemoScenarioPanel(
  activeScenario: DemoScenario?,
  activeCaption: String?,
  onRunScenario: (DemoScenario) -> Unit,
  onResetDemo: () -> Unit,
  modifier: Modifier = Modifier,
  showResetAndTourControls: Boolean = true,
  onReplayTour: (() -> Unit)? = null,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = PureWhite,
    borderColor = c.clinicalBlueAccent.copy(alpha = 0.45f),
    useGlass = true,
    highReadabilityGlass = true,
    modifier = modifier.fillMaxWidth().testTag("demo_scenario_panel"),
  ) {
    FlowRow(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = c.clinicalBlueAccent)
        Text("Try a demo scenario", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      DemoPillBadge(text = "Demo")
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text =
        "Run a complete sample flow using fictional data for Sunita Devi. Scenarios never send real messages or touch real user accounts.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )

    if (!activeCaption.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        shape = ButtonShape,
        color = c.mist,
        border = BorderStroke(1.dp, c.clinicalBlueAccent.copy(alpha = 0.4f)),
        modifier =
          Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag("demo_scenario_active_caption"),
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Active demo: ${activeScenario?.title ?: "Sample scenario"}",
            style = MaterialTheme.typography.labelLarge,
            color = c.deepOcean,
          )
          Text(text = activeCaption, style = MaterialTheme.typography.bodyMedium, color = c.ink)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      DemoScenario.entries.forEach { scenario ->
        val isActive = activeScenario == scenario
        val icon =
          when (scenario) {
            DemoScenario.LOW_SUGAR_2AM -> Icons.Filled.Warning
            DemoScenario.HIGH_BLOOD_PRESSURE -> Icons.Filled.MonitorHeart
            DemoScenario.SCAN_SAMPLE_RX -> Icons.Filled.DocumentScanner
            DemoScenario.MEDICINE_RUNNING_OUT -> Icons.Filled.Inventory2
            DemoScenario.MISSED_DOSE -> Icons.Filled.NotificationsOff
          }
        val iconTint =
          when (scenario) {
            DemoScenario.LOW_SUGAR_2AM,
            DemoScenario.HIGH_BLOOD_PRESSURE -> c.statusUrgentFg
            else -> c.clinicalBlueAccent
          }

        Surface(
          onClick = { onRunScenario(scenario) },
          shape = ButtonShape,
          color = if (isActive) c.mist else c.mistSoft,
          border =
            BorderStroke(
              width = if (isActive) 1.5.dp else 1.dp,
              color = if (isActive) c.clinicalBlueAccent else c.hairlineBorder,
            ),
          modifier =
            Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 56.dp)
              .testTag("demo_scenario_${scenario.id}"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = scenario.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = c.deepOcean,
                )
                DemoPillBadge(text = "Demo")
              }
              Text(
                text = scenario.caption,
                style = MaterialTheme.typography.labelMedium,
                color = c.inkSecondary,
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Run ${scenario.title}",
              tint = c.clinicalBlueAccent,
            )
          }
        }
      }
    }

    if (showResetAndTourControls) {
      Spacer(modifier = Modifier.height(14.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        SehatSecondaryButton(
          text = "Reset demo",
          icon = Icons.Filled.Refresh,
          onClick = onResetDemo,
          minHeight = 48.dp,
          testTag = "demo_scenario_reset_btn",
        )
        if (onReplayTour != null) {
          SehatGhostButton(
            text = "Guided tour",
            icon = Icons.Filled.Explore,
            onClick = onReplayTour,
            testTag = "demo_replay_tour_btn",
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuidedTourOverlay(
  visible: Boolean,
  onNavigateArea: (String) -> Unit,
  onDismissTour: () -> Unit,
) {
  if (!visible) return
  val c = SehatPalette.current
  val steps = GuidedTourCatalog.steps
  var stepIdx by remember { mutableIntStateOf(0) }
  val current = steps[stepIdx.coerceIn(0, steps.lastIndex)]
  val reducedTransparency = rememberPrefersReducedTransparency()

  Dialog(onDismissRequest = onDismissTour) {
    Surface(
      shape = GlassTokens.GlassCornerShape,
      color = GlassTokens.cardFill(PureWhite, highReadability = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
      border = GlassTokens.borderStroke(c.clinicalBlueAccent.copy(alpha = 0.55f), isDark = c.isDark),
      shadowElevation = 14.dp,
      modifier =
        Modifier
          .fillMaxWidth()
          .widthIn(max = 460.dp)
          .semantics {
            paneTitle = "Guided tour"
            liveRegion = LiveRegionMode.Polite
          }
          .testTag("guided_tour_dialog"),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = c.clinicalBlueAccent)
            Text(
              text = "Step ${current.stepNumber} of ${steps.size}",
              style = MaterialTheme.typography.labelLarge,
              color = c.clinicalBlueAccent,
            )
            DemoPillBadge(text = "Tour")
          }
          IconButton(
            onClick = onDismissTour,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("guided_tour_close_btn"),
          ) {
            Icon(Icons.Filled.Close, contentDescription = "Skip guided tour", tint = c.deepOcean)
          }
        }

        SehatProgressMeter(
          progress = current.stepNumber.toFloat() / steps.size.toFloat(),
          height = 6.dp,
        )

        Text(
          text = current.title,
          style = MaterialTheme.typography.headlineMedium,
          color = c.deepOcean,
        )
        Text(
          text = current.body,
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )

        SehatGhostButton(
          text = "Go to ${current.areaLabel}",
          icon = Icons.AutoMirrored.Filled.ArrowForward,
          onClick = {
            onDismissTour()
            onNavigateArea(current.targetRoute)
          },
          testTag = "guided_tour_jump_btn",
        )

        Spacer(modifier = Modifier.height(2.dp))
        FlowRow(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          SehatGhostButton(
            text = if (stepIdx > 0) "Back" else "Skip tour",
            onClick = {
              if (stepIdx > 0) stepIdx-- else onDismissTour()
            },
            testTag = "guided_tour_back_or_skip_btn",
          )
          SehatPrimaryButton(
            text = if (stepIdx < steps.lastIndex) "Next step" else "Done",
            minHeight = 48.dp,
            onClick = {
              if (stepIdx < steps.lastIndex) {
                stepIdx++
              } else {
                onDismissTour()
              }
            },
            testTag = "guided_tour_next_btn",
          )
        }
      }
    }
  }
}

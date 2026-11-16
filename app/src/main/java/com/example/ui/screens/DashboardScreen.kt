package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.HealthStatusLevel
import com.example.data.RefillStatus
import com.example.data.SeededAlert
import com.example.data.SeededDoseEvent
import com.example.data.SeededMedicine
import com.example.data.SeededReading
import com.example.domain.demo.DemoScenario
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.AppRoutes
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.DemoScenarioPanel
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatProgressMeter
import com.example.ui.components.SehatRingProgress
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatSparkline
import com.example.ui.components.StatusChip
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.components.sehatScroll3DKeyed
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.ClinicalBlue
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.DeepOceanDark
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.Mist
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency
import com.example.ui.theme.tabular
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onToggleDoseTaken: (String) -> Unit,
  onNavigate: (String) -> Unit,
  onRunDemoScenario: (DemoScenario) -> Unit = {},
  onResetDemo: () -> Unit = {},
  onReplayTour: () -> Unit = {},
  onSetSimpleMode: (Boolean) -> Unit = {},
  onSwitchToDemoAccount: () -> Unit = {},
  onSwitchToPersonalAccount: () -> Unit = {},
  onLogoutToCreateAccount: () -> Unit = {},
  onLogoutToLogin: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val reducedTransparency = rememberPrefersReducedTransparency()
  var visibleTiles by remember { mutableIntStateOf(if (reducedMotion) 6 else 0) }

  LaunchedEffect(reducedMotion) {
    if (reducedMotion) {
      visibleTiles = 6
    } else {
      for (i in 1..6) {
        visibleTiles = i
        delay(60)
      }
    }
  }

  val takenCount = uiState.dosesToday.count { it.taken }
  val totalDoses = uiState.dosesToday.size
  val latestReading = uiState.readings.lastOrNull()
  val listState = rememberLazyListState()

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val twoColumns = maxWidth >= 680.dp

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
      modifier = Modifier.fillMaxSize().testTag(if (uiState.simpleModeActive) "simple_mode_dashboard" else "dashboard_list"),
    ) {
      if (uiState.simpleModeActive) {
        // SIMPLE MODE: Voice-first, large text, 4 big action cards
        item(key = "simple_banner") {
          Surface(
            shape = CardShape,
            color = c.deepOcean,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Simple Mode (Voice-First)", style = MaterialTheme.typography.headlineMedium, color = PureWhite)
                DemoPillBadge(text = "Active")
              }
              Text(
                "High contrast, large text layout for elderly users. Spoken confirmation enabled.",
                style = MaterialTheme.typography.bodyLarge,
                color = Mist,
              )
              Spacer(modifier = Modifier.height(4.dp))
              SehatSecondaryButton(
                text = "Switch to full view",
                onClick = { onSetSimpleMode(false) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "switch_to_full_view_btn",
              )
            }
          }
        }

        item(key = "simple_status_summary") {
          SehatCard(containerColor = c.surface) {
            Text("Quick Health Status", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = latestReading?.let { "Latest: ${it.glucoseMgDl} mg/dL (${it.contextLabel})" } ?: "No reading today",
              style = MaterialTheme.typography.bodyLarge.tabular(),
              color = c.ink,
            )
            Text(
              text = "Today's Doses: $takenCount / $totalDoses taken",
              style = MaterialTheme.typography.bodyLarge.tabular(),
              color = c.ink,
            )
          }
        }

        item(key = "simple_actions_hdr") {
          Text("Primary Actions", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        }

        item(key = "simple_action_log") {
          SehatPrimaryButton(
            text = "1. Log reading",
            icon = Icons.Filled.Add,
            onClick = { onNavigate(AppRoutes.LOG_READING) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            testTag = "simple_action_log",
          )
        }

        item(key = "simple_action_meds") {
          SehatPrimaryButton(
            text = "2. My medicines",
            icon = Icons.Filled.Medication,
            onClick = { onNavigate(AppRoutes.MEDICINES) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            testTag = "simple_action_meds",
          )
        }

        item(key = "simple_action_scan") {
          SehatPrimaryButton(
            text = "3. Scan prescription",
            icon = Icons.Filled.DocumentScanner,
            onClick = { onNavigate(AppRoutes.SCAN_RX) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            testTag = "simple_action_scan",
          )
        }

        item(key = "simple_action_call") {
          SehatPrimaryButton(
            text = "4. Call family",
            icon = Icons.Filled.Groups,
            onClick = { onNavigate(AppRoutes.FAMILY) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            testTag = "simple_action_call",
          )
        }

        item(key = "simple_action_ai") {
          SehatPrimaryButton(
            text = "5. Care AI (Do's & Chat)",
            icon = Icons.Filled.AutoAwesome,
            onClick = { onNavigate(AppRoutes.ASSISTANT) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            testTag = "simple_action_ai",
          )
        }
      } else {
        // Sculpted Deep Ocean Hero Greeting Banner + Sample Data Notice
        item(key = "dash_hero") {
          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.sehatScroll3DKeyed(listState, "dash_hero", reducedMotion, 5f),
          ) {
            Surface(
              shape = CardShape,
              color = DeepOcean,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .shadow(
                    elevation = 10.dp,
                    shape = CardShape,
                    ambientColor = DeepOceanDark.copy(alpha = 0.35f),
                    spotColor = ClinicalBlue.copy(alpha = 0.45f),
                  ),
            ) {
              Box(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .background(
                      Brush.linearGradient(
                        colors = listOf(DeepOceanDark, DeepOcean, ClinicalBlue),
                      ),
                    ),
              ) {
                Column(
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.fillMaxWidth().padding(20.dp),
                ) {
                  Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    val greetingText = remember(uiState.patient.name) {
                      val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                      val prefix =
                        when (hour) {
                          in 5..11 -> "Good morning"
                          in 12..16 -> "Good afternoon"
                          in 17..21 -> "Good evening"
                          else -> "Hello"
                        }
                      val cleanName = uiState.patient.name.trim()
                      if (cleanName.isNotEmpty()) "$prefix, $cleanName" else prefix
                    }
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                      Text(
                        text = greetingText,
                        style = MaterialTheme.typography.displayMedium,
                        color = PureWhite,
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text =
                          "Age ${uiState.patient.age} · ${uiState.patient.conditions.joinToString(" · ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mist,
                      )
                    }
                    if (uiState.session.isDemoMode) {
                      DemoPillBadge(text = "Demo mode")
                    }
                  }

                  if (latestReading != null) {
                    Surface(
                      shape = PillShape,
                      color = PureWhite.copy(alpha = 0.14f),
                      modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    ) {
                      Text(
                        text = "Sugar ${latestReading.glucoseMgDl} · BP ${latestReading.systolic}/${latestReading.diastolic}",
                        style = MaterialTheme.typography.labelMedium.tabular(),
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                      )
                    }
                  }
                }
              }
            }

            Surface(
              shape = GlassTokens.GlassCornerShape,
              color =
                GlassTokens.cardFill(
                  c.mist,
                  highReadability = false,
                  reducedTransparency = reducedTransparency,
                  isDark = c.isDark,
                ),
              border = GlassTokens.borderStroke(c.hairlineBorder, isDark = c.isDark),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              ) {
                Text(
                  text =
                    if (uiState.session.isDemoMode) {
                      "Sample data for demonstration only (Sunita Devi). You can log out to create your own personal account or switch back to Demo Mode anytime."
                    } else {
                      "Logged in as ${uiState.patient.name} (Personal Account). Verify medicine details with your doctor or pharmacist."
                    },
                  style = MaterialTheme.typography.labelMedium,
                  color = c.deepOcean,
                )
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  if (uiState.session.isDemoMode) {
                    if (uiState.session.hasSavedPersonalAccount) {
                      SehatPrimaryButton(
                        text = "Switch to ${uiState.session.savedPersonalName}",
                        icon = Icons.Filled.Person,
                        onClick = onSwitchToPersonalAccount,
                        testTag = "dash_switch_personal_btn",
                      )
                    }
                    SehatPrimaryButton(
                      text = "Create My Account",
                      icon = Icons.Filled.Add,
                      onClick = onLogoutToCreateAccount,
                      testTag = "dash_create_account_btn",
                    )
                    SehatSecondaryButton(
                      text = "Log Out / Sign In",
                      icon = Icons.AutoMirrored.Filled.Logout,
                      minHeight = 48.dp,
                      onClick = onLogoutToLogin,
                      testTag = "dash_logout_btn",
                    )
                  } else {
                    SehatSecondaryButton(
                      text = "Switch to Demo (Sunita Devi)",
                      icon = Icons.Filled.Refresh,
                      minHeight = 48.dp,
                      onClick = onSwitchToDemoAccount,
                      testTag = "dash_switch_demo_btn",
                    )
                    SehatSecondaryButton(
                      text = "Log Out",
                      icon = Icons.AutoMirrored.Filled.Logout,
                      minHeight = 48.dp,
                      onClick = onLogoutToLogin,
                      testTag = "dash_logout_btn",
                    )
                  }
                }
              }
            }
          }
        }

        // Quick Actions Tile
        item(key = "dash_quick_actions") {
          StaggeredTile(
            visible = visibleTiles >= 1,
            reducedMotion = reducedMotion,
            modifier = Modifier.sehatScroll3DKeyed(listState, "dash_quick_actions", reducedMotion),
          ) {
            QuickActionsTile(
              onLogReading = { onNavigate(AppRoutes.LOG_READING) },
              onScanRx = { onNavigate(AppRoutes.SCAN_RX) },
              onOpenCareAi = { onNavigate(AppRoutes.ASSISTANT) },
            )
          }
        }

        if (twoColumns) {
          item(key = "dash_twocol_1") {
            Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Box(modifier = Modifier.weight(1f)) {
                StaggeredTile(visible = visibleTiles >= 2, reducedMotion = reducedMotion) {
                  TodayDosesTile(
                    taken = takenCount,
                    total = totalDoses,
                    doses = uiState.dosesToday,
                    onToggleDose = onToggleDoseTaken,
                  )
                }
              }
              Box(modifier = Modifier.weight(1f)) {
                StaggeredTile(visible = visibleTiles >= 3, reducedMotion = reducedMotion) {
                  VitalsTile(
                    latestReading = latestReading,
                    readings = uiState.readings,
                    onViewTrends = { onNavigate(AppRoutes.TRENDS) },
                  )
                }
              }
            }
          }

          item(key = "dash_twocol_2") {
            Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Box(modifier = Modifier.weight(1f)) {
                StaggeredTile(visible = visibleTiles >= 4, reducedMotion = reducedMotion) {
                  RefillsTile(
                    medicines = uiState.medicines,
                    onOpenMedicines = { onNavigate(AppRoutes.MEDICINES) },
                  )
                }
              }
              Box(modifier = Modifier.weight(1f)) {
                StaggeredTile(visible = visibleTiles >= 5, reducedMotion = reducedMotion) {
                  AlertsTile(
                    alerts = uiState.alerts,
                    onOpenFamily = { onNavigate(AppRoutes.FAMILY) },
                  )
                }
              }
            }
          }
        } else {
          item(key = "dash_doses") {
            StaggeredTile(visible = visibleTiles >= 2, reducedMotion = reducedMotion) {
              TodayDosesTile(
                taken = takenCount,
                total = totalDoses,
                doses = uiState.dosesToday,
                onToggleDose = onToggleDoseTaken,
              )
            }
          }

          item(key = "dash_vitals") {
            StaggeredTile(visible = visibleTiles >= 3, reducedMotion = reducedMotion) {
              VitalsTile(
                latestReading = latestReading,
                readings = uiState.readings,
                onViewTrends = { onNavigate(AppRoutes.TRENDS) },
              )
            }
          }

          item(key = "dash_refills") {
            StaggeredTile(visible = visibleTiles >= 4, reducedMotion = reducedMotion) {
              RefillsTile(
                medicines = uiState.medicines,
                onOpenMedicines = { onNavigate(AppRoutes.MEDICINES) },
              )
            }
          }

          item(key = "dash_alerts") {
            StaggeredTile(visible = visibleTiles >= 5, reducedMotion = reducedMotion) {
              AlertsTile(
                alerts = uiState.alerts,
                onOpenFamily = { onNavigate(AppRoutes.FAMILY) },
              )
            }
          }
        }

        item(key = "dash_timeline") {
          StaggeredTile(
            visible = visibleTiles >= 6,
            reducedMotion = reducedMotion,
            modifier = Modifier.sehatScroll3DKeyed(listState, "dash_timeline", reducedMotion),
          ) {
            MedicineTimelineTile(
              medicines = uiState.medicines,
              onOpenMedicines = { onNavigate(AppRoutes.MEDICINES) },
            )
          }
        }

        if (uiState.session.isDemoMode) {
          item(key = "dash_demo_scenarios") {
            StaggeredTile(
              visible = visibleTiles >= 6,
              reducedMotion = reducedMotion,
              modifier = Modifier.sehatScroll3DKeyed(listState, "dash_demo_scenarios", reducedMotion),
            ) {
              DemoScenarioPanel(
                activeScenario = uiState.activeDemoScenario,
                activeCaption = uiState.activeDemoScenarioCaption,
                onRunScenario = onRunDemoScenario,
                onResetDemo = onResetDemo,
                onReplayTour = onReplayTour,
                showResetAndTourControls = true,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StaggeredTile(
  visible: Boolean,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  if (reducedMotion) {
    Box(modifier = modifier) { content() }
  } else {
    AnimatedVisibility(
      visible = visible,
      enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 12 },
      modifier = modifier,
    ) {
      content()
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionsTile(
  onLogReading: () -> Unit,
  onScanRx: () -> Unit,
  onOpenCareAi: () -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    useGlass = true,
    highReadabilityGlass = false,
  ) {
    Text("Quick actions", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Log a sugar or blood pressure check, scan a prescription, or ask Care AI for Do's, Don'ts & chat.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(14.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      SehatPrimaryButton(
        text = "Log reading",
        icon = Icons.Filled.Add,
        onClick = onLogReading,
        testTag = "quick_log_reading_btn",
      )
      SehatSecondaryButton(
        text = "Scan prescription",
        icon = Icons.Filled.DocumentScanner,
        onClick = onScanRx,
        minHeight = 56.dp,
        testTag = "quick_scan_rx_btn",
      )
      SehatSecondaryButton(
        text = "Ask Care AI",
        icon = Icons.Filled.AutoAwesome,
        onClick = onOpenCareAi,
        minHeight = 56.dp,
        testTag = "quick_care_ai_btn",
      )
    }
  }
}

@Composable
private fun TodayDosesTile(
  taken: Int,
  total: Int,
  doses: List<SeededDoseEvent>,
  onToggleDose: (String) -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    useGlass = true,
    highReadabilityGlass = true,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
        Text("Today's medicines", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (taken == total) "All doses marked taken today." else "Tap a dose below when taken.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
      }
      SehatRingProgress(completed = taken, total = total, size = 92.dp, strokeWidth = 10.dp)
    }
    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(8.dp))
    doses.forEach { dose ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(dose.medicineLabel, style = MaterialTheme.typography.labelLarge, color = c.ink)
          Text(
            "${dose.slot.title} (${dose.slot.timeWindow}) · ${dose.instruction}",
            style = MaterialTheme.typography.labelMedium,
            color = c.inkSecondary,
          )
        }
        Surface(
          onClick = { onToggleDose(dose.id) },
          shape = ButtonShape,
          color = if (dose.taken) c.statusNormalBg else c.mistSoft,
          border = BorderStroke(1.dp, if (dose.taken) c.statusNormalBorder else c.hairlineBorder),
          modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("dose_toggle_${dose.id}"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          ) {
            if (dose.taken) {
              Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = c.statusNormalFg,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = if (dose.taken) "Taken" else "Mark as taken",
              style = MaterialTheme.typography.labelMedium,
              color = if (dose.taken) c.statusNormalFg else c.deepOcean,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun VitalsTile(
  latestReading: SeededReading?,
  readings: List<SeededReading>,
  onViewTrends: () -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    useGlass = true,
    highReadabilityGlass = true,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.MonitorHeart, contentDescription = null, tint = c.clinicalBlueAccent)
        Text("Latest vitals", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      StatusChip(
        level = latestReading?.status ?: HealthStatusLevel.INFO,
        label = latestReading?.status?.label ?: "No data",
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
    if (latestReading != null) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Blood sugar", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${latestReading.glucoseMgDl}",
              style = MaterialTheme.typography.headlineLarge.tabular(),
              color = c.deepOcean,
              softWrap = false,
              modifier = Modifier.alignByBaseline(),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "mg/dL",
              style = MaterialTheme.typography.labelLarge,
              color = c.inkSecondary,
              softWrap = false,
              modifier = Modifier.alignByBaseline(),
            )
          }
          Text(latestReading.contextLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
        }
        Column(modifier = Modifier.weight(1f)) {
          Text("Blood pressure", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${latestReading.systolic}/${latestReading.diastolic}",
              style = MaterialTheme.typography.headlineLarge.tabular(),
              color = c.deepOcean,
              softWrap = false,
              modifier = Modifier.alignByBaseline(),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "mmHg",
              style = MaterialTheme.typography.labelLarge,
              color = c.inkSecondary,
              softWrap = false,
              modifier = Modifier.alignByBaseline(),
            )
          }
          Text("Latest reading", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    SehatSparkline(values = readings.map { it.glucoseMgDl.toFloat() })
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("14-day sugar trend (70–180 band)", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
      SehatGhostButton(text = "See trends", onClick = onViewTrends, testTag = "dashboard_trends_btn")
    }
  }
}

@Composable
private fun RefillsTile(
  medicines: List<SeededMedicine>,
  onOpenMedicines: () -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    useGlass = true,
    highReadabilityGlass = true,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Medication, contentDescription = null, tint = c.clinicalBlueAccent)
        Text("Refill watch", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      DemoPillBadge(text = "Demo")
    }
    Spacer(modifier = Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      medicines.sortedBy { it.daysLeft }.take(3).forEach { med ->
        val progressColor =
          when (med.refillStatus) {
            RefillStatus.OUT, RefillStatus.DUE -> c.statusUrgentFg
            RefillStatus.SOON, RefillStatus.UNKNOWN -> c.statusCautionFg
            RefillStatus.OK -> c.clinicalBlueAccent
          }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
              Text(med.brandName, style = MaterialTheme.typography.labelLarge, color = c.ink)
              Text(
                text =
                  med.daysLeftOrNull?.let { "${med.stockCount} ${med.unit} left · $it days" }
                    ?: "Stock unknown. Ask your pharmacist",
                style = MaterialTheme.typography.labelMedium.tabular(),
                color = c.inkSecondary,
              )
            }
            StatusChip(level = med.refillStatus.badgeLevel, label = med.refillStatus.label)
          }
          SehatProgressMeter(
            progress = (med.daysLeft / 15f).coerceIn(0.05f, 1f),
            color = progressColor,
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    SehatGhostButton(text = "Manage stock & medicines", onClick = onOpenMedicines)
  }
}

@Composable
private fun AlertsTile(
  alerts: List<SeededAlert>,
  onOpenFamily: () -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    useGlass = true,
    highReadabilityGlass = true,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = c.statusUrgentFg)
        Text("Family alerts", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      DemoPillBadge(text = "Demo")
    }
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      alerts.forEach { alert ->
        Surface(
          shape = CardShape,
          color = c.mistSoft,
          border = BorderStroke(1.dp, c.hairlineBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              StatusChip(level = alert.level)
              Text(alert.timestampLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            }
            Text(alert.title, style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = c.ink)
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    SehatGhostButton(text = "View family contacts", onClick = onOpenFamily)
  }
}

@Composable
private fun MedicineTimelineTile(
  medicines: List<SeededMedicine>,
  onOpenMedicines: () -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    useGlass = true,
    highReadabilityGlass = true,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = c.clinicalBlueAccent)
        Text("Daily medicine timeline", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      SehatGhostButton(text = "All (${medicines.size})", onClick = onOpenMedicines)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      medicines.forEach { med ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${med.slot.title} (${med.slot.timeWindow}) · ${med.brandName}",
              style = MaterialTheme.typography.labelLarge,
              color = c.deepOcean,
            )
            Text(
              text = "${med.genericName} ${med.strength} · ${med.foodTiming}",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
          }
          DemoPillBadge(text = med.frequency.substringBefore(" "))
        }
        HorizontalDivider(color = c.hairlineBorder)
      }
    }
  }
}

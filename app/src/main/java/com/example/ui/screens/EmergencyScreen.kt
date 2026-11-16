package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SeededCaregiver
import com.example.domain.risk.RiskAssessment
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.launchGoogleMapsSearch
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusUrgentFg

private val EmergencyBg = Color(0xFF991515)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmergencyScreen(
  assessment: RiskAssessment,
  caregivers: List<SeededCaregiver>,
  onCloseEmergency: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val reducedMotion = rememberPrefersReducedMotion()
  val primaryCaregiver = caregivers.firstOrNull()

  // One calm, gentle pulse on the primary Call 112 button (disabled under reduced motion)
  val infiniteTransition = rememberInfiniteTransition(label = "emergency_pulse")
  val callPulse by
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = if (reducedMotion) 1f else 1.03f,
      animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
      label = "call_pulse",
    )

  Surface(
    color = EmergencyBg,
    contentColor = PureWhite,
    modifier = modifier.fillMaxSize().testTag("emergency_screen"),
  ) {
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
    ) {
      // Top Critical Action Area: ALWAYS visible without scrolling at every screen size
      Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(Icons.Filled.Warning, contentDescription = null, tint = PureWhite, modifier = Modifier.size(32.dp))
          Text(
            text = "EMERGENCY HELP",
            style = MaterialTheme.typography.displayMedium,
            color = PureWhite,
          )
        }

        Text(
          text = assessment.headline,
          style = MaterialTheme.typography.headlineMedium,
          color = PureWhite,
        )

        // Call 112, Call Nearest Hospital, and Call Family
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val stackCalls = maxWidth < 360.dp
          if (stackCalls) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              Button(
                onClick = { launchSafeDialer(context, "112") },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = StatusUrgentFg),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp).scale(callPulse).testTag("emergency_call_112_btn"),
              ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = StatusUrgentFg)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call 112", style = MaterialTheme.typography.titleMedium, color = StatusUrgentFg)
              }
              Button(
                onClick = { launchSafeDialer(context, "108") },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = StatusUrgentFg),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp).testTag("emergency_call_hospital_btn"),
              ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = StatusUrgentFg)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call Nearest Hospital", style = MaterialTheme.typography.titleMedium, color = StatusUrgentFg)
              }
              Button(
                onClick = { launchSafeDialer(context, primaryCaregiver?.phone ?: "112") },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite.copy(alpha = 0.18f), contentColor = PureWhite),
                border = BorderStroke(2.dp, PureWhite),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp).testTag("emergency_call_family_btn"),
              ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = primaryCaregiver?.name?.substringBefore(" ")?.let { "Call $it" } ?: "Call family",
                  style = MaterialTheme.typography.titleMedium,
                  color = PureWhite,
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                  onClick = { launchSafeDialer(context, "112") },
                  shape = ButtonShape,
                  colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = StatusUrgentFg),
                  contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                  modifier =
                    Modifier
                      .weight(1f)
                      .defaultMinSize(minHeight = 56.dp)
                      .scale(callPulse)
                      .testTag("emergency_call_112_btn"),
                ) {
                  Icon(Icons.Filled.Call, contentDescription = null, tint = StatusUrgentFg)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Call 112", style = MaterialTheme.typography.titleMedium, color = StatusUrgentFg)
                }

                Button(
                  onClick = { launchSafeDialer(context, "108") },
                  shape = ButtonShape,
                  colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = StatusUrgentFg),
                  contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                  modifier =
                    Modifier
                      .weight(1f)
                      .defaultMinSize(minHeight = 56.dp)
                      .testTag("emergency_call_hospital_btn"),
                ) {
                  Icon(Icons.Filled.Call, contentDescription = null, tint = StatusUrgentFg)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Call Hospital", style = MaterialTheme.typography.titleMedium, color = StatusUrgentFg)
                }
              }

              Button(
                onClick = { launchSafeDialer(context, primaryCaregiver?.phone ?: "112") },
                shape = ButtonShape,
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = PureWhite.copy(alpha = 0.18f),
                    contentColor = PureWhite,
                  ),
                border = BorderStroke(2.dp, PureWhite),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .testTag("emergency_call_family_btn"),
              ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = primaryCaregiver?.name?.substringBefore(" ")?.let { "Call $it (Family)" } ?: "Call Family",
                  style = MaterialTheme.typography.titleMedium,
                  color = PureWhite,
                )
              }
            }
          }
        }
      }

      // First-Aid Steps & Google Maps Nearby Hospitals Card
      Surface(
        shape = CardShape,
        color = PureWhite,
        contentColor = Ink,
        modifier = Modifier.weight(1f, fill = false).fillMaxWidth().padding(vertical = 8.dp),
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.verticalScroll(rememberScrollState()).padding(18.dp),
        ) {
          Text("What to do right now", style = MaterialTheme.typography.titleLarge, color = StatusUrgentFg)
          assessment.guidanceSteps.forEach { step ->
            Text("• $step", style = MaterialTheme.typography.bodyLarge, color = Ink)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Nearby Emergency Hospitals (Google Maps Data)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            "Tap to call an emergency medical facility or open Google Maps for directions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          // Hospital 1
          Surface(
            shape = CardShape,
            color = Mist,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(12.dp),
            ) {
              Text("City General Hospital Emergency", style = MaterialTheme.typography.titleSmall)
              Text("Open 24/7 • ~1.8 km away", style = MaterialTheme.typography.bodySmall)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { launchSafeDialer(context, "108") },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                  modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                ) {
                  Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Call Emergency")
                }
                OutlinedButton(
                  onClick = { launchGoogleMapsSearch(context, "City General Hospital emergency") },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepOcean),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                  modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                ) {
                  Text("View Maps", color = DeepOcean)
                }
              }
            }
          }

          // Hospital 2
          Surface(
            shape = CardShape,
            color = Mist,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(12.dp),
            ) {
              Text("Apollo Emergency Care", style = MaterialTheme.typography.titleSmall)
              Text("Open 24/7 • ~3.2 km away", style = MaterialTheme.typography.bodySmall)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { launchSafeDialer(context, "108") },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                  modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                ) {
                  Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Call Emergency")
                }
                OutlinedButton(
                  onClick = { launchGoogleMapsSearch(context, "Apollo Emergency Care hospital") },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepOcean),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                  modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                ) {
                  Text("View Maps", color = DeepOcean)
                }
              }
            }
          }

          OutlinedButton(
            onClick = { launchGoogleMapsSearch(context, "hospital emergency near me") },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepOcean),
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
          ) {
            Text("Open Google Maps (Find All Nearby Hospitals)", color = DeepOcean)
          }

          if (primaryCaregiver != null) {
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Button(
                onClick = {
                  launchWhatsAppAlert(context, primaryCaregiver.phone, assessment.whatsappSummarySnippet)
                },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = StatusUrgentFg, contentColor = PureWhite),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier.defaultMinSize(minHeight = 52.dp),
              ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "WhatsApp ${primaryCaregiver.name}",
                  style = MaterialTheme.typography.labelLarge,
                  color = PureWhite,
                )
              }
              DemoPillBadge(text = "Demo")
            }
          }
        }
      }

      // Bottom exit + safety reminder
      Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
          onClick = onCloseEmergency,
          shape = ButtonShape,
          border = BorderStroke(1.5.dp, PureWhite),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp).testTag("close_emergency_btn"),
        ) {
          Icon(Icons.Filled.Close, contentDescription = null, tint = PureWhite)
          Spacer(modifier = Modifier.width(8.dp))
          Text("I am safe. Return to SehatSaathi", color = PureWhite)
        }
        Text(
          text = "SehatSaathi is a support tool, not a doctor. In an emergency call 112.",
          style = MaterialTheme.typography.labelMedium,
          color = PureWhite.copy(alpha = 0.9f),
        )
      }
    }
  }
}

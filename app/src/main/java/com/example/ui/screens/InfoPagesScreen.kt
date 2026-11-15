package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.SiteConfig
import com.example.ui.components.SafetyDisclaimerFooter
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatIconButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.launchSafeEmail
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency

enum class InfoSection(val title: String) {
  SAFETY("Safety"),
  PRIVACY("Privacy"),
  TERMS("Terms"),
  ABOUT("About"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoPagesScreen(
  initialSection: InfoSection,
  isAuthenticated: Boolean,
  isDemoMode: Boolean,
  onBack: () -> Unit,
  onHomeClick: () -> Unit,
  onShowToast: (String) -> Unit,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val c = SehatPalette.current
  val reducedTransparency = rememberPrefersReducedTransparency()
  var activeSection by remember(initialSection) { mutableStateOf(initialSection) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(if (isAuthenticated) Color.Transparent else c.background)
        .testTag("info_pages_screen"),
  ) {
    if (!isAuthenticated) {
      Surface(
        color = GlassTokens.chromeFill(scrolled = false, reducedTransparency = reducedTransparency, isDark = c.isDark),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
          SehatIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go back",
            onClick = onBack,
            testTag = "info_back_btn",
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .clip(ButtonShape)
                .clickable(role = Role.Button, onClick = onHomeClick)
                .semantics { contentDescription = "SehatSaathi home" }
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(30.dp).clip(ButtonShape).background(DeepOcean),
            ) {
              Icon(Icons.Filled.Favorite, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("SehatSaathi", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          }
        }
      }
    }

    Box(
      contentAlignment = Alignment.TopCenter,
      modifier =
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp,
          ),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth().widthIn(max = 820.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector =
              when (activeSection) {
                InfoSection.SAFETY -> Icons.Filled.HealthAndSafety
                InfoSection.PRIVACY -> Icons.Filled.Lock
                InfoSection.TERMS -> Icons.Filled.Policy
                InfoSection.ABOUT -> Icons.Filled.Info
              },
            contentDescription = null,
            tint = c.deepOcean,
          )
          Text(
            text = "${activeSection.title} & Support",
            style = MaterialTheme.typography.headlineLarge,
            color = c.deepOcean,
          )
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          InfoSection.entries.forEach { section ->
            FilterChip(
              selected = activeSection == section,
              onClick = { activeSection = section },
              label = { Text(section.title) },
              colors =
                FilterChipDefaults.filterChipColors(
                  containerColor = c.surface,
                  labelColor = c.ink,
                  selectedContainerColor = c.clinicalBlue,
                  selectedLabelColor = PureWhite,
                ),
              border =
                FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = activeSection == section,
                  borderColor = c.hairlineBorder,
                ),
              modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("info_tab_${section.name.lowercase()}"),
            )
          }
        }

        when (activeSection) {
          InfoSection.SAFETY -> {
            Surface(
              shape = CardShape,
              color = c.statusUrgentBg,
              border = BorderStroke(1.dp, c.statusUrgentBorder),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("In an emergency, call 112 right away", style = MaterialTheme.typography.titleMedium, color = c.statusUrgentFg)
                Text(
                  "SehatSaathi is a daily support companion, not a doctor or diagnostic service. Never delay emergency care or stop prescribed medicines without speaking to your doctor.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = c.ink,
                )
                SehatSecondaryButton(
                  text = "Call emergency ${SiteConfig.EMERGENCY_NUMBER}",
                  icon = Icons.Filled.Call,
                  minHeight = 48.dp,
                  onClick = { launchSafeDialer(context, SiteConfig.EMERGENCY_NUMBER, onShowToast) },
                  testTag = "info_call_112_btn",
                )
              }
            }

            SehatCard(containerColor = c.surface) {
              Text("How SehatSaathi keeps checks safe", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              Spacer(modifier = Modifier.height(8.dp))
              InfoBullet("Clear rules decide danger levels, duplicate medicines, interactions, and refill alerts — never unverified AI guesses.")
              InfoBullet("Every medicine name, dose, reading, and tablet count is shown to you to confirm before saving.")
              InfoBullet("If an item is missing from our reference list or unclear in a photo, we ask you to check with your doctor or pharmacist.")
              InfoBullet("Simulated features such as Demo WhatsApp alerts and Demo timers are clearly marked with a Demo badge.")
            }
          }

          InfoSection.PRIVACY -> {
            SehatCard(containerColor = c.surface) {
              Text("How your health information is handled", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              Spacer(modifier = Modifier.height(8.dp))
              InfoBullet("Your profile, medicine schedule, readings, and Demo session are stored locally on your device.")
              InfoBullet("Photos or spoken phrases are sent for AI reading only when you tap to scan or explain while connected to the internet.")
              InfoBullet("If you are offline, keypad logging, safety rules, schedule tracking, and calling family still work on-device.")
              InfoBullet("Inside Account & Security, you can export your records as JSON or permanently delete your local session and exports at any time.")
            }
          }

          InfoSection.TERMS -> {
            SehatCard(containerColor = c.surface) {
              Text("Terms of use", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              Spacer(modifier = Modifier.height(8.dp))
              InfoBullet("SehatSaathi is provided to help patients and families organize prescriptions, track sugar and blood pressure, and prepare doctor visit summaries.")
              InfoBullet("You remain responsible for verifying extracted prescription lines, generic matches, and meter readings before acting on them.")
              InfoBullet("Generic alternatives match active ingredient and strength from our bundled sample list; always confirm with your pharmacist before switching brands.")
              InfoBullet("Always ask your doctor or pharmacist before starting, stopping, or changing any medicine.")
            }
          }

          InfoSection.ABOUT -> {
            SehatCard(containerColor = c.surface) {
              Text("About SehatSaathi", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "SehatSaathi helps people managing diabetes or blood pressure — and the family members supporting them — read prescriptions clearly, follow daily schedules, watch refill stock, and spot concerning readings early.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
              if (SiteConfig.SUPPORT_PHONE.isNotBlank() || SiteConfig.SUPPORT_EMAIL.isNotBlank() || SiteConfig.SUPPORT_ADDRESS.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Support contact", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                Spacer(modifier = Modifier.height(6.dp))
                if (SiteConfig.SUPPORT_PHONE.isNotBlank()) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier =
                      Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable(role = Role.Button) {
                          launchSafeDialer(context, SiteConfig.SUPPORT_PHONE, onShowToast)
                        }
                        .padding(vertical = 6.dp),
                  ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = c.clinicalBlueAccent, modifier = Modifier.size(18.dp))
                    Text(
                      text = SiteConfig.SUPPORT_PHONE,
                      style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                      color = c.clinicalBlueAccent,
                    )
                  }
                }
                if (SiteConfig.SUPPORT_EMAIL.isNotBlank()) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier =
                      Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable(role = Role.Button) {
                          launchSafeEmail(context, SiteConfig.SUPPORT_EMAIL, "SehatSaathi support", onShowToast)
                        }
                        .padding(vertical = 6.dp),
                  ) {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = c.clinicalBlueAccent, modifier = Modifier.size(18.dp))
                    Text(
                      text = SiteConfig.SUPPORT_EMAIL,
                      style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                      color = c.clinicalBlueAccent,
                    )
                  }
                }
                if (SiteConfig.SUPPORT_ADDRESS.isNotBlank()) {
                  Text(
                    text = SiteConfig.SUPPORT_ADDRESS,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.inkSecondary,
                  )
                }
              }
            }
          }
        }

        if (isAuthenticated) {
          SehatSecondaryButton(
            text = "Go back",
            onClick = onBack,
            minHeight = 52.dp,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }

    if (!isAuthenticated) {
      SafetyDisclaimerFooter(
        isDemoMode = isDemoMode,
        onLogoClick = onHomeClick,
        onOpenInfo = { section -> activeSection = section },
        modifier = Modifier.navigationBarsPadding(),
      )
    }
  }
}

@Composable
private fun InfoBullet(text: String) {
  val c = SehatPalette.current
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
  ) {
    Text("•", style = MaterialTheme.typography.bodyLarge, color = c.clinicalBlueAccent)
    Text(text, style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.weight(1f))
  }
}

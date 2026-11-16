package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.HealthStatusLevel
import com.example.domain.demo.DemoScenario
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.DemoScenarioPanel
import com.example.ui.components.SafetyDisclaimerFooter
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatProgressMeter
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.StatusChip
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.ClinicalBlue
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.DeepOceanDark
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency
import com.example.ui.theme.tabular
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingScreen(
  onGetStarted: () -> Unit,
  onLogIn: () -> Unit,
  onTryDemo: () -> Unit,
  onLaunchDemoScenario: (DemoScenario) -> Unit = {},
  onOpenInfo: (InfoSection) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val reducedTransparency = rememberPrefersReducedTransparency()
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(c.background)) {
    val isNarrowPhone = maxWidth < 380.dp

    Column(modifier = Modifier.fillMaxSize()) {
      Surface(
        color =
          GlassTokens.chromeFill(
            scrolled = scrollState.value > 12,
            reducedTransparency = reducedTransparency,
            isDark = c.isDark,
          ),
        shadowElevation = 0.dp,
        modifier =
          Modifier
            .fillMaxWidth()
            .shadow(
              elevation = if (scrollState.value > 12) 4.dp else 1.dp,
              ambientColor = DeepOceanDark.copy(alpha = 0.12f),
              spotColor = DeepOceanDark.copy(alpha = 0.16f),
            )
            .statusBarsPadding(),
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier =
              Modifier
                .fillMaxWidth()
                .widthIn(max = 1120.dp)
                .padding(horizontal = if (isNarrowPhone) 12.dp else 16.dp, vertical = 10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier =
                Modifier
                  .weight(1f, fill = false)
                  .clip(ButtonShape)
                  .clickable(role = Role.Button) { scope.launch { scrollState.animateScrollTo(0) } }
                  .semantics { contentDescription = "SehatSaathi home" }
                  .defaultMinSize(minHeight = 48.dp)
                  .padding(horizontal = 4.dp, vertical = 4.dp)
                  .testTag("landing_logo_home"),
            ) {
              SehatLogoMarkCanvas(modifier = Modifier.size(34.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "SehatSaathi",
                style = MaterialTheme.typography.titleLarge,
                color = c.deepOcean,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Row(
              horizontalArrangement = Arrangement.spacedBy(if (isNarrowPhone) 4.dp else 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              SehatGhostButton(text = "Log in", onClick = onLogIn, testTag = "landing_login_btn")
              SehatPrimaryButton(
                text = if (isNarrowPhone) "Start" else "Get started",
                onClick = onGetStarted,
                minHeight = 48.dp,
                testTag = "landing_get_started_top_btn",
              )
            }
          }
          HorizontalDivider(color = c.hairlineBorder)
        }
      }

      Column(
        modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState),
      ) {
        HeroSection(
          onGetStarted = onGetStarted,
          onTryDemo = onTryDemo,
        )

        HeartbeatDividerLine(color = c.clinicalBlueAccent.copy(alpha = 0.28f))

        // Problem & 4-step walkthrough in plain, neighborly words
        Box(
          contentAlignment = Alignment.TopCenter,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(c.background)
              .padding(horizontal = 16.dp, vertical = 32.dp),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp),
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.widthIn(max = 720.dp),
            ) {
              Text(
                text = "What happens after the clinic visit",
                style = MaterialTheme.typography.displayMedium,
                color = c.deepOcean,
              )
              Text(
                text =
                  "Most families leave the clinic with a paper slip, a blood sugar meter, and three or four tablet strips. At 2 AM, when a reading looks wrong or a strip runs low, it is hard to remember which tablet is which or when to call for help. Here is how SehatSaathi helps day to day.",
                style = MaterialTheme.typography.bodyLarge,
                color = c.inkSecondary,
              )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
              val twoCols = maxWidth >= 720.dp
              if (twoCols) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                  Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                    ScanStoryCard(modifier = Modifier.weight(1f))
                    TrackStoryCard(modifier = Modifier.weight(1f))
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                    AlertStoryCard(modifier = Modifier.weight(1f))
                    RefillStoryCard(modifier = Modifier.weight(1f))
                  }
                }
              } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  ScanStoryCard(modifier = Modifier.fillMaxWidth())
                  TrackStoryCard(modifier = Modifier.fillMaxWidth())
                  AlertStoryCard(modifier = Modifier.fillMaxWidth())
                  RefillStoryCard(modifier = Modifier.fillMaxWidth())
                }
              }
            }
          }
        }

        // Safety Rules & "What SehatSaathi cannot do"
        Box(
          contentAlignment = Alignment.TopCenter,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(listOf(c.mistSoft, c.mist)))
              .padding(horizontal = 16.dp, vertical = 32.dp),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(Icons.Filled.Shield, contentDescription = null, tint = c.deepOcean, modifier = Modifier.size(26.dp))
              Text(
                text = "Built around clear medical safety rules",
                style = MaterialTheme.typography.headlineLarge,
                color = c.deepOcean,
              )
            }
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              SafetyPrincipleCard(
                title = "Rules decide safety, not AI",
                body = "AI only reads handwriting and explains terms in plain words. Fixed clinical thresholds decide every alert.",
                modifier = Modifier.widthIn(min = 260.dp, max = 360.dp),
              )
              SafetyPrincipleCard(
                title = "You check every number first",
                body = "Medicine names, doses, and blood sugar numbers are never saved until you review and confirm them.",
                modifier = Modifier.widthIn(min = 260.dp, max = 360.dp),
              )
              SafetyPrincipleCard(
                title = "Unknown is never marked safe",
                body = "If a medicine is not in our 100-medicine reference list, we tell you openly to ask your pharmacist.",
                modifier = Modifier.widthIn(min = 260.dp, max = 360.dp),
              )
            }

            SehatCard(
              containerColor = c.surface,
              modifier = Modifier.fillMaxWidth().testTag("landing_what_it_cannot_do"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = c.deepOcean, modifier = Modifier.size(22.dp))
                Text(
                  text = "What SehatSaathi cannot do",
                  style = MaterialTheme.typography.titleLarge,
                  color = c.deepOcean,
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "• It is not a doctor and cannot diagnose any illness.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "• It never tells you to start, stop, or change a medicine dose.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "• It only checks duplicate salts, interactions, and generic options against its bundled 100-medicine reference list. Anything outside that list must be checked by your pharmacist.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "• Reading a new prescription photo requires internet, though you can always type medicines and log readings offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
            }
          }
        }

        FaqSection(
          reducedMotion = reducedMotion,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 30.dp),
        )

        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(c.background)
              .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp),
          ) {
            SehatCard(
              containerColor = c.surface,
              modifier = Modifier.fillMaxWidth().testTag("landing_judge_quickstart"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = "Quick start for reviewers (3 steps)",
                  style = MaterialTheme.typography.titleLarge,
                  color = c.deepOcean,
                )
                DemoPillBadge(text = "Track 2 · Healthcare")
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "1. Tap 'Try demo' (or any scenario button below) to open Sunita Devi's sample profile with no sign-up required.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "2. Log a blood sugar reading (such as 54, 138, 250, or 300 mg/dL) or blood pressure (180/120 mmHg), or scan the sample prescription.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "3. Open Family or Profile to switch between Patient and Caregiver views, print the 1-page Doctor Summary PDF, or sign out to create your own personal account.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
              )
            }

            DemoScenarioPanel(
              activeScenario = null,
              activeCaption = null,
              onRunScenario = onLaunchDemoScenario,
              onResetDemo = {},
              showResetAndTourControls = false,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }

        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(listOf(c.mistSoft, c.mist)))
              .padding(horizontal = 16.dp, vertical = 32.dp),
        ) {
          SehatCard(
            modifier = Modifier.fillMaxWidth().widthIn(max = 760.dp),
          ) {
            Text(
              text = "Try the sample profile or start your own",
              style = MaterialTheme.typography.headlineLarge,
              color = c.deepOcean,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text =
                "Open Sunita Devi's sample account with 4 medicines, 14 days of readings, and refill tracking. No sign-up is needed for the demo.",
              style = MaterialTheme.typography.bodyLarge,
              color = c.inkSecondary,
            )
            Spacer(modifier = Modifier.height(18.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              SehatPrimaryButton(
                text = "Try demo",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onTryDemo,
                testTag = "landing_try_demo_bottom_btn",
              )
              SehatSecondaryButton(
                text = "Create account",
                onClick = onGetStarted,
                minHeight = 56.dp,
                testTag = "landing_signup_bottom_btn",
              )
            }
          }
        }

        SafetyDisclaimerFooter(
          isDemoMode = false,
          onLogoClick = { scope.launch { scrollState.animateScrollTo(0) } },
          onOpenInfo = onOpenInfo,
          showExtendedLinks = true,
          modifier = Modifier.navigationBarsPadding(),
        )
      }
    }
  }
}

@Composable
private fun HeroSection(
  onGetStarted: () -> Unit,
  onTryDemo: () -> Unit,
) {
  val c = SehatPalette.current
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(c.background, c.mistSoft, c.mist.copy(alpha = 0.55f))))
        .padding(horizontal = 16.dp, vertical = 28.dp),
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp)) {
      val splitHero = maxWidth >= 820.dp
      if (splitHero) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(32.dp),
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
          HeroCopyAndActions(
            onGetStarted = onGetStarted,
            onTryDemo = onTryDemo,
            modifier = Modifier.weight(1.15f),
          )
          HeroSampleSnapshotCard(
            modifier = Modifier.weight(0.85f),
          )
        }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(22.dp),
          modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        ) {
          HeroCopyAndActions(
            onGetStarted = onGetStarted,
            onTryDemo = onTryDemo,
            modifier = Modifier.fillMaxWidth(),
          )
          HeroSampleSnapshotCard(
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroCopyAndActions(
  onGetStarted: () -> Unit,
  onTryDemo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Column(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = modifier,
  ) {
    DemoPillBadge(text = "Daily support for diabetes and blood pressure")

    Text(
      text = "Understand your medicines. Stay safe every day.",
      style = MaterialTheme.typography.displayLarge,
      color = c.deepOcean,
    )

    Text(
      text =
        "Take a photo of your prescription. We read it, you check it. Log your blood sugar or blood pressure, see what to do first if a reading is low or high, and keep track of how many tablets are left.",
      style = MaterialTheme.typography.bodyLarge,
      color = c.ink,
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SehatPrimaryButton(
        text = "Get started",
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = onGetStarted,
        testTag = "hero_get_started_btn",
      )
      SehatSecondaryButton(
        text = "Try demo",
        onClick = onTryDemo,
        minHeight = 56.dp,
        testTag = "hero_try_demo_btn",
      )
    }
  }
}

@Composable
private fun HeroSampleSnapshotCard(
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Surface(
    shape = CardShape,
    color = c.surface,
    border = BorderStroke(1.dp, c.hairlineBorder),
    shadowElevation = 3.dp,
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.padding(18.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          PhoneHeartbeatLineIllustration(color = c.deepOcean, modifier = Modifier.size(34.dp))
          Column {
            Text("Sunita Devi · Daily view", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            Text("Sample patient (Demo)", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
          }
        }
        StatusChip(level = HealthStatusLevel.NORMAL, label = "In range")
      }

      Surface(
        shape = ButtonShape,
        color = c.mistSoft,
        border = BorderStroke(1.dp, c.hairlineBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(14.dp),
        ) {
          Column {
            Text("Blood sugar (Fasting)", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            Text(
              text = "138 mg/dL",
              style = MaterialTheme.typography.headlineMedium.tabular(),
              color = c.deepOcean,
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text("Blood pressure", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            Text(
              text = "128/82 mmHg",
              style = MaterialTheme.typography.headlineMedium.tabular(),
              color = c.deepOcean,
            )
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c.clinicalBlueAccent, modifier = Modifier.size(18.dp))
          Text("3 of 4 medicines taken today", style = MaterialTheme.typography.labelMedium, color = c.ink)
        }
        DemoPillBadge(text = "Demo")
      }
    }
  }
}

@Composable
private fun ScanStoryCard(modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      RxSheetLineIllustration(color = c.deepOcean, modifier = Modifier.size(36.dp))
      Text("1. Read your prescription", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Take a photo of your prescription. We read it, you check it before anything is saved.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = ButtonShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Amaryl 1 mg · Before breakfast", style = MaterialTheme.typography.bodyMedium, color = c.ink)
          StatusChip(level = HealthStatusLevel.NORMAL, label = "Verified")
        }
        HorizontalDivider(color = c.hairlineBorder)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Glycomet 500 mg · After meals", style = MaterialTheme.typography.bodyMedium, color = c.ink)
          StatusChip(level = HealthStatusLevel.INFO, label = "Generic match")
        }
      }
    }
  }
}

@Composable
private fun TrackStoryCard(modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      PhoneHeartbeatLineIllustration(color = c.deepOcean, modifier = Modifier.size(36.dp))
      Text("2. Log sugar and blood pressure", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Type on the large keypad, speak a reading, or scan your meter. If your sugar drops below 70 mg/dL, we show you what to do first.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = ButtonShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "138 mg/dL",
            style = MaterialTheme.typography.headlineLarge.tabular(),
            color = c.deepOcean,
          )
          StatusChip(level = HealthStatusLevel.NORMAL, label = "In range")
        }
        SehatProgressMeter(progress = 0.56f, color = c.clinicalBlueAccent)
        Text("Fasting · Confirmed by Sunita Devi", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
      }
    }
  }
}

@Composable
private fun AlertStoryCard(modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      PillOrganizerLineIllustration(color = c.deepOcean, modifier = Modifier.size(36.dp))
      Text("3. Keep your family in the loop", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "When a reading is dangerously low or high, clear first-aid steps and a ready-to-send family alert appear right away.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = ButtonShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          StatusChip(level = HealthStatusLevel.URGENT, label = "Urgent Low")
          DemoPillBadge(text = "Demo")
        }
        Text(
          text = "\"Alert: Sunita Devi's sugar is 62 mg/dL (low) at 2:04 AM. Please call her now.\"",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
      }
    }
  }
}

@Composable
private fun RefillStoryCard(modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      RefillCalendarLineIllustration(color = c.deepOcean, modifier = Modifier.size(36.dp))
      Text("4. Know when tablets run low", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "2 tablets left is about 2 days. Stock updates when you mark a dose as taken, and warns you before a strip finishes.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = ButtonShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Amaryl 1 mg", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
          StatusChip(level = HealthStatusLevel.URGENT, label = "2 days left")
        }
        SehatProgressMeter(progress = 0.20f, color = c.statusUrgentFg)
        Text("2 tablets left · Refill due", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
      }
    }
  }
}

@Composable
private fun SafetyPrincipleCard(
  title: String,
  body: String,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  SehatCard(modifier = modifier, containerColor = c.surface) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c.clinicalBlueAccent, modifier = Modifier.size(20.dp))
      Text(title, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium, color = c.ink)
  }
}

@Composable
private fun FaqSection(
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val items = remember {
    listOf(
      "Is SehatSaathi a doctor or diagnostic tool?" to
        "No. SehatSaathi helps you read prescriptions and track your daily numbers. It never diagnoses an illness or changes your medicine dose. Always ask your doctor or pharmacist.",
      "What works when I have no internet?" to
        "Your daily medicine schedule, tablet counts, and blood sugar or blood pressure safety checks all run on your phone without internet. Reading a new prescription photo needs internet, and you can type medicines in manually anytime.",
      "What is Demo mode?" to
        "Demo mode opens a fictional sample profile for Sunita Devi (age 66) with 4 medicines and 14 days of readings so you can test every screen before creating your own account.",
      "Who can see my readings and family alerts?" to
        "Your readings stay on your phone. Only family contacts you invite and mark as consented are shown in family alert previews, and you can remove them whenever you want.",
    )
  }
  var expandedIndex by remember { mutableIntStateOf(0) }

  Box(contentAlignment = Alignment.TopCenter, modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth().widthIn(max = 840.dp),
    ) {
      Text("Common questions", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
      items.forEachIndexed { idx, (question, answer) ->
        val isExpanded = expandedIndex == idx
        SehatCard(
          onClick = { expandedIndex = if (isExpanded) -1 else idx },
          containerColor = if (isExpanded) c.mistSoft else c.surface,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
          ) {
            Text(
              text = question,
              style = MaterialTheme.typography.titleMedium,
              color = c.deepOcean,
              modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            Icon(
              imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
              contentDescription = if (isExpanded) "Collapse answer" else "Expand answer",
              tint = c.deepOcean,
            )
          }
          AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(if (reducedMotion) 0 else 200)) + fadeIn(tween(if (reducedMotion) 0 else 200)),
            exit = shrinkVertically(tween(if (reducedMotion) 0 else 180)),
          ) {
            Text(
              text = answer,
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
              modifier = Modifier.padding(top = 10.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SehatLogoMarkCanvas(modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .clip(ButtonShape)
        .background(DeepOcean),
  ) {
    Canvas(modifier = Modifier.size(20.dp)) {
      val stroke = 2.dp.toPx()
      val w = size.width
      val h = size.height
      val path = Path().apply {
        moveTo(0f, h * 0.55f)
        lineTo(w * 0.25f, h * 0.55f)
        lineTo(w * 0.38f, h * 0.22f)
        lineTo(w * 0.55f, h * 0.82f)
        lineTo(w * 0.70f, h * 0.45f)
        lineTo(w, h * 0.45f)
      }
      drawPath(
        path = path,
        color = PureWhite,
        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
    }
  }
}

@Composable
private fun HeartbeatDividerLine(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.fillMaxWidth().height(18.dp)) {
    val y = size.height * 0.5f
    val w = size.width
    val path = Path().apply {
      moveTo(0f, y)
      lineTo(w * 0.44f, y)
      lineTo(w * 0.46f, y - 6.dp.toPx())
      lineTo(w * 0.48f, y + 7.dp.toPx())
      lineTo(w * 0.50f, y - 8.dp.toPx())
      lineTo(w * 0.52f, y + 4.dp.toPx())
      lineTo(w * 0.54f, y)
      lineTo(w, y)
    }
    drawPath(
      path = path,
      color = color,
      style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
  }
}

@Composable
private fun RxSheetLineIllustration(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val s = 1.75.dp.toPx()
    val w = size.width
    val h = size.height
    drawRoundRect(
      color = color,
      topLeft = Offset(w * 0.16f, h * 0.10f),
      size = Size(w * 0.68f, h * 0.80f),
      cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
      style = Stroke(width = s),
    )
    drawLine(color, Offset(w * 0.28f, h * 0.30f), Offset(w * 0.52f, h * 0.30f), strokeWidth = s, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.28f, h * 0.48f), Offset(w * 0.72f, h * 0.48f), strokeWidth = s, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.28f, h * 0.64f), Offset(w * 0.64f, h * 0.64f), strokeWidth = s, cap = StrokeCap.Round)
  }
}

@Composable
private fun PhoneHeartbeatLineIllustration(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val s = 1.75.dp.toPx()
    val w = size.width
    val h = size.height
    drawRoundRect(
      color = color,
      topLeft = Offset(w * 0.22f, h * 0.08f),
      size = Size(w * 0.56f, h * 0.84f),
      cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
      style = Stroke(width = s),
    )
    val pulse = Path().apply {
      moveTo(w * 0.30f, h * 0.50f)
      lineTo(w * 0.42f, h * 0.50f)
      lineTo(w * 0.47f, h * 0.36f)
      lineTo(w * 0.54f, h * 0.64f)
      lineTo(w * 0.60f, h * 0.50f)
      lineTo(w * 0.70f, h * 0.50f)
    }
    drawPath(pulse, color = ClinicalBlue, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))
  }
}

@Composable
private fun PillOrganizerLineIllustration(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val s = 1.75.dp.toPx()
    val w = size.width
    val h = size.height
    drawRoundRect(
      color = color,
      topLeft = Offset(w * 0.10f, h * 0.22f),
      size = Size(w * 0.80f, h * 0.56f),
      cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
      style = Stroke(width = s),
    )
    drawLine(color, Offset(w * 0.37f, h * 0.22f), Offset(w * 0.37f, h * 0.78f), strokeWidth = s)
    drawLine(color, Offset(w * 0.63f, h * 0.22f), Offset(w * 0.63f, h * 0.78f), strokeWidth = s)
    drawCircle(color = ClinicalBlue, radius = w * 0.06f, center = Offset(w * 0.24f, h * 0.50f))
    drawCircle(color = ClinicalBlue, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.50f))
  }
}

@Composable
private fun RefillCalendarLineIllustration(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val s = 1.75.dp.toPx()
    val w = size.width
    val h = size.height
    drawRoundRect(
      color = color,
      topLeft = Offset(w * 0.14f, h * 0.18f),
      size = Size(w * 0.72f, h * 0.68f),
      cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
      style = Stroke(width = s),
    )
    drawLine(color, Offset(w * 0.14f, h * 0.36f), Offset(w * 0.86f, h * 0.36f), strokeWidth = s)
    drawLine(color, Offset(w * 0.32f, h * 0.10f), Offset(w * 0.32f, h * 0.24f), strokeWidth = s, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.68f, h * 0.10f), Offset(w * 0.68f, h * 0.24f), strokeWidth = s, cap = StrokeCap.Round)
    drawCircle(color = ClinicalBlue, radius = w * 0.07f, center = Offset(w * 0.64f, h * 0.62f))
  }
}

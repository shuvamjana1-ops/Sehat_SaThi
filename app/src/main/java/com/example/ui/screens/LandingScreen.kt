package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
import com.example.ui.components.sehatColumnScroll3D
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.ClinicalBlue
import com.example.ui.theme.DeepOceanDark
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency
import com.example.ui.theme.tabular
import kotlinx.coroutines.delay

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
  val density = LocalDensity.current

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(c.background)) {
    val isNarrowPhone = maxWidth < 380.dp
    val viewportHeightPx = with(density) { maxHeight.toPx() }
    val scrollProgress =
      if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
      } else {
        0f
      }

    Column(modifier = Modifier.fillMaxSize()) {
      // Sticky Premium Header + Live Scroll Progress Indicator
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
              elevation = if (scrollState.value > 12) 6.dp else 1.dp,
              ambientColor = DeepOceanDark.copy(alpha = 0.16f),
              spotColor = DeepOceanDark.copy(alpha = 0.2f),
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
                .widthIn(max = 1200.dp)
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
              Box(
                contentAlignment = Alignment.Center,
                modifier =
                  Modifier
                    .size(36.dp)
                    .shadow(
                      elevation = 4.dp,
                      shape = ButtonShape,
                      ambientColor = DeepOceanDark.copy(alpha = 0.3f),
                      spotColor = ClinicalBlue.copy(alpha = 0.35f),
                    )
                    .clip(ButtonShape)
                    .background(Brush.linearGradient(listOf(DeepOceanDark, ClinicalBlue))),
              ) {
                Icon(
                  imageVector = Icons.Filled.Favorite,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(18.dp),
                )
              }
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
          // Slim luminous scroll progress bar
          Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(c.mist)) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth(scrollProgress.coerceAtLeast(0.04f))
                  .height(2.5.dp)
                  .background(Brush.horizontalGradient(listOf(c.deepOcean, c.clinicalBlueAccent))),
            )
          }
        }
      }

      // Main scrollable body with 3D perspective depth & parallax
      Column(
        modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState),
      ) {
        HeroStorySection(
          scrollState = scrollState,
          reducedMotion = reducedMotion,
          onGetStarted = onGetStarted,
          onTryDemo = onTryDemo,
        )

        // Scroll Storytelling Sections: Scan -> Track -> Alert -> Refill
        Box(
          contentAlignment = Alignment.TopCenter,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(c.background)
              .padding(horizontal = 16.dp, vertical = 30.dp),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp),
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion, 4f),
            ) {
              Text(
                text = "One calm safety loop between doctor visits",
                style = MaterialTheme.typography.displayMedium,
                color = c.deepOcean,
              )
              Text(
                text =
                  "Every step is written in clear everyday English, with large buttons and family peace of mind.",
                style = MaterialTheme.typography.bodyLarge,
                color = c.inkSecondary,
              )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
              val twoCols = maxWidth >= 720.dp
              if (twoCols) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                  Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                    ScanStoryCard(
                      reducedMotion = reducedMotion,
                      modifier =
                        Modifier
                          .weight(1f)
                          .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                    )
                    TrackStoryCard(
                      reducedMotion = reducedMotion,
                      modifier =
                        Modifier
                          .weight(1f)
                          .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                    )
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                    AlertStoryCard(
                      modifier =
                        Modifier
                          .weight(1f)
                          .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                    )
                    RefillStoryCard(
                      reducedMotion = reducedMotion,
                      modifier =
                        Modifier
                          .weight(1f)
                          .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                    )
                  }
                }
              } else {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                  ScanStoryCard(
                    reducedMotion = reducedMotion,
                    modifier =
                      Modifier
                        .fillMaxWidth()
                        .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                  )
                  TrackStoryCard(
                    reducedMotion = reducedMotion,
                    modifier =
                      Modifier
                        .fillMaxWidth()
                        .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                  )
                  AlertStoryCard(
                    modifier =
                      Modifier
                        .fillMaxWidth()
                        .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                  )
                  RefillStoryCard(
                    reducedMotion = reducedMotion,
                    modifier =
                      Modifier
                        .fillMaxWidth()
                        .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
                  )
                }
              }
            }
          }
        }

        // Safety Principles Strip
        Box(
          contentAlignment = Alignment.TopCenter,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(listOf(c.mistSoft, c.mist)))
              .padding(horizontal = 16.dp, vertical = 34.dp),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion, 4f),
            ) {
              Icon(Icons.Filled.Shield, contentDescription = null, tint = c.deepOcean, modifier = Modifier.size(28.dp))
              Text(
                text = "Built around medical safety rules",
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
                body = "AI only reads handwriting and explains terms. Clear clinical rules decide alerts.",
                modifier =
                  Modifier
                    .widthIn(min = 260.dp, max = 360.dp)
                    .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
              )
              SafetyPrincipleCard(
                title = "You confirm every number",
                body = "Medicine names, doses, and sugar readings are never saved until you check and confirm.",
                modifier =
                  Modifier
                    .widthIn(min = 260.dp, max = 360.dp)
                    .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
              )
              SafetyPrincipleCard(
                title = "Unknown is never marked safe",
                body = "If a medicine is not in our sample list, we clearly ask you to check with your pharmacist.",
                modifier =
                  Modifier
                    .widthIn(min = 260.dp, max = 360.dp)
                    .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion),
              )
            }
          }
        }

        // FAQ Accordion
        FaqSection(
          scrollState = scrollState,
          viewportHeightPx = viewportHeightPx,
          reducedMotion = reducedMotion,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 30.dp),
        )

        // One-tap Judge Demo Scenarios
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(c.background)
              .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
          DemoScenarioPanel(
            activeScenario = null,
            activeCaption = null,
            onRunScenario = onLaunchDemoScenario,
            onResetDemo = {},
            showResetAndTourControls = false,
            modifier =
              Modifier
                .fillMaxWidth()
                .widthIn(max = 1120.dp)
                .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion, 4f),
          )
        }

        // Final Call to Action
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(listOf(c.mistSoft, c.mist)))
              .padding(horizontal = 16.dp, vertical = 34.dp),
        ) {
          SehatCard(
            modifier =
              Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion, 5f),
          ) {
            Text(
              text = "See how SehatSaathi feels in 10 seconds",
              style = MaterialTheme.typography.headlineLarge,
              color = c.deepOcean,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text =
                "Explore Sunita Devi's seeded demo account with 4 medicines, 14 days of readings, and refill tracking — no sign-up needed.",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroStorySection(
  scrollState: ScrollState,
  reducedMotion: Boolean,
  onGetStarted: () -> Unit,
  onTryDemo: () -> Unit,
) {
  val headlineWords = remember { listOf("Understand", "your", "medicines.", "Stay", "safe", "every", "day.") }
  var visibleWords by remember { mutableIntStateOf(if (reducedMotion) headlineWords.size else 0) }
  var ecgStarted by remember { mutableStateOf(reducedMotion) }

  LaunchedEffect(reducedMotion) {
    if (reducedMotion) {
      visibleWords = headlineWords.size
      ecgStarted = true
    } else {
      ecgStarted = true
      for (i in 1..headlineWords.size) {
        visibleWords = i
        delay(80)
      }
    }
  }

  val ecgFraction by
    animateFloatAsState(
      targetValue = if (ecgStarted) 1f else 0f,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 700, easing = FastOutSlowInEasing),
      label = "hero_ecg",
    )

  val infiniteTransition = rememberInfiniteTransition(label = "hero_shapes")
  val floatOffset by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = if (reducedMotion) 0f else 18f,
      animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
      label = "float_offset",
    )

  val c = SehatPalette.current
  val blobPrimary = c.clinicalBlueAccent
  val blobSecondary = c.deepOcean

  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(c.mist, c.mistSoft, c.background)))
        .padding(horizontal = 16.dp, vertical = 30.dp),
  ) {
    // Parallax translucent spheres + self-drawing ECG wave
    Canvas(
      modifier =
        Modifier
          .matchParentSize()
          .graphicsLayer {
            if (!reducedMotion) {
              translationY = scrollState.value * 0.25f
            }
          },
    ) {
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(blobPrimary.copy(alpha = 0.14f), blobPrimary.copy(alpha = 0f)),
            center = Offset(size.width * 0.85f, size.height * 0.24f + floatOffset),
            radius = size.minDimension * 0.42f,
          ),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.85f, size.height * 0.24f + floatOffset),
      )
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(blobSecondary.copy(alpha = 0.10f), blobSecondary.copy(alpha = 0f)),
            center = Offset(size.width * 0.14f, size.height * 0.74f - floatOffset),
            radius = size.minDimension * 0.34f,
          ),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.14f, size.height * 0.74f - floatOffset),
      )

      val yBase = size.height * 0.88f
      val w = size.width
      val ecgPoints =
        listOf(
          0f to yBase,
          w * 0.20f to yBase,
          w * 0.24f to yBase - 14.dp.toPx(),
          w * 0.27f to yBase + 10.dp.toPx(),
          w * 0.31f to yBase - 42.dp.toPx(),
          w * 0.35f to yBase + 26.dp.toPx(),
          w * 0.39f to yBase - 10.dp.toPx(),
          w * 0.43f to yBase,
          w * 0.68f to yBase,
          w * 0.72f to yBase - 30.dp.toPx(),
          w * 0.76f to yBase + 18.dp.toPx(),
          w * 0.80f to yBase,
          w to yBase,
        )
      val path = Path()
      val limit = ((ecgPoints.size - 1) * ecgFraction).toInt().coerceAtLeast(1)
      ecgPoints.forEachIndexed { index, (x, y) ->
        if (index <= limit) {
          if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
      }
      drawPath(
        path = path,
        color = blobPrimary.copy(alpha = 0.24f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp)) {
      val splitHero = maxWidth >= 820.dp
      if (splitHero) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(32.dp),
          modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        ) {
          HeroCopyAndActions(
            headlineWords = headlineWords,
            visibleWords = visibleWords,
            onGetStarted = onGetStarted,
            onTryDemo = onTryDemo,
            modifier = Modifier.weight(1.15f),
          )
          Hero3DShowcaseDeck(
            scrollState = scrollState,
            floatOffset = floatOffset,
            reducedMotion = reducedMotion,
            modifier = Modifier.weight(0.85f),
          )
        }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(24.dp),
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
          HeroCopyAndActions(
            headlineWords = headlineWords,
            visibleWords = visibleWords,
            onGetStarted = onGetStarted,
            onTryDemo = onTryDemo,
            modifier = Modifier.fillMaxWidth(),
          )
          Hero3DShowcaseDeck(
            scrollState = scrollState,
            floatOffset = floatOffset,
            reducedMotion = reducedMotion,
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
  headlineWords: List<String>,
  visibleWords: Int,
  onGetStarted: () -> Unit,
  onTryDemo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier,
  ) {
    DemoPillBadge(text = "Support companion for diabetes & blood pressure")

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      headlineWords.forEachIndexed { idx, word ->
        AnimatedVisibility(
          visible = idx < visibleWords,
          enter = fadeIn(tween(220)),
        ) {
          Text(
            text = word,
            style = MaterialTheme.typography.displayLarge,
            color = c.deepOcean,
          )
        }
      }
    }

    Text(
      text =
        "SehatSaathi helps you read prescriptions, check medicine safety, log sugar and blood pressure, and alert your family when a reading needs care.",
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
private fun Hero3DShowcaseDeck(
  scrollState: ScrollState,
  floatOffset: Float,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.padding(vertical = 8.dp),
  ) {
    // Back depth layer (3D stacked translucent card)
    Surface(
      shape = CardShape,
      color = c.mist,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier =
        Modifier
          .fillMaxWidth()
          .height(210.dp)
          .offset(x = 12.dp, y = 14.dp)
          .graphicsLayer {
            if (!reducedMotion) {
              val scrollNorm = (scrollState.value / 700f).coerceIn(0f, 1f)
              cameraDistance = 18f * density
              rotationY = -10f + scrollNorm * 6f
              rotationX = 5f - scrollNorm * 7f
              translationY = (-floatOffset * 0.35f) - (scrollNorm * 16f)
            }
          }
          .shadow(
            elevation = 10.dp,
            shape = CardShape,
            ambientColor = DeepOceanDark.copy(alpha = 0.2f),
            spotColor = DeepOceanDark.copy(alpha = 0.24f),
          ),
    ) {}

    // Primary foreground 3D sculpted preview card
    Surface(
      shape = CardShape,
      color = c.surface,
      border =
        BorderStroke(
          1.2.dp,
          Brush.linearGradient(listOf(c.surface, c.hairlineBorder)),
        ),
      modifier =
        Modifier
          .fillMaxWidth()
          .graphicsLayer {
            if (!reducedMotion) {
              val scrollNorm = (scrollState.value / 700f).coerceIn(0f, 1f)
              cameraDistance = 18f * density
              rotationY = -5f + scrollNorm * 5f
              rotationX = 3f - scrollNorm * 8f
              translationY = (floatOffset * 0.22f) - (scrollNorm * 22f)
              val scale = 1f - scrollNorm * 0.04f
              scaleX = scale
              scaleY = scale
            }
          }
          .shadow(
            elevation = 16.dp,
            shape = CardShape,
            ambientColor = DeepOceanDark.copy(alpha = 0.24f),
            spotColor = ClinicalBlue.copy(alpha = 0.32f),
          ),
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
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
              contentAlignment = Alignment.Center,
              modifier =
                Modifier
                  .size(32.dp)
                  .clip(ButtonShape)
                  .background(Brush.linearGradient(listOf(DeepOceanDark, ClinicalBlue))),
            ) {
              Icon(
                imageVector = Icons.Filled.MonitorHeart,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(18.dp),
              )
            }
            Column {
              Text("Sunita Devi · Daily safety view", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
              Text("Seeded sample snapshot", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            }
          }
          StatusChip(level = HealthStatusLevel.NORMAL, label = "In range")
        }

        Surface(
          shape = CardShape,
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
              Text("Blood sugar", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
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
          DemoPillBadge(text = "Rule verified")
        }
      }
    }
  }
}

@Composable
private fun ScanStoryCard(reducedMotion: Boolean, modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  var showParsedList by remember { mutableStateOf(true) }
  LaunchedEffect(reducedMotion) {
    if (!reducedMotion) {
      while (true) {
        delay(2600)
        showParsedList = !showParsedList
      }
    }
  }

  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Icon(Icons.Filled.DocumentScanner, contentDescription = null, tint = c.clinicalBlueAccent)
      Text("1. Scan prescription", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text =
        if (showParsedList) "Turns handwritten notes into a clear daily list for you to confirm:"
        else "Start from a prescription photo:",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!showParsedList) {
          Text("Dr. R. Mehta · Rx 12 Nov", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
          Text("Tab Amaryl 1mg  1-0-0  a/c", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
          Text("Tab Glycomet 500  1-0-1  p/c", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
        } else {
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
}

@Composable
private fun TrackStoryCard(reducedMotion: Boolean, modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  val animatedSugar by
    animateIntAsState(
      targetValue = 138,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 700),
      label = "story_sugar",
    )

  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Icon(Icons.Filled.MonitorHeart, contentDescription = null, tint = c.clinicalBlueAccent)
      Text("2. Track sugar & BP", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Log by photo, voice, or large keypad — then confirm before saving.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = CardShape,
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
            text = "$animatedSugar mg/dL",
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
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = c.statusUrgentFg)
      Text("3. Alert family quickly", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "If a reading is dangerously low or high, clear first-aid steps and family alerts appear right away.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = CardShape,
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
private fun RefillStoryCard(reducedMotion: Boolean, modifier: Modifier = Modifier) {
  val c = SehatPalette.current
  val daysLeft by
    animateIntAsState(
      targetValue = 2,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 700),
      label = "story_refill_days",
    )

  SehatCard(modifier = modifier, containerColor = c.mistSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Icon(Icons.Filled.Medication, contentDescription = null, tint = c.statusCautionFg)
      Text("4. Never run out of tablets", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Stock updates only when you tap Taken, and warns you days before tablets finish.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      shape = CardShape,
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
          StatusChip(level = HealthStatusLevel.URGENT, label = "$daysLeft days left")
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
  scrollState: ScrollState,
  viewportHeightPx: Float,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val items = remember {
    listOf(
      "Is SehatSaathi a doctor or diagnostic tool?" to
        "No. SehatSaathi is a support tool that helps you read prescriptions and track readings. It never diagnoses or changes your medicine dose. Always ask your doctor or pharmacist.",
      "Does it work without internet?" to
        "Yes. Your medicine schedule, stock counts, and danger threshold rules work locally on your device. Prescription photo reading waits until internet is available, with manual entry always ready.",
      "What is Demo mode?" to
        "Demo mode loads a fictional patient named Sunita Devi with sample medicines and 14 days of readings so you can try the app safely without creating a real account.",
      "Who can see my family alerts?" to
        "Only family contacts you invite and who consent can receive alerts, and you can remove their access at any time.",
    )
  }
  var expandedIndex by remember { mutableIntStateOf(0) }

  Box(contentAlignment = Alignment.TopCenter, modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth().widthIn(max = 840.dp),
    ) {
      Text("Frequently asked questions", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
      items.forEachIndexed { idx, (question, answer) ->
        val isExpanded = expandedIndex == idx
        SehatCard(
          onClick = { expandedIndex = if (isExpanded) -1 else idx },
          containerColor = if (isExpanded) c.mistSoft else c.surface,
          modifier = Modifier.sehatColumnScroll3D(scrollState, viewportHeightPx, reducedMotion, 4f),
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
            enter = expandVertically(tween(220)) + fadeIn(tween(220)),
            exit = shrinkVertically(tween(200)),
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

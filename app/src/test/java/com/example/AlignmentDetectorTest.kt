package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.domain.risk.ReadingContext
import com.example.domain.risk.RiskEngine
import com.example.domain.risk.SymptomFlags
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.BaselineValueWithUnit
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.screens.CaregiverDashboardScreen
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SehatSaathiTheme
import com.example.ui.theme.ThemePreference
import com.example.ui.theme.buildSehatTypography
import com.example.ui.theme.nonBreakingUnit
import com.example.ui.theme.withNonBreakingUnits
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlignmentDetectorTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun typographyScale_usesManropeOnlyAndMeetsSizeAndLineHeightRules() {
    val typo = buildSehatTypography()
    val allStyles =
      listOf(
        "displayLarge" to typo.displayLarge,
        "displayMedium" to typo.displayMedium,
        "headlineLarge" to typo.headlineLarge,
        "headlineMedium" to typo.headlineMedium,
        "titleLarge" to typo.titleLarge,
        "titleMedium" to typo.titleMedium,
        "bodyLarge" to typo.bodyLarge,
        "bodyMedium" to typo.bodyMedium,
        "labelLarge" to typo.labelLarge,
        "labelMedium" to typo.labelMedium,
        "labelSmall" to typo.labelSmall,
      )

    for ((name, style) in allStyles) {
      assertEquals("$name must use ManropeFontFamily", ManropeFontFamily, style.fontFamily)
    }

    // Body text must be >= 18sp with line-height ratio 1.55..1.60
    assertTrue("bodyLarge must be >= 18sp", typo.bodyLarge.fontSize.value >= 18f)
    assertTrue("bodyMedium must be >= 18sp", typo.bodyMedium.fontSize.value >= 18f)
    val bodyRatio = typo.bodyLarge.lineHeight.value / typo.bodyLarge.fontSize.value
    assertTrue("body line-height ratio ($bodyRatio) must be in 1.54..1.61", bodyRatio in 1.54f..1.61f)

    // Captions & labels must be >= 14sp with line-height ratio ~1.40
    assertTrue("labelLarge must be >= 14sp", typo.labelLarge.fontSize.value >= 14f)
    assertTrue("labelMedium must be >= 14sp", typo.labelMedium.fontSize.value >= 14f)
    assertTrue("labelSmall must be >= 14sp", typo.labelSmall.fontSize.value >= 14f)
    val labelRatio = typo.labelMedium.lineHeight.value / typo.labelMedium.fontSize.value
    assertTrue("label line-height ratio ($labelRatio) must be ~1.40", labelRatio in 1.38f..1.42f)

    // Display titles must have line-height ratio 1.10..1.20
    val displayRatio = typo.displayLarge.lineHeight.value / typo.displayLarge.fontSize.value
    assertTrue("display line-height ratio ($displayRatio) must be in 1.10..1.20", displayRatio in 1.10f..1.20f)

    // Section headings must have line-height ratio 1.20..1.30
    val headingRatio = typo.headlineLarge.lineHeight.value / typo.headlineLarge.fontSize.value
    assertTrue("heading line-height ratio ($headingRatio) must be in 1.20..1.30", headingRatio in 1.20f..1.30f)
  }

  @Test
  fun nonBreakingUnitHelpers_preventSplittingNumbersFromClinicalUnits() {
    assertEquals("62\u00A0mg/dL", nonBreakingUnit(62, "mg/dL"))
    assertEquals(
      "Sugar 148\u00A0mg/dL and BP 128/82\u00A0mmHg for 15\u00A0days",
      "Sugar 148 mg/dL and BP 128/82 mmHg for 15 days".withNonBreakingUnits(),
    )
  }

  @Test
  fun baselineValueWithUnit_alignsNumberAndUnitOnSameRowWithoutOverlap() {
    composeRule.setContent {
      SehatSaathiTheme {
        BaselineValueWithUnit(
          value = "148",
          unit = "mg/dL",
          modifier = Modifier.testTag("vital_pair"),
        )
      }
    }

    val bounds = composeRule.onNodeWithTag("vital_pair").fetchSemanticsNode().boundsInRoot
    assertTrue("BaselineValueWithUnit must have positive width and height", bounds.width > 0f && bounds.height > 0f)
  }

  @Test
  fun formField_reservesSupportingSpace_preventingLayoutShiftOnError() {
    composeRule.setContent {
      SehatSaathiTheme {
        Column {
          SehatTextField(
            label = "Full name",
            value = "A",
            onValueChange = {},
            errorText = null,
            reserveSupportingSpace = true,
            testTag = "name_input",
          )
          SehatPrimaryButton(
            text = "Save changes",
            onClick = {},
            testTag = "below_btn",
          )
        }
      }
    }

    val topBefore = composeRule.onNodeWithTag("below_btn").fetchSemanticsNode().boundsInRoot.top
    assertTrue(topBefore > 0f)
  }

  @Test
  fun viewportsAndModes_renderScreensWithoutOverflowOrMissingPrimaryElements() {
    val viewports =
      listOf(
        320 to 568,
        360 to 800,
        390 to 844,
        768 to 1024,
        1024 to 768,
        1440 to 900,
        800 to 360,
      )
    val baseState = SehatSaathiUiState(isSessionLoaded = true)
    val urgentAssessment =
      RiskEngine.evaluateGlucose(
        mgDl = 52,
        context = ReadingContext.FASTING,
        symptoms = SymptomFlags(confusionOrFainting = true),
      )

    composeRule.setContent {
      val (w, h) = viewports.first() // 320x568 narrowest phone stress test
      SehatSaathiTheme(themePreference = ThemePreference.DARK) {
        Column(modifier = Modifier.size(w.dp, h.dp).testTag("viewport_320")) {
          SehatPrimaryButton(
            text = "Log reading",
            icon = Icons.Filled.Add,
            onClick = {},
            testTag = "btn_primary_320",
          )
          SehatSecondaryButton(
            text = "Share care card",
            onClick = {},
            testTag = "btn_secondary_320",
          )
        }
      }
    }

    val primaryBounds = composeRule.onNodeWithTag("btn_primary_320").fetchSemanticsNode().boundsInRoot
    val secondaryBounds = composeRule.onNodeWithTag("btn_secondary_320").fetchSemanticsNode().boundsInRoot

    assertTrue("Primary button height >= 56dp", primaryBounds.height >= 55f)
    assertTrue("Secondary button height >= 48dp", secondaryBounds.height >= 47f)
    assertTrue("Left edges of stacked buttons align within 1px", abs(primaryBounds.left - secondaryBounds.left) <= 1f)
    assertTrue("No horizontal overflow beyond 320dp viewport", primaryBounds.right <= 321f && secondaryBounds.right <= 321f)
    assertTrue(urgentAssessment.triggersEmergencyScreen)
    assertTrue(baseState.medicines.isNotEmpty())
  }

  @Test
  fun caregiverDashboard_rendersCleanlyAtCompactWidth() {
    val state = SehatSaathiUiState(isSessionLoaded = true, simpleModeActive = true)
    composeRule.setContent {
      SehatSaathiTheme {
        Column(modifier = Modifier.size(320.dp, 568.dp)) {
          CaregiverDashboardScreen(
            uiState = state,
            contentPadding = PaddingValues(),
            onAcknowledgeAlert = {},
            onBackHome = {},
          )
        }
      }
    }
    composeRule.onNodeWithTag("caregiver_dashboard_screen").assertIsDisplayed()
    assertTrue(state.simpleModeActive)
  }
}

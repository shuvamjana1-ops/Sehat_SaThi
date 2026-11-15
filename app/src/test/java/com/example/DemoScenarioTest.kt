package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.HealthStatusLevel
import com.example.data.RefillStatus
import com.example.domain.demo.DemoScenario
import com.example.domain.demo.GuidedTourCatalog
import com.example.ui.SehatSaathiViewModel
import com.example.ui.components.AppRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DemoScenarioTest {

  private fun createViewModel(): SehatSaathiViewModel {
    val app = ApplicationProvider.getApplicationContext<Application>()
    return SehatSaathiViewModel(app).also {
      shadowOf(android.os.Looper.getMainLooper()).idle()
    }
  }

  @Test
  fun `low sugar at 2 AM scenario logs 55 mgdL, opens emergency screen, and creates Demo alert`() {
    val vm = createViewModel()
    vm.runDemoScenario(DemoScenario.LOW_SUGAR_2AM)

    val state = vm.uiState.value
    assertEquals(DemoScenario.LOW_SUGAR_2AM, state.activeDemoScenario)
    assertTrue(state.showEmergencyScreen)
    assertNotNull(state.emergencyAssessment)
    assertEquals(55, state.readings.last().glucoseMgDl)
    assertTrue(state.alerts.first().isDemo)
    assertTrue(state.alerts.first().title.contains("Demo"))
    assertEquals(AppRoutes.LOG_READING, state.pendingDemoNavigationRoute)

    vm.resetDemoData()
  }

  @Test
  fun `high blood pressure scenario logs 185 over 125 and triggers crisis guidance`() {
    val vm = createViewModel()
    vm.runDemoScenario(DemoScenario.HIGH_BLOOD_PRESSURE)

    val state = vm.uiState.value
    assertEquals(DemoScenario.HIGH_BLOOD_PRESSURE, state.activeDemoScenario)
    assertFalse(state.showEmergencyScreen)
    assertEquals(185, state.readings.last().systolic)
    assertEquals(125, state.readings.last().diastolic)
    assertEquals(HealthStatusLevel.URGENT, state.latestAssessment?.statusLevel)
    assertTrue(state.alerts.first().isDemo)
    assertEquals(AppRoutes.LOG_READING, state.pendingDemoNavigationRoute)

    vm.resetDemoData()
  }

  @Test
  fun `scan sample prescription scenario loads fixture image and flagged extracted items`() {
    val vm = createViewModel()
    vm.runDemoScenario(DemoScenario.SCAN_SAMPLE_RX)

    val state = vm.uiState.value
    assertEquals(DemoScenario.SCAN_SAMPLE_RX, state.activeDemoScenario)
    assertEquals(1, state.rxScanner.step)
    assertTrue(state.rxScanner.isSampleResult)
    assertNotNull(state.rxScanner.capturedBitmap)
    assertEquals(3, state.rxScanner.extractedMedicines.size)
    assertTrue(state.rxScanner.extractedMedicines.any { it.needsVerification })
    assertEquals(AppRoutes.SCAN_RX, state.pendingDemoNavigationRoute)

    vm.resetDemoData()
  }

  @Test
  fun `medicine running out scenario sets Glycomet to 3 days left and navigates to medicines`() {
    val vm = createViewModel()
    vm.runDemoScenario(DemoScenario.MEDICINE_RUNNING_OUT)

    val state = vm.uiState.value
    val glycomet = state.medicines.firstOrNull { it.id == "med-metformin" }
    assertNotNull(glycomet)
    assertEquals(3, glycomet?.daysLeft)
    assertEquals(RefillStatus.SOON, glycomet?.refillStatus)
    assertEquals(AppRoutes.MEDICINES, state.pendingDemoNavigationRoute)

    vm.resetDemoData()
  }

  @Test
  fun `missed dose scenario sets first dose pending, activates escalation, and adds calm Demo alert`() {
    val vm = createViewModel()
    vm.runDemoScenario(DemoScenario.MISSED_DOSE)

    val state = vm.uiState.value
    assertFalse(state.dosesToday.first().taken)
    assertTrue(state.escalationDemo.active)
    assertTrue(state.alerts.first().isDemo)
    assertTrue(state.alerts.first().message.contains("Ask your doctor or pharmacist."))
    assertEquals(AppRoutes.FAMILY, state.pendingDemoNavigationRoute)

    vm.resetDemoData()
  }

  @Test
  fun `reset demo clears active scenario, caption, pending route, and restores readings`() {
    val vm = createViewModel()
    val initialReadingCount = vm.uiState.value.readings.size
    vm.runDemoScenario(DemoScenario.LOW_SUGAR_2AM)
    assertEquals(initialReadingCount + 1, vm.uiState.value.readings.size)

    vm.resetDemoData()
    val resetState = vm.uiState.value
    assertNull(resetState.activeDemoScenario)
    assertNull(resetState.activeDemoScenarioCaption)
    assertNull(resetState.pendingDemoNavigationRoute)
    assertFalse(resetState.showEmergencyScreen)
    assertEquals(initialReadingCount, resetState.readings.size)
  }

  @Test
  fun `signed-in non-demo account never runs demo scenarios or replays tour`() {
    val vm = createViewModel()
    vm.dismissGuidedTour()
    val nonDemoSession =
      com.example.data.AuthSession(
        isAuthenticated = true,
        isDemoMode = false,
        displayName = "Asha Verma",
        email = "asha.verma@example.com",
      )
    assertFalse(SehatSaathiViewModel.canRunDemoSession(nonDemoSession))
    vm.setSessionForTest(nonDemoSession)

    val initialReadingCount = vm.uiState.value.readings.size
    vm.runDemoScenario(DemoScenario.LOW_SUGAR_2AM)
    vm.replayGuidedTour()

    val state = vm.uiState.value
    assertTrue(state.session.isAuthenticated)
    assertFalse(state.session.isDemoMode)
    assertNull(state.activeDemoScenario)
    assertFalse(state.showEmergencyScreen)
    assertEquals(initialReadingCount, state.readings.size)
    assertTrue(state.guidedTourDismissed)
  }

  @Test
  fun `guided tour catalog covers Scan, Log Reading, Refills, and Family in 4 steps`() {
    val steps = GuidedTourCatalog.steps
    assertEquals(4, steps.size)
    assertEquals(
      listOf(AppRoutes.SCAN_RX, AppRoutes.LOG_READING, AppRoutes.MEDICINES, AppRoutes.FAMILY),
      steps.map { it.targetRoute },
    )
  }
}

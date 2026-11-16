package com.example

import com.example.data.HealthStatusLevel
import com.example.data.SeededAlert
import com.example.data.SeededDoseEvent
import com.example.data.DaySlot
import com.example.data.SeededReading
import com.example.data.SeededMedicine
import com.example.domain.caregiver.ActionNeeded
import com.example.domain.caregiver.CaregiverDomain
import com.example.domain.caregiver.CaregiverPatientLink
import com.example.domain.caregiver.MissedDoseEscalationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaregiverDomainTest {

  @Test
  fun `caregiver cannot read unlinked or unconsented patient`() {
    val links = listOf(
      CaregiverPatientLink(
        caregiverId = "cg-1",
        patientId = "demo-patient-sunita",
        patientName = "Sunita Devi",
        patientAge = 67,
        conditions = listOf("Diabetes"),
        consentStatus = "Consented",
      ),
      CaregiverPatientLink(
        caregiverId = "cg-2",
        patientId = "demo-patient-sunita",
        patientName = "Sunita Devi",
        patientAge = 67,
        conditions = listOf("Diabetes"),
        consentStatus = "Pending",
      )
    )

    // Valid consented link
    assertTrue(CaregiverDomain.verifyCaregiverAccess("cg-1", "demo-patient-sunita", links))

    // Pending consent -> denied
    assertFalse(CaregiverDomain.verifyCaregiverAccess("cg-2", "demo-patient-sunita", links))

    // Completely unlinked caregiver -> denied
    assertFalse(CaregiverDomain.verifyCaregiverAccess("cg-99", "demo-patient-sunita", links))
  }

  @Test
  fun `missed dose escalation follows grace and escalation windows`() {
    val doses = listOf(
      SeededDoseEvent("d1", "m1", "Amaryl", "1 tab", DaySlot.MORNING, taken = false)
    )
    val config = MissedDoseEscalationConfig(
      graceWindowMinutes = 60,
      escalationWindowMinutes = 120,
      patientOptedIn = true,
    )

    // 30 mins elapsed -> no action needed
    val dec1 = CaregiverDomain.evaluateMissedDoseEscalation(doses, config, 30)
    assertEquals(ActionNeeded.NONE, dec1.action)

    // 75 mins elapsed -> nudge patient
    val dec2 = CaregiverDomain.evaluateMissedDoseEscalation(doses, config, 75)
    assertEquals(ActionNeeded.NUDGE_PATIENT, dec2.action)

    // 135 mins elapsed -> notify caregiver
    val dec3 = CaregiverDomain.evaluateMissedDoseEscalation(doses, config, 135)
    assertEquals(ActionNeeded.NOTIFY_CAREGIVER, dec3.action)
  }

  @Test
  fun `weekly digest correctly computes adherence and statistics`() {
    val readings = listOf(
      SeededReading("r1", "Day 1", 190, 145, 90, "High", HealthStatusLevel.CAUTION),
      SeededReading("r2", "Day 2", 62, 110, 70, "Low", HealthStatusLevel.URGENT),
    )
    val doses = listOf(
      SeededDoseEvent("d1", "m1", "Med 1", "1 tab", DaySlot.MORNING, taken = true),
      SeededDoseEvent("d2", "m2", "Med 2", "1 tab", DaySlot.AFTERNOON, taken = false),
    )
    val medicines = listOf(
      SeededMedicine("m1", "Amaryl", "Glimepiride", "1mg", "Tab", "OD", "Before", DaySlot.MORNING, 3, "tablets", 1, true, false, null, 3)
    )
    val alerts = listOf(
      SeededAlert("a1", "Alert", "Test", "1h", HealthStatusLevel.URGENT, true)
    )

    val digest = CaregiverDomain.generateWeeklyDigest(
      patientId = "demo-patient-sunita",
      patientName = "Sunita Devi",
      readings = readings,
      doses = doses,
      medicines = medicines,
      alerts = alerts,
      optedIn = true,
    )

    assertEquals(50, digest.adherencePercentage)
    assertEquals(1, digest.highReadingsCount)
    assertEquals(1, digest.lowReadingsCount)
    assertEquals(1, digest.refillGapsCount)
    assertEquals(1, digest.openAlertsCount)
    assertTrue(digest.generatedSummaryText.contains("Sunita Devi"))
  }
}

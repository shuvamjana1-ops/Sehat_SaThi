package com.example

import com.example.data.AuthSessionRepository
import com.example.data.DemoSeedData
import com.example.data.RefillStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun passwordValidation_enforcesLengthLettersNumbersAndBlocklist() {
    assertFalse(AuthSessionRepository.evaluatePassword("short1").isValid)
    assertFalse(AuthSessionRepository.evaluatePassword("onlyletterslong").isValid)
    assertFalse(AuthSessionRepository.evaluatePassword("1234567890").isValid)
    assertFalse(AuthSessionRepository.evaluatePassword("password123").isValid)
    assertTrue(AuthSessionRepository.evaluatePassword("SunitaCare2026").isValid)
  }

  @Test
  fun seededData_containsExpectedMedicinesReadingsAndRefillStatuses() {
    assertEquals(4, DemoSeedData.medicines.size)
    assertEquals(14, DemoSeedData.readings14Days.size)
    assertEquals(2, DemoSeedData.caregivers.size)

    val statuses = DemoSeedData.medicines.map { it.refillStatus }
    assertTrue(statuses.contains(RefillStatus.DUE))
    assertTrue(statuses.contains(RefillStatus.SOON))
    assertTrue(statuses.contains(RefillStatus.OK))
  }
}

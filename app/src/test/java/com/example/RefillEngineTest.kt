package com.example

import com.example.data.DemoSeedData
import com.example.data.RefillStatus
import com.example.domain.refill.RefillEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RefillEngineTest {

  @Test
  fun stockThresholdsClassifyOutDueSoonAndOk() {
    assertEquals(RefillStatus.OUT, RefillEngine.evaluateStatus(0, 1, isCritical = true))
    assertEquals(RefillStatus.DUE, RefillEngine.evaluateStatus(2, 1, isCritical = true))
    assertEquals(RefillStatus.SOON, RefillEngine.evaluateStatus(7, 1, isCritical = true))
    assertEquals(RefillStatus.OK, RefillEngine.evaluateStatus(10, 1, isCritical = true))

    // Non-critical medicine uses 5-day soon window
    assertEquals(RefillStatus.OK, RefillEngine.evaluateStatus(7, 1, isCritical = false))
    assertEquals(RefillStatus.SOON, RefillEngine.evaluateStatus(5, 1, isCritical = false))
  }

  @Test
  fun unknownStockNeverShownAsOkOrPlentiful() {
    assertEquals(
      RefillStatus.UNKNOWN,
      RefillEngine.evaluateStatus(stockCount = 30, dailyUnits = 1, isCritical = false, unknownStock = true),
    )
    assertEquals(
      RefillStatus.UNKNOWN,
      RefillEngine.evaluateStatus(stockCount = 10, dailyUnits = 0, isCritical = false, unknownStock = false),
    )
    assertNull(RefillEngine.computeDaysLeft(stockCount = 30, dailyUnits = 1, unknownStock = true))
  }

  @Test
  fun restockAdjustAndSnoozeProduceHistoryAndUpdateStock() {
    val med = DemoSeedData.medicines.first()
    val (restocked, restockEntry) = RefillEngine.restock(med, 15)
    assertEquals(med.stockCount + 15, restocked.stockCount)
    assertEquals(15, restockEntry.deltaUnits)

    val (markedUnknown, _) = RefillEngine.adjustStock(restocked, 10, markUnknown = true)
    assertEquals(RefillStatus.UNKNOWN, markedUnknown.refillStatus)

    val (snoozed, snoozeEntry) = RefillEngine.snoozeReminder(med, 2)
    assertEquals("Snoozed for 2 days", snoozed.snoozedUntilLabel)
    assertTrue(snoozeEntry.actionLabel.contains("Snoozed"))
  }

  @Test
  fun prescriptionEndingNoticesFlagWithin7Days() {
    val notices = RefillEngine.prescriptionEndingNotices(DemoSeedData.medicines)
    assertTrue(notices.any { it.contains("Amaryl 1 mg") && it.contains("5 day") })
  }
}

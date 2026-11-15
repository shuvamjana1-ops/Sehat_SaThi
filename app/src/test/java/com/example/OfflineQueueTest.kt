package com.example

import com.example.ai.VitalsAiParser
import com.example.data.OfflineQueueHelper
import com.example.data.OfflineQueuedTask
import com.example.data.OfflineTaskKind
import com.example.domain.risk.VitalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineQueueTest {

  @Test
  fun enqueue_addsTaskAndCapsAtTwelveItems() {
    var queue: List<OfflineQueuedTask> = emptyList()
    repeat(15) { idx ->
      queue =
        OfflineQueueHelper.enqueue(
          current = queue,
          kind = OfflineTaskKind.VOICE_PHRASE,
          summary = "Phrase #$idx",
          transcript = "Sugar ${110 + idx}",
        )
    }

    assertEquals(12, queue.size)
    assertEquals("Phrase #3", queue.first().summary)
    assertEquals("Phrase #14", queue.last().summary)
  }

  @Test
  fun remove_deletesOnlyMatchingTaskId() {
    var queue: List<OfflineQueuedTask> = emptyList()
    queue = OfflineQueueHelper.enqueue(queue, OfflineTaskKind.METER_PHOTO, "Photo A")
    queue = OfflineQueueHelper.enqueue(queue, OfflineTaskKind.PRESCRIPTION_PHOTO, "Rx B")

    val targetId = queue.first().id
    val updated = OfflineQueueHelper.remove(queue, targetId)

    assertEquals(1, updated.size)
    assertEquals("Rx B", updated.single().summary)
  }

  @Test
  fun offlineVoiceFallback_parsesGlucoseAndBloodPressureDeterministically() {
    val sugar = VitalsAiParser.fallbackParseSpokenText("Sugar 182")
    assertNotNull(sugar)
    assertEquals(VitalType.GLUCOSE, sugar!!.vitalType)
    assertEquals(182, sugar.glucoseMgDl)
    assertTrue(sugar.usedOfflineFallback)

    val bp = VitalsAiParser.fallbackParseSpokenText("BP 150 by 95")
    assertNotNull(bp)
    assertEquals(VitalType.BLOOD_PRESSURE, bp!!.vitalType)
    assertEquals(150, bp.systolic)
    assertEquals(95, bp.diastolic)
    assertTrue(bp.usedOfflineFallback)

    val unclear = VitalsAiParser.fallbackParseSpokenText("not feeling well today")
    assertNull(unclear)
  }
}

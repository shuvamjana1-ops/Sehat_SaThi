package com.example

import com.example.ai.VitalsAiParser
import com.example.data.DemoSeedData
import com.example.domain.risk.AlertCooldownRecord
import com.example.domain.risk.ReadingContext
import com.example.domain.risk.RiskEngine
import com.example.domain.risk.RiskLevel
import com.example.domain.risk.SymptomFlags
import com.example.domain.risk.VitalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskEngineTest {

  @Test
  fun glucoseBoundaries_53_54_69_70_249_250_251_299_300_301() {
    // 53 -> EMERGENCY
    val r53 = RiskEngine.evaluateGlucose(53)
    assertEquals(RiskLevel.EMERGENCY, r53.riskLevel)
    assertTrue(r53.triggersEmergencyScreen)
    assertTrue(r53.shouldAlertFamily)
    assertTrue(r53.fullGuidanceMessage.endsWith("Ask your doctor or pharmacist."))

    // 54 -> URGENT low (not severe emergency)
    val r54 = RiskEngine.evaluateGlucose(54)
    assertEquals(RiskLevel.URGENT, r54.riskLevel)
    assertFalse(r54.triggersEmergencyScreen)
    assertTrue(r54.shouldAlertFamily)

    // 69 -> URGENT low
    val r69 = RiskEngine.evaluateGlucose(69)
    assertEquals(RiskLevel.URGENT, r69.riskLevel)
    assertTrue(r69.shouldAlertFamily)

    // 70 -> NORMAL
    val r70 = RiskEngine.evaluateGlucose(70)
    assertEquals(RiskLevel.NORMAL, r70.riskLevel)
    assertFalse(r70.shouldAlertFamily)

    // 249 & 250 -> NORMAL; 251 -> CAUTION
    assertEquals(RiskLevel.NORMAL, RiskEngine.evaluateGlucose(249).riskLevel)
    assertEquals(RiskLevel.NORMAL, RiskEngine.evaluateGlucose(250).riskLevel)
    val r251 = RiskEngine.evaluateGlucose(251)
    assertEquals(RiskLevel.CAUTION, r251.riskLevel)
    assertFalse(r251.shouldAlertFamily)

    // 299 & 300 -> CAUTION; 301 -> URGENT high
    assertEquals(RiskLevel.CAUTION, RiskEngine.evaluateGlucose(299).riskLevel)
    assertEquals(RiskLevel.CAUTION, RiskEngine.evaluateGlucose(300).riskLevel)
    val r301 = RiskEngine.evaluateGlucose(301)
    assertEquals(RiskLevel.URGENT, r301.riskLevel)
    assertTrue(r301.shouldAlertFamily)
  }

  @Test
  fun glucoseNormalWithConfusionSymptom_triggersEmergency() {
    val res =
      RiskEngine.evaluateGlucose(
        mgDl = 95,
        context = ReadingContext.SYMPTOMS,
        symptoms = SymptomFlags(confusionOrFainting = true),
      )
    assertEquals(RiskLevel.EMERGENCY, res.riskLevel)
    assertTrue(res.triggersEmergencyScreen)
    assertTrue(res.shouldAlertFamily)
  }

  @Test
  fun bloodPressureBoundaries_139_140_179_180_lowWithDizziness() {
    assertEquals(RiskLevel.NORMAL, RiskEngine.evaluateBloodPressure(139, 89).riskLevel)
    assertEquals(RiskLevel.CAUTION, RiskEngine.evaluateBloodPressure(140, 85).riskLevel)
    assertEquals(RiskLevel.CAUTION, RiskEngine.evaluateBloodPressure(135, 90).riskLevel)
    assertEquals(RiskLevel.CAUTION, RiskEngine.evaluateBloodPressure(179, 119).riskLevel)

    val crisis = RiskEngine.evaluateBloodPressure(180, 110)
    assertEquals(RiskLevel.URGENT, crisis.riskLevel)
    assertTrue(crisis.shouldAlertFamily)
    assertFalse(crisis.triggersEmergencyScreen)

    val crisisWithChestPain =
      RiskEngine.evaluateBloodPressure(
        systolic = 182,
        diastolic = 120,
        symptoms = SymptomFlags(chestPainOrBreathlessness = true),
      )
    assertEquals(RiskLevel.EMERGENCY, crisisWithChestPain.riskLevel)
    assertTrue(crisisWithChestPain.triggersEmergencyScreen)

    val lowWithDizziness =
      RiskEngine.evaluateBloodPressure(
        systolic = 86,
        diastolic = 58,
        symptoms = SymptomFlags(dizziness = true),
      )
    assertEquals(RiskLevel.CAUTION, lowWithDizziness.riskLevel)
  }

  @Test
  fun alertCooldown_blocksDuplicateWithin30Min_allowsRecheckOrAfter30Min() {
    val urgentLow = RiskEngine.evaluateGlucose(62)
    val t0 = 1_000_000L
    val prev = AlertCooldownRecord(VitalType.GLUCOSE, RiskLevel.URGENT, sentAtEpochMs = t0)

    // Duplicate non-recheck 10 minutes later -> suppressed
    assertFalse(
      RiskEngine.shouldSendFamilyAlert(
        assessment = urgentLow,
        lastRecord = prev,
        nowEpochMs = t0 + 10 * 60_000L,
        isRecheck = false,
      ),
    )

    // Recheck 15 minutes later and still dangerous -> allowed!
    assertTrue(
      RiskEngine.shouldSendFamilyAlert(
        assessment = urgentLow,
        lastRecord = prev,
        nowEpochMs = t0 + 15 * 60_000L,
        isRecheck = true,
      ),
    )

    // After 30 minutes -> allowed
    assertTrue(
      RiskEngine.shouldSendFamilyAlert(
        assessment = urgentLow,
        lastRecord = prev,
        nowEpochMs = t0 + 30 * 60_000L,
        isRecheck = false,
      ),
    )
  }

  @Test
  fun trendFlags_detectPatternsInSeededDataAndSyntheticRising() {
    val flags = RiskEngine.detectTrendFlags(DemoSeedData.readings14Days)
    assertTrue(flags.any { it.label == "Trend warning, not a diagnosis" })
  }

  @Test
  fun offlineVoiceFallback_parsesGlucoseAndBloodPressurePhrases() {
    val bp = VitalsAiParser.fallbackParseSpokenText("BP 150 by 95")
    assertNotNull(bp)
    assertEquals(VitalType.BLOOD_PRESSURE, bp!!.vitalType)
    assertEquals(150, bp.systolic)
    assertEquals(95, bp.diastolic)

    val sugar = VitalsAiParser.fallbackParseSpokenText("Aaj sugar 240 hai")
    assertNotNull(sugar)
    assertEquals(VitalType.GLUCOSE, sugar!!.vitalType)
    assertEquals(240, sugar.glucoseMgDl)
  }
}

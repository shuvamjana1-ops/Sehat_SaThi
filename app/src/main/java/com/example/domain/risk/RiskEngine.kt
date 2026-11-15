package com.example.domain.risk

import com.example.data.HealthStatusLevel
import com.example.data.SeededReading

enum class VitalType {
  GLUCOSE,
  BLOOD_PRESSURE,
}

enum class ReadingContext(val label: String) {
  FASTING("Fasting"),
  AFTER_MEAL("After meal"),
  BEDTIME("Bedtime"),
  SYMPTOMS("With symptoms"),
}

enum class RiskLevel(val label: String) {
  NORMAL("Normal"),
  CAUTION("Caution"),
  URGENT("Urgent"),
  EMERGENCY("Emergency"),
}

data class SymptomFlags(
  val confusionOrFainting: Boolean = false,
  val isUnconscious: Boolean = false,
  val dizziness: Boolean = false,
  val chestPainOrBreathlessness: Boolean = false,
  val severeHeadacheOrWeakness: Boolean = false,
) {
  val hasSevereNeuroSymptoms: Boolean
    get() = confusionOrFainting || isUnconscious

  val hasBpCrisisEmergencySymptoms: Boolean
    get() = chestPainOrBreathlessness || severeHeadacheOrWeakness || confusionOrFainting || isUnconscious
}

data class RiskAssessment(
  val vitalType: VitalType,
  val riskLevel: RiskLevel,
  val statusLevel: HealthStatusLevel,
  val headline: String,
  val guidanceSteps: List<String>,
  val fullGuidanceMessage: String,
  val shouldAlertFamily: Boolean,
  val triggersEmergencyScreen: Boolean,
  val show15MinRecheckReminder: Boolean = false,
  val show5MinRecheckReminder: Boolean = false,
  val whatsappSummarySnippet: String,
)

data class AlertCooldownRecord(
  val vitalType: VitalType,
  val riskLevel: RiskLevel,
  val sentAtEpochMs: Long,
)

data class TrendFlag(
  val title: String,
  val detail: String,
  val label: String = "Trend warning, not a diagnosis",
)

object RiskEngine {

  private const val MANDATORY_SUFFIX = "Ask your doctor or pharmacist."

  fun evaluateGlucose(
    mgDl: Int,
    context: ReadingContext = ReadingContext.FASTING,
    symptoms: SymptomFlags = SymptomFlags(),
    config: RiskThresholdsConfig = RiskThresholdsConfig(),
    patientName: String = "Sunita Devi",
    timeLabel: String = "now",
  ): RiskAssessment {
    return when {
      mgDl < config.glucoseSevereLowMgDl || symptoms.hasSevereNeuroSymptoms -> {
        val steps =
          listOf(
            "Do not give food or drink if the person is unconscious.",
            "If awake and able to swallow safely, take 15 g of fast sugar (3 glucose tablets or 3 teaspoons of sugar in water).",
            "Call family and call 112 right away.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.GLUCOSE,
          riskLevel = RiskLevel.EMERGENCY,
          statusLevel = HealthStatusLevel.URGENT,
          headline = "Severe low blood sugar or urgent symptoms ($mgDl mg/dL)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = true,
          triggersEmergencyScreen = true,
          show15MinRecheckReminder = true,
          whatsappSummarySnippet =
            "URGENT Alert: $patientName's blood sugar is $mgDl mg/dL (severe low / symptoms) at $timeLabel. Please call her or 112 right now.",
        )
      }

      mgDl < config.glucoseLowMgDl -> {
        val steps =
          listOf(
            "If you are awake and can swallow, take 15 g of fast sugar (for example 3 glucose tablets, or 3 teaspoons of sugar in water).",
            "Recheck in 15 minutes.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.GLUCOSE,
          riskLevel = RiskLevel.URGENT,
          statusLevel = HealthStatusLevel.URGENT,
          headline = "Urgent low blood sugar ($mgDl mg/dL)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = true,
          triggersEmergencyScreen = false,
          show15MinRecheckReminder = true,
          whatsappSummarySnippet =
            "Alert: $patientName's blood sugar is $mgDl mg/dL (low) at $timeLabel. Please call her now.",
        )
      }

      mgDl > config.glucoseHighUrgentMgDl -> {
        val steps =
          listOf(
            "Drink plain water to stay hydrated.",
            "Follow your doctor's written sick-day or high-sugar plan.",
            "Contact your doctor promptly.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.GLUCOSE,
          riskLevel = RiskLevel.URGENT,
          statusLevel = HealthStatusLevel.URGENT,
          headline = "Urgent high blood sugar ($mgDl mg/dL)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = true,
          triggersEmergencyScreen = false,
          whatsappSummarySnippet =
            "Alert: $patientName's blood sugar is $mgDl mg/dL (very high) at $timeLabel. Please check in with her now.",
        )
      }

      mgDl > config.glucoseHighCautionMgDl -> {
        val steps =
          listOf(
            "Drink plain water and rest calmly.",
            "Follow your doctor's written plan and recheck later today.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.GLUCOSE,
          riskLevel = RiskLevel.CAUTION,
          statusLevel = HealthStatusLevel.CAUTION,
          headline = "High blood sugar ($mgDl mg/dL · ${context.label})",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = false,
          triggersEmergencyScreen = false,
          whatsappSummarySnippet =
            "Notice: $patientName's blood sugar is $mgDl mg/dL (${context.label}) at $timeLabel.",
        )
      }

      else -> {
        val steps =
          listOf(
            "Your reading ($mgDl mg/dL · ${context.label}) is outside urgent danger thresholds.",
            "Continue following your regular schedule.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.GLUCOSE,
          riskLevel = RiskLevel.NORMAL,
          statusLevel = HealthStatusLevel.NORMAL,
          headline = "Blood sugar in safe range ($mgDl mg/dL)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = false,
          triggersEmergencyScreen = false,
          whatsappSummarySnippet =
            "$patientName logged a blood sugar reading of $mgDl mg/dL (${context.label}).",
        )
      }
    }
  }

  fun evaluateBloodPressure(
    systolic: Int,
    diastolic: Int,
    context: ReadingContext = ReadingContext.FASTING,
    symptoms: SymptomFlags = SymptomFlags(),
    config: RiskThresholdsConfig = RiskThresholdsConfig(),
    patientName: String = "Sunita Devi",
    timeLabel: String = "now",
  ): RiskAssessment {
    val isCrisis =
      systolic >= config.bpCrisisSystolic || diastolic >= config.bpCrisisDiastolic
    val isElevated =
      systolic >= config.bpElevatedSystolic || diastolic >= config.bpElevatedDiastolic
    val isLow =
      systolic < config.bpLowSystolic || diastolic < config.bpLowDiastolic

    return when {
      isCrisis && symptoms.hasBpCrisisEmergencySymptoms -> {
        val steps =
          listOf(
            "Blood pressure is $systolic/$diastolic mmHg with warning symptoms.",
            "Sit or lie down calmly and call 112 and your family right away.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.BLOOD_PRESSURE,
          riskLevel = RiskLevel.EMERGENCY,
          statusLevel = HealthStatusLevel.URGENT,
          headline = "Blood pressure crisis with symptoms ($systolic/$diastolic mmHg)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = true,
          triggersEmergencyScreen = true,
          show5MinRecheckReminder = true,
          whatsappSummarySnippet =
            "URGENT Alert: $patientName's BP is $systolic/$diastolic mmHg with warning symptoms at $timeLabel. Please call her or 112 now.",
        )
      }

      isCrisis -> {
        val steps =
          listOf(
            "Sit quietly and rest for 5 minutes, then recheck.",
            "If you have chest pain, breathlessness, severe headache, or weakness, call 112 immediately.",
            "Do not change any medicine dose on your own.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.BLOOD_PRESSURE,
          riskLevel = RiskLevel.URGENT,
          statusLevel = HealthStatusLevel.URGENT,
          headline = "Blood pressure crisis range ($systolic/$diastolic mmHg)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = true,
          triggersEmergencyScreen = false,
          show5MinRecheckReminder = true,
          whatsappSummarySnippet =
            "Alert: $patientName's blood pressure is $systolic/$diastolic mmHg (crisis range) at $timeLabel. Please call her now.",
        )
      }

      isLow && symptoms.dizziness -> {
        val steps =
          listOf(
            "Sit or lie down safely right away so you do not fall.",
            "Drink plain water and contact your doctor.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.BLOOD_PRESSURE,
          riskLevel = RiskLevel.CAUTION,
          statusLevel = HealthStatusLevel.CAUTION,
          headline = "Low blood pressure with dizziness ($systolic/$diastolic mmHg)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = false,
          triggersEmergencyScreen = false,
          whatsappSummarySnippet =
            "Notice: $patientName's BP is $systolic/$diastolic mmHg with dizziness at $timeLabel.",
        )
      }

      isElevated -> {
        val steps =
          listOf(
            "Sit and rest quietly, then recheck in 5 to 10 minutes.",
            "Keep a record of this reading for your doctor visit.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.BLOOD_PRESSURE,
          riskLevel = RiskLevel.CAUTION,
          statusLevel = HealthStatusLevel.CAUTION,
          headline = "Elevated blood pressure ($systolic/$diastolic mmHg)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = false,
          triggersEmergencyScreen = false,
          whatsappSummarySnippet =
            "Notice: $patientName's blood pressure is $systolic/$diastolic mmHg (${context.label}).",
        )
      }

      else -> {
        val steps =
          listOf(
            "Blood pressure ($systolic/$diastolic mmHg) is within the standard safe range.",
            "Keep following your daily routine.",
            MANDATORY_SUFFIX,
          )
        RiskAssessment(
          vitalType = VitalType.BLOOD_PRESSURE,
          riskLevel = RiskLevel.NORMAL,
          statusLevel = HealthStatusLevel.NORMAL,
          headline = "Blood pressure in safe range ($systolic/$diastolic mmHg)",
          guidanceSteps = steps,
          fullGuidanceMessage = steps.joinToString(" "),
          shouldAlertFamily = false,
          triggersEmergencyScreen = false,
          whatsappSummarySnippet =
            "$patientName logged a blood pressure reading of $systolic/$diastolic mmHg.",
        )
      }
    }
  }

  /**
   * Cooldown suppresses duplicate non-recheck family alerts within 30 minutes.
   * Re-alerts if it is a recheck and still dangerous, or if severity escalated to EMERGENCY.
   */
  fun shouldSendFamilyAlert(
    assessment: RiskAssessment,
    lastRecord: AlertCooldownRecord?,
    nowEpochMs: Long,
    isRecheck: Boolean = false,
    config: RiskThresholdsConfig = RiskThresholdsConfig(),
  ): Boolean {
    if (!assessment.shouldAlertFamily) return false
    if (lastRecord == null) return true
    val elapsedMs = nowEpochMs - lastRecord.sentAtEpochMs
    val cooldownMs = config.alertCooldownMinutes * 60_000L
    if (elapsedMs >= cooldownMs) return true
    if (assessment.riskLevel == RiskLevel.EMERGENCY && lastRecord.riskLevel != RiskLevel.EMERGENCY) {
      return true
    }
    if (isRecheck && (assessment.riskLevel == RiskLevel.URGENT || assessment.riskLevel == RiskLevel.EMERGENCY)) {
      return true
    }
    if (lastRecord.vitalType != assessment.vitalType) return true
    return false
  }

  /**
   * Rule-based pattern detection over recent readings.
   * Always labelled "Trend warning, not a diagnosis".
   */
  fun detectTrendFlags(readings: List<SeededReading>): List<TrendFlag> {
    if (readings.size < 3) return emptyList()
    val flags = mutableListOf<TrendFlag>()

    // 1. Three rising glucose readings in a row
    val last3 = readings.takeLast(3).map { it.glucoseMgDl }
    if (last3[0] < last3[1] && last3[1] < last3[2]) {
      flags +=
        TrendFlag(
          title = "Three rising sugar readings in a row",
          detail = "Recent sugar went ${last3[0]} → ${last3[1]} → ${last3[2]} mg/dL. Ask your doctor or pharmacist.",
        )
    }

    // 2. Repeated lows (< 70) at the same context
    val lowGroups =
      readings.filter { it.glucoseMgDl < 70 }.groupBy { it.contextLabel.lowercase() }
    lowGroups.entries.firstOrNull { it.value.size >= 2 }?.let { (ctx, items) ->
      flags +=
        TrendFlag(
          title = "Repeated low sugar ($ctx)",
          detail = "${items.size} readings below 70 mg/dL recorded during $ctx. Ask your doctor or pharmacist.",
        )
    }

    // 3. Large variability over last 7 readings
    val recent7 = readings.takeLast(7).map { it.glucoseMgDl }
    val spread = (recent7.maxOrNull() ?: 0) - (recent7.minOrNull() ?: 0)
    if (spread >= 120) {
      flags +=
        TrendFlag(
          title = "Large sugar swings over the last 7 days",
          detail = "Readings ranged by $spread mg/dL (${recent7.minOrNull()} to ${recent7.maxOrNull()}). Ask your doctor or pharmacist.",
        )
    }

    // 4. Readings above range (> 180) on several of the last 5 readings
    val recent5High = readings.takeLast(5).count { it.glucoseMgDl > 180 }
    if (recent5High >= 3) {
      flags +=
        TrendFlag(
          title = "Several recent readings above 180 mg/dL",
          detail = "$recent5High of the last 5 readings were above 180 mg/dL. Ask your doctor or pharmacist.",
        )
    }

    return flags
  }
}

package com.example.domain.risk

import com.example.data.HealthStatusLevel
import com.example.data.SeededReading

enum class VitalType(val label: String) {
  GLUCOSE("Blood Sugar"),
  BLOOD_PRESSURE("Blood Pressure"),
}

enum class ReadingContext(val label: String) {
  FASTING("Fasting"),
  BEFORE_MEAL("Before meal"),
  AFTER_MEAL("After meal"),
  BEDTIME("Bedtime"),
  FEELING_UNWELL("Feeling unwell"),
  SYMPTOMS("Feeling unwell"),
}

data class SymptomFlags(
  val dizziness: Boolean = false,
  val confusionOrFainting: Boolean = false,
  val chestPainOrBreathlessness: Boolean = false,
  val severeHeadacheOrWeakness: Boolean = false,
) {
  val hasSevereEmergencySymptoms: Boolean
    get() = confusionOrFainting || chestPainOrBreathlessness || severeHeadacheOrWeakness

  val anySelected: Boolean
    get() = dizziness || hasSevereEmergencySymptoms
}

enum class RiskLevel(val label: String) {
  NORMAL("Normal"),
  CAUTION("Caution"),
  URGENT("Urgent"),
  EMERGENCY("Emergency"),
}

data class VitalValidationResult(
  val isValid: Boolean,
  val glucoseMgDl: Int? = null,
  val systolic: Int? = null,
  val diastolic: Int? = null,
  val errorMessage: String? = null,
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

data class RiskAssessment(
  val vitalType: VitalType,
  val displayReading: String,
  val contextLabel: String,
  val riskLevel: RiskLevel,
  val statusLevel: HealthStatusLevel,
  val headline: String,
  val guidanceSteps: List<String>,
  val whyThisResultRule: String = "",
  val shouldAlertFamily: Boolean,
  val triggersEmergencyScreen: Boolean,
  val show15MinRecheckReminder: Boolean = false,
  val show5MinRecheckReminder: Boolean = false,
  val whatsappSummarySnippet: String,
) {
  val triggerEmergencyMode: Boolean
    get() = triggersEmergencyScreen

  val fullGuidanceMessage: String
    get() = (guidanceSteps + listOf("Ask your doctor or pharmacist.")).joinToString(" ")
}

/**
 * Deterministic vital-sign risk engine.
 *
 * SAFETY INVARIANT:
 * - Never calls AI/Gemini to classify danger.
 * - Never gives dosing advice or diagnoses.
 * - Never classifies invalid, negative, zero, or out-of-range inputs as Normal/Safe.
 * - Every guidance message ends with "Ask your doctor or pharmacist."
 */
object RiskEngine {

  const val MIN_VALID_GLUCOSE_MG_DL = 20
  const val MAX_VALID_GLUCOSE_MG_DL = 600
  const val MIN_VALID_SYSTOLIC_MM_HG = 40
  const val MAX_VALID_SYSTOLIC_MM_HG = 300
  const val MIN_VALID_DIASTOLIC_MM_HG = 20
  const val MAX_VALID_DIASTOLIC_MM_HG = 200

  fun validateGlucoseInput(rawInput: String): VitalValidationResult {
    val trimmed = rawInput.trim()
    if (trimmed.isEmpty()) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Please enter a blood sugar number between $MIN_VALID_GLUCOSE_MG_DL and $MAX_VALID_GLUCOSE_MG_DL mg/dL.",
      )
    }
    if (trimmed.startsWith("-")) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Blood sugar cannot be negative. Enter a whole number between $MIN_VALID_GLUCOSE_MG_DL and $MAX_VALID_GLUCOSE_MG_DL mg/dL.",
      )
    }
    if (trimmed.contains(".")) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Please enter a whole number in mg/dL without decimals (between $MIN_VALID_GLUCOSE_MG_DL and $MAX_VALID_GLUCOSE_MG_DL mg/dL).",
      )
    }
    val parsed = trimmed.toIntOrNull()
      ?: return VitalValidationResult(
        isValid = false,
        errorMessage = "Invalid blood sugar input. Please enter digits only ($MIN_VALID_GLUCOSE_MG_DL to $MAX_VALID_GLUCOSE_MG_DL mg/dL).",
      )
    if (parsed !in MIN_VALID_GLUCOSE_MG_DL..MAX_VALID_GLUCOSE_MG_DL) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Enter a valid blood sugar between $MIN_VALID_GLUCOSE_MG_DL and $MAX_VALID_GLUCOSE_MG_DL mg/dL.",
      )
    }
    return VitalValidationResult(isValid = true, glucoseMgDl = parsed)
  }

  fun validateBloodPressureInput(rawSystolic: String, rawDiastolic: String): VitalValidationResult {
    val sysTrimmed = rawSystolic.trim()
    val diaTrimmed = rawDiastolic.trim()
    if (sysTrimmed.isEmpty() || diaTrimmed.isEmpty()) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Please enter both upper (systolic) and lower (diastolic) blood pressure numbers.",
      )
    }
    if (sysTrimmed.startsWith("-") || diaTrimmed.startsWith("-")) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Blood pressure numbers cannot be negative. Please enter positive whole numbers.",
      )
    }
    if (sysTrimmed.contains(".") || diaTrimmed.contains(".")) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Please enter whole blood pressure numbers in mmHg without decimals.",
      )
    }
    val sys = sysTrimmed.toIntOrNull()
    val dia = diaTrimmed.toIntOrNull()
    if (sys == null || dia == null) {
      return VitalValidationResult(
        isValid = false,
        errorMessage = "Invalid blood pressure input. Please enter digits only for both systolic and diastolic.",
      )
    }
    if (sys !in MIN_VALID_SYSTOLIC_MM_HG..MAX_VALID_SYSTOLIC_MM_HG ||
      dia !in MIN_VALID_DIASTOLIC_MM_HG..MAX_VALID_DIASTOLIC_MM_HG ||
      sys <= dia
    ) {
      return VitalValidationResult(
        isValid = false,
        errorMessage =
          "Enter valid blood pressure numbers (Upper $MIN_VALID_SYSTOLIC_MM_HG–$MAX_VALID_SYSTOLIC_MM_HG, Lower $MIN_VALID_DIASTOLIC_MM_HG–$MAX_VALID_DIASTOLIC_MM_HG, with Upper higher than Lower).",
      )
    }
    return VitalValidationResult(isValid = true, systolic = sys, diastolic = dia)
  }

  fun evaluateGlucose(
    mgDl: Int,
    context: ReadingContext = ReadingContext.FASTING,
    symptoms: SymptomFlags = SymptomFlags(),
    is15MinRecheck: Boolean = false,
    config: RiskThresholdsConfig = RiskThresholdsConfig(),
    patientName: String = "Sunita Devi",
    timeLabel: String = "Just now",
  ): RiskAssessment {
    val display = "$mgDl mg/dL"

    // Out-of-bounds safety guard: never classify invalid/extreme values as Normal/Safe.
    if (mgDl !in MIN_VALID_GLUCOSE_MG_DL..MAX_VALID_GLUCOSE_MG_DL) {
      return RiskAssessment(
        vitalType = VitalType.GLUCOSE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.CAUTION,
        statusLevel = HealthStatusLevel.CAUTION,
        headline = "Invalid or out-of-range reading ($display). Please verify",
        guidanceSteps =
          listOf(
            "This number is outside the valid glucometer range ($MIN_VALID_GLUCOSE_MG_DL–$MAX_VALID_GLUCOSE_MG_DL mg/dL) and cannot be marked safe.",
            "Re-check the glucometer screen or repeat the finger-prick test carefully.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Input range guard fired because $mgDl mg/dL is outside valid range ($MIN_VALID_GLUCOSE_MG_DL–$MAX_VALID_GLUCOSE_MG_DL mg/dL).",
        shouldAlertFamily = false,
        triggersEmergencyScreen = false,
        whatsappSummarySnippet =
          "Note: $patientName entered an out-of-range blood sugar value ($display) at $timeLabel that needs verification.",
      )
    }

    // 1. Severe hypoglycemia (< 54 mg/dL) OR low (< 70) with confusion/fainting or still < 70 on 15-min recheck
    if (mgDl < config.glucoseSevereLowMgDl ||
      (mgDl < config.glucoseLowMgDl && (symptoms.hasSevereEmergencySymptoms || is15MinRecheck))
    ) {
      val ruleReason =
        when {
          mgDl < config.glucoseSevereLowMgDl ->
            "Rule: Blood sugar $mgDl mg/dL < ${config.glucoseSevereLowMgDl} mg/dL (critical low threshold)."
          is15MinRecheck ->
            "Rule: Blood sugar $mgDl mg/dL remained < ${config.glucoseLowMgDl} mg/dL on 15-minute recheck."
          else ->
            "Rule: Blood sugar $mgDl mg/dL < ${config.glucoseLowMgDl} mg/dL with severe emergency symptoms."
        }
      return RiskAssessment(
        vitalType = VitalType.GLUCOSE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.EMERGENCY,
        statusLevel = HealthStatusLevel.URGENT,
        headline = "Critical Low Blood Sugar ($display)",
        guidanceSteps =
          listOf(
            "Sit or lie down safely right now. Do not walk alone or drive.",
            "If awake and able to swallow safely, take fast-acting sugar as advised in your doctor's written low-sugar plan (such as glucose tablets, fruit juice, or sugar water).",
            "Do NOT take any diabetes medicine right now, and never give food or liquid to someone who is drowsy or unconscious.",
            "Call a family member or 112 immediately if you feel confused, faint, or if sugar stays below ${config.glucoseLowMgDl} mg/dL after 15 minutes.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule = ruleReason,
        shouldAlertFamily = true,
        triggersEmergencyScreen = true,
        show15MinRecheckReminder = true,
        whatsappSummarySnippet =
          "EMERGENCY ALERT: $patientName's blood sugar is critically low at $display (${context.label}, $timeLabel). Please call her immediately or dial 112.",
      )
    }

    // 1b. Severe emergency symptoms (confusion, fainting, chest pain, severe weakness) even with normal/elevated glucose
    if (symptoms.hasSevereEmergencySymptoms) {
      return RiskAssessment(
        vitalType = VitalType.GLUCOSE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.EMERGENCY,
        statusLevel = HealthStatusLevel.URGENT,
        headline = "Emergency Symptoms Reported ($display)",
        guidanceSteps =
          listOf(
            "Sit or lie down safely right now. Do not walk alone or drive.",
            "Because severe symptoms (confusion, fainting, chest pain, or sudden weakness) are present, call 112 or a family member immediately.",
            "Do NOT change or take extra medicine on your own.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Severe emergency symptoms reported alongside blood sugar $mgDl mg/dL -> Emergency escalation.",
        shouldAlertFamily = true,
        triggersEmergencyScreen = true,
        whatsappSummarySnippet =
          "EMERGENCY ALERT: $patientName reported severe emergency symptoms with blood sugar $display ($timeLabel). Please call her or 112 immediately.",
      )
    }

    // 2. Low blood sugar (54 - 69 mg/dL)
    if (mgDl < config.glucoseLowMgDl) {
      return RiskAssessment(
        vitalType = VitalType.GLUCOSE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.URGENT,
        statusLevel = HealthStatusLevel.URGENT,
        headline = "Low Blood Sugar ($display). Act Now",
        guidanceSteps =
          listOf(
            "Sit down and take fast-acting sugar right away as advised in your doctor's written low-sugar care plan (such as glucose tablets, fruit juice, or sugar water).",
            "Wait 15 minutes, rest quietly, and recheck your blood sugar.",
            "If the recheck is still below ${config.glucoseLowMgDl} mg/dL or you feel dizzy or confused, alert your family or call 112.",
            "Never change your diabetes medicine dose on your own.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Blood sugar $mgDl mg/dL is between ${config.glucoseSevereLowMgDl} and ${config.glucoseLowMgDl - 1} mg/dL (< ${config.glucoseLowMgDl} mg/dL low threshold).",
        shouldAlertFamily = true,
        triggersEmergencyScreen = false,
        show15MinRecheckReminder = true,
        whatsappSummarySnippet =
          "Urgent Alert: $patientName's blood sugar is low at $display (${context.label}, $timeLabel). She is resting and rechecking in 15 minutes. Please check on her.",
      )
    }

    // 3. Very high blood sugar (>= 300 mg/dL)
    if (mgDl >= config.glucoseHighUrgentMgDl) {
      return RiskAssessment(
        vitalType = VitalType.GLUCOSE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.URGENT,
        statusLevel = HealthStatusLevel.URGENT,
        headline = "Very High Blood Sugar ($display)",
        guidanceSteps =
          listOf(
            "Drink plain water if you are able to swallow comfortably.",
            "Do NOT take extra diabetes tablets or change your dose on your own.",
            "If you have nausea, vomiting, stomach pain, rapid breathing, or confusion, call 112 or go to the nearest clinic immediately.",
            "Contact your family and doctor today to review this reading.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Blood sugar $mgDl mg/dL >= ${config.glucoseHighUrgentMgDl} mg/dL (very high urgent threshold).",
        shouldAlertFamily = true,
        triggersEmergencyScreen = false,
        whatsappSummarySnippet =
          "Urgent Alert: $patientName's blood sugar is very high at $display (${context.label}, $timeLabel). Please call her and check with her doctor.",
      )
    }

    // 4. Elevated blood sugar (250 - 299 mg/dL)
    if (mgDl >= config.glucoseHighCautionMgDl) {
      return RiskAssessment(
        vitalType = VitalType.GLUCOSE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.CAUTION,
        statusLevel = HealthStatusLevel.CAUTION,
        headline = "High Blood Sugar ($display). Caution",
        guidanceSteps =
          listOf(
            "Sip plain water and avoid sugary drinks or sweets right now.",
            "Check whether you took your scheduled medicines as prescribed. Never double a dose.",
            "Recheck your blood sugar in a few hours and share this log at your next clinic visit.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Blood sugar $mgDl mg/dL is between ${config.glucoseHighCautionMgDl} and ${config.glucoseHighUrgentMgDl - 1} mg/dL (>= ${config.glucoseHighCautionMgDl} mg/dL caution threshold).",
        shouldAlertFamily = false,
        triggersEmergencyScreen = false,
        whatsappSummarySnippet =
          "Note: $patientName's blood sugar is $display (${context.label}, $timeLabel), marked Caution.",
      )
    }

    // 5. Normal / in everyday range (70 - 249 mg/dL)
    return RiskAssessment(
      vitalType = VitalType.GLUCOSE,
      displayReading = display,
      contextLabel = context.label,
      riskLevel = RiskLevel.NORMAL,
      statusLevel = HealthStatusLevel.NORMAL,
      headline = "Blood Sugar in Safe Range ($display)",
      guidanceSteps =
        listOf(
          "Your reading is within the everyday target range (${config.glucoseLowMgDl}–${config.glucoseHighCautionMgDl - 1} mg/dL).",
          "Continue your regular meals, water intake, and prescribed medicine schedule.",
          "Ask your doctor or pharmacist.",
        ),
      whyThisResultRule =
        "Rule: Blood sugar $mgDl mg/dL is within ${config.glucoseLowMgDl}–${config.glucoseHighCautionMgDl - 1} mg/dL normal target range.",
      shouldAlertFamily = false,
      triggersEmergencyScreen = false,
      whatsappSummarySnippet =
        "$patientName logged a blood sugar reading of $display (${context.label}, $timeLabel), Normal.",
    )
  }

  fun evaluateBloodPressure(
    systolic: Int,
    diastolic: Int,
    context: ReadingContext = ReadingContext.FASTING,
    symptoms: SymptomFlags = SymptomFlags(),
    is5MinRecheck: Boolean = false,
    config: RiskThresholdsConfig = RiskThresholdsConfig(),
    patientName: String = "Sunita Devi",
    timeLabel: String = "Just now",
  ): RiskAssessment {
    val display = "$systolic/$diastolic mmHg"

    // Out-of-bounds safety guard: never classify invalid/extreme values as Normal/Safe.
    if (systolic !in MIN_VALID_SYSTOLIC_MM_HG..MAX_VALID_SYSTOLIC_MM_HG ||
      diastolic !in MIN_VALID_DIASTOLIC_MM_HG..MAX_VALID_DIASTOLIC_MM_HG ||
      systolic <= diastolic
    ) {
      return RiskAssessment(
        vitalType = VitalType.BLOOD_PRESSURE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.CAUTION,
        statusLevel = HealthStatusLevel.CAUTION,
        headline = "Invalid or out-of-range blood pressure ($display). Please verify",
        guidanceSteps =
          listOf(
            "This blood pressure number is outside valid limits and cannot be marked safe.",
            "Re-check the cuff position and enter Upper ($MIN_VALID_SYSTOLIC_MM_HG–$MAX_VALID_SYSTOLIC_MM_HG) and Lower ($MIN_VALID_DIASTOLIC_MM_HG–$MAX_VALID_DIASTOLIC_MM_HG) numbers with Upper higher than Lower.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Input range guard fired because $display is outside valid BP bounds or systolic <= diastolic.",
        shouldAlertFamily = false,
        triggersEmergencyScreen = false,
        whatsappSummarySnippet =
          "Note: $patientName entered an out-of-range blood pressure value ($display) at $timeLabel that needs verification.",
      )
    }

    // 1. Hypertensive crisis (>= 180 or >= 120)
    if (systolic >= config.bpCrisisSystolic || diastolic >= config.bpCrisisDiastolic) {
      val isEmergency = symptoms.hasSevereEmergencySymptoms || is5MinRecheck
      val ruleReason =
        buildString {
          append("Rule: Blood pressure $display triggered urgent high threshold (")
          val parts = mutableListOf<String>()
          if (systolic >= config.bpCrisisSystolic) parts.add("systolic $systolic >= ${config.bpCrisisSystolic} mmHg")
          if (diastolic >= config.bpCrisisDiastolic) parts.add("diastolic $diastolic >= ${config.bpCrisisDiastolic} mmHg")
          append(parts.joinToString(" and "))
          if (isEmergency) append(", with emergency escalation")
          append(").")
        }
      return RiskAssessment(
        vitalType = VitalType.BLOOD_PRESSURE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = if (isEmergency) RiskLevel.EMERGENCY else RiskLevel.URGENT,
        statusLevel = HealthStatusLevel.URGENT,
        headline = "Very High Blood Pressure ($display)",
        guidanceSteps =
          listOf(
            "Sit quietly with your back supported and feet flat on the floor for 5 minutes, then recheck once.",
            "Do NOT take extra blood pressure tablets on your own.",
            "If you have chest pain, shortness of breath, severe headache, numbness, or trouble speaking, call 112 immediately.",
            "Even without symptoms, contact your doctor or clinic today if your repeat reading stays at or above ${config.bpCrisisSystolic}/${config.bpCrisisDiastolic} mmHg.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule = ruleReason,
        shouldAlertFamily = true,
        triggersEmergencyScreen = isEmergency,
        show5MinRecheckReminder = true,
        whatsappSummarySnippet =
          "URGENT ALERT: $patientName's blood pressure is very high at $display (${context.label}, $timeLabel). Please call her now.",
      )
    }

    // 1b. Severe emergency symptoms even below 180/120
    if (symptoms.hasSevereEmergencySymptoms) {
      return RiskAssessment(
        vitalType = VitalType.BLOOD_PRESSURE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.EMERGENCY,
        statusLevel = HealthStatusLevel.URGENT,
        headline = "Emergency Symptoms Reported ($display)",
        guidanceSteps =
          listOf(
            "Sit or lie down safely right now and rest quietly.",
            "Because severe symptoms (chest pain, breathlessness, confusion, or sudden weakness) are present, call 112 or a family member immediately.",
            "Do NOT take extra blood pressure tablets on your own.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule =
          "Rule: Severe emergency symptoms reported alongside blood pressure $display -> Emergency escalation.",
        shouldAlertFamily = true,
        triggersEmergencyScreen = true,
        show5MinRecheckReminder = true,
        whatsappSummarySnippet =
          "EMERGENCY ALERT: $patientName reported severe emergency symptoms with blood pressure $display ($timeLabel). Please call her or 112 immediately.",
      )
    }

    // 2. Low blood pressure (< 90 or < 60)
    if (systolic < config.bpLowSystolic || diastolic < config.bpLowDiastolic) {
      val hasDizziness = symptoms.dizziness
      val ruleReason =
        buildString {
          append("Rule: Blood pressure $display is below low threshold (")
          val parts = mutableListOf<String>()
          if (systolic < config.bpLowSystolic) parts.add("systolic $systolic < ${config.bpLowSystolic} mmHg")
          if (diastolic < config.bpLowDiastolic) parts.add("diastolic $diastolic < ${config.bpLowDiastolic} mmHg")
          append(parts.joinToString(" or "))
          append(if (hasDizziness) " WITH dizziness -> Caution + family alert)." else " -> Caution).")
        }
      return RiskAssessment(
        vitalType = VitalType.BLOOD_PRESSURE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.CAUTION,
        statusLevel = HealthStatusLevel.CAUTION,
        headline =
          if (hasDizziness) "Low Blood Pressure with Dizziness ($display)"
          else "Low Blood Pressure ($display). Caution",
        guidanceSteps =
          listOf(
            "Sit or lie down slowly and raise your feet slightly. Do not stand up quickly.",
            "Sip plain water if you are awake and able to swallow safely.",
            "Do not take your next blood pressure tablet before speaking with your doctor if you feel dizzy or faint.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule = ruleReason,
        shouldAlertFamily = hasDizziness,
        triggersEmergencyScreen = false,
        show5MinRecheckReminder = true,
        whatsappSummarySnippet =
          "Alert: $patientName's blood pressure is low at $display (${context.label}, $timeLabel)${if (hasDizziness) " with dizziness" else ""}. Please check on her.",
      )
    }

    // 3. Elevated / High blood pressure (>= 140 or >= 90)
    if (systolic >= config.bpElevatedSystolic || diastolic >= config.bpElevatedDiastolic) {
      val ruleReason =
        buildString {
          append("Rule: Blood pressure $display reached caution threshold (")
          val parts = mutableListOf<String>()
          if (systolic >= config.bpElevatedSystolic) parts.add("systolic $systolic >= ${config.bpElevatedSystolic} mmHg")
          if (diastolic >= config.bpElevatedDiastolic) parts.add("diastolic $diastolic >= ${config.bpElevatedDiastolic} mmHg")
          append(parts.joinToString(" or "))
          append(").")
        }
      return RiskAssessment(
        vitalType = VitalType.BLOOD_PRESSURE,
        displayReading = display,
        contextLabel = context.label,
        riskLevel = RiskLevel.CAUTION,
        statusLevel = HealthStatusLevel.CAUTION,
        headline = "Elevated Blood Pressure ($display). Caution",
        guidanceSteps =
          listOf(
            "Rest quietly for 5 minutes and recheck your blood pressure with the cuff at heart level.",
            "Check that you took your regular prescribed blood pressure medicine today. Never double a dose.",
            "Share this reading with your doctor at your next visit.",
            "Ask your doctor or pharmacist.",
          ),
        whyThisResultRule = ruleReason,
        shouldAlertFamily = false,
        triggersEmergencyScreen = false,
        show5MinRecheckReminder = true,
        whatsappSummarySnippet =
          "Note: $patientName's blood pressure is $display (${context.label}, $timeLabel), Caution.",
      )
    }

    // 4. Normal blood pressure (90-139 / 60-89)
    return RiskAssessment(
      vitalType = VitalType.BLOOD_PRESSURE,
      displayReading = display,
      contextLabel = context.label,
      riskLevel = RiskLevel.NORMAL,
      statusLevel = HealthStatusLevel.NORMAL,
      headline = "Blood Pressure in Safe Range ($display)",
      guidanceSteps =
        listOf(
          "Your blood pressure is within the everyday target range (${config.bpLowSystolic}–${config.bpElevatedSystolic - 1}/${config.bpLowDiastolic}–${config.bpElevatedDiastolic - 1} mmHg).",
          "Keep following your regular medicine schedule, low-salt meals, and rest.",
          "Ask your doctor or pharmacist.",
        ),
      whyThisResultRule =
        "Rule: Blood pressure $display is within ${config.bpLowSystolic}–${config.bpElevatedSystolic - 1}/${config.bpLowDiastolic}–${config.bpElevatedDiastolic - 1} mmHg normal target range.",
      shouldAlertFamily = false,
      triggersEmergencyScreen = false,
      whatsappSummarySnippet =
        "$patientName logged a blood pressure reading of $display (${context.label}, $timeLabel), Normal.",
    )
  }

  fun shouldSendFamilyAlert(
    assessment: RiskAssessment,
    lastRecord: AlertCooldownRecord?,
    nowEpochMs: Long,
    isRecheck: Boolean = false,
    config: RiskThresholdsConfig = RiskThresholdsConfig(),
  ): Boolean {
    if (!assessment.shouldAlertFamily) return false
    if (isRecheck) return true
    if (assessment.riskLevel == RiskLevel.EMERGENCY) return true
    if (lastRecord == null) return true
    val sameCategory =
      lastRecord.vitalType == assessment.vitalType && lastRecord.riskLevel == assessment.riskLevel
    if (!sameCategory) return true
    val cooldownMs = config.alertCooldownMinutes * 60_000L
    return (nowEpochMs - lastRecord.sentAtEpochMs) >= cooldownMs
  }

  fun detectTrendFlags(readings: List<SeededReading>): List<TrendFlag> {
    if (readings.size < 3) return emptyList()
    val flags = mutableListOf<TrendFlag>()

    val fastingSugars =
      readings.filter { it.contextLabel.contains("Fasting", ignoreCase = true) }.takeLast(3)
    if (fastingSugars.size == 3 &&
      fastingSugars[0].glucoseMgDl < fastingSugars[1].glucoseMgDl &&
      fastingSugars[1].glucoseMgDl < fastingSugars[2].glucoseMgDl &&
      fastingSugars[2].glucoseMgDl >= 140
    ) {
      flags.add(
        TrendFlag(
          title = "3 rising fasting blood sugar readings",
          detail =
            "Last 3 fasting readings rose (${fastingSugars.joinToString(" -> ") { "${it.glucoseMgDl} mg/dL" }}). Share this pattern at your next clinic visit. Ask your doctor or pharmacist.",
        ),
      )
    }

    val highCount = readings.takeLast(7).count { it.glucoseMgDl >= 180 || it.systolic >= 140 || it.diastolic >= 90 }
    if (highCount >= 2) {
      flags.add(
        TrendFlag(
          title = "$highCount elevated readings in recent log",
          detail =
            "$highCount recent readings were above everyday targets. Bring your Doctor Summary to your clinic visit. Ask your doctor or pharmacist.",
        ),
      )
    }

    val lowCount = readings.count { it.glucoseMgDl < 70 }
    if (lowCount >= 1) {
      flags.add(
        TrendFlag(
          title = "Low blood sugar episode recorded",
          detail =
            "$lowCount reading(s) fell below 70 mg/dL. Review meal timing and medicine schedule with your doctor. Ask your doctor or pharmacist.",
        ),
      )
    }

    return flags
  }
}

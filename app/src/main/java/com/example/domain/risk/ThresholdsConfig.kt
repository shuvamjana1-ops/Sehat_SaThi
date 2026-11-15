package com.example.domain.risk

/**
 * Configurable clinical threshold boundaries for deterministic vitals evaluation.
 * Documented as: "Reviewed against commonly published guidance; have a clinician confirm before real use."
 */
data class RiskThresholdsConfig(
  val glucoseSevereLowMgDl: Int = 54,
  val glucoseLowMgDl: Int = 70,
  val glucoseHighCautionMgDl: Int = 250,
  val glucoseHighUrgentMgDl: Int = 300,
  val bpElevatedSystolic: Int = 140,
  val bpElevatedDiastolic: Int = 90,
  val bpCrisisSystolic: Int = 180,
  val bpCrisisDiastolic: Int = 120,
  val bpLowSystolic: Int = 90,
  val bpLowDiastolic: Int = 60,
  val alertCooldownMinutes: Long = 30L,
  val disclaimerNote: String =
    "Thresholds reviewed against commonly published guidance; have a clinician confirm before real use.",
)

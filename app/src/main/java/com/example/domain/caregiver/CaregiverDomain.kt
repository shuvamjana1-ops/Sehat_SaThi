package com.example.domain.caregiver

import com.example.data.HealthStatusLevel
import com.example.data.SeededAlert
import com.example.data.SeededDoseEvent
import com.example.data.SeededReading
import com.example.data.SeededMedicine

data class CaregiverPatientLink(
  val caregiverId: String,
  val patientId: String,
  val patientName: String,
  val patientAge: Int,
  val conditions: List<String>,
  val consentStatus: String, // "Consented", "Pending", "Revoked"
  val sharedDataTypes: List<String> = listOf("readings", "doses", "refills", "alerts"),
)

data class MissedDoseEscalationConfig(
  val graceWindowMinutes: Int = 60,
  val escalationWindowMinutes: Int = 120,
  val patientOptedIn: Boolean = true,
  val quietHoursEnabled: Boolean = true,
  val quietStart: String = "10:00 PM",
  val quietEnd: String = "6:30 AM",
)

data class WeeklyCaregiverDigest(
  val patientId: String,
  val patientName: String,
  val weekLabel: String = "Last 7 Days",
  val adherencePercentage: Int,
  val dosesTakenCount: Int,
  val dosesTotalCount: Int,
  val highReadingsCount: Int,
  val lowReadingsCount: Int,
  val refillGapsCount: Int,
  val openAlertsCount: Int,
  val optedInForDigest: Boolean = true,
  val generatedSummaryText: String,
)

object CaregiverDomain {
  fun verifyCaregiverAccess(
    caregiverId: String,
    patientId: String,
    links: List<CaregiverPatientLink>,
  ): Boolean {
    val link = links.firstOrNull { it.caregiverId == caregiverId && it.patientId == patientId }
    return link != null && link.consentStatus.equals("Consented", ignoreCase = true)
  }

  fun evaluateMissedDoseEscalation(
    doses: List<SeededDoseEvent>,
    config: MissedDoseEscalationConfig,
    minutesElapsedSinceDue: Int,
  ): EscalationDecision {
    if (!config.patientOptedIn) {
      return EscalationDecision(ActionNeeded.NONE, "Patient opted out of automated escalations.")
    }
    val pendingDoses = doses.filter { !it.taken }
    if (pendingDoses.isEmpty()) {
      return EscalationDecision(ActionNeeded.NONE, "All scheduled doses for today are marked taken.")
    }

    return when {
      minutesElapsedSinceDue >= config.escalationWindowMinutes -> {
        EscalationDecision(
          ActionNeeded.NOTIFY_CAREGIVER,
          "Dose overdue by ${minutesElapsedSinceDue}m. Escalating calm notification to caregiver.",
        )
      }
      minutesElapsedSinceDue >= config.graceWindowMinutes -> {
        EscalationDecision(
          ActionNeeded.NUDGE_PATIENT,
          "Dose pending past grace window (${config.graceWindowMinutes}m). Nudging patient in-app.",
        )
      }
      else -> {
        EscalationDecision(ActionNeeded.NONE, "Within normal tolerance window.")
      }
    }
  }

  fun generateWeeklyDigest(
    patientId: String,
    patientName: String,
    readings: List<SeededReading>,
    doses: List<SeededDoseEvent>,
    medicines: List<SeededMedicine>,
    alerts: List<SeededAlert>,
    optedIn: Boolean,
  ): WeeklyCaregiverDigest {
    val totalDoses = doses.size.coerceAtLeast(1)
    val takenDoses = doses.count { it.taken }
    val adherence = (takenDoses * 100) / totalDoses

    val highCount = readings.count { it.glucoseMgDl > 180 || it.systolic > 140 }
    val lowCount = readings.count { it.glucoseMgDl < 70 }
    val refillGaps = medicines.count { it.daysLeft <= 5 }
    val openAlerts = alerts.size

    val summary = buildString {
      append("$patientName's weekly health digest: ")
      append("$adherence% medication adherence ($takenDoses/$totalDoses doses taken). ")
      append("Recorded $highCount high and $lowCount low episodes. ")
      if (refillGaps > 0) {
        append("$refillGaps medicine(s) require refilling soon. ")
      } else {
        append("All medicine stock levels are adequate. ")
      }
      append("Ask your doctor or pharmacist.")
    }

    return WeeklyCaregiverDigest(
      patientId = patientId,
      patientName = patientName,
      adherencePercentage = adherence,
      dosesTakenCount = takenDoses,
      dosesTotalCount = totalDoses,
      highReadingsCount = highCount,
      lowReadingsCount = lowCount,
      refillGapsCount = refillGaps,
      openAlertsCount = openAlerts,
      optedInForDigest = optedIn,
      generatedSummaryText = summary,
    )
  }
}

enum class ActionNeeded {
  NONE,
  NUDGE_PATIENT,
  NOTIFY_CAREGIVER,
}

data class EscalationDecision(
  val action: ActionNeeded,
  val reasoning: String,
)

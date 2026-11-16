package com.example.domain.refill

import com.example.data.PatientProfile
import com.example.data.RefillHistoryEntry
import com.example.data.RefillStatus
import com.example.data.SeededMedicine

object RefillEngine {

  const val UNKNOWN_STOCK_MESSAGE =
    "Stock not in our records. Never assume plentiful. Please check your strip and ask your pharmacist."

  fun computeDaysLeft(
    stockCount: Int,
    dailyUnits: Int,
    unknownStock: Boolean = false,
  ): Int? {
    if (unknownStock || dailyUnits <= 0) return null
    return (stockCount.coerceAtLeast(0)) / dailyUnits
  }

  fun evaluateStatus(
    stockCount: Int,
    dailyUnits: Int,
    isCritical: Boolean,
    unknownStock: Boolean = false,
  ): RefillStatus {
    if (unknownStock || dailyUnits <= 0) return RefillStatus.UNKNOWN
    if (stockCount <= 0) return RefillStatus.OUT
    val days = stockCount / dailyUnits
    val soonThreshold = if (isCritical) 8 else 5
    return when {
      days <= 2 -> RefillStatus.DUE
      days <= soonThreshold -> RefillStatus.SOON
      else -> RefillStatus.OK
    }
  }

  fun prescriptionEndingNotices(medicines: List<SeededMedicine>): List<String> {
    return medicines.mapNotNull { med ->
      val remaining = med.prescriptionDaysRemaining ?: return@mapNotNull null
      when {
        remaining <= 0 ->
          "${med.brandName}: prescription duration has ended. Contact your doctor for a fresh prescription."
        remaining <= 7 ->
          "${med.brandName}: prescription ends in $remaining day(s). Ask your doctor about renewal."
        else -> null
      }
    }
  }

  fun restock(
    medicine: SeededMedicine,
    addedUnits: Int,
    timestampLabel: String = "Just now",
  ): Pair<SeededMedicine, RefillHistoryEntry> {
    val safeAdd = addedUnits.coerceAtLeast(1)
    val updated =
      medicine.copy(
        stockCount = medicine.stockCount.coerceAtLeast(0) + safeAdd,
        unknownStock = false,
        snoozedUntilLabel = null,
      )
    val entry =
      RefillHistoryEntry(
        id = "refill-${System.currentTimeMillis()}-${medicine.id}",
        medicineId = medicine.id,
        medicineName = medicine.brandName,
        actionLabel = "Restocked +$safeAdd ${medicine.unit} (now ${updated.stockCount})",
        timestampLabel = timestampLabel,
        deltaUnits = safeAdd,
      )
    return updated to entry
  }

  fun adjustStock(
    medicine: SeededMedicine,
    newCount: Int,
    markUnknown: Boolean = false,
    timestampLabel: String = "Just now",
  ): Pair<SeededMedicine, RefillHistoryEntry> {
    val clamped = newCount.coerceAtLeast(0)
    val updated =
      medicine.copy(
        stockCount = clamped,
        unknownStock = markUnknown,
        snoozedUntilLabel = null,
      )
    val actionLabel =
      if (markUnknown) "Marked stock as unknown (ask pharmacist)"
      else "Adjusted stock to $clamped ${medicine.unit}"
    val entry =
      RefillHistoryEntry(
        id = "adj-${System.currentTimeMillis()}-${medicine.id}",
        medicineId = medicine.id,
        medicineName = medicine.brandName,
        actionLabel = actionLabel,
        timestampLabel = timestampLabel,
        deltaUnits = clamped - medicine.stockCount,
      )
    return updated to entry
  }

  fun snoozeReminder(
    medicine: SeededMedicine,
    days: Int = 2,
    timestampLabel: String = "Just now",
  ): Pair<SeededMedicine, RefillHistoryEntry> {
    val label = "Snoozed for $days days"
    val updated = medicine.copy(snoozedUntilLabel = label)
    val entry =
      RefillHistoryEntry(
        id = "snooze-${System.currentTimeMillis()}-${medicine.id}",
        medicineId = medicine.id,
        medicineName = medicine.brandName,
        actionLabel = "Refill reminder $label",
        timestampLabel = timestampLabel,
        deltaUnits = 0,
      )
    return updated to entry
  }

  fun buildPharmacyWhatsAppText(
    patient: PatientProfile,
    medicines: List<SeededMedicine>,
  ): String {
    val needed =
      medicines.filter {
        it.refillStatus == RefillStatus.OUT ||
          it.refillStatus == RefillStatus.DUE ||
          it.refillStatus == RefillStatus.SOON ||
          it.refillStatus == RefillStatus.UNKNOWN
      }
    val lines =
      if (needed.isEmpty()) {
        medicines.joinToString("\n") { "- ${it.brandName} (${it.genericName} ${it.strength})" }
      } else {
        needed.joinToString("\n") { med ->
          "- ${med.brandName} (${med.genericName} ${med.strength}) · status: ${med.refillStatus.label}"
        }
      }
    return buildString {
      appendLine("Hello ${patient.pharmacyName},")
      appendLine("Refill request for ${patient.name}:")
      appendLine(lines)
      append("Please confirm availability and exact-strength prices before packing. (Sent via SehatSaathi Demo)")
    }
  }
}

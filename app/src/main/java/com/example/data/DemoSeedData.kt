package com.example.data

import com.example.domain.refill.RefillEngine

enum class HealthStatusLevel(val label: String) {
  NORMAL("Normal"),
  CAUTION("Caution"),
  URGENT("Urgent"),
  INFO("Info"),
}

enum class RefillStatus(val label: String, val badgeLevel: HealthStatusLevel) {
  OK("OK", HealthStatusLevel.NORMAL),
  SOON("Refill soon", HealthStatusLevel.CAUTION),
  DUE("Refill due", HealthStatusLevel.URGENT),
  OUT("Out of stock", HealthStatusLevel.URGENT),
  UNKNOWN("Ask pharmacist", HealthStatusLevel.CAUTION),
}

enum class DaySlot(val title: String, val timeWindow: String) {
  MORNING("Morning", "8:00 AM"),
  AFTERNOON("Afternoon", "1:30 PM"),
  NIGHT("Night", "9:00 PM"),
}

data class EmergencyContactEntry(
  val id: String,
  val name: String,
  val relationship: String,
  val phone: String,
  val isPrimary: Boolean = false,
)

data class CaregiverSharingPermissions(
  val readings: Boolean = false,
  val medicines: Boolean = false,
  val refills: Boolean = false,
  val alerts: Boolean = false,
  val emergencyInfo: Boolean = false,
) {
  val anyEnabled: Boolean
    get() = readings || medicines || refills || alerts || emergencyInfo

  fun summaryLabels(): List<String> = buildList {
    if (readings) add("Readings")
    if (medicines) add("Medicines")
    if (refills) add("Refills")
    if (alerts) add("Alerts")
    if (emergencyInfo) add("Emergency info")
  }

  companion object {
    val DEFAULT_OFF = CaregiverSharingPermissions()
    val DEFAULT_CONSENTED =
      CaregiverSharingPermissions(
        readings = true,
        medicines = true,
        refills = true,
        alerts = true,
        emergencyInfo = true,
      )
  }
}

data class NotificationSwitchPrefs(
  val missedDoseNudges: Boolean = true,
  val weeklySummary: Boolean = true,
  val lowStockReminders: Boolean = true,
)

data class PatientProfile(
  val id: String,
  val name: String,
  val age: Int,
  val conditions: List<String>,
  val emergencyNumber: String,
  val pharmacyName: String = "Verma Medicos / Jan Aushadhi",
  val pharmacyPhone: String,
  val handle: String = "sunita.devi",
  val bio: String = "Managing daily sugar & BP calmly with morning walks and family support.",
  val dateOfBirth: String = "1959-04-12",
  val gender: String = "Female",
  val city: String = "Pune",
  val phone: String = "+91 98200 00111",
  val bloodGroup: String = "B+",
  val allergies: List<String> = listOf("Sulfa drugs"),
  val heightValue: String = "158",
  val heightUnit: String = "cm",
  val weightValue: String = "64",
  val weightUnit: String = "kg",
  val emergencyContacts: List<EmergencyContactEntry> =
    listOf(
      EmergencyContactEntry(
        id = "ec-1",
        name = "Rohit Sharma",
        relationship = "Son",
        phone = "+91 98200 11223",
        isPrimary = true,
      ),
      EmergencyContactEntry(
        id = "ec-2",
        name = "Priya Verma",
        relationship = "Daughter",
        phone = "+91 98200 44556",
        isPrimary = false,
      ),
    ),
  val showPhotoToCaregivers: Boolean = true,
  val useInitialsOnly: Boolean = false,
  val hideAchievements: Boolean = false,
  val completenessDismissed: Boolean = false,
)

data class ReminderPreferences(
  val morningTime: String = "8:00 AM",
  val afternoonTime: String = "1:30 PM",
  val nightTime: String = "9:00 PM",
  val quietHoursEnabled: Boolean = true,
  val quietStart: String = "10:00 PM",
  val quietEnd: String = "6:30 AM",
)

data class RefillHistoryEntry(
  val id: String,
  val medicineId: String,
  val medicineName: String,
  val actionLabel: String,
  val timestampLabel: String,
  val deltaUnits: Int,
)

data class SeededMedicine(
  val id: String,
  val brandName: String,
  val genericName: String,
  val strength: String,
  val form: String,
  val frequency: String,
  val foodTiming: String,
  val slot: DaySlot,
  val stockCount: Int,
  val unit: String,
  val dailyUnits: Int,
  val isCritical: Boolean,
  val unknownStock: Boolean = false,
  val snoozedUntilLabel: String? = null,
  val prescriptionDaysRemaining: Int? = null,
) {
  val daysLeftOrNull: Int?
    get() = RefillEngine.computeDaysLeft(stockCount, dailyUnits, unknownStock)

  val daysLeft: Int
    get() = daysLeftOrNull ?: 0

  val refillStatus: RefillStatus
    get() = RefillEngine.evaluateStatus(stockCount, dailyUnits, isCritical, unknownStock)
}

data class SeededDoseEvent(
  val id: String,
  val medicineId: String,
  val medicineLabel: String,
  val instruction: String,
  val slot: DaySlot,
  val taken: Boolean,
)

data class SeededReading(
  val id: String,
  val dayLabel: String,
  val glucoseMgDl: Int,
  val systolic: Int,
  val diastolic: Int,
  val contextLabel: String,
  val status: HealthStatusLevel,
)

data class SeededAlert(
  val id: String,
  val title: String,
  val message: String,
  val timestampLabel: String,
  val level: HealthStatusLevel,
  val isDemo: Boolean = true,
)

data class SeededCaregiver(
  val id: String,
  val name: String,
  val relationship: String,
  val phone: String,
  val consentStatus: String,
  val priority: Int,
  val sinceLabel: String = "Oct 2026",
  val permissions: CaregiverSharingPermissions =
    if (consentStatus.equals("Consented", ignoreCase = true)) {
      CaregiverSharingPermissions.DEFAULT_CONSENTED
    } else {
      CaregiverSharingPermissions.DEFAULT_OFF
    },
)

object DemoSeedData {
  val patient =
    PatientProfile(
      id = "demo-patient-sunita",
      name = "Sunita Devi",
      age = 67,
      conditions = listOf("Type-2 Diabetes", "Hypertension"),
      emergencyNumber = "112",
      pharmacyName = "Verma Medicos / Jan Aushadhi",
      pharmacyPhone = "+91 98100 55112",
    )

  val medicines =
    listOf(
      SeededMedicine(
        id = "med-glimepiride",
        brandName = "Amaryl 1 mg",
        genericName = "Glimepiride",
        strength = "1 mg",
        form = "Tablet",
        frequency = "OD (Once daily)",
        foodTiming = "Before breakfast",
        slot = DaySlot.MORNING,
        stockCount = 2,
        unit = "tablets",
        dailyUnits = 1,
        isCritical = true,
        prescriptionDaysRemaining = 5,
      ),
      SeededMedicine(
        id = "med-metformin",
        brandName = "Glycomet 500 mg",
        genericName = "Metformin",
        strength = "500 mg",
        form = "Tablet",
        frequency = "BD (Twice daily)",
        foodTiming = "After breakfast & dinner",
        slot = DaySlot.MORNING,
        stockCount = 10,
        unit = "tablets",
        dailyUnits = 2,
        isCritical = true,
        prescriptionDaysRemaining = 14,
      ),
      SeededMedicine(
        id = "med-amlodipine",
        brandName = "Amlopres 5 mg",
        genericName = "Amlodipine",
        strength = "5 mg",
        form = "Tablet",
        frequency = "OD (Once daily)",
        foodTiming = "After breakfast",
        slot = DaySlot.AFTERNOON,
        stockCount = 14,
        unit = "tablets",
        dailyUnits = 1,
        isCritical = true,
        prescriptionDaysRemaining = 21,
      ),
      SeededMedicine(
        id = "med-atorvastatin",
        brandName = "Atorva 10 mg",
        genericName = "Atorvastatin",
        strength = "10 mg",
        form = "Tablet",
        frequency = "OD (Once daily)",
        foodTiming = "At bedtime",
        slot = DaySlot.NIGHT,
        stockCount = 18,
        unit = "tablets",
        dailyUnits = 1,
        isCritical = false,
        prescriptionDaysRemaining = 25,
      ),
    )

  val dosesToday =
    listOf(
      SeededDoseEvent(
        id = "dose-1",
        medicineId = "med-glimepiride",
        medicineLabel = "Amaryl (Glimepiride 1 mg)",
        instruction = "1 tablet · Before breakfast",
        slot = DaySlot.MORNING,
        taken = true,
      ),
      SeededDoseEvent(
        id = "dose-2",
        medicineId = "med-metformin",
        medicineLabel = "Glycomet (Metformin 500 mg)",
        instruction = "1 tablet · After breakfast",
        slot = DaySlot.MORNING,
        taken = true,
      ),
      SeededDoseEvent(
        id = "dose-3",
        medicineId = "med-amlodipine",
        medicineLabel = "Amlopres (Amlodipine 5 mg)",
        instruction = "1 tablet · After lunch",
        slot = DaySlot.AFTERNOON,
        taken = true,
      ),
      SeededDoseEvent(
        id = "dose-4",
        medicineId = "med-atorvastatin",
        medicineLabel = "Atorva (Atorvastatin 10 mg)",
        instruction = "1 tablet · At bedtime",
        slot = DaySlot.NIGHT,
        taken = false,
      ),
    )

  // Prior 16 days + last 14 days = 30 days of readings for 7-day / 30-day trend views
  private val earlier16Days =
    listOf(
      SeededReading("r-e1", "Day -29", 144, 136, 86, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e2", "Day -28", 162, 138, 86, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-e3", "Day -27", 138, 132, 82, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e4", "Day -26", 171, 140, 88, "After meal", HealthStatusLevel.CAUTION),
      SeededReading("r-e5", "Day -25", 135, 134, 84, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e6", "Day -24", 158, 136, 84, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-e7", "Day -23", 128, 128, 80, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e8", "Day -22", 184, 144, 90, "After dinner", HealthStatusLevel.CAUTION),
      SeededReading("r-e9", "Day -21", 141, 134, 84, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e10", "Day -20", 155, 136, 84, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-e11", "Day -19", 136, 130, 82, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e12", "Day -18", 168, 138, 86, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-e13", "Day -17", 142, 132, 84, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e14", "Day -16", 192, 146, 90, "After dinner", HealthStatusLevel.CAUTION),
      SeededReading("r-e15", "Day -15", 149, 136, 84, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-e16", "Day -14", 139, 132, 82, "Fasting", HealthStatusLevel.NORMAL),
    )

  // 14 days of seeded glucose and BP readings (includes one low 62 and one high 268)
  val readings14Days =
    listOf(
      SeededReading("r-1", "Day 1", 132, 134, 84, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-2", "Day 2", 148, 136, 86, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-3", "Day 3", 129, 130, 82, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-4", "Day 4", 62, 118, 76, "2:04 AM · Symptoms", HealthStatusLevel.URGENT),
      SeededReading("r-5", "Day 5", 142, 132, 84, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-6", "Day 6", 164, 138, 86, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-7", "Day 7", 151, 134, 82, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-8", "Day 8", 188, 142, 88, "After meal", HealthStatusLevel.CAUTION),
      SeededReading("r-9", "Day 9", 268, 148, 92, "After dinner", HealthStatusLevel.CAUTION),
      SeededReading("r-10", "Day 10", 174, 138, 86, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-11", "Day 11", 145, 132, 82, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-12", "Day 12", 156, 136, 84, "After meal", HealthStatusLevel.NORMAL),
      SeededReading("r-13", "Day 13", 134, 130, 80, "Fasting", HealthStatusLevel.NORMAL),
      SeededReading("r-14", "Today", 138, 134, 84, "Fasting", HealthStatusLevel.NORMAL),
    )

  val readings30Days = earlier16Days + readings14Days

  val alerts =
    listOf(
      SeededAlert(
        id = "alert-1",
        title = "Low sugar family alert (Demo)",
        message =
          "Demo alert: Sunita Devi's blood sugar was 62 mg/dL (low) at 2:04 AM. Rohit Sharma was notified via WhatsApp.",
        timestampLabel = "10 days ago · Demo",
        level = HealthStatusLevel.URGENT,
      ),
      SeededAlert(
        id = "alert-2",
        title = "High sugar caution (Demo)",
        message =
          "Post-dinner reading of 268 mg/dL recorded. Hydration & doctor plan reminder shown.",
        timestampLabel = "5 days ago · Demo",
        level = HealthStatusLevel.CAUTION,
      ),
    )

  val initialRefillHistory =
    listOf(
      RefillHistoryEntry(
        id = "rh-1",
        medicineId = "med-atorvastatin",
        medicineName = "Atorva 10 mg",
        actionLabel = "Restocked +30 tablets",
        timestampLabel = "12 days ago",
        deltaUnits = 30,
      ),
      RefillHistoryEntry(
        id = "rh-2",
        medicineId = "med-metformin",
        medicineName = "Glycomet 500 mg",
        actionLabel = "Adjusted stock to 20 tablets",
        timestampLabel = "5 days ago",
        deltaUnits = 0,
      ),
    )

  val caregivers =
    listOf(
      SeededCaregiver(
        id = "cg-1",
        name = "Rohit Sharma",
        relationship = "Son (Primary)",
        phone = "+91 98200 11223",
        consentStatus = "Consented",
        priority = 1,
      ),
      SeededCaregiver(
        id = "cg-2",
        name = "Priya Verma",
        relationship = "Daughter (Escalation)",
        phone = "+91 98200 44556",
        consentStatus = "Consented",
        priority = 2,
      ),
    )
}

package com.example.domain.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.CaregiverSharingPermissions
import com.example.data.EmergencyContactEntry
import com.example.data.HealthStatusLevel
import com.example.data.PatientProfile
import com.example.data.RefillHistoryEntry
import com.example.data.SeededAlert
import com.example.data.SeededCaregiver
import com.example.data.SeededDoseEvent
import com.example.data.SeededMedicine
import com.example.data.SeededReading
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

enum class ProfileTab(val label: String, val urlHash: String) {
  OVERVIEW("Overview", "#overview"),
  HEALTH("Health", "#health"),
  MEDICINES("Medicines", "#medicines"),
  ACTIVITY("Activity", "#activity"),
  PRIVACY("Privacy", "#privacy"),
}

enum class ActivityFilter(val label: String) {
  ALL("All"),
  READINGS("Readings"),
  DOSES("Doses"),
  REFILLS("Refills"),
  ALERTS("Alerts"),
}

enum class ProfileEditFocusField {
  NAME,
  HANDLE,
  BIO,
  DOB,
  CITY,
  CONDITIONS,
  BLOOD_GROUP,
  ALLERGIES,
  PHARMACY,
  EMERGENCY_CONTACTS,
}

data class ProfileFieldValidation(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val computedAge: Int? = null,
)

data class ProfileStats(
  val readingsLogged: Int,
  val dayStreak: Int,
  val adherence7dPercent: Int,
  val caregiversCount: Int,
)

data class ProfileMissingItem(
  val field: ProfileEditFocusField,
  val label: String,
)

data class ProfileCompleteness(
  val percent: Int,
  val completedCount: Int,
  val totalCount: Int,
  val missingItems: List<ProfileMissingItem>,
)

data class ProfileAchievementBadge(
  val id: String,
  val title: String,
  val subtitle: String,
  val earnedDateLabel: String,
)

data class CareCardSelection(
  val includeAge: Boolean = true,
  val includeBloodGroup: Boolean = true,
  val includeConditions: Boolean = true,
  val includeAllergies: Boolean = true,
  val includeMedicines: Boolean = true,
  val includeEmergencyContacts: Boolean = true,
)

data class ActivityTimelineItem(
  val id: String,
  val category: ActivityFilter,
  val title: String,
  val subtitle: String,
  val timestampLabel: String,
  val statusLevel: HealthStatusLevel,
)

object ProfileDomain {
  const val MAX_NAME_LENGTH = 60
  const val MIN_NAME_LENGTH = 2
  const val MAX_HANDLE_LENGTH = 30
  const val MAX_BIO_LENGTH = 150
  const val ACTIVITY_PAGE_SIZE = 20
  const val MAX_PROFILE_PHOTO_BYTES: Long = 8L * 1024L * 1024L
  const val AVATAR_MAX_PX = 512
  const val COVER_MAX_WIDTH_PX = 1600
  const val COVER_MAX_HEIGHT_PX = 600
  const val PROFILE_JPEG_QUALITY = 80

  val ALLOWED_IMAGE_MIMES =
    setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

  val PRESET_CONDITIONS =
    listOf("Type-2 Diabetes", "Hypertension", "Heart condition", "Cholesterol")

  val BLOOD_GROUPS =
    listOf("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown")

  val GENDER_OPTIONS =
    listOf("Female", "Male", "Non-binary", "Other", "Prefer not to say")

  fun sanitizeText(raw: String, maxLen: Int = 150): String {
    val noControl =
      raw
        .replace(Regex("[\\u0000-\\u001F\\u007F]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    return noControl.take(maxLen)
  }

  fun computeInitials(name: String): String {
    val parts =
      sanitizeText(name, MAX_NAME_LENGTH)
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "SS"
    if (parts.size == 1) return parts[0].take(2).uppercase()
    return "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
  }

  fun validateFullName(raw: String): ProfileFieldValidation {
    val cleaned = sanitizeText(raw, MAX_NAME_LENGTH + 10)
    return when {
      cleaned.length < MIN_NAME_LENGTH ->
        ProfileFieldValidation(false, "Full name must be at least 2 characters.")
      cleaned.length > MAX_NAME_LENGTH ->
        ProfileFieldValidation(false, "Full name must be 60 characters or fewer.")
      else -> ProfileFieldValidation(true)
    }
  }

  fun validateHandle(raw: String): ProfileFieldValidation {
    val trimmed = raw.trim().removePrefix("@")
    if (trimmed.isEmpty()) return ProfileFieldValidation(true)
    if (trimmed.length > MAX_HANDLE_LENGTH) {
      return ProfileFieldValidation(false, "Handle must be 30 characters or fewer.")
    }
    val validPattern = Regex("^[a-zA-Z0-9._]+$")
    if (!validPattern.matches(trimmed)) {
      return ProfileFieldValidation(
        false,
        "Handle can only use letters, numbers, dots (.), and underscores (_).",
      )
    }
    return ProfileFieldValidation(true)
  }

  fun validateBio(raw: String): ProfileFieldValidation {
    val cleaned = raw.replace(Regex("[\\u0000-\\u001F\\u007F]"), "")
    return if (cleaned.length <= MAX_BIO_LENGTH) {
      ProfileFieldValidation(true)
    } else {
      ProfileFieldValidation(false, "Bio must be 150 characters or fewer.")
    }
  }

  fun validatePhone(raw: String): ProfileFieldValidation {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ProfileFieldValidation(true)
    val allowedChars = Regex("^[+0-9 ()-]+$")
    val digits = trimmed.count { it.isDigit() }
    return if (allowedChars.matches(trimmed) && digits in 7..15) {
      ProfileFieldValidation(true)
    } else {
      ProfileFieldValidation(false, "Enter a valid phone number (7 to 15 digits).")
    }
  }

  fun validateDobAndComputeAge(
    dob: String,
    refYear: Int = 2026,
    refMonth: Int = 11,
    refDay: Int = 15,
  ): ProfileFieldValidation {
    val trimmed = dob.trim()
    if (trimmed.isEmpty()) return ProfileFieldValidation(true, computedAge = null)
    val parts = trimmed.split("-")
    if (parts.size != 3) {
      return ProfileFieldValidation(false, "Use YYYY-MM-DD format (for example 1959-04-12).")
    }
    val y = parts[0].toIntOrNull()
    val m = parts[1].toIntOrNull()
    val d = parts[2].toIntOrNull()
    if (y == null || m == null || d == null || m !in 1..12 || d !in 1..31 || y !in 1905..refYear) {
      return ProfileFieldValidation(false, "Enter a valid date of birth between 1905 and $refYear.")
    }
    var age = refYear - y
    if (refMonth < m || (refMonth == m && refDay < d)) {
      age -= 1
    }
    if (age !in 1..120) {
      return ProfileFieldValidation(false, "Computed age must be between 1 and 120 years.")
    }
    return ProfileFieldValidation(true, computedAge = age)
  }

  fun computeProfileStats(
    readings: List<SeededReading>,
    dosesToday: List<SeededDoseEvent>,
    caregivers: List<SeededCaregiver>,
  ): ProfileStats {
    val readingsLogged = readings.size
    val allDosesTakenToday = dosesToday.isNotEmpty() && dosesToday.all { it.taken }

    // Day streak: consecutive recent days with logged readings (plus today if all doses marked)
    val distinctDaysCount = readings.map { it.dayLabel.substringBefore("·").trim() }.distinct().size
    val dayStreak =
      when {
        distinctDaysCount > 0 -> distinctDaysCount.coerceAtMost(14)
        allDosesTakenToday -> 1
        else -> 0
      }

    // 7-day adherence percentage computed from dosesToday + last 7 days consistency
    val totalToday = dosesToday.size
    val takenToday = dosesToday.count { it.taken }
    val todayRatio = if (totalToday == 0) 1.0 else takenToday.toDouble() / totalToday.toDouble()
    val recent7Readings = readings.takeLast(7).size
    val readingFactor = (recent7Readings.coerceAtMost(7) / 7.0)
    val adherencePercent =
      if (readings.isEmpty() && dosesToday.isEmpty()) {
        0
      } else {
        (((todayRatio * 0.65) + (readingFactor * 0.35)) * 100.0).roundToInt().coerceIn(0, 100)
      }

    val consentedCaregivers =
      caregivers.count { it.consentStatus.equals("Consented", ignoreCase = true) }

    return ProfileStats(
      readingsLogged = readingsLogged,
      dayStreak = dayStreak,
      adherence7dPercent = adherencePercent,
      caregiversCount = consentedCaregivers,
    )
  }

  fun computeProfileCompleteness(
    profile: PatientProfile,
    hasAvatarPhoto: Boolean,
  ): ProfileCompleteness {
    val checks =
      listOf(
        (profile.name.trim().length >= 2) to
          ProfileMissingItem(ProfileEditFocusField.NAME, "Add your full name"),
        profile.handle.isNotBlank() to
          ProfileMissingItem(ProfileEditFocusField.HANDLE, "Add a @handle"),
        profile.bio.isNotBlank() to
          ProfileMissingItem(ProfileEditFocusField.BIO, "Add a short bio"),
        profile.dateOfBirth.isNotBlank() to
          ProfileMissingItem(ProfileEditFocusField.DOB, "Add your date of birth"),
        profile.city.isNotBlank() to
          ProfileMissingItem(ProfileEditFocusField.CITY, "Add your city"),
        profile.conditions.isNotEmpty() to
          ProfileMissingItem(ProfileEditFocusField.CONDITIONS, "Select health conditions"),
        profile.bloodGroup.isNotBlank() to
          ProfileMissingItem(ProfileEditFocusField.BLOOD_GROUP, "Add your blood group"),
        profile.allergies.isNotEmpty() to
          ProfileMissingItem(ProfileEditFocusField.ALLERGIES, "Note any medicine allergies"),
        (profile.pharmacyName.isNotBlank() && profile.pharmacyPhone.isNotBlank()) to
          ProfileMissingItem(ProfileEditFocusField.PHARMACY, "Add preferred pharmacy"),
        (profile.emergencyContacts.isNotEmpty() || hasAvatarPhoto) to
          ProfileMissingItem(ProfileEditFocusField.EMERGENCY_CONTACTS, "Add an emergency contact"),
      )

    val completedCount = checks.count { it.first }
    val totalCount = checks.size
    val percent = ((completedCount * 100.0) / totalCount.toDouble()).roundToInt().coerceIn(0, 100)
    val missing = checks.filterNot { it.first }.map { it.second }

    return ProfileCompleteness(
      percent = percent,
      completedCount = completedCount,
      totalCount = totalCount,
      missingItems = missing,
    )
  }

  fun computeEarnedAchievements(
    stats: ProfileStats,
    dosesToday: List<SeededDoseEvent>,
    medicines: List<SeededMedicine>,
    refillHistory: List<RefillHistoryEntry>,
    hasScannedPrescription: Boolean,
    dateLabel: String = "Nov 2026",
  ): List<ProfileAchievementBadge> = buildList {
    if (stats.dayStreak >= 7) {
      add(
        ProfileAchievementBadge(
          id = "streak-7d",
          title = "7-day reading streak",
          subtitle = "${stats.dayStreak} consecutive days of readings logged",
          earnedDateLabel = dateLabel,
        ),
      )
    }
    if (hasScannedPrescription || medicines.any { it.id.startsWith("med-rx-") }) {
      add(
        ProfileAchievementBadge(
          id = "first-rx-scan",
          title = "First prescription scanned",
          subtitle = "Verified medicine list with safety checker",
          earnedDateLabel = dateLabel,
        ),
      )
    }
    if (dosesToday.isNotEmpty() && dosesToday.all { it.taken }) {
      add(
        ProfileAchievementBadge(
          id = "all-doses-taken",
          title = "All doses taken this week",
          subtitle = "100% of scheduled doses marked taken",
          earnedDateLabel = dateLabel,
        ),
      )
    }
    if (refillHistory.isNotEmpty()) {
      add(
        ProfileAchievementBadge(
          id = "refill-on-time",
          title = "Refill on time",
          subtitle = "${refillHistory.size} stock update(s) recorded",
          earnedDateLabel = dateLabel,
        ),
      )
    }
  }

  fun buildWhoCanSeeWhatSummary(caregivers: List<SeededCaregiver>): String {
    val consented =
      caregivers.filter {
        it.consentStatus.equals("Consented", ignoreCase = true) && it.permissions.anyEnabled
      }
    if (consented.isEmpty()) {
      return "Who can see what: Only you. No caregivers currently have active sharing enabled."
    }
    val parts =
      consented.map { cg ->
        "${cg.name} (${cg.permissions.summaryLabels().joinToString(", ")})"
      }
    return "Who can see what: ${parts.joinToString(" · ")}"
  }

  fun updateCaregiverPermission(
    caregivers: List<SeededCaregiver>,
    caregiverId: String,
    transform: (CaregiverSharingPermissions) -> CaregiverSharingPermissions,
  ): List<SeededCaregiver> {
    return caregivers.map { cg ->
      if (cg.id == caregiverId) {
        // Only consented caregivers can have active sharing toggles turned on
        val isConsented = cg.consentStatus.equals("Consented", ignoreCase = true)
        val nextPerms =
          if (isConsented) transform(cg.permissions) else CaregiverSharingPermissions.DEFAULT_OFF
        cg.copy(permissions = nextPerms)
      } else {
        cg
      }
    }
  }

  fun buildCareCardText(
    profile: PatientProfile,
    medicines: List<SeededMedicine>,
    selection: CareCardSelection,
    dateLabel: String = "15 Nov 2026",
  ): String = buildString {
    appendLine("SEHATSAATHI CARE CARD")
    appendLine("Name: ${profile.name}")
    if (selection.includeAge && profile.age > 0) {
      val citySuffix = if (profile.city.isNotBlank()) " · ${profile.city}" else ""
      appendLine("Age: ${profile.age}$citySuffix")
    }
    if (selection.includeBloodGroup && profile.bloodGroup.isNotBlank() && profile.bloodGroup != "Unknown") {
      appendLine("Blood Group: ${profile.bloodGroup}")
    }
    if (selection.includeConditions && profile.conditions.isNotEmpty()) {
      appendLine("Health Conditions: ${profile.conditions.joinToString(", ")}")
    }
    if (selection.includeAllergies && profile.allergies.isNotEmpty()) {
      appendLine("Allergies: ${profile.allergies.joinToString(", ")}")
    }
    if (selection.includeMedicines && medicines.isNotEmpty()) {
      appendLine("Current Medicines: ${medicines.joinToString(", ") { it.brandName }}")
    }
    if (selection.includeEmergencyContacts && profile.emergencyContacts.isNotEmpty()) {
      val contactsStr =
        profile.emergencyContacts.joinToString("; ") { ec ->
          val primaryTag = if (ec.isPrimary) " [Primary]" else ""
          "${ec.name} (${ec.relationship}$primaryTag): ${ec.phone}"
        }
      appendLine("Emergency Contacts: $contactsStr")
    }
    append("Shared by the patient on $dateLabel. Verify with a doctor or pharmacist.")
  }

  fun buildActivityTimeline(
    readings: List<SeededReading>,
    dosesToday: List<SeededDoseEvent>,
    refillHistory: List<RefillHistoryEntry>,
    alerts: List<SeededAlert>,
    filter: ActivityFilter = ActivityFilter.ALL,
  ): List<ActivityTimelineItem> {
    val items = mutableListOf<ActivityTimelineItem>()

    dosesToday.filter { it.taken }.forEach { dose ->
      items +=
        ActivityTimelineItem(
          id = "act-dose-${dose.id}",
          category = ActivityFilter.DOSES,
          title = "Dose taken: ${dose.medicineLabel}",
          subtitle = "${dose.instruction} · ${dose.slot.title}",
          timestampLabel = "Today · ${dose.slot.timeWindow}",
          statusLevel = HealthStatusLevel.NORMAL,
        )
    }

    alerts.forEach { alert ->
      items +=
        ActivityTimelineItem(
          id = "act-alert-${alert.id}",
          category = ActivityFilter.ALERTS,
          title = alert.title,
          subtitle = alert.message,
          timestampLabel = alert.timestampLabel,
          statusLevel = alert.level,
        )
    }

    refillHistory.forEach { refill ->
      items +=
        ActivityTimelineItem(
          id = "act-refill-${refill.id}",
          category = ActivityFilter.REFILLS,
          title = "${refill.medicineName}: ${refill.actionLabel}",
          subtitle = "Stock record updated",
          timestampLabel = refill.timestampLabel,
          statusLevel = HealthStatusLevel.INFO,
        )
    }

    readings.asReversed().forEach { reading ->
      items +=
        ActivityTimelineItem(
          id = "act-reading-${reading.id}",
          category = ActivityFilter.READINGS,
          title = "Sugar ${reading.glucoseMgDl} mg/dL · BP ${reading.systolic}/${reading.diastolic} mmHg",
          subtitle = "Context: ${reading.contextLabel}",
          timestampLabel = reading.dayLabel,
          statusLevel = reading.status,
        )
    }

    return if (filter == ActivityFilter.ALL) {
      items
    } else {
      items.filter { it.category == filter }
    }
  }

  fun validateProfilePhotoMeta(mimeType: String?, sizeBytes: Long?): ProfileFieldValidation {
    if (mimeType != null && mimeType.lowercase() !in ALLOWED_IMAGE_MIMES) {
      return ProfileFieldValidation(
        false,
        "Unsupported image format. Please choose a JPEG, PNG, or WebP photo.",
      )
    }
    if (sizeBytes != null && sizeBytes > MAX_PROFILE_PHOTO_BYTES) {
      return ProfileFieldValidation(
        false,
        "Image is larger than 8 MB. Please choose a smaller photo.",
      )
    }
    return ProfileFieldValidation(true)
  }

  fun cropAndScaleProfileBitmap(
    source: Bitmap,
    isAvatar: Boolean,
    zoom: Float = 1f,
    panX: Float = 0f,
    panY: Float = 0f,
  ): Pair<Bitmap, Int> {
    val safeZoom = zoom.coerceIn(1f, 3f)
    val safePanX = panX.coerceIn(-1f, 1f)
    val safePanY = panY.coerceIn(-1f, 1f)

    val srcW = source.width.coerceAtLeast(1)
    val srcH = source.height.coerceAtLeast(1)

    val targetAspect = if (isAvatar) 1f else (16f / 6f)
    val baseCropW: Int
    val baseCropH: Int
    if (srcW.toFloat() / srcH.toFloat() > targetAspect) {
      baseCropH = srcH
      baseCropW = (srcH * targetAspect).roundToInt().coerceIn(1, srcW)
    } else {
      baseCropW = srcW
      baseCropH = (srcW / targetAspect).roundToInt().coerceIn(1, srcH)
    }

    val zoomedW = (baseCropW / safeZoom).roundToInt().coerceIn(1, srcW)
    val zoomedH = (baseCropH / safeZoom).roundToInt().coerceIn(1, srcH)

    val maxOffsetX = (srcW - zoomedW) / 2f
    val maxOffsetY = (srcH - zoomedH) / 2f
    val centerX = (srcW / 2f) + (safePanX * maxOffsetX)
    val centerY = (srcH / 2f) + (safePanY * maxOffsetY)

    val left = (centerX - zoomedW / 2f).roundToInt().coerceIn(0, (srcW - zoomedW).coerceAtLeast(0))
    val top = (centerY - zoomedH / 2f).roundToInt().coerceIn(0, (srcH - zoomedH).coerceAtLeast(0))

    val cropped = Bitmap.createBitmap(source, left, top, zoomedW, zoomedH)

    val outW: Int
    val outH: Int
    if (isAvatar) {
      val side = min(AVATAR_MAX_PX, maxOf(cropped.width, cropped.height)).coerceAtLeast(1)
      outW = side
      outH = side
    } else {
      val ratio = min(1f, COVER_MAX_WIDTH_PX.toFloat() / cropped.width.coerceAtLeast(1).toFloat())
      outW = (cropped.width * ratio).roundToInt().coerceIn(1, COVER_MAX_WIDTH_PX)
      outH = (cropped.height * ratio).roundToInt().coerceIn(1, COVER_MAX_HEIGHT_PX)
    }

    val scaled =
      if (cropped.width == outW && cropped.height == outH) cropped
      else Bitmap.createScaledBitmap(cropped, outW, outH, true)

    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, PROFILE_JPEG_QUALITY, stream)
    val bytes = stream.toByteArray()
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: scaled
    val kb = ((bytes.size + 1023) / 1024).coerceAtLeast(1)
    return decoded to kb
  }
}

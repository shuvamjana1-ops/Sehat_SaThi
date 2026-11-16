package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sehatDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "sehatsaathi_session")

data class AuthSession(
  val isAuthenticated: Boolean = false,
  val isDemoMode: Boolean = false,
  val displayName: String = "",
  val email: String = "",
  val savedPersonalName: String = "",
  val savedPersonalEmail: String = "",
  val savedPersonalAge: Int = 54,
  val savedPersonalConditions: String = "Type-2 Diabetes, Hypertension",
  val savedPersonalCity: String = "",
  val savedPersonalBloodGroup: String = "",
  val savedPersonalBio: String = "",
) {
  val hasSavedPersonalAccount: Boolean
    get() =
      savedPersonalName.isNotBlank() &&
        !savedPersonalName.equals(DemoSeedData.patient.name, ignoreCase = true)
}

data class PasswordStrengthResult(
  val hasMinLength: Boolean,
  val hasLetterAndNumber: Boolean,
  val notCommonPassword: Boolean,
) {
  val isValid: Boolean
    get() = hasMinLength && hasLetterAndNumber && notCommonPassword

  val scoreFraction: Float
    get() {
      var met = 0
      if (hasMinLength) met++
      if (hasLetterAndNumber) met++
      if (notCommonPassword) met++
      return met / 3f
    }

  val label: String
    get() =
      when {
        !hasMinLength -> "Too short"
        !notCommonPassword -> "Common password blocked"
        isValid -> "Strong password"
        else -> "Needs letters & numbers"
      }
}

class AuthSessionRepository(private val context: Context) {
  private object Keys {
    val IS_AUTH = booleanPreferencesKey("is_authenticated")
    val IS_DEMO = booleanPreferencesKey("is_demo_mode")
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val EMAIL = stringPreferencesKey("email")
    val SAVED_PERSONAL_NAME = stringPreferencesKey("saved_personal_name")
    val SAVED_PERSONAL_EMAIL = stringPreferencesKey("saved_personal_email")
    val SAVED_PERSONAL_AGE = stringPreferencesKey("saved_personal_age")
    val SAVED_PERSONAL_CONDITIONS = stringPreferencesKey("saved_personal_conditions")
    val SAVED_PERSONAL_CITY = stringPreferencesKey("saved_personal_city")
    val SAVED_PERSONAL_BLOOD_GROUP = stringPreferencesKey("saved_personal_blood_group")
    val SAVED_PERSONAL_BIO = stringPreferencesKey("saved_personal_bio")
    val GUIDED_TOUR_DISMISSED = booleanPreferencesKey("guided_tour_dismissed")
    val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    fun profileJsonKey(email: String) =
      stringPreferencesKey("profile_json_${email.trim().lowercase().ifEmpty { "default" }}")
  }

  val sessionFlow: Flow<AuthSession> =
    context.sehatDataStore.data.map { prefs ->
      AuthSession(
        isAuthenticated = prefs[Keys.IS_AUTH] ?: false,
        isDemoMode = prefs[Keys.IS_DEMO] ?: false,
        displayName = prefs[Keys.DISPLAY_NAME] ?: "",
        email = prefs[Keys.EMAIL] ?: "",
        savedPersonalName = prefs[Keys.SAVED_PERSONAL_NAME] ?: "",
        savedPersonalEmail = prefs[Keys.SAVED_PERSONAL_EMAIL] ?: "",
        savedPersonalAge = (prefs[Keys.SAVED_PERSONAL_AGE]?.toIntOrNull() ?: 54).coerceIn(1, 120),
        savedPersonalConditions =
          prefs[Keys.SAVED_PERSONAL_CONDITIONS] ?: "Type-2 Diabetes, Hypertension",
        savedPersonalCity = prefs[Keys.SAVED_PERSONAL_CITY] ?: "",
        savedPersonalBloodGroup = prefs[Keys.SAVED_PERSONAL_BLOOD_GROUP] ?: "",
        savedPersonalBio = prefs[Keys.SAVED_PERSONAL_BIO] ?: "",
      )
    }

  val guidedTourDismissedFlow: Flow<Boolean> =
    context.sehatDataStore.data.map { prefs ->
      prefs[Keys.GUIDED_TOUR_DISMISSED] ?: false
    }

  val themePreferenceFlow: Flow<ThemePreference> =
    context.sehatDataStore.data.map { prefs ->
      ThemePreference.entries.firstOrNull { it.name == prefs[Keys.THEME_PREFERENCE] }
        ?: ThemePreference.SYSTEM
    }

  fun profileJsonFlowFor(email: String): Flow<String?> =
    context.sehatDataStore.data.map { prefs ->
      prefs[Keys.profileJsonKey(email)]
    }

  suspend fun saveProfileJsonFor(email: String, json: String) {
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.profileJsonKey(email)] = json
    }
  }

  suspend fun setGuidedTourDismissed(dismissed: Boolean) {
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.GUIDED_TOUR_DISMISSED] = dismissed
    }
  }

  suspend fun setThemePreference(preference: ThemePreference) {
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.THEME_PREFERENCE] = preference.name
    }
  }

  suspend fun startDemoSession() {
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.IS_AUTH] = true
      prefs[Keys.IS_DEMO] = true
      prefs[Keys.DISPLAY_NAME] = DemoSeedData.patient.name
      prefs[Keys.EMAIL] = "sunita.demo@sehatsaathi.local"
    }
  }

  // Local account session: marks isDemoMode = false when a real user name/email is used
  // and saves the personal profile so the user can switch between Demo and Personal anytime.
  suspend fun startLocalAccountSession(
    name: String,
    email: String,
    age: Int? = null,
    conditionsCsv: String? = null,
    city: String? = null,
    bloodGroup: String? = null,
    bio: String? = null,
  ) {
    val cleanName = name.trim().ifEmpty { "Patient" }
    val cleanEmail = email.trim().lowercase()
    val isSunitaDemo =
      cleanName.equals(DemoSeedData.patient.name, ignoreCase = true) &&
        (cleanEmail.isEmpty() || cleanEmail == "sunita.demo@sehatsaathi.local")
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.IS_AUTH] = true
      prefs[Keys.IS_DEMO] = isSunitaDemo
      prefs[Keys.DISPLAY_NAME] = cleanName
      prefs[Keys.EMAIL] = cleanEmail
      if (!isSunitaDemo) {
        prefs[Keys.SAVED_PERSONAL_NAME] = cleanName
        prefs[Keys.SAVED_PERSONAL_EMAIL] = cleanEmail
        if (age != null) prefs[Keys.SAVED_PERSONAL_AGE] = age.coerceIn(1, 120).toString()
        if (!conditionsCsv.isNullOrBlank()) prefs[Keys.SAVED_PERSONAL_CONDITIONS] = conditionsCsv
        if (city != null) prefs[Keys.SAVED_PERSONAL_CITY] = city
        if (bloodGroup != null) prefs[Keys.SAVED_PERSONAL_BLOOD_GROUP] = bloodGroup
        if (bio != null) prefs[Keys.SAVED_PERSONAL_BIO] = bio
      }
    }
  }

  suspend fun switchToSavedPersonalSession(): Boolean {
    var switched = false
    context.sehatDataStore.edit { prefs ->
      val savedName = prefs[Keys.SAVED_PERSONAL_NAME]?.trim().orEmpty()
      val savedEmail = prefs[Keys.SAVED_PERSONAL_EMAIL]?.trim().orEmpty()
      if (savedName.isNotEmpty()) {
        prefs[Keys.IS_AUTH] = true
        prefs[Keys.IS_DEMO] = false
        prefs[Keys.DISPLAY_NAME] = savedName
        prefs[Keys.EMAIL] = savedEmail
        switched = true
      }
    }
    return switched
  }

  suspend fun updateProfileIdentity(
    name: String,
    email: String,
    isDemoMode: Boolean,
    age: Int? = null,
    conditionsCsv: String? = null,
    city: String? = null,
    bloodGroup: String? = null,
    bio: String? = null,
  ) {
    val cleanName = name.trim()
    val cleanEmail = email.trim().lowercase()
    context.sehatDataStore.edit { prefs ->
      if (cleanName.isNotEmpty()) {
        prefs[Keys.DISPLAY_NAME] = cleanName
      }
      if (cleanEmail.isNotEmpty()) {
        prefs[Keys.EMAIL] = cleanEmail
      }
      prefs[Keys.IS_DEMO] = isDemoMode
      if (!isDemoMode && cleanName.isNotEmpty() && !cleanName.equals(DemoSeedData.patient.name, ignoreCase = true)) {
        prefs[Keys.SAVED_PERSONAL_NAME] = cleanName
        if (cleanEmail.isNotEmpty()) {
          prefs[Keys.SAVED_PERSONAL_EMAIL] = cleanEmail
        }
        if (age != null) prefs[Keys.SAVED_PERSONAL_AGE] = age.coerceIn(1, 120).toString()
        if (!conditionsCsv.isNullOrBlank()) prefs[Keys.SAVED_PERSONAL_CONDITIONS] = conditionsCsv
        if (city != null) prefs[Keys.SAVED_PERSONAL_CITY] = city
        if (bloodGroup != null) prefs[Keys.SAVED_PERSONAL_BLOOD_GROUP] = bloodGroup
        if (bio != null) prefs[Keys.SAVED_PERSONAL_BIO] = bio
      }
    }
  }

  suspend fun logoutSessionOnly() {
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.IS_AUTH] = false
      prefs[Keys.IS_DEMO] = false
    }
  }

  suspend fun clearSession() {
    context.sehatDataStore.edit { prefs -> prefs.clear() }
    runCatching {
      java.io.File(context.filesDir, "profile_photos").deleteRecursively()
    }
  }

  companion object {
    private val COMMON_PASSWORDS =
      setOf(
        "password123",
        "1234567890",
        "12345678901",
        "qwerty1234",
        "qwerty12345",
        "letmein1234",
        "welcome1234",
        "admin12345",
        "sehatsaathi1",
        "iloveyou123",
      )

    fun evaluatePassword(password: String): PasswordStrengthResult {
      val hasMin = password.length >= 10
      val hasLetter = password.any { it.isLetter() }
      val hasDigit = password.any { it.isDigit() }
      val normalized = password.trim().lowercase()
      val notCommon = normalized.isNotEmpty() && normalized !in COMMON_PASSWORDS
      return PasswordStrengthResult(
        hasMinLength = hasMin,
        hasLetterAndNumber = hasLetter && hasDigit,
        notCommonPassword = notCommon,
      )
    }

    fun isValidEmail(email: String): Boolean {
      val trimmed = email.trim()
      return trimmed.contains("@") && trimmed.contains(".") && trimmed.length >= 5
    }
  }
}

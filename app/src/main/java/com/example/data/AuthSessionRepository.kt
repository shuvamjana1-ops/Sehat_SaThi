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
)

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
    val GUIDED_TOUR_DISMISSED = booleanPreferencesKey("guided_tour_dismissed")
    val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
  }

  val sessionFlow: Flow<AuthSession> =
    context.sehatDataStore.data.map { prefs ->
      AuthSession(
        isAuthenticated = prefs[Keys.IS_AUTH] ?: false,
        isDemoMode = prefs[Keys.IS_DEMO] ?: false,
        displayName = prefs[Keys.DISPLAY_NAME] ?: "",
        email = prefs[Keys.EMAIL] ?: "",
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

  // Clearly labelled Demo-mode local account session (no custom password hashing;
  // real Firebase Google Sign-In / backend auth is wired when cloud backend is provisioned).
  suspend fun startLocalAccountSession(name: String, email: String) {
    context.sehatDataStore.edit { prefs ->
      prefs[Keys.IS_AUTH] = true
      prefs[Keys.IS_DEMO] = true
      prefs[Keys.DISPLAY_NAME] = name.trim().ifEmpty { "Patient" }
      prefs[Keys.EMAIL] = email.trim().lowercase()
    }
  }

  suspend fun clearSession() {
    context.sehatDataStore.edit { prefs -> prefs.clear() }
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

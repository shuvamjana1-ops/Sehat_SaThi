package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ExtractedRxMedicine
import com.example.ai.ImageQualityAssessment
import com.example.ai.ParsedPrescriptionResult
import com.example.ai.ParsedVitalExtraction
import com.example.ai.PrescriptionAiParser
import com.example.ai.VitalsAiParser
import com.example.data.AuthSession
import com.example.data.AuthSessionRepository
import com.example.data.CaregiverSharingPermissions
import com.example.data.ConnectivityObserver
import com.example.data.DaySlot
import com.example.data.DemoSeedData
import com.example.data.DrugKnowledgeDataset
import com.example.data.DrugKnowledgeRepository
import com.example.data.EmergencyContactEntry
import com.example.data.HealthStatusLevel
import com.example.data.NotificationSwitchPrefs
import com.example.data.OfflineQueueHelper
import com.example.data.OfflineQueuedTask
import com.example.data.OfflineTaskKind
import com.example.data.PatientProfile
import com.example.data.RefillHistoryEntry
import com.example.data.ReminderPreferences
import com.example.data.SeededAlert
import com.example.data.SeededCaregiver
import com.example.data.SeededDoseEvent
import com.example.data.SeededMedicine
import com.example.data.SeededReading
import com.example.domain.demo.DemoScenario
import com.example.domain.demo.createSamplePrescriptionBitmap
import com.example.domain.drugs.InteractionChecker
import com.example.domain.profile.ProfileDomain
import com.example.domain.drugs.InteractionWarning
import com.example.domain.drugs.MedicineCheckInput
import com.example.domain.drugs.MedicineSafetyCheckReport
import com.example.domain.refill.RefillEngine
import com.example.export.DoctorSummaryAndDataExporter
import com.example.domain.risk.AlertCooldownRecord
import com.example.domain.risk.ReadingContext
import com.example.domain.risk.RiskAssessment
import com.example.domain.risk.RiskEngine
import com.example.domain.risk.RiskThresholdsConfig
import com.example.domain.risk.SymptomFlags
import com.example.domain.risk.TrendFlag
import com.example.domain.risk.VitalType
import com.example.ui.components.AppRoutes
import com.example.ui.theme.ThemePreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingReadingConfirmation(
  val vitalType: VitalType,
  val glucoseMgDl: Int? = null,
  val systolic: Int? = null,
  val diastolic: Int? = null,
  val sourceLabel: String,
  val confidence: Double? = null,
  val needsVerification: Boolean = false,
)

data class EscalationDemoState(
  val active: Boolean = false,
  val secondsRemaining: Int = 0,
  val primaryCaregiverName: String = "",
  val secondaryCaregiverName: String = "",
  val escalated: Boolean = false,
  val acknowledged: Boolean = false,
  val alertSnippet: String = "",
)

data class RxScannerState(
  val step: Int = 0, // 0=Capture, 1=Review, 2=Checks, 3=Schedule
  val capturedBitmap: Bitmap? = null,
  val qualityIssue: ImageQualityAssessment? = null,
  val isScanning: Boolean = false,
  val scanError: String? = null,
  val isSampleResult: Boolean = false,
  val doctorName: String? = null,
  val rxDate: String? = null,
  val extractedMedicines: List<ExtractedRxMedicine> = emptyList(),
  val warnings: List<String> = emptyList(),
  val checkReport: MedicineSafetyCheckReport? = null,
  val isExplainingInteraction: Boolean = false,
  val interactionExplanation: String? = null,
)

data class SehatSaathiUiState(
  val isSessionLoaded: Boolean = false,
  val showPreloader: Boolean = true,
  val preloaderProgress: Float = 0.25f,
  val session: AuthSession = AuthSession(),
  val patient: PatientProfile = DemoSeedData.patient,
  val reminderPrefs: ReminderPreferences = ReminderPreferences(),
  val medicines: List<SeededMedicine> = DemoSeedData.medicines,
  val refillHistory: List<RefillHistoryEntry> = DemoSeedData.initialRefillHistory,
  val dosesToday: List<SeededDoseEvent> = DemoSeedData.dosesToday,
  val skippedDoseIds: Set<String> = emptySet(),
  val readings: List<SeededReading> = DemoSeedData.readings30Days,
  val alerts: List<SeededAlert> = DemoSeedData.alerts,
  val caregivers: List<SeededCaregiver> = DemoSeedData.caregivers,
  val drugDataset: DrugKnowledgeDataset? = null,
  val activeSafetyReport: MedicineSafetyCheckReport? = null,
  val extraCheckMedicineName: String = "",
  val thresholdsConfig: RiskThresholdsConfig = RiskThresholdsConfig(),
  val trendFlags: List<TrendFlag> = RiskEngine.detectTrendFlags(DemoSeedData.readings30Days),
  val isParsingAi: Boolean = false,
  val aiParseError: String? = null,
  val pendingConfirmation: PendingReadingConfirmation? = null,
  val latestAssessment: RiskAssessment? = null,
  val lastAlertSentForAssessment: Boolean = false,
  val cooldownSuppressedMessage: String? = null,
  val isExplainingAi: Boolean = false,
  val aiPlainExplanation: String? = null,
  val showEmergencyScreen: Boolean = false,
  val emergencyAssessment: RiskAssessment? = null,
  val escalationDemo: EscalationDemoState = EscalationDemoState(),
  val rxScanner: RxScannerState = RxScannerState(),
  val isDeviceOnline: Boolean = true,
  val isOfflineSimulationEnabled: Boolean = false,
  val offlineQueue: List<OfflineQueuedTask> = emptyList(),
  val isSyncingOfflineQueue: Boolean = false,
  val activeDemoScenario: DemoScenario? = null,
  val activeDemoScenarioCaption: String? = null,
  val pendingDemoNavigationRoute: String? = null,
  val pendingPublicNavigationRoute: String? = null,
  val guidedTourDismissed: Boolean = true,
  val themePreference: ThemePreference = ThemePreference.SYSTEM,
  val caregiverViewMode: Boolean = false,
  val weeklyDigestOptedIn: Boolean = true,
  val simpleModeActive: Boolean = false,
  val avatarBitmap: Bitmap? = null,
  val coverBitmap: Bitmap? = null,
  val notificationSwitches: NotificationSwitchPrefs = NotificationSwitchPrefs(),
  val hasScannedPrescription: Boolean = false,
  val toastMessage: String? = null,
) {
  val effectiveOnline: Boolean
    get() = isDeviceOnline && !isOfflineSimulationEnabled
}

class SehatSaathiViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = AuthSessionRepository(application.applicationContext)
  private val vitalsAiParser = VitalsAiParser()
  private val rxAiParser = PrescriptionAiParser()

  private val _uiState = MutableStateFlow(SehatSaathiUiState())
  val uiState: StateFlow<SehatSaathiUiState> = _uiState.asStateFlow()

  private var preloaderStarted = false
  private var lastAlertRecord: AlertCooldownRecord? = null
  private var escalationJob: Job? = null

  init {
    val loadedDataset = runCatching { DrugKnowledgeRepository.loadFromAssets(application.applicationContext) }.getOrNull()
    _uiState.update { state ->
      val initialReport =
        loadedDataset?.let { ds ->
          runSafetyReportFor(ds, state.medicines, state.extraCheckMedicineName)
        }
      state.copy(drugDataset = loadedDataset, activeSafetyReport = initialReport)
    }

    viewModelScope.launch {
      val initialSession = repository.sessionFlow.first()
      _uiState.update { state ->
        val syncedPatient = buildSyncedPatientForSession(initialSession, state.patient)
        val syncedAlerts =
          if (!syncedPatient.name.equals("Sunita Devi", ignoreCase = true)) {
            state.alerts.map { a ->
              a.copy(
                message = a.message.replace("Sunita Devi", syncedPatient.name, ignoreCase = true),
                isDemo = initialSession.isDemoMode,
              )
            }
          } else {
            DemoSeedData.alerts
          }
        state.copy(
          isSessionLoaded = true,
          session = initialSession,
          preloaderProgress = 0.75f,
          patient = syncedPatient,
          alerts = syncedAlerts,
        )
      }
      repository.sessionFlow.collect { session ->
        _uiState.update { state ->
          val syncedPatient = buildSyncedPatientForSession(session, state.patient)
          val syncedAlerts =
            if (!syncedPatient.name.equals("Sunita Devi", ignoreCase = true)) {
              state.alerts.map { a ->
                a.copy(
                  message =
                    a.message
                      .replace("Sunita Devi", syncedPatient.name, ignoreCase = true)
                      .replace(state.patient.name, syncedPatient.name, ignoreCase = true),
                  isDemo = session.isDemoMode,
                )
              }
            } else {
              DemoSeedData.alerts
            }
          state.copy(
            session = session,
            patient = syncedPatient,
            alerts = syncedAlerts,
          )
        }
      }
    }

    viewModelScope.launch {
      repository.guidedTourDismissedFlow.collect { dismissed ->
        _uiState.update { it.copy(guidedTourDismissed = dismissed) }
      }
    }

    viewModelScope.launch {
      repository.themePreferenceFlow.collect { pref ->
        _uiState.update { it.copy(themePreference = pref) }
      }
    }

    viewModelScope.launch {
      runCatching {
        ConnectivityObserver(application.applicationContext).onlineFlow.collect { online ->
          _uiState.update { it.copy(isDeviceOnline = online) }
        }
      }
    }
  }

  fun runSessionPreloaderIfNeeded(reducedMotion: Boolean) {
    if (preloaderStarted) return
    preloaderStarted = true
    if (reducedMotion) {
      _uiState.update { it.copy(showPreloader = false, preloaderProgress = 1f) }
      return
    }
    viewModelScope.launch {
      delay(350)
      _uiState.update { it.copy(preloaderProgress = 0.9f) }
      delay(450)
      _uiState.update { it.copy(preloaderProgress = 1f, showPreloader = false) }
    }
  }

  private fun buildSyncedPatientForSession(
    session: AuthSession,
    current: PatientProfile,
  ): PatientProfile {
    if (!session.isAuthenticated || session.displayName.isBlank()) {
      return DemoSeedData.patient
    }
    if (session.isDemoMode && session.displayName.equals(DemoSeedData.patient.name, ignoreCase = true)) {
      return DemoSeedData.patient
    }
    val clean = session.displayName.trim()
    val slug =
      clean.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), ".").trim('.').ifEmpty { "patient" }
    val nextHandle =
      if (
        current.handle.isBlank() ||
          (current.handle.equals("sunita.devi", ignoreCase = true) && !clean.equals("Sunita Devi", ignoreCase = true))
      ) {
        slug
      } else {
        current.handle
      }
    val parsedConditions =
      session.savedPersonalConditions
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .ifEmpty { current.conditions }
    val nextBio =
      when {
        session.savedPersonalBio.isNotBlank() -> session.savedPersonalBio
        current.bio == DemoSeedData.patient.bio && !clean.equals("Sunita Devi", ignoreCase = true) ->
          "Personal daily vitals, medication schedule, and family care profile for $clean."
        else -> current.bio
      }
    val nextAge =
      if (!clean.equals("Sunita Devi", ignoreCase = true) && current.age == DemoSeedData.patient.age) {
        session.savedPersonalAge
      } else {
        current.age
      }
    return current.copy(
      name = clean,
      handle = nextHandle,
      age = nextAge,
      conditions = parsedConditions,
      city = session.savedPersonalCity.ifBlank { current.city },
      bloodGroup = session.savedPersonalBloodGroup.ifBlank { current.bloodGroup },
      bio = nextBio,
    )
  }

  fun enterDemoMode() {
    resetDemoDataInternal()
    _uiState.update { state ->
      state.copy(
        session =
          state.session.copy(
            isAuthenticated = true,
            isDemoMode = true,
            displayName = DemoSeedData.patient.name,
            email = "sunita.demo@sehatsaathi.local",
          ),
        patient = DemoSeedData.patient,
        alerts = DemoSeedData.alerts,
        toastMessage = "Demo mode active with seeded patient Sunita Devi.",
      )
    }
    viewModelScope.launch {
      repository.startDemoSession()
    }
  }

  fun switchToDemoAccount() {
    enterDemoMode()
  }

  fun switchToPersonalAccount() {
    val currentSession = _uiState.value.session
    if (currentSession.hasSavedPersonalAccount) {
      val personalName = currentSession.savedPersonalName.trim()
      val personalEmail =
        currentSession.savedPersonalEmail.trim().ifEmpty {
          val slug =
            personalName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), ".").trim('.').ifEmpty { "patient" }
          "$slug@sehatsaathi.app"
        }
      val nextSession =
        currentSession.copy(
          isAuthenticated = true,
          isDemoMode = false,
          displayName = personalName,
          email = personalEmail,
        )
      _uiState.update { state ->
        val syncedPatient = buildSyncedPatientForSession(nextSession, state.patient)
        val syncedAlerts =
          state.alerts.map { a ->
            a.copy(
              message = a.message.replace("Sunita Devi", personalName, ignoreCase = true),
              isDemo = false,
            )
          }
        state.copy(
          session = nextSession,
          patient = syncedPatient,
          alerts = syncedAlerts,
          toastMessage = "Switched to $personalName's personal account.",
        )
      }
      viewModelScope.launch {
        repository.switchToSavedPersonalSession()
      }
    } else {
      logoutAndGoToSignUp()
    }
  }

  fun logoutAndGoToSignUp() {
    resetDemoDataInternal()
    _uiState.update { state ->
      state.copy(
        session = state.session.copy(isAuthenticated = false, isDemoMode = false),
        pendingPublicNavigationRoute = AppRoutes.SIGN_UP,
        toastMessage = "Logged out of Demo Mode. Create your personal account below.",
      )
    }
    viewModelScope.launch {
      repository.logoutSessionOnly()
    }
  }

  fun logoutAndGoToLogin() {
    resetDemoDataInternal()
    _uiState.update { state ->
      state.copy(
        session = state.session.copy(isAuthenticated = false, isDemoMode = false),
        pendingPublicNavigationRoute = AppRoutes.LOG_IN,
        toastMessage = "Signed out. Log in to your account or switch to Demo Mode anytime.",
      )
    }
    viewModelScope.launch {
      repository.logoutSessionOnly()
    }
  }

  fun dismissGuidedTour() {
    _uiState.update { it.copy(guidedTourDismissed = true) }
    viewModelScope.launch { repository.setGuidedTourDismissed(true) }
  }

  fun replayGuidedTour() {
    if (!canRunDemoSession(_uiState.value.session)) return
    _uiState.update { it.copy(guidedTourDismissed = false) }
    viewModelScope.launch { repository.setGuidedTourDismissed(false) }
  }

  fun setThemePreference(preference: ThemePreference) {
    _uiState.update { it.copy(themePreference = preference) }
    viewModelScope.launch { repository.setThemePreference(preference) }
  }

  fun toggleDarkMode(currentlyDark: Boolean) {
    val next = if (currentlyDark) ThemePreference.LIGHT else ThemePreference.DARK
    setThemePreference(next)
  }

  fun consumePendingDemoNavigation() {
    _uiState.update { it.copy(pendingDemoNavigationRoute = null) }
  }

  fun consumePendingPublicNavigation() {
    _uiState.update { it.copy(pendingPublicNavigationRoute = null) }
  }

  fun launchDemoScenarioFromLanding(scenario: DemoScenario) {
    viewModelScope.launch {
      repository.startDemoSession()
      repository.setGuidedTourDismissed(true)
      resetDemoDataInternal()
      applyDemoScenarioInternal(scenario)
    }
  }

  fun runDemoScenario(scenario: DemoScenario) {
    val current = _uiState.value
    if (!canRunDemoSession(current.session)) {
      return
    }
    resetDemoDataInternal()
    applyDemoScenarioInternal(scenario)
  }

  @androidx.annotation.VisibleForTesting
  internal fun setSessionForTest(session: AuthSession) {
    _uiState.update { it.copy(session = session) }
  }

  companion object {
    fun canRunDemoSession(session: AuthSession): Boolean =
      !session.isAuthenticated || session.isDemoMode
  }

  private fun applyDemoScenarioInternal(scenario: DemoScenario) {
    val patientName = _uiState.value.patient.name.ifBlank { DemoSeedData.patient.name }
    val config = _uiState.value.thresholdsConfig
    val nowMs = System.currentTimeMillis()

    when (scenario) {
      DemoScenario.LOW_SUGAR_2AM -> {
        val assessment =
          RiskEngine.evaluateGlucose(
            mgDl = 55,
            context = ReadingContext.FASTING,
            symptoms = SymptomFlags(confusionOrFainting = true, dizziness = true),
            config = config,
            patientName = patientName,
            timeLabel = "2:00 AM",
          )
        val latest = _uiState.value.readings.lastOrNull()
        val demoReading =
          SeededReading(
            id = "demo-r-$nowMs",
            dayLabel = "Today · 2:00 AM (Demo)",
            glucoseMgDl = 55,
            systolic = latest?.systolic ?: 134,
            diastolic = latest?.diastolic ?: 84,
            contextLabel = ReadingContext.FASTING.label,
            status = assessment.statusLevel,
          )
        val demoAlert =
          SeededAlert(
            id = "demo-alert-$nowMs",
            title = "Severe low blood sugar at 2:00 AM (Demo)",
            message = assessment.whatsappSummarySnippet,
            timestampLabel = "2:00 AM · Demo",
            level = HealthStatusLevel.URGENT,
            isDemo = true,
          )
        val updatedReadings = _uiState.value.readings + demoReading
        _uiState.update {
          it.copy(
            readings = updatedReadings,
            trendFlags = RiskEngine.detectTrendFlags(updatedReadings),
            alerts = listOf(demoAlert) + it.alerts,
            latestAssessment = assessment,
            lastAlertSentForAssessment = true,
            showEmergencyScreen = true,
            emergencyAssessment = assessment,
            activeDemoScenario = scenario,
            activeDemoScenarioCaption = scenario.caption,
            pendingDemoNavigationRoute = AppRoutes.LOG_READING,
            toastMessage = "Demo scenario: Low sugar (55 mg/dL) logged at 2:00 AM.",
          )
        }
        startDemoEscalationTimer(assessment.whatsappSummarySnippet)
      }

      DemoScenario.HIGH_BLOOD_PRESSURE -> {
        val assessment =
          RiskEngine.evaluateBloodPressure(
            systolic = 185,
            diastolic = 125,
            context = ReadingContext.FASTING,
            symptoms = SymptomFlags(),
            config = config,
            patientName = patientName,
            timeLabel = "Just now",
          )
        val latest = _uiState.value.readings.lastOrNull()
        val demoReading =
          SeededReading(
            id = "demo-r-$nowMs",
            dayLabel = "Today · Demo crisis",
            glucoseMgDl = latest?.glucoseMgDl ?: 138,
            systolic = 185,
            diastolic = 125,
            contextLabel = "Resting",
            status = assessment.statusLevel,
          )
        val demoAlert =
          SeededAlert(
            id = "demo-alert-$nowMs",
            title = "${assessment.headline} (Demo)",
            message = assessment.whatsappSummarySnippet,
            timestampLabel = "Just now · Demo",
            level = HealthStatusLevel.URGENT,
            isDemo = true,
          )
        val updatedReadings = _uiState.value.readings + demoReading
        _uiState.update {
          it.copy(
            readings = updatedReadings,
            trendFlags = RiskEngine.detectTrendFlags(updatedReadings),
            alerts = listOf(demoAlert) + it.alerts,
            latestAssessment = assessment,
            lastAlertSentForAssessment = true,
            showEmergencyScreen = false,
            activeDemoScenario = scenario,
            activeDemoScenarioCaption = scenario.caption,
            pendingDemoNavigationRoute = AppRoutes.LOG_READING,
            toastMessage = "Demo scenario: BP 185/125 logged. Showing crisis guidance.",
          )
        }
        startDemoEscalationTimer(assessment.whatsappSummarySnippet)
      }

      DemoScenario.SCAN_SAMPLE_RX -> {
        loadDemoSamplePrescription()
        _uiState.update {
          it.copy(
            activeDemoScenario = scenario,
            activeDemoScenarioCaption = scenario.caption,
            pendingDemoNavigationRoute = AppRoutes.SCAN_RX,
            toastMessage = "Loaded bundled sample prescription (Sample result).",
          )
        }
      }

      DemoScenario.MEDICINE_RUNNING_OUT -> {
        val updatedMeds =
          _uiState.value.medicines.map { med ->
            if (med.id == "med-metformin") {
              med.copy(
                stockCount = (med.dailyUnits * 3).coerceAtLeast(3),
                unknownStock = false,
                snoozedUntilLabel = null,
              )
            } else {
              med
            }
          }
        val report =
          _uiState.value.drugDataset?.let { ds ->
            runSafetyReportFor(ds, updatedMeds, "")
          }
        _uiState.update {
          it.copy(
            medicines = updatedMeds,
            activeSafetyReport = report ?: it.activeSafetyReport,
            activeDemoScenario = scenario,
            activeDemoScenarioCaption = scenario.caption,
            pendingDemoNavigationRoute = AppRoutes.MEDICINES,
            toastMessage = "Demo scenario: Glycomet 500 mg has 3 days left. Refill alert ready.",
          )
        }
      }

      DemoScenario.MISSED_DOSE -> {
        val updatedDoses =
          _uiState.value.dosesToday.mapIndexed { index, dose ->
            if (index == 0) dose.copy(taken = false) else dose
          }
        val missedDoseLabel = updatedDoses.firstOrNull()?.medicineLabel ?: "Morning medicine"
        val calmMsg =
          "Demo alert: $patientName has not marked her morning dose ($missedDoseLabel) yet. Please call to check in calmly. Never advise doubling a missed dose. Ask your doctor or pharmacist."
        val missedAlert =
          SeededAlert(
            id = "demo-missed-$nowMs",
            title = "Missed morning dose nudge (Demo)",
            message = calmMsg,
            timestampLabel = "Overdue 2h · Demo",
            level = HealthStatusLevel.CAUTION,
            isDemo = true,
          )
        _uiState.update {
          it.copy(
            dosesToday = updatedDoses,
            alerts = listOf(missedAlert) + it.alerts,
            activeDemoScenario = scenario,
            activeDemoScenarioCaption = scenario.caption,
            pendingDemoNavigationRoute = AppRoutes.FAMILY,
            toastMessage = "Demo scenario: Simulated missed dose escalation to caregiver.",
          )
        }
        startDemoEscalationTimer(calmMsg)
      }
    }
  }

  fun signInDemoLocal(email: String, password: String): String? {
    if (email.isBlank() || password.isBlank()) {
      return "Please enter your email and password."
    }
    if (!AuthSessionRepository.isValidEmail(email) || password.length < 10) {
      return "Incorrect email or password."
    }
    val cleanEmail = email.trim().lowercase()
    val currentSession = _uiState.value.session
    val resolvedName =
      if (
        currentSession.hasSavedPersonalAccount &&
          (cleanEmail == currentSession.savedPersonalEmail.lowercase() ||
            !cleanEmail.equals("sunita.demo@sehatsaathi.local", ignoreCase = true))
      ) {
        currentSession.savedPersonalName.ifBlank {
          cleanEmail
            .substringBefore("@")
            .split(".", "_", "-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            .ifBlank { "Patient" }
        }
      } else {
        cleanEmail
          .substringBefore("@")
          .split(".", "_", "-")
          .filter { it.isNotBlank() }
          .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
          .ifBlank { "Patient" }
      }
    val nextSession =
      currentSession.copy(
        isAuthenticated = true,
        isDemoMode = false,
        displayName = resolvedName,
        email = cleanEmail,
        savedPersonalName = resolvedName,
        savedPersonalEmail = cleanEmail,
      )
    _uiState.update { state ->
      val syncedPatient = buildSyncedPatientForSession(nextSession, state.patient)
      state.copy(
        session = nextSession,
        patient = syncedPatient,
        toastMessage = "Welcome back, $resolvedName!",
      )
    }
    viewModelScope.launch {
      repository.startLocalAccountSession(name = resolvedName, email = cleanEmail)
    }
    return null
  }

  fun quickSignInSavedPersonalAccount() {
    switchToPersonalAccount()
  }

  fun signUpDemoLocal(name: String, email: String, password: String): String? {
    val cleanName = ProfileDomain.sanitizeText(name, ProfileDomain.MAX_NAME_LENGTH)
    if (cleanName.length < 2) {
      return "Please enter your full name."
    }
    val cleanEmail = email.trim().lowercase()
    if (!AuthSessionRepository.isValidEmail(cleanEmail)) {
      return "Please enter a valid email address."
    }
    val strength = AuthSessionRepository.evaluatePassword(password)
    if (!strength.isValid) {
      return "Please choose a stronger password that meets all checklist items."
    }
    val slug =
      cleanName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), ".").trim('.').ifEmpty { "patient" }
    val personalBio = "Personal daily vitals, medication schedule, and family care profile for $cleanName."
    val nextSession =
      _uiState.value.session.copy(
        isAuthenticated = true,
        isDemoMode = false,
        displayName = cleanName,
        email = cleanEmail,
        savedPersonalName = cleanName,
        savedPersonalEmail = cleanEmail,
        savedPersonalBio = personalBio,
      )
    _uiState.update { state ->
      val syncedPatient =
        buildSyncedPatientForSession(nextSession, state.patient).copy(
          name = cleanName,
          handle = slug,
          bio = personalBio,
        )
      val syncedAlerts =
        state.alerts.map { a ->
          a.copy(
            message = a.message.replace("Sunita Devi", cleanName, ignoreCase = true),
            isDemo = false,
          )
        }
      state.copy(
        session = nextSession,
        patient = syncedPatient,
        alerts = syncedAlerts,
        toastMessage = "Welcome, $cleanName! Your personal account is ready.",
      )
    }
    viewModelScope.launch {
      repository.startLocalAccountSession(
        name = cleanName,
        email = cleanEmail,
        bio = personalBio,
      )
    }
    return null
  }

  fun toggleDoseTaken(doseId: String) {
    _uiState.update { state ->
      val target = state.dosesToday.find { it.id == doseId } ?: return@update state
      val newTaken = !target.taken
      val updatedDoses =
        state.dosesToday.map { dose ->
          if (dose.id == doseId) dose.copy(taken = newTaken) else dose
        }
      val updatedMeds =
        state.medicines.map { med ->
          if (med.id == target.medicineId) {
            val delta = if (newTaken) -1 else 1
            med.copy(stockCount = (med.stockCount + delta).coerceAtLeast(0))
          } else {
            med
          }
        }
      val updatedSkipped =
        if (newTaken) state.skippedDoseIds - doseId else state.skippedDoseIds
      state.copy(
        dosesToday = updatedDoses,
        medicines = updatedMeds,
        skippedDoseIds = updatedSkipped,
        toastMessage =
          if (newTaken) "Marked ${target.medicineLabel} as taken."
          else "Marked ${target.medicineLabel} as pending.",
      )
    }
  }

  fun skipDose(doseId: String) {
    _uiState.update { state ->
      val target = state.dosesToday.find { it.id == doseId } ?: return@update state
      val updatedDoses =
        state.dosesToday.map { dose ->
          if (dose.id == doseId) dose.copy(taken = false) else dose
        }
      state.copy(
        dosesToday = updatedDoses,
        skippedDoseIds = state.skippedDoseIds + doseId,
        toastMessage = "Skipped ${target.medicineLabel} for now.",
      )
    }
  }

  // --- Phase 2: Log Reading, AI Parsing, Deterministic Risk Engine & Family Alerts ---

  fun parseMeterBitmapWithAi(bitmap: Bitmap) {
    if (!_uiState.value.effectiveOnline) {
      _uiState.update { state ->
        state.copy(
          isParsingAi = false,
          pendingConfirmation = null,
          offlineQueue =
            OfflineQueueHelper.enqueue(
              current = state.offlineQueue,
              kind = OfflineTaskKind.METER_PHOTO,
              summary = "Meter photo (${bitmap.width}x${bitmap.height})",
              bitmap = bitmap,
            ),
          aiParseError =
            "No internet right now. Your meter photo is saved to sync later, or you can type the numbers on the keypad below.",
          toastMessage = "No internet. Meter photo saved to sync later.",
        )
      }
      return
    }
    viewModelScope.launch {
      _uiState.update {
        it.copy(isParsingAi = true, aiParseError = null, pendingConfirmation = null)
      }
      val result = vitalsAiParser.parseMeterPhoto(bitmap)
      result
        .onSuccess { extracted -> handleAiExtraction(extracted, sourceLabel = "Photo scan") }
        .onFailure {
          _uiState.update {
            it.copy(
              isParsingAi = false,
              aiParseError =
                "We could not read this image. Please retake the photo or enter the details yourself.",
            )
          }
        }
    }
  }

  fun parseVoicePhrase(transcript: String) {
    if (!_uiState.value.effectiveOnline) {
      val clean = transcript.trim().take(200)
      val offlineParsed = VitalsAiParser.fallbackParseSpokenText(clean)
      if (offlineParsed != null) {
        handleAiExtraction(offlineParsed, sourceLabel = "Voice (offline parser)")
      } else if (clean.isNotEmpty()) {
        _uiState.update { state ->
          state.copy(
            isParsingAi = false,
            offlineQueue =
              OfflineQueueHelper.enqueue(
                current = state.offlineQueue,
                kind = OfflineTaskKind.VOICE_PHRASE,
                summary = "Voice phrase: \"${clean.take(40)}\"",
                transcript = clean,
              ),
            aiParseError =
              "No internet and the spoken phrase was unclear. It is saved to sync later, or you can type the numbers on the keypad below.",
            toastMessage = "Offline voice phrase saved to sync later.",
          )
        }
      } else {
        _uiState.update {
          it.copy(
            isParsingAi = false,
            aiParseError = "Please say or type a sugar or BP number, or use the keypad.",
          )
        }
      }
      return
    }
    viewModelScope.launch {
      _uiState.update {
        it.copy(isParsingAi = true, aiParseError = null, pendingConfirmation = null)
      }
      val extracted = vitalsAiParser.parseVoiceTranscript(transcript)
      if (extracted == null) {
        _uiState.update {
          it.copy(
            isParsingAi = false,
            aiParseError =
              "Could not hear a clear sugar or BP number. Try saying 'Sugar 182' or 'BP 150 by 95', or use the keypad.",
          )
        }
      } else {
        handleAiExtraction(
          extracted,
          sourceLabel = if (extracted.usedOfflineFallback) "Voice (offline parser)" else "Voice",
        )
      }
    }
  }

  private fun handleAiExtraction(extracted: ParsedVitalExtraction, sourceLabel: String) {
    if (extracted.vitalType == VitalType.GLUCOSE && extracted.glucoseMgDl != null) {
      _uiState.update {
        it.copy(
          isParsingAi = false,
          pendingConfirmation =
            PendingReadingConfirmation(
              vitalType = VitalType.GLUCOSE,
              glucoseMgDl = extracted.glucoseMgDl,
              sourceLabel = sourceLabel,
              confidence = extracted.confidence,
              needsVerification = extracted.needsVerification,
            ),
        )
      }
    } else if (
      extracted.vitalType == VitalType.BLOOD_PRESSURE &&
        extracted.systolic != null &&
        extracted.diastolic != null
    ) {
      _uiState.update {
        it.copy(
          isParsingAi = false,
          pendingConfirmation =
            PendingReadingConfirmation(
              vitalType = VitalType.BLOOD_PRESSURE,
              systolic = extracted.systolic,
              diastolic = extracted.diastolic,
              sourceLabel = sourceLabel,
              confidence = extracted.confidence,
              needsVerification = extracted.needsVerification,
            ),
        )
      }
    } else {
      _uiState.update {
        it.copy(
          isParsingAi = false,
          aiParseError =
            "Numbers in the image or phrase were unclear (please verify). Use the large keypad below.",
        )
      }
    }
  }

  fun stageKeypadConfirmation(
    vitalType: VitalType,
    glucoseMgDl: Int?,
    systolic: Int?,
    diastolic: Int?,
  ) {
    _uiState.update {
      it.copy(
        aiParseError = null,
        pendingConfirmation =
          PendingReadingConfirmation(
            vitalType = vitalType,
            glucoseMgDl = glucoseMgDl,
            systolic = systolic,
            diastolic = diastolic,
            sourceLabel = "Keypad",
            confidence = 1.0,
            needsVerification = false,
          ),
      )
    }
  }

  fun clearPendingConfirmation() {
    _uiState.update { it.copy(pendingConfirmation = null, aiParseError = null) }
  }

  fun confirmAndSaveReading(
    context: ReadingContext,
    symptoms: SymptomFlags,
    isRecheck: Boolean,
  ) {
    val pending = _uiState.value.pendingConfirmation ?: return
    val patientName = _uiState.value.patient.name
    val timeLabel = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    val config = _uiState.value.thresholdsConfig

    val assessment =
      when (pending.vitalType) {
        VitalType.GLUCOSE ->
          RiskEngine.evaluateGlucose(
            mgDl = pending.glucoseMgDl ?: return,
            context = context,
            symptoms = symptoms,
            config = config,
            patientName = patientName,
            timeLabel = timeLabel,
          )
        VitalType.BLOOD_PRESSURE ->
          RiskEngine.evaluateBloodPressure(
            systolic = pending.systolic ?: return,
            diastolic = pending.diastolic ?: return,
            context = context,
            symptoms = symptoms,
            config = config,
            patientName = patientName,
            timeLabel = timeLabel,
          )
      }

    val latest = _uiState.value.readings.lastOrNull()
    val newReading =
      SeededReading(
        id = "r-${System.currentTimeMillis()}",
        dayLabel = "Today · $timeLabel",
        glucoseMgDl = pending.glucoseMgDl ?: (latest?.glucoseMgDl ?: 138),
        systolic = pending.systolic ?: (latest?.systolic ?: 134),
        diastolic = pending.diastolic ?: (latest?.diastolic ?: 84),
        contextLabel = context.label,
        status = assessment.statusLevel,
      )

    val nowMs = System.currentTimeMillis()
    val shouldNotify =
      RiskEngine.shouldSendFamilyAlert(
        assessment = assessment,
        lastRecord = lastAlertRecord,
        nowEpochMs = nowMs,
        isRecheck = isRecheck,
        config = config,
      )

    var cooldownMsg: String? = null
    val updatedAlerts = _uiState.value.alerts.toMutableList()

    if (assessment.shouldAlertFamily) {
      if (shouldNotify) {
        lastAlertRecord =
          AlertCooldownRecord(
            vitalType = assessment.vitalType,
            riskLevel = assessment.riskLevel,
            sentAtEpochMs = nowMs,
          )
        updatedAlerts.add(
          0,
          SeededAlert(
            id = "alert-$nowMs",
            title = "${assessment.headline} (Demo alert)",
            message = assessment.whatsappSummarySnippet,
            timestampLabel = "Just now · Demo",
            level = assessment.statusLevel,
            isDemo = true,
          ),
        )
        startDemoEscalationTimer(assessment.whatsappSummarySnippet)
      } else {
        cooldownMsg =
          "Family alert already sent within the last ${config.alertCooldownMinutes} minutes. Use 'Recheck reading' if it is still dangerous."
      }
    }

    val updatedReadings = _uiState.value.readings + newReading
    val updatedTrends = RiskEngine.detectTrendFlags(updatedReadings)

    _uiState.update {
      it.copy(
        readings = updatedReadings,
        alerts = updatedAlerts,
        trendFlags = updatedTrends,
        pendingConfirmation = null,
        latestAssessment = assessment,
        lastAlertSentForAssessment = shouldNotify,
        cooldownSuppressedMessage = cooldownMsg,
        aiPlainExplanation = null,
        showEmergencyScreen = assessment.triggersEmergencyScreen,
        emergencyAssessment = if (assessment.triggersEmergencyScreen) assessment else null,
        toastMessage = "Reading confirmed & saved (${assessment.riskLevel.label}).",
      )
    }
  }

  fun explainAssessmentWithAi(assessment: RiskAssessment) {
    viewModelScope.launch {
      _uiState.update { it.copy(isExplainingAi = true) }
      vitalsAiParser
        .explainRiskPlainly(assessment)
        .onSuccess { explanation ->
          _uiState.update { it.copy(isExplainingAi = false, aiPlainExplanation = explanation) }
        }
        .onFailure { err ->
          _uiState.update {
            it.copy(
              isExplainingAi = false,
              toastMessage = err.message ?: "Could not load plain explanation right now.",
            )
          }
        }
    }
  }

  private fun startDemoEscalationTimer(snippet: String) {
    escalationJob?.cancel()
    val caregivers = _uiState.value.caregivers
    val primary = caregivers.firstOrNull()?.name ?: "Primary family contact"
    val secondary = caregivers.getOrNull(1)?.name ?: "Secondary family contact"

    _uiState.update {
      it.copy(
        escalationDemo =
          EscalationDemoState(
            active = true,
            secondsRemaining = 20,
            primaryCaregiverName = primary,
            secondaryCaregiverName = secondary,
            escalated = false,
            acknowledged = false,
            alertSnippet = snippet,
          ),
      )
    }

    escalationJob =
      viewModelScope.launch {
        for (sec in 19 downTo 0) {
          delay(1000L)
          val current = _uiState.value.escalationDemo
          if (!current.active || current.acknowledged) return@launch
          if (sec == 0) {
            _uiState.update { state ->
              val escalationAlert =
                SeededAlert(
                  id = "esc-${System.currentTimeMillis()}",
                  title = "Escalated to $secondary (Demo 5-min timer)",
                  message =
                    "Demo: $primary did not acknowledge in time, so the alert escalated to $secondary.",
                  timestampLabel = "Just now · Demo",
                  level = HealthStatusLevel.URGENT,
                  isDemo = true,
                )
              state.copy(
                escalationDemo =
                  state.escalationDemo.copy(secondsRemaining = 0, escalated = true),
                alerts = listOf(escalationAlert) + state.alerts,
                toastMessage = "Demo escalation: forwarded to $secondary.",
              )
            }
          } else {
            _uiState.update { state ->
              state.copy(escalationDemo = state.escalationDemo.copy(secondsRemaining = sec))
            }
          }
        }
      }
  }

  fun acknowledgeFamilyAlert() {
    escalationJob?.cancel()
    _uiState.update { state ->
      state.copy(
        escalationDemo = state.escalationDemo.copy(active = false, acknowledged = true),
        toastMessage = "Family alert acknowledged (Demo).",
      )
    }
  }

  fun triggerDemoEscalationTest() {
    val patientName = _uiState.value.patient.name
    startDemoEscalationTimer(
      "Alert: $patientName's blood sugar is 62 mg/dL (low). Please call her now.",
    )
  }

  fun openEmergencyScreenManually(assessment: RiskAssessment? = null) {
    val active =
      assessment
        ?: _uiState.value.emergencyAssessment
        ?: RiskEngine.evaluateGlucose(
          mgDl = 52,
          patientName = _uiState.value.patient.name,
          timeLabel = "now",
        )
    _uiState.update {
      it.copy(showEmergencyScreen = true, emergencyAssessment = active)
    }
  }

  fun dismissEmergencyScreen() {
    _uiState.update { it.copy(showEmergencyScreen = false) }
  }

  fun clearLatestResult() {
    _uiState.update {
      it.copy(
        latestAssessment = null,
        aiPlainExplanation = null,
        cooldownSuppressedMessage = null,
      )
    }
  }

  // --- Phase 3: Prescription Scanner, Interaction/Duplicate Checker, Generic Finder ---

  fun processPrescriptionBitmap(bitmap: Bitmap) {
    val quality = PrescriptionAiParser.assessImageQuality(bitmap)
    if (!quality.isGood) {
      _uiState.update { state ->
        state.copy(
          rxScanner =
            state.rxScanner.copy(
              step = 0,
              capturedBitmap = bitmap,
              qualityIssue = quality,
              isScanning = false,
              scanError = null,
            ),
        )
      }
      return
    }

    if (!_uiState.value.effectiveOnline) {
      _uiState.update { state ->
        state.copy(
          offlineQueue =
            OfflineQueueHelper.enqueue(
              current = state.offlineQueue,
              kind = OfflineTaskKind.PRESCRIPTION_PHOTO,
              summary = "Prescription photo (${bitmap.width}x${bitmap.height})",
              bitmap = bitmap,
            ),
          rxScanner =
            state.rxScanner.copy(
              step = 0,
              capturedBitmap = bitmap,
              qualityIssue = null,
              isScanning = false,
              scanError =
                "No internet right now. Your prescription photo is saved for when internet returns, or tap 'Enter medicines manually' below.",
            ),
          toastMessage = "No internet. Prescription photo saved to sync later.",
        )
      }
      return
    }

    viewModelScope.launch {
      _uiState.update { state ->
        state.copy(
          rxScanner =
            state.rxScanner.copy(
              capturedBitmap = bitmap,
              qualityIssue = null,
              isScanning = true,
              scanError = null,
            ),
        )
      }
      val result = rxAiParser.extractPrescription(bitmap)
      result
        .onSuccess { parsed -> applyParsedPrescription(parsed) }
        .onFailure {
          _uiState.update { state ->
            state.copy(
              rxScanner =
                state.rxScanner.copy(
                  isScanning = false,
                  scanError =
                    "We could not read that photo. Try again in better light, or type the medicines in.",
                ),
            )
          }
        }
    }
  }

  private fun applyParsedPrescription(parsed: ParsedPrescriptionResult) {
    if (parsed.imageQuality == "poor" && parsed.medicines.isEmpty()) {
      _uiState.update { state ->
        state.copy(
          rxScanner =
            state.rxScanner.copy(
              isScanning = false,
              qualityIssue =
                ImageQualityAssessment(
                  isGood = false,
                  issueTitle = "Prescription looks hard to read",
                  issueGuidance =
                    "The handwriting or lighting was unclear. Please retake in bright light or enter manually.",
                ),
            ),
        )
      }
      return
    }

    _uiState.update { state ->
      state.copy(
        rxScanner =
          state.rxScanner.copy(
            step = 1,
            isScanning = false,
            doctorName = parsed.doctorName,
            rxDate = parsed.date,
            extractedMedicines = parsed.medicines,
            warnings = parsed.warnings,
            scanError = null,
          ),
      )
    }
  }

  fun loadDemoSamplePrescription() {
    val currentPatient = _uiState.value.patient
    val sampleBitmap =
      runCatching {
        createSamplePrescriptionBitmap(
          patientName = currentPatient.name,
          patientAge = currentPatient.age,
        )
      }.getOrNull()
    val demoExtracted =
      listOf(
        ExtractedRxMedicine(
          id = "demo-rx-1",
          nameAsWritten = "Tab Brufen 400",
          normalizedName = "Ibuprofen",
          strength = "400 mg",
          form = "tablet",
          frequency = "BD",
          timing = "after_food",
          durationDays = 5,
          confidence = 0.92,
          needsVerification = false,
        ),
        ExtractedRxMedicine(
          id = "demo-rx-2",
          nameAsWritten = "Tab Telma 40",
          normalizedName = "Telmisartan",
          strength = "40 mg",
          form = "tablet",
          frequency = "OD",
          timing = "morning",
          durationDays = 30,
          confidence = 0.88,
          needsVerification = false,
        ),
        ExtractedRxMedicine(
          id = "demo-rx-3",
          nameAsWritten = "Cap Pan-D / Dolo 650?",
          normalizedName = "Dolo 650",
          strength = "650 mg",
          form = "tablet",
          frequency = "SOS",
          timing = "after_food",
          durationDays = 3,
          confidence = 0.58, // Forces amber "Please verify" state
          needsVerification = true,
        ),
      )
    _uiState.update { state ->
      state.copy(
        rxScanner =
          RxScannerState(
            step = 1,
            capturedBitmap = sampleBitmap,
            isSampleResult = true,
            doctorName = "Dr. R. Mehta (Demo Rx)",
            rxDate = "15 Nov 2026",
            extractedMedicines = demoExtracted,
            warnings = listOf("Sample result: One medicine line has low confidence. Please verify before saving."),
          ),
      )
    }
  }

  fun startManualPrescriptionEntry() {
    _uiState.update { state ->
      state.copy(
        rxScanner =
          RxScannerState(
            step = 1,
            extractedMedicines =
              listOf(
                ExtractedRxMedicine(
                  id = "manual-${System.currentTimeMillis()}",
                  nameAsWritten = "",
                  normalizedName = null,
                  strength = "",
                  form = "tablet",
                  frequency = "OD",
                  timing = "after_food",
                  durationDays = 15,
                  confidence = 1.0,
                  needsVerification = false,
                ),
              ),
          ),
      )
    }
  }

  fun updateRxMedicine(updated: ExtractedRxMedicine) {
    _uiState.update { state ->
      state.copy(
        rxScanner =
          state.rxScanner.copy(
            extractedMedicines =
              state.rxScanner.extractedMedicines.map {
                if (it.id == updated.id) updated else it
              },
          ),
      )
    }
  }

  fun addBlankRxMedicine() {
    _uiState.update { state ->
      val next =
        ExtractedRxMedicine(
          id = "manual-${System.currentTimeMillis()}",
          nameAsWritten = "",
          normalizedName = null,
          strength = "",
          form = "tablet",
          frequency = "OD",
          timing = "after_food",
          durationDays = 15,
          confidence = 1.0,
          needsVerification = false,
        )
      state.copy(
        rxScanner =
          state.rxScanner.copy(extractedMedicines = state.rxScanner.extractedMedicines + next),
      )
    }
  }

  fun removeRxMedicine(id: String) {
    _uiState.update { state ->
      state.copy(
        rxScanner =
          state.rxScanner.copy(
            extractedMedicines = state.rxScanner.extractedMedicines.filterNot { it.id == id },
          ),
      )
    }
  }

  fun runRxSafetyChecks() {
    val dataset = _uiState.value.drugDataset ?: return
    val rxMeds =
      _uiState.value.rxScanner.extractedMedicines.filter { it.nameAsWritten.isNotBlank() }
    if (rxMeds.isEmpty()) {
      showToast("Add at least one medicine name before running checks.")
      return
    }
    val combinedInputs =
      _uiState.value.medicines.map {
        MedicineCheckInput(it.brandName, it.genericName, it.strength)
      } +
        rxMeds.map {
          MedicineCheckInput(it.nameAsWritten, it.normalizedName, it.strength)
        }
    val report = InteractionChecker.runSafetyCheck(dataset, combinedInputs)
    _uiState.update { state ->
      state.copy(
        rxScanner =
          state.rxScanner.copy(
            step = 2,
            checkReport = report,
            interactionExplanation = null,
          ),
      )
    }
  }

  fun explainInteractionWithAi(warning: InteractionWarning) {
    viewModelScope.launch {
      _uiState.update { it.copy(rxScanner = it.rxScanner.copy(isExplainingInteraction = true)) }
      rxAiParser
        .explainInteractionPlainly(warning)
        .onSuccess { text ->
          _uiState.update {
            it.copy(
              rxScanner =
                it.rxScanner.copy(
                  isExplainingInteraction = false,
                  interactionExplanation = text,
                ),
            )
          }
        }
        .onFailure { err ->
          _uiState.update {
            it.copy(
              rxScanner = it.rxScanner.copy(isExplainingInteraction = false),
              toastMessage = err.message ?: "Could not load explanation.",
            )
          }
        }
    }
  }

  fun saveRxToSchedule() {
    val rxMeds =
      _uiState.value.rxScanner.extractedMedicines.filter { it.nameAsWritten.isNotBlank() }
    if (rxMeds.isEmpty()) return

    val newSeededMeds =
      rxMeds.map { rx ->
        val slot =
          when (rx.timing) {
            "bedtime" -> DaySlot.NIGHT
            "morning", "before_food" -> DaySlot.MORNING
            else -> DaySlot.AFTERNOON
          }
        val dailyUnits =
          when (rx.frequency.uppercase()) {
            "BD" -> 2
            "TDS" -> 3
            "QID" -> 4
            else -> 1
          }
        val foodLabel =
          when (rx.timing) {
            "before_food" -> "Before food"
            "after_food" -> "After food"
            "bedtime" -> "At bedtime"
            "morning" -> "In the morning"
            else -> "As prescribed"
          }
        SeededMedicine(
          id = "med-rx-${rx.id}",
          brandName = rx.nameAsWritten.trim(),
          genericName = rx.normalizedName?.takeIf { it.isNotBlank() } ?: rx.nameAsWritten.trim(),
          strength = rx.strength?.takeIf { it.isNotBlank() } ?: "Not set",
          form = rx.form.replaceFirstChar { it.uppercase() },
          frequency = rx.frequency,
          foodTiming = foodLabel,
          slot = slot,
          stockCount = (rx.durationDays ?: 10) * dailyUnits,
          unit = "${rx.form}s",
          dailyUnits = dailyUnits,
          isCritical = false,
        )
      }

    val newDoseEvents =
      newSeededMeds.map { med ->
        SeededDoseEvent(
          id = "dose-${med.id}",
          medicineId = med.id,
          medicineLabel = "${med.brandName} (${med.strength})",
          instruction = "1 ${med.form.lowercase()} · ${med.foodTiming}",
          slot = med.slot,
          taken = false,
        )
      }

    _uiState.update { state ->
      val mergedMeds = state.medicines + newSeededMeds
      val mergedDoses = state.dosesToday + newDoseEvents
      val refreshedReport =
        state.drugDataset?.let { ds ->
          runSafetyReportFor(ds, mergedMeds, state.extraCheckMedicineName)
        }
      state.copy(
        medicines = mergedMeds,
        dosesToday = mergedDoses,
        activeSafetyReport = refreshedReport,
        hasScannedPrescription = true,
        rxScanner = state.rxScanner.copy(step = 3),
        toastMessage = "Added ${newSeededMeds.size} medicine(s) to your daily schedule.",
      )
    }
  }

  fun setRxStep(step: Int) {
    _uiState.update { it.copy(rxScanner = it.rxScanner.copy(step = step.coerceIn(0, 3))) }
  }

  fun resetRxScanner() {
    _uiState.update { it.copy(rxScanner = RxScannerState()) }
  }

  fun checkExtraMedicineName(name: String) {
    val cleaned = name.trim()
    _uiState.update { state ->
      val report =
        state.drugDataset?.let { ds ->
          runSafetyReportFor(ds, state.medicines, cleaned)
        }
      state.copy(extraCheckMedicineName = cleaned, activeSafetyReport = report)
    }
  }

  private fun runSafetyReportFor(
    dataset: DrugKnowledgeDataset,
    medicines: List<SeededMedicine>,
    extraName: String,
  ): MedicineSafetyCheckReport {
    val inputs =
      medicines.map {
        MedicineCheckInput(it.brandName, it.genericName, it.strength)
      }.toMutableList()
    if (extraName.isNotBlank()) {
      inputs += MedicineCheckInput(displayName = extraName)
    }
    return InteractionChecker.runSafetyCheck(dataset, inputs)
  }

  // --- Phase 4: Refills, Profile, Caregiver Consent Lifecycle, Data Export & Deletion ---

  fun restockMedicine(medicineId: String, addedUnits: Int) {
    _uiState.update { state ->
      val target = state.medicines.find { it.id == medicineId } ?: return@update state
      val (updated, history) = RefillEngine.restock(target, addedUnits)
      state.copy(
        medicines = state.medicines.map { if (it.id == medicineId) updated else it },
        refillHistory = listOf(history) + state.refillHistory,
        toastMessage = "Restocked ${updated.brandName} (+$addedUnits ${updated.unit}).",
      )
    }
  }

  fun adjustMedicineStock(medicineId: String, newCount: Int, markUnknown: Boolean) {
    _uiState.update { state ->
      val target = state.medicines.find { it.id == medicineId } ?: return@update state
      val (updated, history) = RefillEngine.adjustStock(target, newCount, markUnknown)
      state.copy(
        medicines = state.medicines.map { if (it.id == medicineId) updated else it },
        refillHistory = listOf(history) + state.refillHistory,
        toastMessage =
          if (markUnknown) "${updated.brandName} stock marked unknown. Ask your pharmacist."
          else "Updated ${updated.brandName} stock to ${updated.stockCount} ${updated.unit}.",
      )
    }
  }

  fun snoozeMedicineRefill(medicineId: String, days: Int = 2) {
    _uiState.update { state ->
      val target = state.medicines.find { it.id == medicineId } ?: return@update state
      val (updated, history) = RefillEngine.snoozeReminder(target, days)
      state.copy(
        medicines = state.medicines.map { if (it.id == medicineId) updated else it },
        refillHistory = listOf(history) + state.refillHistory,
        toastMessage = "Snoozed refill reminder for ${updated.brandName} ($days days).",
      )
    }
  }

  fun recordPharmacyRefillIntent(medicineName: String) {
    val entry =
      RefillHistoryEntry(
        id = "ph-${System.currentTimeMillis()}",
        medicineId = "pharmacy",
        medicineName = medicineName,
        actionLabel = "Opened WhatsApp refill request (Demo)",
        timestampLabel = "Just now",
        deltaUnits = 0,
      )
    _uiState.update { state ->
      state.copy(
        refillHistory = listOf(entry) + state.refillHistory,
        toastMessage = "Prepared pharmacy WhatsApp order (Demo).",
      )
    }
  }

  fun updatePatientProfile(
    name: String,
    age: Int,
    conditionsText: String,
    pharmacyName: String,
    pharmacyPhone: String,
  ) {
    val cleanedName = ProfileDomain.sanitizeText(name, ProfileDomain.MAX_NAME_LENGTH).ifEmpty { _uiState.value.patient.name }
    val parsedConditions =
      conditionsText
        .split(",")
        .map { ProfileDomain.sanitizeText(it, 48) }
        .filter { it.isNotEmpty() }
        .ifEmpty { _uiState.value.patient.conditions }
    val current = _uiState.value.patient
    val updatedPatient =
      current.copy(
        name = cleanedName,
        age = age.coerceIn(1, 120),
        conditions = parsedConditions,
        pharmacyName = ProfileDomain.sanitizeText(pharmacyName, 80).ifEmpty { current.pharmacyName },
        pharmacyPhone = ProfileDomain.sanitizeText(pharmacyPhone, 24).ifEmpty { current.pharmacyPhone },
      )
    updateFullPatientProfile(updatedPatient, _uiState.value.reminderPrefs)
  }

  fun updateFullPatientProfile(
    updatedPatient: PatientProfile,
    updatedReminders: ReminderPreferences? = null,
  ) {
    val cleanedName =
      ProfileDomain.sanitizeText(updatedPatient.name, ProfileDomain.MAX_NAME_LENGTH)
        .ifEmpty { _uiState.value.patient.name }
    val slug =
      cleanedName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), ".").trim('.').ifEmpty { "patient" }
    val isSunitaDemoName = cleanedName.equals(DemoSeedData.patient.name, ignoreCase = true)
    val resolvedHandle =
      if (
        updatedPatient.handle.isBlank() ||
          (updatedPatient.handle.equals("sunita.devi", ignoreCase = true) && !isSunitaDemoName)
      ) {
        slug
      } else {
        updatedPatient.handle
      }
    val finalPatient = updatedPatient.copy(name = cleanedName, handle = resolvedHandle)

    var persistedEmail = ""
    var persistedDemoMode = false

    _uiState.update { state ->
      val currentEmail = state.session.email.trim()
      val nextEmail =
        if (
          !isSunitaDemoName &&
            (currentEmail.isEmpty() || currentEmail.equals("sunita.demo@sehatsaathi.local", ignoreCase = true))
        ) {
          "$slug@sehatsaathi.app"
        } else {
          currentEmail
        }
      val nextDemoMode = if (!isSunitaDemoName) false else state.session.isDemoMode
      persistedEmail = nextEmail
      persistedDemoMode = nextDemoMode

      // Keep caregivers synchronized with emergency contacts so SOS and family alerts stay consistent
      val updatedCaregivers =
        if (finalPatient.emergencyContacts.isNotEmpty()) {
          val existingByIdOrPhone = state.caregivers.associateBy { it.phone.filter { ch -> ch.isDigit() } }
          val syncedFromEmergency =
            finalPatient.emergencyContacts.mapIndexed { idx, ec ->
              val match = existingByIdOrPhone[ec.phone.filter { ch -> ch.isDigit() }]
              SeededCaregiver(
                id = match?.id ?: ec.id,
                name = ec.name,
                relationship =
                  if (ec.isPrimary && !ec.relationship.contains("Primary", ignoreCase = true)) {
                    "${ec.relationship} (Primary)"
                  } else {
                    ec.relationship
                  },
                phone = ec.phone,
                consentStatus = match?.consentStatus ?: "Consented",
                priority = if (ec.isPrimary) 1 else (idx + 2),
                sinceLabel = match?.sinceLabel ?: "Nov 2026",
                permissions = match?.permissions ?: CaregiverSharingPermissions.DEFAULT_CONSENTED,
              )
            }.sortedBy { it.priority }
          syncedFromEmergency
        } else {
          state.caregivers
        }

      val updatedAlerts =
        if (!isSunitaDemoName) {
          state.alerts.map { alert ->
            alert.copy(
              message =
                alert.message
                  .replace("Sunita Devi", cleanedName, ignoreCase = true)
                  .replace(state.patient.name, cleanedName, ignoreCase = true),
              isDemo = nextDemoMode,
            )
          }
        } else {
          state.alerts
        }

      state.copy(
        session =
          state.session.copy(
            displayName = cleanedName,
            email = nextEmail,
            isDemoMode = nextDemoMode,
            savedPersonalName =
              if (!nextDemoMode) cleanedName else state.session.savedPersonalName,
            savedPersonalEmail =
              if (!nextDemoMode) nextEmail else state.session.savedPersonalEmail,
            savedPersonalAge =
              if (!nextDemoMode) finalPatient.age else state.session.savedPersonalAge,
            savedPersonalConditions =
              if (!nextDemoMode) finalPatient.conditions.joinToString(", ")
              else state.session.savedPersonalConditions,
            savedPersonalCity =
              if (!nextDemoMode) finalPatient.city else state.session.savedPersonalCity,
            savedPersonalBloodGroup =
              if (!nextDemoMode) finalPatient.bloodGroup else state.session.savedPersonalBloodGroup,
            savedPersonalBio =
              if (!nextDemoMode) finalPatient.bio else state.session.savedPersonalBio,
          ),
        patient = finalPatient,
        reminderPrefs = updatedReminders ?: state.reminderPrefs,
        caregivers = updatedCaregivers,
        alerts = updatedAlerts,
        toastMessage = "Profile updated for $cleanedName",
      )
    }

    viewModelScope.launch {
      repository.updateProfileIdentity(
        name = cleanedName,
        email = persistedEmail,
        isDemoMode = persistedDemoMode,
        age = finalPatient.age,
        conditionsCsv = finalPatient.conditions.joinToString(", "),
        city = finalPatient.city,
        bloodGroup = finalPatient.bloodGroup,
        bio = finalPatient.bio,
      )
    }
  }

  fun updateAvatarBitmap(bitmap: Bitmap?) {
    _uiState.update {
      it.copy(
        avatarBitmap = bitmap,
        toastMessage = if (bitmap != null) "Avatar photo updated" else "Avatar photo removed",
      )
    }
  }

  fun updateCoverBitmap(bitmap: Bitmap?) {
    _uiState.update {
      it.copy(
        coverBitmap = bitmap,
        toastMessage = if (bitmap != null) "Cover photo updated" else "Cover photo removed",
      )
    }
  }

  fun togglePhotoPrivacy(showPhotoToCaregivers: Boolean, useInitialsOnly: Boolean) {
    _uiState.update { state ->
      state.copy(
        patient =
          state.patient.copy(
            showPhotoToCaregivers = showPhotoToCaregivers,
            useInitialsOnly = useInitialsOnly,
          ),
        toastMessage = "Photo privacy settings saved.",
      )
    }
  }

  fun toggleHideAchievements(hide: Boolean) {
    _uiState.update { state ->
      state.copy(
        patient = state.patient.copy(hideAchievements = hide),
        toastMessage = if (hide) "Achievements hidden on profile." else "Achievements visible on profile.",
      )
    }
  }

  fun dismissProfileCompleteness() {
    _uiState.update { state ->
      state.copy(patient = state.patient.copy(completenessDismissed = true))
    }
  }

  fun updateNotificationSwitches(prefs: NotificationSwitchPrefs) {
    _uiState.update {
      it.copy(
        notificationSwitches = prefs,
        weeklyDigestOptedIn = prefs.weeklySummary,
        toastMessage = "Notification preferences saved on this device.",
      )
    }
  }

  fun updateCaregiverSharingPermission(
    caregiverId: String,
    transform: (CaregiverSharingPermissions) -> CaregiverSharingPermissions,
  ) {
    _uiState.update { state ->
      val updated = ProfileDomain.updateCaregiverPermission(state.caregivers, caregiverId, transform)
      state.copy(
        caregivers = updated,
        toastMessage = "Caregiver sharing permissions updated immediately.",
      )
    }
  }

  fun resendCaregiverInvite(caregiverId: String) {
    _uiState.update { state ->
      val target = state.caregivers.find { it.id == caregiverId }
      state.copy(
        toastMessage = "Resent consent invite to ${target?.name ?: "caregiver"} (Demo).",
      )
    }
  }

  fun updateReminderPreferences(prefs: ReminderPreferences) {
    _uiState.update {
      it.copy(
        reminderPrefs = prefs,
        toastMessage = "Reminder times and quiet hours updated.",
      )
    }
  }

  fun updateRiskThresholds(config: RiskThresholdsConfig) {
    _uiState.update {
      it.copy(
        thresholdsConfig = config,
        toastMessage = "Safety thresholds updated.",
      )
    }
  }

  fun addCaregiverInvite(name: String, relationship: String, phone: String): String? {
    val cleanName = ProfileDomain.sanitizeText(name, 60)
    val cleanPhone = phone.trim()
    if (cleanName.length < 2) return "Please enter the family contact's name."
    if (cleanPhone.length < 7) return "Please enter a valid phone number."
    val nextPriority = (_uiState.value.caregivers.maxOfOrNull { it.priority } ?: 0) + 1
    val newCg =
      SeededCaregiver(
        id = "cg-${System.currentTimeMillis()}",
        name = cleanName,
        relationship = ProfileDomain.sanitizeText(relationship, 32).ifEmpty { "Family" },
        phone = cleanPhone,
        consentStatus = "Pending invite",
        priority = nextPriority,
        sinceLabel = "Invited today",
        permissions = CaregiverSharingPermissions.DEFAULT_OFF,
      )
    _uiState.update { state ->
      state.copy(
        caregivers = state.caregivers + newCg,
        toastMessage = "Invite created for $cleanName (awaiting consent).",
      )
    }
    return null
  }

  fun acceptCaregiverConsentDemo(caregiverId: String) {
    _uiState.update { state ->
      val updated =
        state.caregivers.map { cg ->
          if (cg.id == caregiverId) {
            cg.copy(
              consentStatus = "Consented",
              sinceLabel = "Just now",
              permissions = CaregiverSharingPermissions.DEFAULT_CONSENTED,
            )
          } else {
            cg
          }
        }
      state.copy(
        caregivers = updated,
        toastMessage = "Caregiver consent accepted (Demo).",
      )
    }
  }

  fun revokeCaregiverConsent(caregiverId: String) {
    _uiState.update { state ->
      val target = state.caregivers.find { it.id == caregiverId }
      state.copy(
        caregivers = state.caregivers.filterNot { it.id == caregiverId },
        toastMessage = "Revoked consent and removed ${target?.name ?: "contact"}.",
      )
    }
  }

  fun verifyReauth(emailInput: String, passwordOrConfirm: String): Boolean {
    val sessionEmail = _uiState.value.session.email.trim().lowercase()
    val cleanEmail = emailInput.trim().lowercase()
    val cleanSecret = passwordOrConfirm.trim()
    if (_uiState.value.session.isDemoMode) {
      return (cleanEmail.isEmpty() || cleanEmail == sessionEmail) &&
        (cleanSecret.equals("CONFIRM", ignoreCase = true) || cleanSecret.length >= 6)
    }
    return cleanEmail == sessionEmail && cleanSecret.length >= 10
  }

  fun deleteAccountAndAllData(confirmationText: String, context: Context): Boolean {
    if (confirmationText.trim() != "DELETE") return false
    DoctorSummaryAndDataExporter.clearExportCache(context)
    resetDemoDataInternal()
    _uiState.update {
      it.copy(
        session = AuthSession(),
        toastMessage = "Account and all local SehatSaathi data permanently deleted.",
      )
    }
    viewModelScope.launch {
      repository.clearSession()
    }
    return true
  }

  fun resetDemoData() {
    resetDemoDataInternal()
    _uiState.update { it.copy(toastMessage = "Demo data reset to initial seeded state.") }
  }

  private fun resetDemoDataInternal() {
    escalationJob?.cancel()
    lastAlertRecord = null
    runCatching { DoctorSummaryAndDataExporter.clearExportCache(getApplication()) }
    _uiState.update { state ->
      val report =
        state.drugDataset?.let { ds ->
          runSafetyReportFor(ds, DemoSeedData.medicines, "")
        }
      state.copy(
        patient = DemoSeedData.patient,
        reminderPrefs = ReminderPreferences(),
        medicines = DemoSeedData.medicines,
        refillHistory = DemoSeedData.initialRefillHistory,
        dosesToday = DemoSeedData.dosesToday,
        skippedDoseIds = emptySet(),
        readings = DemoSeedData.readings30Days,
        alerts = DemoSeedData.alerts,
        caregivers = DemoSeedData.caregivers,
        thresholdsConfig = RiskThresholdsConfig(),
        extraCheckMedicineName = "",
        activeSafetyReport = report,
        trendFlags = RiskEngine.detectTrendFlags(DemoSeedData.readings30Days),
        pendingConfirmation = null,
        latestAssessment = null,
        aiParseError = null,
        aiPlainExplanation = null,
        cooldownSuppressedMessage = null,
        showEmergencyScreen = false,
        emergencyAssessment = null,
        escalationDemo = EscalationDemoState(),
        rxScanner = RxScannerState(),
        isOfflineSimulationEnabled = false,
        offlineQueue = emptyList(),
        isSyncingOfflineQueue = false,
        activeDemoScenario = null,
        activeDemoScenarioCaption = null,
        pendingDemoNavigationRoute = null,
        avatarBitmap = null,
        coverBitmap = null,
        notificationSwitches = NotificationSwitchPrefs(),
        hasScannedPrescription = false,
      )
    }
  }

  // --- Phase 5: Offline Mode Simulation & Queued AI Sync ---

  fun toggleOfflineSimulation(enabled: Boolean) {
    _uiState.update { state ->
      state.copy(
        isOfflineSimulationEnabled = enabled,
        toastMessage =
          if (enabled) "Offline mode simulated (Demo). AI scans will queue locally."
          else "Back online (Demo). Ready to sync queued AI scans.",
      )
    }
  }

  fun syncOfflineQueue() {
    val state = _uiState.value
    if (!state.effectiveOnline) {
      showToast("Still offline. Connect to internet or turn off offline simulation first.")
      return
    }
    if (state.offlineQueue.isEmpty() || state.isSyncingOfflineQueue) return

    viewModelScope.launch {
      _uiState.update { it.copy(isSyncingOfflineQueue = true) }
      val tasks = _uiState.value.offlineQueue
      var syncedCount = 0
      var failedCount = 0

      for (task in tasks) {
        when (task.kind) {
          OfflineTaskKind.METER_PHOTO -> {
            val bmp = task.bitmap
            if (bmp != null) {
              val res = vitalsAiParser.parseMeterPhoto(bmp)
              if (res.isSuccess) {
                res.getOrNull()?.let { parsed ->
                  handleAiExtraction(parsed, sourceLabel = "Synced meter photo")
                }
                _uiState.update { s ->
                  s.copy(offlineQueue = OfflineQueueHelper.remove(s.offlineQueue, task.id))
                }
                syncedCount++
              } else {
                failedCount++
              }
            } else {
              _uiState.update { s ->
                s.copy(offlineQueue = OfflineQueueHelper.remove(s.offlineQueue, task.id))
              }
            }
          }
          OfflineTaskKind.VOICE_PHRASE -> {
            val phrase = task.transcript.orEmpty()
            val parsed = vitalsAiParser.parseVoiceTranscript(phrase)
            if (parsed != null) {
              handleAiExtraction(
                parsed,
                sourceLabel =
                  if (parsed.usedOfflineFallback) "Voice (synced fallback)" else "Synced voice",
              )
              _uiState.update { s ->
                s.copy(offlineQueue = OfflineQueueHelper.remove(s.offlineQueue, task.id))
              }
              syncedCount++
            } else {
              failedCount++
            }
          }
          OfflineTaskKind.PRESCRIPTION_PHOTO -> {
            val bmp = task.bitmap
            if (bmp != null) {
              val res = rxAiParser.extractPrescription(bmp)
              if (res.isSuccess) {
                res.getOrNull()?.let { parsed -> applyParsedPrescription(parsed) }
                _uiState.update { s ->
                  s.copy(offlineQueue = OfflineQueueHelper.remove(s.offlineQueue, task.id))
                }
                syncedCount++
              } else {
                failedCount++
              }
            } else {
              _uiState.update { s ->
                s.copy(offlineQueue = OfflineQueueHelper.remove(s.offlineQueue, task.id))
              }
            }
          }
        }
      }

      _uiState.update {
        it.copy(
          isSyncingOfflineQueue = false,
          toastMessage =
            when {
              syncedCount > 0 && failedCount == 0 ->
                "Synced $syncedCount queued scan(s). Please verify before saving."
              syncedCount > 0 ->
                "Synced $syncedCount scan(s); $failedCount item(s) stayed queued."
              else ->
                "Could not sync queued items right now. You can enter readings or medicines manually."
            },
        )
      }
    }
  }

  fun setCaregiverViewMode(enabled: Boolean) {
    _uiState.update { it.copy(caregiverViewMode = enabled) }
  }

  fun toggleWeeklyDigestOptIn(optedIn: Boolean) {
    _uiState.update { it.copy(weeklyDigestOptedIn = optedIn) }
    showToast(if (optedIn) "Subscribed to weekly caregiver digest." else "Unsubscribed from weekly caregiver digest.")
  }

  fun setSimpleMode(active: Boolean) {
    _uiState.update { it.copy(simpleModeActive = active) }
    showToast(if (active) "Simple Mode enabled (Voice-first, large text)." else "Returned to full view.")
  }

  fun logout() {
    resetDemoDataInternal()
    _uiState.update { state ->
      state.copy(
        session =
          state.session.copy(
            isAuthenticated = false,
            isDemoMode = false,
          ),
        toastMessage = "Signed out. You can log back in, create a new account, or switch to Demo Mode anytime.",
      )
    }
    viewModelScope.launch {
      repository.logoutSessionOnly()
    }
  }

  fun dismissToast() {
    _uiState.update { it.copy(toastMessage = null) }
  }

  fun showToast(message: String) {
    _uiState.update { it.copy(toastMessage = message) }
  }
}

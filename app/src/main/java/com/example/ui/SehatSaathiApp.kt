package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.RefillStatus
import com.example.data.SiteConfig
import com.example.ui.components.AppRoutes
import com.example.ui.components.GuidedTourOverlay
import com.example.ui.components.SehatAppShell
import com.example.ui.components.SehatToastBanner
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyScreen
import com.example.ui.screens.FamilyAlertsScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.InfoPagesScreen
import com.example.ui.screens.InfoSection
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LogInScreen
import com.example.ui.screens.LogReadingScreen
import com.example.ui.screens.MedicinesAndScheduleScreen
import com.example.ui.screens.NotFoundScreen
import com.example.ui.screens.PreloaderOverlay
import com.example.ui.screens.ScanPrescriptionScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.SehatSaathiTheme

@Composable
fun SehatSaathiApp(
  viewModel: SehatSaathiViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val reducedMotion = rememberPrefersReducedMotion()
  val context = LocalContext.current
  val view = LocalView.current
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.LANDING
  val currentInfoArg = backStackEntry?.arguments?.getString("section")

  LaunchedEffect(reducedMotion) {
    viewModel.runSessionPreloaderIfNeeded(reducedMotion = reducedMotion)
  }

  val authOnlyPublicRoutes =
    setOf(
      AppRoutes.LANDING,
      AppRoutes.LOG_IN,
      AppRoutes.SIGN_UP,
      AppRoutes.FORGOT_PASSWORD,
    )
  val allowedWhenLoggedOut = authOnlyPublicRoutes + setOf(AppRoutes.INFO_ROUTE, AppRoutes.NOT_FOUND)

  LaunchedEffect(uiState.isSessionLoaded, uiState.session.isAuthenticated, currentRoute) {
    if (!uiState.isSessionLoaded) return@LaunchedEffect
    if (uiState.session.isAuthenticated && currentRoute in authOnlyPublicRoutes) {
      navController.navigate(AppRoutes.DASHBOARD) {
        popUpTo(0) { inclusive = true }
      }
    } else if (!uiState.session.isAuthenticated && currentRoute !in allowedWhenLoggedOut) {
      navController.navigate(AppRoutes.LANDING) {
        popUpTo(0) { inclusive = true }
      }
    }
  }

  LaunchedEffect(
    uiState.isSessionLoaded,
    uiState.session.isAuthenticated,
    currentRoute,
    uiState.pendingDemoNavigationRoute,
  ) {
    val targetRoute = uiState.pendingDemoNavigationRoute
    if (
      uiState.isSessionLoaded &&
        uiState.session.isAuthenticated &&
        currentRoute !in authOnlyPublicRoutes &&
        targetRoute != null
    ) {
      navigateInsideShell(navController, targetRoute)
      viewModel.consumePendingDemoNavigation()
    }
  }

  LaunchedEffect(currentRoute, currentInfoArg, uiState.showEmergencyScreen) {
    val pageLabel =
      if (uiState.showEmergencyScreen) {
        "Emergency help"
      } else {
        routePageTitle(currentRoute, currentInfoArg)
      }
    val fullTitle = "$pageLabel | ${SiteConfig.APP_NAME}"
    context.findActivity()?.title = fullTitle
    @Suppress("DEPRECATION")
    view.announceForAccessibility(fullTitle)
  }

  val refillAlertCount =
    uiState.medicines.count {
      it.refillStatus == RefillStatus.DUE ||
        it.refillStatus == RefillStatus.SOON ||
        it.refillStatus == RefillStatus.OUT ||
        it.refillStatus == RefillStatus.UNKNOWN
    }

  val homeRoute =
    if (uiState.session.isAuthenticated) AppRoutes.DASHBOARD else AppRoutes.LANDING

  fun goHomeClearingBackstack() {
    navController.navigate(homeRoute) {
      popUpTo(0) { inclusive = true }
    }
  }

  SehatSaathiTheme(themePreference = uiState.themePreference) {
    val isDark = SehatPalette.current.isDark
    val toggleTheme: () -> Unit = { viewModel.toggleDarkMode(isDark) }

    Box(modifier = Modifier.fillMaxSize()) {
      if (uiState.isSessionLoaded) {
      NavHost(
        navController = navController,
        startDestination =
          if (uiState.session.isAuthenticated) AppRoutes.DASHBOARD else AppRoutes.LANDING,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
      ) {
        composable(AppRoutes.LANDING) {
          LandingScreen(
            onGetStarted = { navController.navigate(AppRoutes.SIGN_UP) },
            onLogIn = { navController.navigate(AppRoutes.LOG_IN) },
            onTryDemo = { viewModel.enterDemoMode() },
            onLaunchDemoScenario = viewModel::launchDemoScenarioFromLanding,
            onOpenInfo = { section -> navController.navigate(AppRoutes.infoRoute(section)) },
          )
        }

        composable(AppRoutes.LOG_IN) {
          LogInScreen(
            onBack = { navController.popBackStack() },
            onLoginSubmit = { email, password -> viewModel.signInDemoLocal(email, password) },
            onGoToSignUp = { navController.navigate(AppRoutes.SIGN_UP) },
            onGoToForgotPassword = { navController.navigate(AppRoutes.FORGOT_PASSWORD) },
            onTryDemo = { viewModel.enterDemoMode() },
            onHomeClick = { goHomeClearingBackstack() },
            onOpenInfo = { section -> navController.navigate(AppRoutes.infoRoute(section)) },
          )
        }

        composable(AppRoutes.SIGN_UP) {
          SignUpScreen(
            onBack = { navController.popBackStack() },
            onSignUpSubmit = { name, email, password ->
              viewModel.signUpDemoLocal(name, email, password)
            },
            onGoToLogin = { navController.navigate(AppRoutes.LOG_IN) },
            onTryDemo = { viewModel.enterDemoMode() },
            onHomeClick = { goHomeClearingBackstack() },
            onOpenInfo = { section -> navController.navigate(AppRoutes.infoRoute(section)) },
          )
        }

        composable(AppRoutes.FORGOT_PASSWORD) {
          ForgotPasswordScreen(
            onBack = { navController.popBackStack() },
            onBackToLogin = { navController.popBackStack() },
            onHomeClick = { goHomeClearingBackstack() },
            onOpenInfo = { section -> navController.navigate(AppRoutes.infoRoute(section)) },
          )
        }

        composable(
          route = AppRoutes.INFO_ROUTE,
          arguments = listOf(navArgument("section") { type = NavType.StringType }),
        ) { entry ->
          val sectionArg = entry.arguments?.getString("section")
          val section =
            InfoSection.entries.firstOrNull { it.name.equals(sectionArg, ignoreCase = true) }
              ?: InfoSection.SAFETY
          if (uiState.session.isAuthenticated) {
            ProtectedShell(
              currentRoute = AppRoutes.INFO_ROUTE,
              uiState = uiState,
              refillAlertCount = refillAlertCount,
              onDismissToast = viewModel::dismissToast,
              onSyncOfflineQueue = viewModel::syncOfflineQueue,
              onToggleTheme = toggleTheme,
              onNavigate = { route -> navigateInsideShell(navController, route) },
            ) { padding ->
              InfoPagesScreen(
                initialSection = section,
                isAuthenticated = true,
                isDemoMode = uiState.session.isDemoMode,
                onBack = { navController.popBackStack() },
                onHomeClick = { navigateInsideShell(navController, AppRoutes.DASHBOARD) },
                onShowToast = viewModel::showToast,
                contentPadding = padding,
              )
            }
          } else {
            InfoPagesScreen(
              initialSection = section,
              isAuthenticated = false,
              isDemoMode = false,
              onBack = { navController.popBackStack() },
              onHomeClick = { goHomeClearingBackstack() },
              onShowToast = viewModel::showToast,
            )
          }
        }

        composable(AppRoutes.NOT_FOUND) {
          NotFoundScreen(
            isAuthenticated = uiState.session.isAuthenticated,
            isDemoMode = uiState.session.isDemoMode,
            onGoHome = { goHomeClearingBackstack() },
            onGoBack = {
              if (!navController.popBackStack()) goHomeClearingBackstack()
            },
            onOpenInfo = { section -> navController.navigate(AppRoutes.infoRoute(section)) },
          )
        }

        composable(AppRoutes.DASHBOARD) {
          ProtectedShell(
            currentRoute = AppRoutes.DASHBOARD,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            DashboardScreen(
              uiState = uiState,
              contentPadding = padding,
              onToggleDoseTaken = viewModel::toggleDoseTaken,
              onNavigate = { route -> navigateInsideShell(navController, route) },
              onRunDemoScenario = viewModel::runDemoScenario,
              onResetDemo = viewModel::resetDemoData,
              onReplayTour = viewModel::replayGuidedTour,
            )
          }
        }

        composable(AppRoutes.LOG_READING) {
          ProtectedShell(
            currentRoute = AppRoutes.LOG_READING,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            LogReadingScreen(
              uiState = uiState,
              contentPadding = padding,
              onStageKeypadConfirmation = viewModel::stageKeypadConfirmation,
              onParsePhotoBitmap = viewModel::parseMeterBitmapWithAi,
              onParseVoicePhrase = viewModel::parseVoicePhrase,
              onClearPending = viewModel::clearPendingConfirmation,
              onConfirmAndSave = viewModel::confirmAndSaveReading,
              onExplainAi = viewModel::explainAssessmentWithAi,
              onAcknowledgeAlert = viewModel::acknowledgeFamilyAlert,
              onOpenEmergency = { assessment -> viewModel.openEmergencyScreenManually(assessment) },
              onClearLatestResult = viewModel::clearLatestResult,
              onShowToast = viewModel::showToast,
            )
          }
        }

        composable(AppRoutes.SCAN_RX) {
          ProtectedShell(
            currentRoute = AppRoutes.SCAN_RX,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            ScanPrescriptionScreen(
              uiState = uiState,
              contentPadding = padding,
              onProcessBitmap = viewModel::processPrescriptionBitmap,
              onLoadDemoRx = viewModel::loadDemoSamplePrescription,
              onStartManualEntry = viewModel::startManualPrescriptionEntry,
              onUpdateMedicine = viewModel::updateRxMedicine,
              onAddBlankMedicine = viewModel::addBlankRxMedicine,
              onRemoveMedicine = viewModel::removeRxMedicine,
              onRunSafetyChecks = viewModel::runRxSafetyChecks,
              onExplainInteractionAi = viewModel::explainInteractionWithAi,
              onSaveToSchedule = viewModel::saveRxToSchedule,
              onSetStep = viewModel::setRxStep,
              onResetScanner = viewModel::resetRxScanner,
              onGoToMedicines = { navigateInsideShell(navController, AppRoutes.MEDICINES) },
              onShowToast = viewModel::showToast,
            )
          }
        }

        composable(AppRoutes.MEDICINES) {
          ProtectedShell(
            currentRoute = AppRoutes.MEDICINES,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            MedicinesAndScheduleScreen(
              uiState = uiState,
              contentPadding = padding,
              onToggleDoseTaken = viewModel::toggleDoseTaken,
              onSkipDose = viewModel::skipDose,
              onCheckExtraMedicine = viewModel::checkExtraMedicineName,
              onExplainInteraction = viewModel::explainInteractionWithAi,
              onRestockMedicine = viewModel::restockMedicine,
              onAdjustStock = viewModel::adjustMedicineStock,
              onSnoozeRefill = viewModel::snoozeMedicineRefill,
              onRecordPharmacyOrder = viewModel::recordPharmacyRefillIntent,
              onOpenScanner = { navigateInsideShell(navController, AppRoutes.SCAN_RX) },
              onShowToast = viewModel::showToast,
            )
          }
        }

        composable(AppRoutes.TRENDS) {
          ProtectedShell(
            currentRoute = AppRoutes.TRENDS,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            TrendsScreen(
              uiState = uiState,
              contentPadding = padding,
              onShowToast = viewModel::showToast,
              onOpenMedicines = { navigateInsideShell(navController, AppRoutes.MEDICINES) },
              onBackHome = { navigateInsideShell(navController, AppRoutes.DASHBOARD) },
            )
          }
        }

        composable(AppRoutes.FAMILY) {
          ProtectedShell(
            currentRoute = AppRoutes.FAMILY,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            FamilyAlertsScreen(
              uiState = uiState,
              contentPadding = padding,
              onTriggerDemoEscalation = viewModel::triggerDemoEscalationTest,
              onAcknowledgeAlert = viewModel::acknowledgeFamilyAlert,
              onAddCaregiverInvite = viewModel::addCaregiverInvite,
              onAcceptCaregiverDemo = viewModel::acceptCaregiverConsentDemo,
              onRevokeCaregiver = viewModel::revokeCaregiverConsent,
              onBackHome = { navigateInsideShell(navController, AppRoutes.DASHBOARD) },
            )
          }
        }

        composable(AppRoutes.ACCOUNT) {
          ProtectedShell(
            currentRoute = AppRoutes.ACCOUNT,
            uiState = uiState,
            refillAlertCount = refillAlertCount,
            onDismissToast = viewModel::dismissToast,
            onSyncOfflineQueue = viewModel::syncOfflineQueue,
            onToggleTheme = toggleTheme,
            onNavigate = { route -> navigateInsideShell(navController, route) },
          ) { padding ->
            AccountScreen(
              uiState = uiState,
              contentPadding = padding,
              onSetThemePreference = viewModel::setThemePreference,
              onSaveProfile = viewModel::updatePatientProfile,
              onSaveReminderPrefs = viewModel::updateReminderPreferences,
              onUpdateThresholds = viewModel::updateRiskThresholds,
              onAddCaregiverInvite = viewModel::addCaregiverInvite,
              onAcceptCaregiverDemo = viewModel::acceptCaregiverConsentDemo,
              onRevokeCaregiver = viewModel::revokeCaregiverConsent,
              onVerifyReauth = viewModel::verifyReauth,
              onDeleteAccount = { text -> viewModel.deleteAccountAndAllData(text, context) },
              onResetDemoData = viewModel::resetDemoData,
              onLogout = viewModel::logout,
              onToggleOfflineSimulation = viewModel::toggleOfflineSimulation,
              onSyncOfflineQueue = viewModel::syncOfflineQueue,
              onShowToast = viewModel::showToast,
            )
          }
        }
      }

      if (!uiState.session.isAuthenticated) {
        SehatToastBanner(
          message = uiState.toastMessage,
          onDismiss = viewModel::dismissToast,
        )
      }
    }

    val emergencyAssessment = uiState.emergencyAssessment
    if (uiState.showEmergencyScreen && emergencyAssessment != null) {
      BackHandler { viewModel.dismissEmergencyScreen() }
      EmergencyScreen(
        assessment = emergencyAssessment,
        caregivers = uiState.caregivers,
        onCloseEmergency = viewModel::dismissEmergencyScreen,
      )
    }

    PreloaderOverlay(
      visible = !uiState.showEmergencyScreen && (uiState.showPreloader || !uiState.isSessionLoaded),
      progress = uiState.preloaderProgress,
    )

    GuidedTourOverlay(
      visible =
        uiState.isSessionLoaded &&
          uiState.session.isAuthenticated &&
          uiState.session.isDemoMode &&
          !uiState.guidedTourDismissed &&
          !uiState.showEmergencyScreen &&
          !uiState.showPreloader,
      onNavigateArea = { route -> navigateInsideShell(navController, route) },
      onDismissTour = viewModel::dismissGuidedTour,
    )
    }
  }
}

@Composable
private fun ProtectedShell(
  currentRoute: String,
  uiState: SehatSaathiUiState,
  refillAlertCount: Int,
  onDismissToast: () -> Unit,
  onSyncOfflineQueue: () -> Unit,
  onToggleTheme: () -> Unit,
  onNavigate: (String) -> Unit,
  content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
  if (!uiState.session.isAuthenticated) return
  SehatAppShell(
    currentRoute = currentRoute,
    isDemoMode = uiState.session.isDemoMode,
    isOnline = uiState.effectiveOnline,
    offlineQueueCount = uiState.offlineQueue.size,
    refillAlertCount = refillAlertCount,
    toastMessage = uiState.toastMessage,
    onDismissToast = onDismissToast,
    onSyncOfflineQueue = onSyncOfflineQueue,
    onToggleTheme = onToggleTheme,
    onNavigate = onNavigate,
    content = content,
  )
}

private fun navigateInsideShell(
  navController: androidx.navigation.NavHostController,
  route: String,
) {
  runCatching {
    navController.navigate(route) {
      popUpTo(AppRoutes.DASHBOARD) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }.onFailure {
    navController.navigate(AppRoutes.NOT_FOUND) {
      launchSingleTop = true
    }
  }
}

private fun routePageTitle(route: String, infoSectionArg: String?): String =
  when (route) {
    AppRoutes.LANDING -> "Home"
    AppRoutes.SIGN_UP -> "Create account"
    AppRoutes.LOG_IN -> "Log in"
    AppRoutes.FORGOT_PASSWORD -> "Reset password"
    AppRoutes.DASHBOARD -> "Dashboard"
    AppRoutes.LOG_READING -> "Log reading"
    AppRoutes.SCAN_RX -> "Scan prescription"
    AppRoutes.MEDICINES -> "Medicines & refills"
    AppRoutes.TRENDS -> "Trends & summary"
    AppRoutes.FAMILY -> "Family & alerts"
    AppRoutes.ACCOUNT -> "Account & security"
    AppRoutes.NOT_FOUND -> "Page not found"
    AppRoutes.INFO_ROUTE ->
      InfoSection.entries.firstOrNull { it.name.equals(infoSectionArg, ignoreCase = true) }?.title
        ?: "Safety & support"
    else -> "Page not found"
  }

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }

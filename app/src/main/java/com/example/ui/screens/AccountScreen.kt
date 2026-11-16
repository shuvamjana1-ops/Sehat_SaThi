package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.CaregiverSharingPermissions
import com.example.data.HealthStatusLevel
import com.example.data.NotificationSwitchPrefs
import com.example.data.PatientProfile
import com.example.data.RefillStatus
import com.example.data.ReminderPreferences
import com.example.data.SeededCaregiver
import com.example.domain.profile.ActivityFilter
import com.example.domain.profile.ProfileDomain
import com.example.domain.profile.ProfileEditFocusField
import com.example.domain.profile.ProfileStats
import com.example.domain.profile.ProfileTab
import com.example.domain.risk.RiskThresholdsConfig
import com.example.export.DoctorSummaryAndDataExporter
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.AppRoutes
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatDestructiveButton
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.launchSafeEmail
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.ClinicalBlue
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.Mist
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.ThemePreference
import com.example.ui.theme.rememberPrefersReducedTransparency
import com.example.ui.theme.tabular

private enum class SensitiveActionType {
  EXPORT_JSON,
  DELETE_ACCOUNT,
  CHANGE_PASSWORD,
}

private data class HighlightShortcut(
  val id: String,
  val label: String,
  val icon: ImageVector,
  val route: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onSetThemePreference: (ThemePreference) -> Unit = {},
  onSetSimpleMode: (Boolean) -> Unit = {},
  onSaveProfile: (name: String, age: Int, conditions: String, pharmacyName: String, pharmacyPhone: String) -> Unit,
  onSaveReminderPrefs: (ReminderPreferences) -> Unit,
  onSaveFullProfile: (PatientProfile, ReminderPreferences) -> Unit = { p, r ->
    onSaveProfile(p.name, p.age, p.conditions.joinToString(", "), p.pharmacyName, p.pharmacyPhone)
    onSaveReminderPrefs(r)
  },
  onUpdateAvatar: (android.graphics.Bitmap?) -> Unit = {},
  onUpdateCover: (android.graphics.Bitmap?) -> Unit = {},
  onTogglePhotoPrivacy: (showToCaregivers: Boolean, useInitialsOnly: Boolean) -> Unit = { _, _ -> },
  onToggleHideAchievements: (Boolean) -> Unit = {},
  onDismissCompleteness: () -> Unit = {},
  onUpdateNotificationSwitches: (NotificationSwitchPrefs) -> Unit = {},
  onUpdateCaregiverPermission: (String, (CaregiverSharingPermissions) -> CaregiverSharingPermissions) -> Unit = { _, _ -> },
  onResendCaregiverInvite: (String) -> Unit = {},
  onUpdateThresholds: (RiskThresholdsConfig) -> Unit,
  onAddCaregiverInvite: (name: String, relationship: String, phone: String) -> String?,
  onAcceptCaregiverDemo: (String) -> Unit,
  onRevokeCaregiver: (String) -> Unit,
  onVerifyReauth: (email: String, secret: String) -> Boolean,
  onDeleteAccount: (confirmationText: String) -> Boolean,
  onResetDemoData: () -> Unit,
  onLogout: () -> Unit,
  onToggleOfflineSimulation: (Boolean) -> Unit,
  onSyncOfflineQueue: () -> Unit,
  onShowToast: (String) -> Unit,
  onNavigate: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  val reducedMotion = rememberPrefersReducedMotion()
  val reducedTransparency = rememberPrefersReducedTransparency()

  var activeTab by rememberSaveable { mutableStateOf(ProfileTab.OVERVIEW) }
  var editSheetOpen by remember { mutableStateOf(false) }
  var editFocusField by remember { mutableStateOf<ProfileEditFocusField?>(null) }
  var careCardDialogOpen by remember { mutableStateOf(false) }
  var avatarPreviewOpen by remember { mutableStateOf(false) }
  var photoEditorMode by remember { mutableStateOf<Boolean?>(null) } // true = avatar, false = cover
  var caregiverToRemove by remember { mutableStateOf<SeededCaregiver?>(null) }

  var activityFilter by rememberSaveable { mutableStateOf(ActivityFilter.ALL) }
  var activityPageCount by rememberSaveable { mutableIntStateOf(1) }

  var pendingSensitiveAction by remember { mutableStateOf<SensitiveActionType?>(null) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }
  var showChangePasswordDialog by remember { mutableStateOf(false) }
  var jsonPreviewText by remember { mutableStateOf<String?>(null) }

  val stats =
    remember(uiState.readings, uiState.dosesToday, uiState.caregivers) {
      ProfileDomain.computeProfileStats(
        readings = uiState.readings,
        dosesToday = uiState.dosesToday,
        caregivers = uiState.caregivers,
      )
    }

  val completeness =
    remember(uiState.patient, uiState.avatarBitmap) {
      ProfileDomain.computeProfileCompleteness(
        profile = uiState.patient,
        hasAvatarPhoto = uiState.avatarBitmap != null,
      )
    }

  val achievements =
    remember(stats, uiState.dosesToday, uiState.medicines, uiState.refillHistory, uiState.hasScannedPrescription) {
      ProfileDomain.computeEarnedAchievements(
        stats = stats,
        dosesToday = uiState.dosesToday,
        medicines = uiState.medicines,
        refillHistory = uiState.refillHistory,
        hasScannedPrescription = uiState.hasScannedPrescription,
      )
    }

  val highlights =
    remember {
      listOf(
        HighlightShortcut("hl_meds", "Medicines", Icons.Filled.Medication, AppRoutes.MEDICINES),
        HighlightShortcut("hl_refills", "Refills", Icons.Filled.Refresh, AppRoutes.MEDICINES),
        HighlightShortcut("hl_trends", "Trends", Icons.AutoMirrored.Filled.ShowChart, AppRoutes.TRENDS),
        HighlightShortcut("hl_summary", "Doctor summary", Icons.Filled.Description, AppRoutes.TRENDS),
        HighlightShortcut("hl_family", "Family", Icons.Filled.Groups, AppRoutes.FAMILY),
      )
    }

  // Animated count-up on first view
  var countUpStarted by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) { countUpStarted = true }
  val animDuration = if (reducedMotion) 0 else 550
  val animatedReadings by
    animateIntAsState(
      targetValue = if (countUpStarted) stats.readingsLogged else 0,
      animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
      label = "stat_readings",
    )
  val animatedStreak by
    animateIntAsState(
      targetValue = if (countUpStarted) stats.dayStreak else 0,
      animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
      label = "stat_streak",
    )
  val animatedAdherence by
    animateIntAsState(
      targetValue = if (countUpStarted) stats.adherence7dPercent else 0,
      animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
      label = "stat_adherence",
    )
  val animatedCaregivers by
    animateIntAsState(
      targetValue = if (countUpStarted) stats.caregiversCount else 0,
      animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
      label = "stat_caregivers",
    )

  fun openEditSheet(focus: ProfileEditFocusField? = null) {
    editFocusField = focus
    editSheetOpen = true
  }

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .imePadding()
        .semantics { contentDescription = "Profile | SehatSaathi (private, noindex)" },
  ) {
    val isDesktopTwoColumn = maxWidth >= 960.dp

    if (isDesktopTwoColumn) {
      // Desktop 2-column layout: fixed left profile card + right scrollable tab content
      Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
          Modifier
            .fillMaxSize()
            .padding(
              start = 20.dp,
              end = 20.dp,
              top = 16.dp,
              bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier =
            Modifier
              .width(360.dp)
              .fillMaxHeight()
              .verticalScroll(rememberScrollState()),
        ) {
          ProfilePrivacyLockBanner(isDemoMode = uiState.session.isDemoMode, activeTabHash = activeTab.urlHash)
          ProfileHeaderHeroCard(
            patient = uiState.patient,
            email = uiState.session.email,
            isDemoMode = uiState.session.isDemoMode,
            isDesktop = true,
            avatarBitmap = uiState.avatarBitmap,
            coverBitmap = uiState.coverBitmap,
            animatedReadings = animatedReadings,
            animatedStreak = animatedStreak,
            animatedAdherence = animatedAdherence,
            animatedCaregivers = animatedCaregivers,
            onAvatarClick = { avatarPreviewOpen = true },
            onEditAvatarClick = { photoEditorMode = true },
            onEditCoverClick = { photoEditorMode = false },
            onInlineEditName = { openEditSheet(ProfileEditFocusField.NAME) },
            onInlineEditBio = { openEditSheet(ProfileEditFocusField.BIO) },
            onOpenEditProfile = { openEditSheet(null) },
            onOpenCareCard = { careCardDialogOpen = true },
            onStatReadingsClick = { onNavigate(AppRoutes.TRENDS) },
            onStatStreakClick = { onNavigate(AppRoutes.LOG_READING) },
            onStatAdherenceClick = { onNavigate(AppRoutes.MEDICINES) },
            onStatCaregiversClick = { activeTab = ProfileTab.PRIVACY },
          )
          HighlightsRowCard(
            highlights = highlights,
            onHighlightClick = { route -> onNavigate(route) },
          )
        }

        val rightListState = rememberLazyListState()
        LazyColumn(
          state = rightListState,
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.weight(1f).fillMaxHeight().testTag("account_scroll"),
        ) {
          item(key = "desktop_tab_bar") {
            ProfileStickyTabBar(
              activeTab = activeTab,
              reducedTransparency = reducedTransparency,
              onSelectTab = { activeTab = it },
            )
          }
          item(key = "desktop_tab_body") {
            ProfileTabContentSection(
              activeTab = activeTab,
              uiState = uiState,
              stats = stats,
              completeness = completeness,
              achievements = achievements,
              activityFilter = activityFilter,
              activityPageCount = activityPageCount,
              jsonPreviewText = jsonPreviewText,
              onSelectActivityFilter = {
                activityFilter = it
                activityPageCount = 1
              },
              onLoadMoreActivity = { activityPageCount += 1 },
              onOpenEditFocus = { openEditSheet(it) },
              onDismissCompleteness = onDismissCompleteness,
              onToggleHideAchievements = onToggleHideAchievements,
              onSetThemePreference = onSetThemePreference,
              onSetSimpleMode = onSetSimpleMode,
              onSaveProfile = onSaveProfile,
              onSaveReminderPrefs = onSaveReminderPrefs,
              onUpdateThresholds = onUpdateThresholds,
              onAddCaregiverInvite = onAddCaregiverInvite,
              onAcceptCaregiverDemo = onAcceptCaregiverDemo,
              onRequestRemoveCaregiver = { caregiverToRemove = it },
              onResendCaregiverInvite = onResendCaregiverInvite,
              onUpdateCaregiverPermission = onUpdateCaregiverPermission,
              onTogglePhotoPrivacy = onTogglePhotoPrivacy,
              onUpdateNotificationSwitches = onUpdateNotificationSwitches,
              onRequestExportJson = { pendingSensitiveAction = SensitiveActionType.EXPORT_JSON },
              onRequestDeleteAccount = { pendingSensitiveAction = SensitiveActionType.DELETE_ACCOUNT },
              onRequestChangePassword = { pendingSensitiveAction = SensitiveActionType.CHANGE_PASSWORD },
              onResetDemoData = onResetDemoData,
              onLogout = onLogout,
              onToggleOfflineSimulation = onToggleOfflineSimulation,
              onSyncOfflineQueue = onSyncOfflineQueue,
              onNavigate = onNavigate,
              onShowToast = onShowToast,
            )
          }
        }
      }
    } else {
      // Phone & Tablet single-column layout
      val listState = rememberLazyListState()
      LazyColumn(
        state = listState,
        contentPadding =
          PaddingValues(16.dp, 14.dp, 16.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().testTag("account_scroll"),
      ) {
        item(key = "acc_header") {
          ProfilePrivacyLockBanner(isDemoMode = uiState.session.isDemoMode, activeTabHash = activeTab.urlHash)
        }

        item(key = "acc_hero_card") {
          ProfileHeaderHeroCard(
            patient = uiState.patient,
            email = uiState.session.email,
            isDemoMode = uiState.session.isDemoMode,
            isDesktop = false,
            avatarBitmap = uiState.avatarBitmap,
            coverBitmap = uiState.coverBitmap,
            animatedReadings = animatedReadings,
            animatedStreak = animatedStreak,
            animatedAdherence = animatedAdherence,
            animatedCaregivers = animatedCaregivers,
            onAvatarClick = { avatarPreviewOpen = true },
            onEditAvatarClick = { photoEditorMode = true },
            onEditCoverClick = { photoEditorMode = false },
            onInlineEditName = { openEditSheet(ProfileEditFocusField.NAME) },
            onInlineEditBio = { openEditSheet(ProfileEditFocusField.BIO) },
            onOpenEditProfile = { openEditSheet(null) },
            onOpenCareCard = { careCardDialogOpen = true },
            onStatReadingsClick = { onNavigate(AppRoutes.TRENDS) },
            onStatStreakClick = { onNavigate(AppRoutes.LOG_READING) },
            onStatAdherenceClick = { onNavigate(AppRoutes.MEDICINES) },
            onStatCaregiversClick = { activeTab = ProfileTab.PRIVACY },
          )
        }

        item(key = "acc_highlights") {
          HighlightsRowCard(
            highlights = highlights,
            onHighlightClick = { route -> onNavigate(route) },
          )
        }

        item(key = "acc_tabs") {
          ProfileStickyTabBar(
            activeTab = activeTab,
            reducedTransparency = reducedTransparency,
            onSelectTab = { activeTab = it },
          )
        }

        item(key = "acc_tab_content") {
          ProfileTabContentSection(
            activeTab = activeTab,
            uiState = uiState,
            stats = stats,
            completeness = completeness,
            achievements = achievements,
            activityFilter = activityFilter,
            activityPageCount = activityPageCount,
            jsonPreviewText = jsonPreviewText,
            onSelectActivityFilter = {
              activityFilter = it
              activityPageCount = 1
            },
            onLoadMoreActivity = { activityPageCount += 1 },
            onOpenEditFocus = { openEditSheet(it) },
            onDismissCompleteness = onDismissCompleteness,
            onToggleHideAchievements = onToggleHideAchievements,
            onSetThemePreference = onSetThemePreference,
            onSetSimpleMode = onSetSimpleMode,
            onSaveProfile = onSaveProfile,
            onSaveReminderPrefs = onSaveReminderPrefs,
            onUpdateThresholds = onUpdateThresholds,
            onAddCaregiverInvite = onAddCaregiverInvite,
            onAcceptCaregiverDemo = onAcceptCaregiverDemo,
            onRequestRemoveCaregiver = { caregiverToRemove = it },
            onResendCaregiverInvite = onResendCaregiverInvite,
            onUpdateCaregiverPermission = onUpdateCaregiverPermission,
            onTogglePhotoPrivacy = onTogglePhotoPrivacy,
            onUpdateNotificationSwitches = onUpdateNotificationSwitches,
            onRequestExportJson = { pendingSensitiveAction = SensitiveActionType.EXPORT_JSON },
            onRequestDeleteAccount = { pendingSensitiveAction = SensitiveActionType.DELETE_ACCOUNT },
            onRequestChangePassword = { pendingSensitiveAction = SensitiveActionType.CHANGE_PASSWORD },
            onResetDemoData = onResetDemoData,
            onLogout = onLogout,
            onToggleOfflineSimulation = onToggleOfflineSimulation,
            onSyncOfflineQueue = onSyncOfflineQueue,
            onNavigate = onNavigate,
            onShowToast = onShowToast,
          )
        }
      }
    }
  }

  // Modals & Dialogs
  if (editSheetOpen) {
    EditProfileModal(
      patient = uiState.patient,
      email = uiState.session.email,
      reminderPrefs = uiState.reminderPrefs,
      initialFocusField = editFocusField,
      onSave = { updatedPatient, updatedReminders ->
        onSaveFullProfile(updatedPatient, updatedReminders)
      },
      onGoToSecuritySettings = { activeTab = ProfileTab.PRIVACY },
      onDismiss = { editSheetOpen = false },
    )
  }

  if (careCardDialogOpen) {
    ShareCareCardDialog(
      uiState = uiState,
      onShowToast = onShowToast,
      onDismiss = { careCardDialogOpen = false },
    )
  }

  if (avatarPreviewOpen) {
    AvatarFullPreviewDialog(
      patient = uiState.patient,
      avatarBitmap = uiState.avatarBitmap,
      onEditAvatar = { photoEditorMode = true },
      onRemoveAvatar = { onUpdateAvatar(null) },
      onDismiss = { avatarPreviewOpen = false },
    )
  }

  photoEditorMode?.let { isAvatarMode ->
    PhotoCropAndZoomDialog(
      isAvatar = isAvatarMode,
      currentBitmap = if (isAvatarMode) uiState.avatarBitmap else uiState.coverBitmap,
      patientName = uiState.patient.name,
      onSaveBitmap = { bmp ->
        if (isAvatarMode) onUpdateAvatar(bmp) else onUpdateCover(bmp)
      },
      onShowToast = onShowToast,
      onDismiss = { photoEditorMode = null },
    )
  }

  caregiverToRemove?.let { cg ->
    RemoveCaregiverConfirmDialog(
      caregiver = cg,
      onConfirmRemove = {
        onRevokeCaregiver(cg.id)
        caregiverToRemove = null
      },
      onDismiss = { caregiverToRemove = null },
    )
  }

  pendingSensitiveAction?.let { action ->
    ReauthDialog(
      defaultEmail = uiState.session.email,
      isDemoMode = uiState.session.isDemoMode,
      onVerify = onVerifyReauth,
      onDismiss = { pendingSensitiveAction = null },
      onVerified = {
        pendingSensitiveAction = null
        when (action) {
          SensitiveActionType.EXPORT_JSON -> {
            val json = DoctorSummaryAndDataExporter.buildJsonExport(uiState)
            jsonPreviewText = json
            val file = DoctorSummaryAndDataExporter.writeJsonExportFile(context, json)
            DoctorSummaryAndDataExporter.shareFileWithIntent(
              context = context,
              file = file,
              mimeType = "application/json",
              chooserTitle = "Save or Share SehatSaathi Data JSON",
            )
            onShowToast("Exported your data to ${file.name}.")
          }
          SensitiveActionType.DELETE_ACCOUNT -> showDeleteConfirmDialog = true
          SensitiveActionType.CHANGE_PASSWORD -> showChangePasswordDialog = true
        }
      },
    )
  }

  if (showDeleteConfirmDialog) {
    DeleteAccountConfirmationDialog(
      onConfirmDelete = { typed ->
        val ok = onDeleteAccount(typed)
        if (ok) showDeleteConfirmDialog = false
        ok
      },
      onDismiss = { showDeleteConfirmDialog = false },
    )
  }

  if (showChangePasswordDialog) {
    ChangePasswordDemoDialog(
      onSaved = {
        showChangePasswordDialog = false
        onShowToast("Password updated on this device.")
      },
      onDismiss = { showChangePasswordDialog = false },
    )
  }
}

@Composable
private fun ProfilePrivacyLockBanner(
  isDemoMode: Boolean,
  activeTabHash: String,
) {
  val c = SehatPalette.current
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth().testTag("profile_privacy_lock_banner"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector = Icons.Filled.Lock,
        contentDescription = "Private profile",
        tint = c.deepOcean,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = "Only you and the people you choose can see this.",
        style = MaterialTheme.typography.labelLarge,
        color = c.deepOcean,
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
      Surface(
        shape = PillShape,
        color = c.mistSoft,
        border = BorderStroke(1.dp, c.hairlineBorder),
      ) {
        Text(
          text = activeTabHash,
          style = MaterialTheme.typography.labelSmall.tabular(),
          color = c.inkSecondary,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
      }
      if (isDemoMode) {
        DemoPillBadge(text = "Demo")
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHeaderHeroCard(
  patient: PatientProfile,
  email: String,
  isDemoMode: Boolean,
  isDesktop: Boolean,
  avatarBitmap: android.graphics.Bitmap?,
  coverBitmap: android.graphics.Bitmap?,
  animatedReadings: Int,
  animatedStreak: Int,
  animatedAdherence: Int,
  animatedCaregivers: Int,
  onAvatarClick: () -> Unit,
  onEditAvatarClick: () -> Unit,
  onEditCoverClick: () -> Unit,
  onInlineEditName: () -> Unit,
  onInlineEditBio: () -> Unit,
  onOpenEditProfile: () -> Unit,
  onOpenCareCard: () -> Unit,
  onStatReadingsClick: () -> Unit,
  onStatStreakClick: () -> Unit,
  onStatAdherenceClick: () -> Unit,
  onStatCaregiversClick: () -> Unit,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  val avatarSize = if (isDesktop) 112.dp else 88.dp
  val coverHeight = if (isDesktop) 144.dp else 132.dp
  val initials = ProfileDomain.computeInitials(patient.name)

  SehatCard(
    containerColor = c.surface,
    contentPadding = PaddingValues(0.dp),
    modifier = Modifier.fillMaxWidth().testTag("profile_hero_card"),
  ) {
    // 1. Cover Banner + Overlapping Round Avatar
    Box(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(coverHeight)
            .background(
              Brush.linearGradient(
                colors = listOf(DeepOcean, ClinicalBlue, Mist),
                start = Offset(0f, 0f),
                end = Offset(900f, 360f),
              ),
            ),
      ) {
        if (coverBitmap != null) {
          Image(
            bitmap = coverBitmap.asImageBitmap(),
            contentDescription = "Profile cover photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        }
        // Subtle top-right camera button to change cover
        Surface(
          onClick = onEditCoverClick,
          shape = PillShape,
          color = DeepOcean.copy(alpha = 0.78f),
          border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.65f)),
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp)
              .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
              .testTag("change_cover_photo_btn"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.CameraAlt,
              contentDescription = "Change cover photo",
              tint = PureWhite,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cover", style = MaterialTheme.typography.labelMedium, color = PureWhite)
          }
        }
      }

      // Overlapping Avatar at bottom-start of cover
      Box(
        modifier =
          Modifier
            .padding(start = 18.dp, top = coverHeight - (avatarSize / 2))
            .size(avatarSize),
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(c.mist, c.clinicalBlue.copy(alpha = 0.25f))))
              .border(4.dp, c.surface, CircleShape)
              .clickable(role = Role.Button, onClick = onAvatarClick)
              .semantics { contentDescription = "View full-size profile photo for ${patient.name}" }
              .testTag("profile_avatar_circle"),
        ) {
          if (avatarBitmap != null && !patient.useInitialsOnly) {
            Image(
              bitmap = avatarBitmap.asImageBitmap(),
              contentDescription = "${patient.name} profile avatar",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
          } else {
            Text(
              text = initials,
              style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
              color = c.deepOcean,
            )
          }
        }

        // Small camera badge on avatar with >= 48dp interactive touch target
        Surface(
          onClick = onEditAvatarClick,
          shape = CircleShape,
          color = c.clinicalBlue,
          border = BorderStroke(2.dp, c.surface),
          modifier =
            Modifier
              .align(Alignment.BottomEnd)
              .minimumInteractiveComponentSize()
              .size(36.dp)
              .testTag("change_avatar_photo_btn"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.CameraAlt,
              contentDescription = "Change avatar photo",
              tint = PureWhite,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }

    // 2. Identity, Bio, Info Chips, Action Buttons, and Instagram-style Stats Row
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
      // Name + Handle (tappable inline shortcut to Edit Profile)
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .clickable(role = Role.Button, onClick = onInlineEditName)
            .padding(vertical = 2.dp)
            .testTag("profile_inline_name_area"),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = patient.name,
            style = MaterialTheme.typography.headlineLarge,
            color = c.deepOcean,
          )
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit name",
            tint = c.clinicalBlueAccent,
            modifier = Modifier.size(18.dp),
          )
        }
        if (patient.handle.isNotBlank()) {
          Text(
            text = "@${patient.handle.removePrefix("@")}",
            style = MaterialTheme.typography.bodyMedium,
            color = c.inkSecondary,
          )
        }
      }

      // Bio (tappable inline shortcut to Edit Profile focused on Bio)
      Surface(
        onClick = onInlineEditBio,
        shape = ButtonShape,
        color = c.mistSoft,
        border = BorderStroke(1.dp, c.hairlineBorder),
        modifier = Modifier.fillMaxWidth().testTag("profile_inline_bio_area"),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
          Text(
            text =
              patient.bio.ifBlank {
                "No bio yet — tap to add a short note about your routine."
              },
            style = MaterialTheme.typography.bodyMedium,
            color = if (patient.bio.isBlank()) c.inkSecondary else c.ink,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${patient.bio.length}/${ProfileDomain.MAX_BIO_LENGTH}",
            style = MaterialTheme.typography.labelSmall.tabular(),
            color = c.inkSecondary,
          )
        }
      }

      // Info chips: Age, City, Blood Group, Conditions
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ProfileInfoPillChip(text = "Age ${patient.age}")
        if (patient.city.isNotBlank()) {
          ProfileInfoPillChip(text = patient.city)
        }
        if (patient.bloodGroup.isNotBlank() && patient.bloodGroup != "Unknown") {
          ProfileInfoPillChip(text = "Blood ${patient.bloodGroup}")
        }
        patient.conditions.forEach { condition ->
          ProfileInfoPillChip(text = condition)
        }
      }

      // Email line
      val fallbackSlug =
        patient.handle
          .removePrefix("@")
          .ifBlank { patient.name.lowercase().replace(Regex("[^a-z0-9]+"), ".").trim('.') }
          .ifBlank { "patient" }
      val displayEmail =
        if (
          !patient.name.equals("Sunita Devi", ignoreCase = true) &&
            (email.isBlank() || email.equals("sunita.demo@sehatsaathi.local", ignoreCase = true))
        ) {
          "$fallbackSlug@sehatsaathi.app"
        } else {
          email.ifBlank { "sunita.demo@sehatsaathi.local" }
        }
      val isRealEmail = !isDemoMode && !displayEmail.endsWith(".local", ignoreCase = true)
      if (isRealEmail) {
        Text(
          text = "Email: $displayEmail",
          style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
          color = c.clinicalBlueAccent,
          modifier =
            Modifier
              .defaultMinSize(minHeight = 48.dp)
              .clickable(role = Role.Button) {
                launchSafeEmail(context, displayEmail, "SehatSaathi account")
              }
              .padding(vertical = 4.dp),
        )
      } else {
        Text(
          text = "Email: $displayEmail",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
        )
      }

      // Primary action buttons: stack cleanly below 360dp so labels never clip at 320px
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stackButtons = maxWidth < 360.dp
        if (stackButtons) {
          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            SehatPrimaryButton(
              text = "Edit profile",
              icon = Icons.Filled.Edit,
              onClick = onOpenEditProfile,
              modifier = Modifier.fillMaxWidth(),
              testTag = "open_edit_profile_btn",
            )
            SehatSecondaryButton(
              text = "Share care card",
              icon = Icons.Filled.Share,
              minHeight = 56.dp,
              onClick = onOpenCareCard,
              modifier = Modifier.fillMaxWidth(),
              testTag = "open_care_card_btn",
            )
          }
        } else {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            SehatPrimaryButton(
              text = "Edit profile",
              icon = Icons.Filled.Edit,
              onClick = onOpenEditProfile,
              modifier = Modifier.weight(1f),
              testTag = "open_edit_profile_btn",
            )
            SehatSecondaryButton(
              text = "Share care card",
              icon = Icons.Filled.Share,
              minHeight = 56.dp,
              onClick = onOpenCareCard,
              modifier = Modifier.weight(1f),
              testTag = "open_care_card_btn",
            )
          }
        }
      }

      HorizontalDivider(color = c.hairlineBorder)

      // Instagram-style Stats Row (Readings logged, Day streak, Adherence 7d, Caregivers)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().testTag("profile_stats_row"),
      ) {
        ProfileStatCell(
          valueText = "$animatedReadings",
          label = "Readings",
          onClick = onStatReadingsClick,
          modifier = Modifier.weight(1f),
          testTag = "stat_readings_btn",
        )
        ProfileStatCell(
          valueText = "${animatedStreak}d",
          label = "Day streak",
          onClick = onStatStreakClick,
          modifier = Modifier.weight(1f),
          testTag = "stat_streak_btn",
        )
        ProfileStatCell(
          valueText = "$animatedAdherence%",
          label = "Adherence (7d)",
          onClick = onStatAdherenceClick,
          modifier = Modifier.weight(1f),
          testTag = "stat_adherence_btn",
        )
        ProfileStatCell(
          valueText = "$animatedCaregivers",
          label = "Caregivers",
          onClick = onStatCaregiversClick,
          modifier = Modifier.weight(1f),
          testTag = "stat_caregivers_btn",
        )
      }
    }
  }
}

@Composable
private fun ProfileInfoPillChip(text: String) {
  val c = SehatPalette.current
  Surface(
    shape = PillShape,
    color = c.mist,
    border = BorderStroke(1.dp, c.hairlineBorder),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = c.deepOcean,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
    )
  }
}

@Composable
private fun ProfileStatCell(
  valueText: String,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val c = SehatPalette.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier =
      modifier
        .clip(ButtonShape)
        .clickable(role = Role.Button, onClick = onClick)
        .defaultMinSize(minHeight = 56.dp)
        .padding(vertical = 6.dp, horizontal = 4.dp)
        .testTag(testTag),
  ) {
    Text(
      text = valueText,
      style = MaterialTheme.typography.headlineMedium.tabular(),
      color = c.deepOcean,
      softWrap = false,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = c.inkSecondary,
      maxLines = 2,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun HighlightsRowCard(
  highlights: List<HighlightShortcut>,
  onHighlightClick: (String) -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    contentPadding = PaddingValues(vertical = 14.dp, horizontal = 14.dp),
    modifier = Modifier.fillMaxWidth().testTag("profile_highlights_card"),
  ) {
    Text(
      text = "Quick highlights",
      style = MaterialTheme.typography.labelLarge,
      color = c.deepOcean,
      modifier = Modifier.padding(horizontal = 4.dp),
    )
    Spacer(modifier = Modifier.height(10.dp))
    // Contained horizontal scroll inside its own LazyRow
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(horizontal = 4.dp),
      modifier = Modifier.fillMaxWidth().testTag("profile_highlights_row"),
    ) {
      items(highlights, key = { it.id }) { item ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier =
            Modifier
              .width(88.dp)
              .clip(ButtonShape)
              .clickable(role = Role.Button) { onHighlightClick(item.route) }
              .padding(vertical = 4.dp)
              .testTag(item.id),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier =
              Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(c.mistSoft)
                .border(
                  BorderStroke(
                    2.dp,
                    Brush.linearGradient(listOf(c.deepOcean, c.clinicalBlueAccent)),
                  ),
                  CircleShape,
                ),
          ) {
            Icon(
              imageVector = item.icon,
              contentDescription = item.label,
              tint = c.deepOcean,
              modifier = Modifier.size(26.dp),
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = c.ink,
            maxLines = 2,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileStickyTabBar(
  activeTab: ProfileTab,
  reducedTransparency: Boolean,
  onSelectTab: (ProfileTab) -> Unit,
) {
  val c = SehatPalette.current
  Surface(
    shape = PillShape,
    color = GlassTokens.chromeFill(scrolled = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
    border = GlassTokens.borderStroke(c.hairlineBorder, isDark = c.isDark),
    shadowElevation = 3.dp,
    modifier = Modifier.fillMaxWidth().testTag("profile_tab_bar"),
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val useScrollableTabs = maxWidth < 480.dp
      val rowModifier =
        if (useScrollableTabs) {
          Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(4.dp)
        } else {
          Modifier.fillMaxWidth().padding(4.dp)
        }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (useScrollableTabs) Arrangement.spacedBy(4.dp) else Arrangement.SpaceBetween,
        modifier = rowModifier,
      ) {
        ProfileTab.entries.forEach { tab ->
          val isSelected = activeTab == tab
          val tabModifier =
            if (useScrollableTabs) {
              Modifier.defaultMinSize(minWidth = 84.dp, minHeight = 48.dp)
            } else {
              Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
            }
          Surface(
            onClick = { onSelectTab(tab) },
            shape = PillShape,
            color = if (isSelected) c.clinicalBlue else Color.Transparent,
            modifier =
              tabModifier
                .semantics {
                  selected = isSelected
                  contentDescription = "${tab.label} tab (${tab.urlHash})"
                }
                .testTag("profile_tab_${tab.name.lowercase()}"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) PureWhite else c.deepOcean,
                softWrap = false,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileTabContentSection(
  activeTab: ProfileTab,
  uiState: SehatSaathiUiState,
  stats: ProfileStats,
  completeness: com.example.domain.profile.ProfileCompleteness,
  achievements: List<com.example.domain.profile.ProfileAchievementBadge>,
  activityFilter: ActivityFilter,
  activityPageCount: Int,
  jsonPreviewText: String?,
  onSelectActivityFilter: (ActivityFilter) -> Unit,
  onLoadMoreActivity: () -> Unit,
  onOpenEditFocus: (ProfileEditFocusField?) -> Unit,
  onDismissCompleteness: () -> Unit,
  onToggleHideAchievements: (Boolean) -> Unit,
  onSetThemePreference: (ThemePreference) -> Unit,
  onSetSimpleMode: (Boolean) -> Unit,
  onSaveProfile: (name: String, age: Int, conditions: String, pharmacyName: String, pharmacyPhone: String) -> Unit,
  onSaveReminderPrefs: (ReminderPreferences) -> Unit,
  onUpdateThresholds: (RiskThresholdsConfig) -> Unit,
  onAddCaregiverInvite: (name: String, relationship: String, phone: String) -> String?,
  onAcceptCaregiverDemo: (String) -> Unit,
  onRequestRemoveCaregiver: (SeededCaregiver) -> Unit,
  onResendCaregiverInvite: (String) -> Unit,
  onUpdateCaregiverPermission: (String, (CaregiverSharingPermissions) -> CaregiverSharingPermissions) -> Unit,
  onTogglePhotoPrivacy: (Boolean, Boolean) -> Unit,
  onUpdateNotificationSwitches: (NotificationSwitchPrefs) -> Unit,
  onRequestExportJson: () -> Unit,
  onRequestDeleteAccount: () -> Unit,
  onRequestChangePassword: () -> Unit,
  onResetDemoData: () -> Unit,
  onLogout: () -> Unit,
  onToggleOfflineSimulation: (Boolean) -> Unit,
  onSyncOfflineQueue: () -> Unit,
  onNavigate: (String) -> Unit,
  onShowToast: (String) -> Unit,
) {
  val reducedMotion = rememberPrefersReducedMotion()
  val fadeDuration = if (reducedMotion) 0 else 180

  AnimatedContent(
    targetState = activeTab,
    transitionSpec = { fadeIn(tween(fadeDuration)) togetherWith fadeOut(tween(fadeDuration)) },
    label = "profile_tab_swap",
  ) { tab ->
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
      when (tab) {
        ProfileTab.OVERVIEW -> {
          ProfileOverviewTabContent(
            uiState = uiState,
            completeness = completeness,
            achievements = achievements,
            onOpenEditFocus = onOpenEditFocus,
            onDismissCompleteness = onDismissCompleteness,
            onToggleHideAchievements = onToggleHideAchievements,
            onSetThemePreference = onSetThemePreference,
            onSetSimpleMode = onSetSimpleMode,
            onSaveProfile = onSaveProfile,
            onSaveReminderPrefs = onSaveReminderPrefs,
            onNavigate = onNavigate,
            onShowToast = onShowToast,
          )
        }
        ProfileTab.HEALTH -> {
          ProfileHealthTabContent(
            uiState = uiState,
            onNavigate = onNavigate,
          )
        }
        ProfileTab.MEDICINES -> {
          ProfileMedicinesTabContent(
            uiState = uiState,
            onNavigate = onNavigate,
          )
        }
        ProfileTab.ACTIVITY -> {
          ProfileActivityTabContent(
            uiState = uiState,
            activityFilter = activityFilter,
            activityPageCount = activityPageCount,
            onSelectFilter = onSelectActivityFilter,
            onLoadMore = onLoadMoreActivity,
            onNavigate = onNavigate,
          )
        }
        ProfileTab.PRIVACY -> {
          ProfilePrivacyTabContent(
            uiState = uiState,
            jsonPreviewText = jsonPreviewText,
            onAddCaregiverInvite = onAddCaregiverInvite,
            onAcceptCaregiverDemo = onAcceptCaregiverDemo,
            onRequestRemoveCaregiver = onRequestRemoveCaregiver,
            onResendCaregiverInvite = onResendCaregiverInvite,
            onUpdateCaregiverPermission = onUpdateCaregiverPermission,
            onTogglePhotoPrivacy = onTogglePhotoPrivacy,
            onUpdateNotificationSwitches = onUpdateNotificationSwitches,
            onUpdateThresholds = onUpdateThresholds,
            onToggleOfflineSimulation = onToggleOfflineSimulation,
            onSyncOfflineQueue = onSyncOfflineQueue,
            onRequestExportJson = onRequestExportJson,
            onRequestDeleteAccount = onRequestDeleteAccount,
            onRequestChangePassword = onRequestChangePassword,
            onResetDemoData = onResetDemoData,
            onLogout = onLogout,
            onShowToast = onShowToast,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileOverviewTabContent(
  uiState: SehatSaathiUiState,
  completeness: com.example.domain.profile.ProfileCompleteness,
  achievements: List<com.example.domain.profile.ProfileAchievementBadge>,
  onOpenEditFocus: (ProfileEditFocusField?) -> Unit,
  onDismissCompleteness: () -> Unit,
  onToggleHideAchievements: (Boolean) -> Unit,
  onSetThemePreference: (ThemePreference) -> Unit,
  onSetSimpleMode: (Boolean) -> Unit,
  onSaveProfile: (name: String, age: Int, conditions: String, pharmacyName: String, pharmacyPhone: String) -> Unit,
  onSaveReminderPrefs: (ReminderPreferences) -> Unit,
  onNavigate: (String) -> Unit,
  onShowToast: (String) -> Unit,
) {
  val c = SehatPalette.current
  val context = LocalContext.current

  var nameInput by remember(uiState.patient.name) { mutableStateOf(uiState.patient.name) }
  var ageInput by remember(uiState.patient.age) { mutableStateOf(uiState.patient.age.toString()) }
  var conditionsInput by remember(uiState.patient.conditions) {
    mutableStateOf(uiState.patient.conditions.joinToString(", "))
  }
  var pharmacyNameInput by remember(uiState.patient.pharmacyName) {
    mutableStateOf(uiState.patient.pharmacyName)
  }
  var pharmacyPhoneInput by remember(uiState.patient.pharmacyPhone) {
    mutableStateOf(uiState.patient.pharmacyPhone)
  }

  var morningTime by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.morningTime) }
  var afternoonTime by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.afternoonTime) }
  var nightTime by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.nightTime) }
  var quietEnabled by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.quietHoursEnabled) }
  var quietStart by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.quietStart) }
  var quietEnd by remember(uiState.reminderPrefs) { mutableStateOf(uiState.reminderPrefs.quietEnd) }

  // 1. Profile Completeness Meter (skippable & dismissible)
  if (!uiState.patient.completenessDismissed && completeness.percent < 100) {
    SehatCard(
      containerColor = c.surface,
      modifier = Modifier.fillMaxWidth().testTag("profile_completeness_card"),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Profile ${completeness.percent}% complete",
          style = MaterialTheme.typography.titleMedium.tabular(),
          color = c.deepOcean,
        )
        SehatGhostButton(
          text = "Dismiss",
          onClick = onDismissCompleteness,
          testTag = "dismiss_completeness_btn",
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      LinearProgressIndicator(
        progress = { completeness.percent / 100f },
        color = c.clinicalBlue,
        trackColor = c.mist,
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(PillShape),
      )
      if (completeness.missingItems.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          "Tap a suggestion to complete your care details:",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          completeness.missingItems.forEach { missing ->
            Surface(
              onClick = { onOpenEditFocus(missing.field) },
              shape = PillShape,
              color = c.mistSoft,
              border = BorderStroke(1.dp, c.clinicalBlueAccent),
              modifier = Modifier.defaultMinSize(minHeight = 40.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = c.deepOcean, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(missing.label, style = MaterialTheme.typography.labelMedium, color = c.deepOcean)
              }
            }
          }
        }
      }
    }
  }

  // 2. Emergency Card Summary + Quick Actions
  SehatCard(
    containerColor = c.surface,
    modifier = Modifier.fillMaxWidth().testTag("profile_emergency_card_summary"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Favorite, contentDescription = null, tint = c.statusUrgentFg)
        Text("Emergency card summary", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      SehatGhostButton(
        text = "Edit",
        onClick = { onOpenEditFocus(ProfileEditFocusField.EMERGENCY_CONTACTS) },
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    val primaryContact =
      uiState.patient.emergencyContacts.firstOrNull { it.isPrimary }
        ?: uiState.patient.emergencyContacts.firstOrNull()
    Text(
      text =
        buildString {
          if (uiState.patient.bloodGroup.isNotBlank()) append("Blood group: ${uiState.patient.bloodGroup} · ")
          if (uiState.patient.allergies.isNotEmpty()) {
            append("Allergies: ${uiState.patient.allergies.joinToString(", ")}")
          } else {
            append("No medicine allergies listed")
          }
        },
      style = MaterialTheme.typography.bodyMedium,
      color = c.ink,
    )
    if (primaryContact != null) {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            "Primary contact: ${primaryContact.name} (${primaryContact.relationship})",
            style = MaterialTheme.typography.labelLarge,
            color = c.deepOcean,
          )
          Text(primaryContact.phone, style = MaterialTheme.typography.bodyMedium.tabular(), color = c.inkSecondary)
        }
        SehatSecondaryButton(
          text = "Call",
          icon = Icons.Filled.Call,
          minHeight = 48.dp,
          onClick = { launchSafeDialer(context, primaryContact.phone, onShowToast) },
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      SehatPrimaryButton(
        text = "Log reading",
        icon = Icons.Filled.Add,
        onClick = { onNavigate(AppRoutes.LOG_READING) },
        modifier = Modifier.weight(1f),
        testTag = "overview_quick_log_btn",
      )
      SehatSecondaryButton(
        text = "Scan prescription",
        icon = Icons.Filled.DocumentScanner,
        minHeight = 56.dp,
        onClick = { onNavigate(AppRoutes.SCAN_RX) },
        modifier = Modifier.weight(1f),
        testTag = "overview_quick_scan_btn",
      )
    }
  }

  // 3. Honest Local Achievements Row (with toggle to hide)
  SehatCard(
    containerColor = c.surface,
    modifier = Modifier.fillMaxWidth().testTag("profile_achievements_card"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = c.clinicalBlueAccent)
        Text("Milestones & consistency", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      }
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Hide", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
        Switch(
          checked = uiState.patient.hideAchievements,
          onCheckedChange = onToggleHideAchievements,
          modifier = Modifier.testTag("hide_achievements_toggle"),
        )
      }
    }
    if (!uiState.patient.hideAchievements) {
      Spacer(modifier = Modifier.height(8.dp))
      if (achievements.isEmpty()) {
        Text(
          "Milestones appear here as you log readings or mark daily doses.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          achievements.forEach { badge ->
            Surface(
              shape = ButtonShape,
              color = c.mistSoft,
              border = BorderStroke(1.dp, c.hairlineBorder),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(12.dp),
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(badge.title, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                  Text(badge.subtitle, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
                }
                StatusChip(level = HealthStatusLevel.NORMAL, label = badge.earnedDateLabel)
              }
            }
          }
        }
      }
    }
  }

  // 4. Appearance & Theme Selector + Simple Mode Toggle (Preserving existing test tags!)
  SehatCard(containerColor = c.surface) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(
        imageVector = if (c.isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
        contentDescription = null,
        tint = c.clinicalBlueAccent,
      )
      Text("Appearance & theme", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Choose System default, crisp Light mode, or high-contrast Dark mode (WCAG AA).",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      ThemePreference.entries.forEach { pref ->
        val selected = uiState.themePreference == pref
        val prefIcon =
          when (pref) {
            ThemePreference.SYSTEM -> Icons.Filled.SettingsBrightness
            ThemePreference.LIGHT -> Icons.Filled.LightMode
            ThemePreference.DARK -> Icons.Filled.DarkMode
          }
        Surface(
          onClick = { onSetThemePreference(pref) },
          shape = PillShape,
          color = if (selected) c.clinicalBlue else c.mistSoft,
          border = BorderStroke(1.5.dp, if (selected) c.clinicalBlue else c.hairlineBorder),
          modifier =
            Modifier
              .weight(1f)
              .defaultMinSize(minHeight = 48.dp)
              .testTag("theme_pref_${pref.name.lowercase()}"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
          ) {
            Icon(
              imageVector = prefIcon,
              contentDescription = null,
              tint = if (selected) PureWhite else c.deepOcean,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = pref.label,
              style = MaterialTheme.typography.labelLarge,
              color = if (selected) PureWhite else c.deepOcean,
              maxLines = 1,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(12.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
        Text("Simple Mode (Elderly & Voice)", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
        Text(
          text = "Minimal layout with 4 big actions and spoken confirmations.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
      }
      Switch(
        checked = uiState.simpleModeActive,
        onCheckedChange = onSetSimpleMode,
        modifier = Modifier.testTag("simple_mode_toggle"),
      )
    }
  }

  // 5. Direct Quick Edit Card (preserves account_name_input, save_profile_btn, save_reminders_btn)
  SehatCard(containerColor = c.surface) {
    Text("Patient & pharmacy details", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    Spacer(modifier = Modifier.height(10.dp))
    SehatTextField(
      label = "Patient name",
      value = nameInput,
      onValueChange = { nameInput = it },
      testTag = "account_name_input",
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      SehatTextField(
        label = "Age",
        value = ageInput,
        onValueChange = { ageInput = it.filter { ch -> ch.isDigit() }.take(3) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(0.32f),
      )
      SehatTextField(
        label = "Conditions (comma-separated)",
        value = conditionsInput,
        onValueChange = { conditionsInput = it },
        modifier = Modifier.weight(0.68f),
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    SehatTextField(
      label = "Preferred pharmacy name",
      value = pharmacyNameInput,
      onValueChange = { pharmacyNameInput = it },
    )
    Spacer(modifier = Modifier.height(10.dp))
    SehatTextField(
      label = "Pharmacy phone / WhatsApp",
      value = pharmacyPhoneInput,
      onValueChange = { pharmacyPhoneInput = it },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    )
    Spacer(modifier = Modifier.height(12.dp))
    SehatPrimaryButton(
      text = "Save profile & pharmacy",
      icon = Icons.Filled.Save,
      onClick = {
        onSaveProfile(
          nameInput,
          ageInput.toIntOrNull() ?: uiState.patient.age,
          conditionsInput,
          pharmacyNameInput,
          pharmacyPhoneInput,
        )
      },
      modifier = Modifier.fillMaxWidth(),
      testTag = "save_profile_btn",
    )

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(12.dp))
    Text("Reminder times & quiet hours", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      SehatTextField(
        label = "Morning",
        value = morningTime,
        onValueChange = { morningTime = it },
        modifier = Modifier.weight(1f),
      )
      SehatTextField(
        label = "Afternoon",
        value = afternoonTime,
        onValueChange = { afternoonTime = it },
        modifier = Modifier.weight(1f),
      )
      SehatTextField(
        label = "Night",
        value = nightTime,
        onValueChange = { nightTime = it },
        modifier = Modifier.weight(1f),
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    SehatSecondaryButton(
      text = "Save reminder settings",
      minHeight = 52.dp,
      onClick = {
        onSaveReminderPrefs(
          ReminderPreferences(
            morningTime = morningTime.ifBlank { "8:00 AM" },
            afternoonTime = afternoonTime.ifBlank { "1:30 PM" },
            nightTime = nightTime.ifBlank { "9:00 PM" },
            quietHoursEnabled = quietEnabled,
            quietStart = quietStart.ifBlank { "10:00 PM" },
            quietEnd = quietEnd.ifBlank { "6:30 AM" },
          ),
        )
      },
      modifier = Modifier.fillMaxWidth(),
      testTag = "save_reminders_btn",
    )
  }
}

@Composable
private fun ProfileHealthTabContent(
  uiState: SehatSaathiUiState,
  onNavigate: (String) -> Unit,
) {
  val c = SehatPalette.current
  val recentReadings = uiState.readings.takeLast(7)
  val latest = uiState.readings.lastOrNull()

  SehatCard(
    containerColor = c.surface,
    modifier = Modifier.fillMaxWidth().testTag("profile_health_tab_card"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Latest vitals & 7-day sparkline", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      if (latest != null) {
        StatusChip(level = latest.status, label = latest.status.label)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    if (latest == null) {
      Text(
        "No readings logged yet. Log your first blood sugar or BP reading to see your trend sparkline.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.inkSecondary,
      )
    } else {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
          shape = ButtonShape,
          color = c.mistSoft,
          border = BorderStroke(1.dp, c.hairlineBorder),
          modifier = Modifier.weight(1f),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Blood sugar", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
            Text(
              "${latest.glucoseMgDl} mg/dL",
              style = MaterialTheme.typography.headlineMedium.tabular(),
              color = c.deepOcean,
            )
            Text(latest.contextLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
          }
        }
        Surface(
          shape = ButtonShape,
          color = c.mistSoft,
          border = BorderStroke(1.dp, c.hairlineBorder),
          modifier = Modifier.weight(1f),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Blood pressure", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
            Text(
              "${latest.systolic}/${latest.diastolic} mmHg",
              style = MaterialTheme.typography.headlineMedium.tabular(),
              color = c.deepOcean,
            )
            Text(latest.dayLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Text("Last ${recentReadings.size} sugar readings (mg/dL)", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
      Spacer(modifier = Modifier.height(8.dp))
      val sparkColor = c.clinicalBlue
      Canvas(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(ButtonShape)
            .background(c.mistSoft)
            .padding(12.dp),
      ) {
        if (recentReadings.size >= 2) {
          val values = recentReadings.map { it.glucoseMgDl.toFloat() }
          val minV = (values.minOrNull() ?: 60f) - 10f
          val maxV = (values.maxOrNull() ?: 220f) + 10f
          val span = (maxV - minV).coerceAtLeast(20f)
          val path = Path()
          values.forEachIndexed { idx, v ->
            val x = (idx.toFloat() / (values.size - 1).coerceAtLeast(1)) * size.width
            val y = size.height - ((v - minV) / span) * size.height
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = sparkColor, radius = 4.dp.toPx(), center = Offset(x, y))
          }
          drawPath(path = path, color = sparkColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      SehatPrimaryButton(
        text = "Open full Trends & Summary",
        icon = Icons.AutoMirrored.Filled.ShowChart,
        onClick = { onNavigate(AppRoutes.TRENDS) },
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ProfileMedicinesTabContent(
  uiState: SehatSaathiUiState,
  onNavigate: (String) -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.surface,
    modifier = Modifier.fillMaxWidth().testTag("profile_medicines_tab_card"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Current medicines (${uiState.medicines.size})", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      SehatGhostButton(
        text = "Manage schedule",
        onClick = { onNavigate(AppRoutes.MEDICINES) },
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    if (uiState.medicines.isEmpty()) {
      Text(
        "No medicines in your list yet.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.inkSecondary,
      )
      Spacer(modifier = Modifier.height(10.dp))
      SehatPrimaryButton(
        text = "Add your first medicine",
        icon = Icons.Filled.Add,
        onClick = { onNavigate(AppRoutes.SCAN_RX) },
      )
    } else {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        uiState.medicines.forEach { med ->
          val daysLeft = med.daysLeftOrNull ?: 0
          val meterFraction = (daysLeft / 30f).coerceIn(0.05f, 1f)
          val meterColor =
            when (med.refillStatus) {
              RefillStatus.OK -> c.statusNormalFg
              RefillStatus.SOON, RefillStatus.UNKNOWN -> c.statusCautionFg
              RefillStatus.DUE, RefillStatus.OUT -> c.statusUrgentFg
            }
          Surface(
            shape = ButtonShape,
            color = c.mistSoft,
            border = BorderStroke(1.dp, c.hairlineBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(med.brandName, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                  Text(
                    "${med.genericName} ${med.strength} · ${med.frequency} · ${med.foodTiming}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.inkSecondary,
                  )
                }
                StatusChip(level = med.refillStatus.badgeLevel, label = med.refillStatus.label)
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text =
                    med.daysLeftOrNull?.let { "${med.stockCount} ${med.unit} (~$it days left)" }
                      ?: "Stock not set — ask pharmacist",
                  style = MaterialTheme.typography.labelMedium.tabular(),
                  color = c.ink,
                )
              }
              LinearProgressIndicator(
                progress = { meterFraction },
                color = meterColor,
                trackColor = c.surface,
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(PillShape),
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      SehatPrimaryButton(
        text = "Open Refills & Schedule",
        icon = Icons.Filled.Medication,
        onClick = { onNavigate(AppRoutes.MEDICINES) },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileActivityTabContent(
  uiState: SehatSaathiUiState,
  activityFilter: ActivityFilter,
  activityPageCount: Int,
  onSelectFilter: (ActivityFilter) -> Unit,
  onLoadMore: () -> Unit,
  onNavigate: (String) -> Unit,
) {
  val c = SehatPalette.current
  val allItems =
    remember(uiState.readings, uiState.dosesToday, uiState.refillHistory, uiState.alerts, activityFilter) {
      ProfileDomain.buildActivityTimeline(
        readings = uiState.readings,
        dosesToday = uiState.dosesToday,
        refillHistory = uiState.refillHistory,
        alerts = uiState.alerts,
        filter = activityFilter,
      )
    }
  val visibleCount = (activityPageCount * ProfileDomain.ACTIVITY_PAGE_SIZE).coerceAtMost(allItems.size)
  val visibleItems = allItems.take(visibleCount)
  val hasMore = visibleCount < allItems.size

  SehatCard(
    containerColor = c.surface,
    modifier = Modifier.fillMaxWidth().testTag("profile_activity_tab_card"),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(Icons.Filled.History, contentDescription = null, tint = c.clinicalBlueAccent)
      Text("Activity timeline (${allItems.size})", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      ActivityFilter.entries.forEach { filter ->
        FilterChip(
          selected = activityFilter == filter,
          onClick = { onSelectFilter(filter) },
          label = { Text(filter.label) },
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = c.mist,
              selectedLabelColor = c.deepOcean,
            ),
          modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("activity_filter_${filter.name.lowercase()}"),
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    if (visibleItems.isEmpty()) {
      Text(
        "No activity recorded in this category yet.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.inkSecondary,
      )
      Spacer(modifier = Modifier.height(10.dp))
      SehatPrimaryButton(
        text = "Log a reading",
        icon = Icons.Filled.Add,
        onClick = { onNavigate(AppRoutes.LOG_READING) },
      )
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        visibleItems.forEach { item ->
          Surface(
            shape = ButtonShape,
            color = c.mistSoft,
            border = BorderStroke(1.dp, c.hairlineBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.padding(12.dp),
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
                Text(item.timestampLabel, style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
              }
              StatusChip(level = item.statusLevel, label = item.category.label)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      if (hasMore) {
        SehatSecondaryButton(
          text = "Load more (${allItems.size - visibleCount} remaining)",
          minHeight = 52.dp,
          onClick = onLoadMore,
          modifier = Modifier.fillMaxWidth(),
          testTag = "activity_load_more_btn",
        )
      } else {
        Text(
          text = "Showing all ${allItems.size} activity item(s).",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfilePrivacyTabContent(
  uiState: SehatSaathiUiState,
  jsonPreviewText: String?,
  onAddCaregiverInvite: (name: String, relationship: String, phone: String) -> String?,
  onAcceptCaregiverDemo: (String) -> Unit,
  onRequestRemoveCaregiver: (SeededCaregiver) -> Unit,
  onResendCaregiverInvite: (String) -> Unit,
  onUpdateCaregiverPermission: (String, (CaregiverSharingPermissions) -> CaregiverSharingPermissions) -> Unit,
  onTogglePhotoPrivacy: (Boolean, Boolean) -> Unit,
  onUpdateNotificationSwitches: (NotificationSwitchPrefs) -> Unit,
  onUpdateThresholds: (RiskThresholdsConfig) -> Unit,
  onToggleOfflineSimulation: (Boolean) -> Unit,
  onSyncOfflineQueue: () -> Unit,
  onRequestExportJson: () -> Unit,
  onRequestDeleteAccount: () -> Unit,
  onRequestChangePassword: () -> Unit,
  onResetDemoData: () -> Unit,
  onLogout: () -> Unit,
  onShowToast: (String) -> Unit,
) {
  val c = SehatPalette.current
  val context = LocalContext.current

  var cgName by remember { mutableStateOf("") }
  var cgRelation by remember { mutableStateOf("Family") }
  var cgPhone by remember { mutableStateOf("") }
  var cgError by remember { mutableStateOf<String?>(null) }

  var lowSugarInput by remember(uiState.thresholdsConfig) {
    mutableStateOf(uiState.thresholdsConfig.glucoseLowMgDl.toString())
  }
  var highSugarUrgentInput by remember(uiState.thresholdsConfig) {
    mutableStateOf(uiState.thresholdsConfig.glucoseHighUrgentMgDl.toString())
  }
  var bpCrisisSysInput by remember(uiState.thresholdsConfig) {
    mutableStateOf(uiState.thresholdsConfig.bpCrisisSystolic.toString())
  }

  val whoCanSeeSummary =
    remember(uiState.caregivers) {
      ProfileDomain.buildWhoCanSeeWhatSummary(uiState.caregivers)
    }

  // 1. Who Can See What + Caregiver Per-Item Sharing Controls
  SehatCard(
    containerColor = c.surface,
    modifier = Modifier.fillMaxWidth().testTag("profile_privacy_sharing_card"),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = c.clinicalBlueAccent)
      Text("Caregiver sharing & consent", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      if (uiState.session.isDemoMode) DemoPillBadge(text = "Demo")
    }
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
      shape = ButtonShape,
      color = c.mistSoft,
      border = BorderStroke(1.dp, c.clinicalBlueAccent),
      modifier = Modifier.fillMaxWidth().testTag("who_can_see_what_banner"),
    ) {
      Text(
        text = whoCanSeeSummary,
        style = MaterialTheme.typography.bodyMedium,
        color = c.deepOcean,
        modifier = Modifier.padding(12.dp),
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
    uiState.caregivers.forEach { cg ->
      val isPending = cg.consentStatus.contains("Pending", ignoreCase = true)
      val cgInitials = ProfileDomain.computeInitials(cg.name)
      Surface(
        shape = CardShape,
        color = c.mistSoft,
        border = BorderStroke(1.dp, c.hairlineBorder),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("caregiver_card_${cg.id}"),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f),
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(CircleShape).background(c.mist),
              ) {
                Text(cgInitials, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
              }
              Column {
                Text(cg.name, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                Text("${cg.relationship} · Since ${cg.sinceLabel}", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
                Text(
                  text = cg.phone,
                  style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                  color = c.clinicalBlueAccent,
                  modifier =
                    Modifier.clickable(role = Role.Button) {
                      launchSafeDialer(context, cg.phone, onShowToast)
                    },
                )
              }
            }
            StatusChip(
              level = if (isPending) HealthStatusLevel.CAUTION else HealthStatusLevel.NORMAL,
              label = cg.consentStatus,
            )
          }

          if (isPending) {
            Text(
              "All sharing toggles remain off until ${cg.name} accepts the consent invite.",
              style = MaterialTheme.typography.labelMedium,
              color = c.statusCautionFg,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              SehatPrimaryButton(
                text = "Simulate accept (Demo)",
                icon = Icons.Filled.Check,
                minHeight = 48.dp,
                onClick = { onAcceptCaregiverDemo(cg.id) },
                testTag = "accept_caregiver_${cg.id}",
              )
              SehatSecondaryButton(
                text = "Resend invite",
                minHeight = 48.dp,
                onClick = { onResendCaregiverInvite(cg.id) },
                testTag = "resend_caregiver_${cg.id}",
              )
              SehatGhostButton(
                text = "Cancel invite",
                onClick = { onRequestRemoveCaregiver(cg) },
                testTag = "cancel_caregiver_${cg.id}",
              )
            }
          } else {
            Text("Allowed to view:", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = cg.permissions.readings,
                onClick = {
                  onUpdateCaregiverPermission(cg.id) { it.copy(readings = !it.readings) }
                },
                label = { Text("Readings") },
                modifier = Modifier.testTag("perm_readings_${cg.id}"),
              )
              FilterChip(
                selected = cg.permissions.medicines,
                onClick = {
                  onUpdateCaregiverPermission(cg.id) { it.copy(medicines = !it.medicines) }
                },
                label = { Text("Medicines") },
                modifier = Modifier.testTag("perm_medicines_${cg.id}"),
              )
              FilterChip(
                selected = cg.permissions.refills,
                onClick = {
                  onUpdateCaregiverPermission(cg.id) { it.copy(refills = !it.refills) }
                },
                label = { Text("Refills") },
                modifier = Modifier.testTag("perm_refills_${cg.id}"),
              )
              FilterChip(
                selected = cg.permissions.alerts,
                onClick = {
                  onUpdateCaregiverPermission(cg.id) { it.copy(alerts = !it.alerts) }
                },
                label = { Text("Alerts") },
                modifier = Modifier.testTag("perm_alerts_${cg.id}"),
              )
              FilterChip(
                selected = cg.permissions.emergencyInfo,
                onClick = {
                  onUpdateCaregiverPermission(cg.id) { it.copy(emergencyInfo = !it.emergencyInfo) }
                },
                label = { Text("Emergency info") },
                modifier = Modifier.testTag("perm_emergency_${cg.id}"),
              )
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
              SehatGhostButton(
                text = "Remove access",
                onClick = { onRequestRemoveCaregiver(cg) },
                testTag = "remove_access_${cg.id}",
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(10.dp))
    Text("Invite a family caregiver", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
    SehatTextField(
      label = "Family contact name",
      value = cgName,
      onValueChange = { cgName = it; cgError = null },
      placeholder = "e.g. Dr. Nair or Anita",
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      SehatTextField(
        label = "Relationship",
        value = cgRelation,
        onValueChange = { cgRelation = it },
        modifier = Modifier.weight(0.45f),
      )
      SehatTextField(
        label = "Phone number",
        value = cgPhone,
        onValueChange = { cgPhone = it; cgError = null },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        placeholder = "+91 98...",
        modifier = Modifier.weight(0.55f),
      )
    }
    cgError?.let { err ->
      Spacer(modifier = Modifier.height(6.dp))
      Text(err, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg)
    }
    Spacer(modifier = Modifier.height(10.dp))
    SehatSecondaryButton(
      text = "Send consent invite (Demo)",
      minHeight = 52.dp,
      onClick = {
        val err = onAddCaregiverInvite(cgName, cgRelation, cgPhone)
        if (err != null) {
          cgError = err
        } else {
          cgName = ""
          cgPhone = ""
        }
      },
      modifier = Modifier.fillMaxWidth(),
      testTag = "add_caregiver_invite_btn",
    )
  }

  // 2. Photo Privacy & Local Notification Preferences
  SehatCard(containerColor = c.surface) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = c.clinicalBlueAccent)
      Text("Photo privacy & notifications", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    PrivacySwitchRow(
      title = "Show my profile photo to caregivers",
      subtitle = "When off, consented caregivers see only your name and initials.",
      checked = uiState.patient.showPhotoToCaregivers,
      onCheckedChange = { onTogglePhotoPrivacy(it, uiState.patient.useInitialsOnly) },
      testTag = "toggle_show_photo_caregivers",
    )
    PrivacySwitchRow(
      title = "Use initials instead of a photo",
      subtitle = "Always display ${ProfileDomain.computeInitials(uiState.patient.name)} on your avatar badge.",
      checked = uiState.patient.useInitialsOnly,
      onCheckedChange = { onTogglePhotoPrivacy(uiState.patient.showPhotoToCaregivers, it) },
      testTag = "toggle_use_initials_only",
    )
    HorizontalDivider(color = c.hairlineBorder, modifier = Modifier.padding(vertical = 8.dp))
    PrivacySwitchRow(
      title = "Missed-dose nudges",
      subtitle = "Calm in-app reminder if a scheduled dose is overdue.",
      checked = uiState.notificationSwitches.missedDoseNudges,
      onCheckedChange = {
        onUpdateNotificationSwitches(uiState.notificationSwitches.copy(missedDoseNudges = it))
      },
      testTag = "toggle_notif_missed_dose",
    )
    PrivacySwitchRow(
      title = "Weekly caregiver summary",
      subtitle = "Include 7-day adherence and reading counts in family digest.",
      checked = uiState.notificationSwitches.weeklySummary,
      onCheckedChange = {
        onUpdateNotificationSwitches(uiState.notificationSwitches.copy(weeklySummary = it))
      },
      testTag = "toggle_notif_weekly_summary",
    )
    PrivacySwitchRow(
      title = "Low-stock refill reminders",
      subtitle = "Highlight medicines with 5 days or fewer remaining.",
      checked = uiState.notificationSwitches.lowStockReminders,
      onCheckedChange = {
        onUpdateNotificationSwitches(uiState.notificationSwitches.copy(lowStockReminders = it))
      },
      testTag = "toggle_notif_low_stock",
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      "Note: Stored locally on this device. Background reminders may not fire when the app is closed or restricted by battery saver.",
      style = MaterialTheme.typography.labelSmall,
      color = c.inkSecondary,
    )
  }

  // 3. Clinician Safety Thresholds & Offline Queue Simulation
  SehatCard(containerColor = c.surface) {
    Text("Safety rule thresholds", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    Text(
      "Deterministic rules use these cutoffs (review with your doctor before changing).",
      style = MaterialTheme.typography.labelMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      SehatTextField(
        label = "Low sugar (<)",
        value = lowSugarInput,
        onValueChange = { lowSugarInput = it.filter { ch -> ch.isDigit() }.take(3) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f),
      )
      SehatTextField(
        label = "Urgent high (>)",
        value = highSugarUrgentInput,
        onValueChange = { highSugarUrgentInput = it.filter { ch -> ch.isDigit() }.take(3) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f),
      )
      SehatTextField(
        label = "BP crisis (>=)",
        value = bpCrisisSysInput,
        onValueChange = { bpCrisisSysInput = it.filter { ch -> ch.isDigit() }.take(3) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f),
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      SehatSecondaryButton(
        text = "Apply thresholds",
        onClick = {
          val next =
            uiState.thresholdsConfig.copy(
              glucoseLowMgDl = (lowSugarInput.toIntOrNull() ?: 70).coerceIn(55, 90),
              glucoseHighUrgentMgDl = (highSugarUrgentInput.toIntOrNull() ?: 300).coerceIn(220, 450),
              bpCrisisSystolic = (bpCrisisSysInput.toIntOrNull() ?: 180).coerceIn(150, 220),
            )
          onUpdateThresholds(next)
        },
        modifier = Modifier.weight(1f),
      )
      SehatGhostButton(
        text = "Defaults",
        onClick = { onUpdateThresholds(RiskThresholdsConfig()) },
      )
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
        Text(
          text =
            if (uiState.effectiveOnline) "Offline simulation: Online"
            else "Offline simulation: Active",
          style = MaterialTheme.typography.titleMedium,
          color = c.deepOcean,
        )
        Text(
          "Queue AI scans locally when offline.",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
        )
      }
      Switch(
        checked = uiState.isOfflineSimulationEnabled,
        onCheckedChange = onToggleOfflineSimulation,
        modifier = Modifier.testTag("offline_sim_switch"),
      )
    }
    if (uiState.offlineQueue.isNotEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      SehatPrimaryButton(
        text = "Sync queued scans (${uiState.offlineQueue.size})",
        icon = Icons.Filled.Sync,
        enabled = uiState.effectiveOnline && !uiState.isSyncingOfflineQueue,
        onClick = onSyncOfflineQueue,
        modifier = Modifier.fillMaxWidth(),
        testTag = "account_sync_queue_btn",
      )
    }
  }

  // 4. Security Shortcuts, JSON Export & Delete Account
  SehatCard(containerColor = c.surface) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(Icons.Filled.Security, contentDescription = null, tint = c.deepOcean)
      Text("Security & your data", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "Downloading all data, changing your password, or deleting your account requires confirming your identity first.",
      style = MaterialTheme.typography.bodyMedium,
      color = c.inkSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      SehatPrimaryButton(
        text = "Download all data (JSON)",
        icon = Icons.Filled.Download,
        onClick = onRequestExportJson,
        testTag = "download_json_btn",
      )
      SehatSecondaryButton(
        text = "Change password",
        icon = Icons.Filled.Lock,
        minHeight = 56.dp,
        onClick = onRequestChangePassword,
        testTag = "change_password_btn",
      )
      SehatSecondaryButton(
        text = "Reset demo data",
        icon = Icons.Filled.Refresh,
        minHeight = 56.dp,
        onClick = onResetDemoData,
        testTag = "reset_demo_data_btn",
      )
      SehatSecondaryButton(
        text = "Log out of all devices",
        icon = Icons.AutoMirrored.Filled.Logout,
        minHeight = 56.dp,
        onClick = onLogout,
        testTag = "logout_btn",
      )
    }

    jsonPreviewText?.let { preview ->
      Spacer(modifier = Modifier.height(12.dp))
      Surface(shape = CardShape, color = c.mist, border = BorderStroke(1.dp, c.hairlineBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("JSON Export Ready (${preview.length} bytes)", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          Text(preview.take(280) + "...", style = MaterialTheme.typography.labelSmall, color = c.ink)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(14.dp))
    Surface(
      shape = CardShape,
      color = c.statusUrgentBg,
      border = BorderStroke(1.dp, c.statusUrgentBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Danger zone", style = MaterialTheme.typography.titleMedium, color = c.statusUrgentFg)
        Text(
          "Permanently deletes your local SehatSaathi session, profile photos, cached PDF/JSON exports, and resets all records.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
        SehatDestructiveButton(
          text = "Delete account & all data",
          icon = Icons.Filled.DeleteForever,
          onClick = onRequestDeleteAccount,
          modifier = Modifier.fillMaxWidth(),
          testTag = "delete_account_btn",
        )
      }
    }
  }
}

@Composable
private fun PrivacySwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  val c = SehatPalette.current
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
      Text(subtitle, style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = PureWhite,
          checkedTrackColor = c.clinicalBlue,
          uncheckedThumbColor = c.inkSecondary,
          uncheckedTrackColor = c.mistSoft,
          uncheckedBorderColor = c.hairlineBorder,
        ),
      modifier = Modifier.testTag(testTag),
    )
  }
}

@Composable
private fun RemoveCaregiverConfirmDialog(
  caregiver: SeededCaregiver,
  onConfirmRemove: () -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.5.dp, c.statusUrgentFg),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth().testTag("remove_caregiver_confirm_dialog"),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Remove access for ${caregiver.name}?", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
        Text(
          "${caregiver.name} (${caregiver.relationship}) will immediately lose access to your shared readings, medicines, refills, and alerts.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(
            text = "Keep access",
            onClick = onDismiss,
            modifier = Modifier.weight(0.5f),
          )
          SehatDestructiveButton(
            text = "Remove access",
            onClick = onConfirmRemove,
            modifier = Modifier.weight(0.5f),
            testTag = "confirm_remove_caregiver_btn",
          )
        }
      }
    }
  }
}

@Composable
private fun ChangePasswordDemoDialog(
  onSaved: () -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  var newPass by remember { mutableStateOf("") }
  val strength = com.example.data.AuthSessionRepository.evaluatePassword(newPass)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Change password", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
        SehatTextField(
          label = "New password (10+ chars, letters & numbers)",
          value = newPass,
          onValueChange = { newPass = it },
          isPassword = true,
          helperText = strength.label,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(0.45f))
          SehatPrimaryButton(
            text = "Update password",
            enabled = strength.isValid,
            onClick = onSaved,
            modifier = Modifier.weight(0.55f),
          )
        }
      }
    }
  }
}

@Composable
private fun ReauthDialog(
  defaultEmail: String,
  isDemoMode: Boolean,
  onVerify: (String, String) -> Boolean,
  onDismiss: () -> Unit,
  onVerified: () -> Unit,
) {
  val c = SehatPalette.current
  var email by remember { mutableStateOf(defaultEmail) }
  var secret by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.5.dp, c.clinicalBlueAccent),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Confirm it is you", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
        Text(
          text =
            if (isDemoMode) "Demo re-authentication: type CONFIRM (or your 6+ char passcode) to continue."
            else "Enter your account email and password to proceed.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        SehatTextField(
          label = "Account email",
          value = email,
          onValueChange = { email = it; error = null },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        SehatTextField(
          label = if (isDemoMode) "Type CONFIRM (Demo)" else "Password",
          value = secret,
          onValueChange = { secret = it; error = null },
          isPassword = !isDemoMode,
          placeholder = if (isDemoMode) "CONFIRM" else "Password",
          testTag = "reauth_secret_input",
        )
        error?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(0.45f))
          SehatPrimaryButton(
            text = "Continue",
            onClick = {
              if (onVerify(email, secret)) onVerified()
              else error = "Verification did not match. Try typing CONFIRM."
            },
            modifier = Modifier.weight(0.55f),
            testTag = "reauth_continue_btn",
          )
        }
      }
    }
  }
}

@Composable
private fun DeleteAccountConfirmationDialog(
  onConfirmDelete: (String) -> Boolean,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  var typed by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(2.dp, c.statusUrgentFg),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Delete all data?", style = MaterialTheme.typography.headlineMedium, color = c.statusUrgentFg)
        Text(
          "Type DELETE in capital letters to permanently erase your local account, readings, medicines, and exports.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
        SehatTextField(
          label = "Type DELETE to confirm",
          value = typed,
          onValueChange = { typed = it },
          placeholder = "DELETE",
          testTag = "delete_confirm_input",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(0.45f))
          SehatDestructiveButton(
            text = "Delete forever",
            enabled = typed.trim() == "DELETE",
            onClick = { onConfirmDelete(typed) },
            modifier = Modifier.weight(0.55f),
            testTag = "confirm_delete_forever_btn",
          )
        }
      }
    }
  }
}

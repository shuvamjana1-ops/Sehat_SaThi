package com.example

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import com.example.data.CaregiverSharingPermissions
import com.example.data.DemoSeedData
import com.example.data.SeededCaregiver
import com.example.domain.profile.ActivityFilter
import com.example.domain.profile.CareCardSelection
import com.example.domain.profile.ProfileDomain
import com.example.domain.profile.ProfileEditFocusField
import com.example.ui.SehatSaathiUiState
import com.example.ui.SehatSaathiViewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.theme.SehatSaathiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileFeatureTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun validateProfileFields_enforcesNameHandleBioAndPhoneRules() {
    assertTrue(ProfileDomain.validateFullName("Ramesh Sharma").isValid)
    assertFalse(ProfileDomain.validateFullName("A").isValid)
    assertFalse(ProfileDomain.validateFullName("x".repeat(65)).isValid)

    assertTrue(ProfileDomain.validateHandle("ramesh_sharma.67").isValid)
    assertTrue(ProfileDomain.validateHandle("").isValid)
    assertFalse(ProfileDomain.validateHandle("Bad Handle!@#").isValid)
    assertFalse(ProfileDomain.validateHandle("a".repeat(31)).isValid)

    assertTrue(ProfileDomain.validateBio("Managing diabetes and BP in Pune.").isValid)
    assertFalse(ProfileDomain.validateBio("x".repeat(151)).isValid)

    assertTrue(ProfileDomain.validatePhone("+91 98765 00000").isValid)
    assertFalse(ProfileDomain.validatePhone("123").isValid)
  }

  @Test
  fun sanitizeText_stripsControlCharactersAndTrimsLength() {
    val dirty = "  Ramesh\u0000\u0007 Sharma  "
    val clean = ProfileDomain.sanitizeText(dirty, 60)
    assertEquals("Ramesh Sharma", clean)
  }

  @Test
  fun validateDobAndComputeAge_handlesValidFutureAndOutOfBoundsDates() {
    val valid67 =
      ProfileDomain.validateDobAndComputeAge(
        dob = "1959-04-12",
        refYear = 2026,
        refMonth = 5,
        refDay = 15,
      )
    assertTrue(valid67.isValid)
    assertEquals(67, valid67.computedAge)

    val valid66 =
      ProfileDomain.validateDobAndComputeAge(
        dob = "1959-08-20",
        refYear = 2026,
        refMonth = 5,
        refDay = 15,
      )
    assertTrue(valid66.isValid)
    assertEquals(66, valid66.computedAge)

    assertFalse(
      ProfileDomain.validateDobAndComputeAge("2028-01-01", 2026, 5, 15).isValid,
    )
    assertFalse(
      ProfileDomain.validateDobAndComputeAge("1880-01-01", 2026, 5, 15).isValid,
    )
    assertFalse(
      ProfileDomain.validateDobAndComputeAge("not-a-date", 2026, 5, 15).isValid,
    )
  }

  @Test
  fun computeProfileStats_computesAccurateCountsFromSeededData() {
    val stats =
      ProfileDomain.computeProfileStats(
        readings = DemoSeedData.readings14Days,
        dosesToday = DemoSeedData.dosesToday,
        caregivers = DemoSeedData.caregivers,
      )
    assertEquals(DemoSeedData.readings14Days.size, stats.readingsLogged)
    assertTrue("Streak should be positive for contiguous readings", stats.dayStreak >= 1)
    assertTrue("Adherence should be between 0 and 100", stats.adherence7dPercent in 0..100)
    assertEquals(2, stats.caregiversCount)
  }

  @Test
  fun computeProfileCompleteness_identifiesCompletedAndMissingSteps() {
    val fullCompleteness =
      ProfileDomain.computeProfileCompleteness(
        profile = DemoSeedData.patient,
        hasAvatarPhoto = true,
      )
    assertEquals(100, fullCompleteness.percent)
    assertTrue(fullCompleteness.missingItems.isEmpty())

    val missingBioAndBlood =
      ProfileDomain.computeProfileCompleteness(
        profile = DemoSeedData.patient.copy(bio = "", bloodGroup = ""),
        hasAvatarPhoto = false,
      )
    assertEquals(80, missingBioAndBlood.percent)
    assertEquals(2, missingBioAndBlood.missingItems.size)
    assertTrue(missingBioAndBlood.missingItems.any { it.field == ProfileEditFocusField.BIO })
    assertTrue(missingBioAndBlood.missingItems.any { it.field == ProfileEditFocusField.BLOOD_GROUP })
  }

  @Test
  fun caregiverPermissions_defaultOffUntilConsentAndUpdateImmediately() {
    val pendingInvite =
      SeededCaregiver(
        id = "cg-new",
        name = "Vikram Sharma",
        relationship = "Brother",
        phone = "+91 98111 22334",
        consentStatus = "Pending invite",
        priority = 2,
        sinceLabel = "Invited today",
        permissions = CaregiverSharingPermissions.DEFAULT_OFF,
      )
    assertFalse(pendingInvite.permissions.anyEnabled)

    // Attempting to enable permissions on a pending (unconsented) invite keeps them off
    val stillPending =
      ProfileDomain.updateCaregiverPermission(
        caregivers = listOf(pendingInvite),
        caregiverId = "cg-new",
      ) { it.copy(readings = true) }
    assertFalse(stillPending.first().permissions.readings)

    val summaryPendingOnly = ProfileDomain.buildWhoCanSeeWhatSummary(stillPending)
    assertTrue(summaryPendingOnly.contains("Only you"))

    // Once consented, per-item permissions toggle immediately
    val consented =
      pendingInvite.copy(
        consentStatus = "Consented",
        permissions = CaregiverSharingPermissions.DEFAULT_CONSENTED,
      )
    val toggled =
      ProfileDomain.updateCaregiverPermission(
        caregivers = listOf(consented),
        caregiverId = "cg-new",
      ) { it.copy(refills = false, emergencyInfo = false) }
    assertTrue(toggled.first().permissions.readings)
    assertTrue(toggled.first().permissions.medicines)
    assertFalse(toggled.first().permissions.refills)

    val summaryConsented = ProfileDomain.buildWhoCanSeeWhatSummary(toggled)
    assertTrue(summaryConsented.contains("Vikram Sharma"))
    assertTrue(summaryConsented.contains("Readings, Medicines, Alerts"))
  }

  @Test
  fun buildCareCardText_respectsFieldSelectionAndIncludesVerificationDisclaimer() {
    val profile =
      DemoSeedData.patient.copy(
        bloodGroup = "", // Unknown blood group should be omitted cleanly
      )
    val selection =
      CareCardSelection(
        includeAge = true,
        includeBloodGroup = true,
        includeConditions = true,
        includeAllergies = true,
        includeMedicines = false, // Excluded by toggle
        includeEmergencyContacts = true,
      )
    val text =
      ProfileDomain.buildCareCardText(
        profile = profile,
        medicines = DemoSeedData.medicines,
        selection = selection,
        dateLabel = "15 May 2026",
      )
    assertTrue(text.contains("Sunita Devi"))
    assertFalse("Blank blood group must be omitted", text.contains("Blood Group:"))
    assertFalse("Deselected medicines must be omitted", text.contains("Current Medicines:"))
    assertTrue(text.contains("Health Conditions: Type-2 Diabetes"))
    assertTrue(
      text.endsWith("Shared by the patient on 15 May 2026. Verify with a doctor or pharmacist."),
    )
  }

  @Test
  fun validateProfilePhotoMeta_rejectsLargeOrUnsupportedFilesAndCropsWithinMaxDimension() {
    assertTrue(
      ProfileDomain.validateProfilePhotoMeta(mimeType = "image/jpeg", sizeBytes = 1024 * 1024L).isValid,
    )
    assertFalse(
      ProfileDomain.validateProfilePhotoMeta(
        mimeType = "image/jpeg",
        sizeBytes = 9L * 1024L * 1024L,
      ).isValid,
    )
    assertFalse(
      ProfileDomain.validateProfilePhotoMeta(mimeType = "image/gif", sizeBytes = 2048L).isValid,
    )

    val source = Bitmap.createBitmap(1200, 900, Bitmap.Config.ARGB_8888)
    val (avatarCropped, avatarKb) =
      ProfileDomain.cropAndScaleProfileBitmap(
        source = source,
        isAvatar = true,
        zoom = 1.2f,
        panX = 0.1f,
        panY = -0.1f,
      )
    assertTrue(avatarCropped.width <= ProfileDomain.AVATAR_MAX_PX)
    assertTrue(avatarCropped.height <= ProfileDomain.AVATAR_MAX_PX)
    assertEquals(avatarCropped.width, avatarCropped.height)
    assertTrue(avatarKb >= 1)

    val (coverCropped, _) =
      ProfileDomain.cropAndScaleProfileBitmap(
        source = source,
        isAvatar = false,
        zoom = 1.0f,
        panX = 0f,
        panY = 0f,
      )
    assertTrue(coverCropped.width <= ProfileDomain.COVER_MAX_WIDTH_PX)
    assertTrue(coverCropped.height <= ProfileDomain.COVER_MAX_HEIGHT_PX)
  }

  @Test
  fun buildActivityTimeline_supportsFilteringByCategory() {
    val allItems =
      ProfileDomain.buildActivityTimeline(
        readings = DemoSeedData.readings14Days,
        dosesToday = DemoSeedData.dosesToday,
        refillHistory = emptyList(),
        alerts = DemoSeedData.alerts,
        filter = ActivityFilter.ALL,
      )
    assertTrue(allItems.isNotEmpty())

    val readingsOnly =
      ProfileDomain.buildActivityTimeline(
        readings = DemoSeedData.readings14Days,
        dosesToday = DemoSeedData.dosesToday,
        refillHistory = emptyList(),
        alerts = DemoSeedData.alerts,
        filter = ActivityFilter.READINGS,
      )
    assertEquals(DemoSeedData.readings14Days.size, readingsOnly.size)
    assertTrue(readingsOnly.all { it.category == ActivityFilter.READINGS })
  }

  @Test
  fun viewModel_updatesFullProfileCaregiverPermissionsAndCleansUpOnLogout() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = SehatSaathiViewModel(app)
    vm.enterDemoMode()

    val updatedProfile =
      vm.uiState.value.patient.copy(
        name = "Sunita K. Devi",
        handle = "sunita_delhi",
        bio = "Staying consistent with morning walks and sugar logs.",
      )
    vm.updateFullPatientProfile(updatedProfile)
    assertEquals("Sunita K. Devi", vm.uiState.value.patient.name)
    assertEquals("sunita_delhi", vm.uiState.value.patient.handle)

    vm.updateCaregiverSharingPermission("cg-1") { it.copy(refills = false) }
    val rohit = vm.uiState.value.caregivers.first { it.id == "cg-1" }
    assertFalse(rohit.permissions.refills)
    assertTrue(rohit.permissions.readings)

    val sampleAvatar = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    vm.updateAvatarBitmap(sampleAvatar)
    assertNotNull(vm.uiState.value.avatarBitmap)

    vm.logout()
    assertFalse(vm.uiState.value.session.isAuthenticated)
    assertNull(vm.uiState.value.avatarBitmap)
    assertNull(vm.uiState.value.coverBitmap)
  }

  @Test
  fun accountScreen_rendersProfileHeaderStatsTabsAndPrivacyNote() {
    val state = SehatSaathiUiState(isSessionLoaded = true)
    composeRule.setContent {
      SehatSaathiTheme {
        AccountScreen(
          uiState = state,
          contentPadding = PaddingValues(),
          onSetThemePreference = {},
          onSetSimpleMode = {},
          onSaveProfile = { _, _, _, _, _ -> },
          onSaveReminderPrefs = {},
          onUpdateThresholds = {},
          onAddCaregiverInvite = { _, _, _ -> null },
          onAcceptCaregiverDemo = {},
          onRevokeCaregiver = {},
          onVerifyReauth = { _, _ -> true },
          onDeleteAccount = { true },
          onResetDemoData = {},
          onLogout = {},
          onToggleOfflineSimulation = {},
          onSyncOfflineQueue = {},
          onShowToast = {},
        )
      }
    }

    composeRule.onNodeWithTag("profile_privacy_lock_banner").assertIsDisplayed()
    composeRule.onNodeWithTag("open_edit_profile_btn").assertExists()
    composeRule.onNodeWithTag("open_care_card_btn").assertExists()
    composeRule.onNodeWithTag("profile_stats_row").assertExists()

    composeRule.onNodeWithTag("account_scroll").performScrollToIndex(2)
    composeRule.onNodeWithTag("profile_highlights_row").assertExists()

    composeRule.onNodeWithTag("account_scroll").performScrollToIndex(3)
    composeRule.onNodeWithTag("profile_tab_bar").assertExists()

    // Switch to Privacy tab (scrolling horizontal tab bar if on compact width) and verify Who can see what summary banner is rendered
    composeRule.onNodeWithTag("profile_tab_privacy").performScrollTo().performClick()
    composeRule.onNodeWithTag("account_scroll").performScrollToIndex(4)
    composeRule.onNodeWithTag("who_can_see_what_banner").assertExists()
  }
}

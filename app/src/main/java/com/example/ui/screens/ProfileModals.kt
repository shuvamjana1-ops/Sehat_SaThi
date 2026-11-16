package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.EmergencyContactEntry
import com.example.data.HealthStatusLevel
import com.example.data.PatientProfile
import com.example.data.ReminderPreferences
import com.example.data.SeededMedicine
import com.example.domain.profile.CareCardSelection
import com.example.domain.profile.ProfileDomain
import com.example.domain.profile.ProfileEditFocusField
import com.example.export.DoctorSummaryAndDataExporter
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.ImageLoadResult
import com.example.ui.components.ImageUploadHelper
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatDestructiveButton
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileModal(
  patient: PatientProfile,
  email: String,
  reminderPrefs: ReminderPreferences,
  initialFocusField: ProfileEditFocusField? = null,
  onSave: (PatientProfile, ReminderPreferences) -> Unit,
  onGoToSecuritySettings: () -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  var fullName by remember(patient) { mutableStateOf(patient.name) }
  var handle by remember(patient) { mutableStateOf(patient.handle) }
  var bio by remember(patient) { mutableStateOf(patient.bio) }
  var dob by remember(patient) { mutableStateOf(patient.dateOfBirth) }
  var gender by remember(patient) { mutableStateOf(patient.gender) }
  var city by remember(patient) { mutableStateOf(patient.city) }
  var phone by remember(patient) { mutableStateOf(patient.phone) }
  var selectedConditions by remember(patient) {
    mutableStateOf(patient.conditions.filter { it in ProfileDomain.PRESET_CONDITIONS }.toSet())
  }
  var otherConditionText by remember(patient) {
    mutableStateOf(
      patient.conditions.filterNot { it in ProfileDomain.PRESET_CONDITIONS }.joinToString(", "),
    )
  }
  var hasOtherCondition by remember(patient) {
    mutableStateOf(patient.conditions.any { it !in ProfileDomain.PRESET_CONDITIONS })
  }
  var bloodGroup by remember(patient) { mutableStateOf(patient.bloodGroup) }
  var allergiesList by remember(patient) { mutableStateOf(patient.allergies) }
  var newAllergyInput by remember { mutableStateOf("") }
  var heightVal by remember(patient) { mutableStateOf(patient.heightValue) }
  var heightUnit by remember(patient) { mutableStateOf(patient.heightUnit.ifBlank { "cm" }) }
  var weightVal by remember(patient) { mutableStateOf(patient.weightValue) }
  var weightUnit by remember(patient) { mutableStateOf(patient.weightUnit.ifBlank { "kg" }) }
  var pharmacyName by remember(patient) { mutableStateOf(patient.pharmacyName) }
  var pharmacyPhone by remember(patient) { mutableStateOf(patient.pharmacyPhone) }

  var morningTime by remember(reminderPrefs) { mutableStateOf(reminderPrefs.morningTime) }
  var afternoonTime by remember(reminderPrefs) { mutableStateOf(reminderPrefs.afternoonTime) }
  var nightTime by remember(reminderPrefs) { mutableStateOf(reminderPrefs.nightTime) }
  var quietEnabled by remember(reminderPrefs) { mutableStateOf(reminderPrefs.quietHoursEnabled) }
  var quietStart by remember(reminderPrefs) { mutableStateOf(reminderPrefs.quietStart) }
  var quietEnd by remember(reminderPrefs) { mutableStateOf(reminderPrefs.quietEnd) }

  var emergencyContacts by remember(patient) { mutableStateOf(patient.emergencyContacts) }
  var newEcName by remember { mutableStateOf("") }
  var newEcRelation by remember { mutableStateOf("Family") }
  var newEcPhone by remember { mutableStateOf("") }
  var ecError by remember { mutableStateOf<String?>(null) }

  var showDiscardConfirm by remember { mutableStateOf(false) }

  val nameVal = ProfileDomain.validateFullName(fullName)
  val handleVal = ProfileDomain.validateHandle(handle)
  val bioVal = ProfileDomain.validateBio(bio)
  val dobVal = ProfileDomain.validateDobAndComputeAge(dob)
  val phoneVal = ProfileDomain.validatePhone(phone)
  val pharmacyPhoneVal = ProfileDomain.validatePhone(pharmacyPhone)

  val computedAge = dobVal.computedAge ?: patient.age

  val combinedConditions =
    remember(selectedConditions, hasOtherCondition, otherConditionText) {
      val extras =
        if (hasOtherCondition) {
          otherConditionText
            .split(",")
            .map { ProfileDomain.sanitizeText(it, 40) }
            .filter { it.isNotEmpty() }
        } else {
          emptyList()
        }
      (selectedConditions.toList() + extras).distinct()
    }

  val candidateProfile =
    patient.copy(
      name = ProfileDomain.sanitizeText(fullName, ProfileDomain.MAX_NAME_LENGTH),
      age = computedAge,
      handle = handle.trim().removePrefix("@").take(ProfileDomain.MAX_HANDLE_LENGTH),
      bio = ProfileDomain.sanitizeText(bio, ProfileDomain.MAX_BIO_LENGTH),
      dateOfBirth = dob.trim(),
      gender = gender,
      city = ProfileDomain.sanitizeText(city, 48),
      phone = phone.trim(),
      conditions = combinedConditions,
      bloodGroup = bloodGroup,
      allergies = allergiesList,
      heightValue = heightVal.trim(),
      heightUnit = heightUnit,
      weightValue = weightVal.trim(),
      weightUnit = weightUnit,
      pharmacyName = ProfileDomain.sanitizeText(pharmacyName, 80),
      pharmacyPhone = pharmacyPhone.trim(),
      emergencyContacts = emergencyContacts,
    )

  val candidateReminders =
    ReminderPreferences(
      morningTime = morningTime.ifBlank { "8:00 AM" },
      afternoonTime = afternoonTime.ifBlank { "1:30 PM" },
      nightTime = nightTime.ifBlank { "9:00 PM" },
      quietHoursEnabled = quietEnabled,
      quietStart = quietStart.ifBlank { "10:00 PM" },
      quietEnd = quietEnd.ifBlank { "6:30 AM" },
    )

  val isDirty = candidateProfile != patient || candidateReminders != reminderPrefs
  val isValid =
    nameVal.isValid &&
      handleVal.isValid &&
      bioVal.isValid &&
      dobVal.isValid &&
      phoneVal.isValid &&
      pharmacyPhoneVal.isValid

  fun requestCancel() {
    if (isDirty) {
      showDiscardConfirm = true
    } else {
      onDismiss()
    }
  }

  Dialog(
    onDismissRequest = { requestCancel() },
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    BackHandler { requestCancel() }
    BoxWithConstraints(
      contentAlignment = Alignment.BottomCenter,
      modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
    ) {
      val isPhoneSheet = maxWidth < 600.dp
      Surface(
        shape =
          if (isPhoneSheet) RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
          else CardShape,
        color = c.surface,
        border = BorderStroke(1.dp, c.hairlineBorder),
        shadowElevation = 16.dp,
        modifier =
          Modifier
            .align(if (isPhoneSheet) Alignment.BottomCenter else Alignment.Center)
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .fillMaxHeight(if (isPhoneSheet) 0.92f else 0.88f)
            .testTag("edit_profile_sheet"),
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Grabber handle & header
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 18.dp, end = 18.dp, bottom = 8.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .width(44.dp)
                  .height(5.dp)
                  .clip(PillShape)
                  .background(c.hairlineBorder),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column {
                Text("Edit profile", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
                if (initialFocusField != null) {
                  Text(
                    "Editing: ${initialFocusField.name.lowercase().replace('_', ' ')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.clinicalBlueAccent,
                  )
                }
              }
              IconButton(
                onClick = { requestCancel() },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("edit_profile_close_btn"),
              ) {
                Icon(Icons.Filled.Close, contentDescription = "Close edit profile", tint = c.deepOcean)
              }
            }
          }
          HorizontalDivider(color = c.hairlineBorder)

          // Scrollable form body
          Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier =
              Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
          ) {
            // Privacy lock notice
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(ButtonShape)
                  .background(c.mistSoft)
                  .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
              Icon(Icons.Filled.Lock, contentDescription = null, tint = c.deepOcean, modifier = Modifier.size(16.dp))
              Text(
                "Only you and the people you choose can see this. Stored on this device only.",
                style = MaterialTheme.typography.labelMedium,
                color = c.deepOcean,
              )
            }

            // 1. Identity & Bio
            Text("Identity & bio", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            SehatTextField(
              label = "Full name (required · ${fullName.trim().length}/${ProfileDomain.MAX_NAME_LENGTH})",
              value = fullName,
              onValueChange = { if (it.length <= ProfileDomain.MAX_NAME_LENGTH + 5) fullName = it },
              errorText = nameVal.errorMessage,
              helperText = "2 to 60 characters",
              testTag = "edit_profile_name_input",
            )

            SehatTextField(
              label = "Display handle (optional · @${handle.trim().removePrefix("@")} · ${handle.trim().removePrefix("@").length}/${ProfileDomain.MAX_HANDLE_LENGTH})",
              value = handle,
              onValueChange = { if (it.length <= ProfileDomain.MAX_HANDLE_LENGTH + 5) handle = it },
              errorText = handleVal.errorMessage,
              helperText = "Letters, numbers, dot (.) and underscore (_)",
              placeholder = "sunita.devi",
              testTag = "edit_profile_handle_input",
            )

            SehatTextField(
              label = "Short bio (${bio.length}/${ProfileDomain.MAX_BIO_LENGTH})",
              value = bio,
              onValueChange = { if (it.length <= ProfileDomain.MAX_BIO_LENGTH + 10) bio = it },
              errorText = bioVal.errorMessage,
              helperText = "Up to 150 characters shown on your profile header",
              singleLine = false,
              testTag = "edit_profile_bio_input",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              SehatTextField(
                label = "Date of birth (YYYY-MM-DD)",
                value = dob,
                onValueChange = { dob = it.take(10) },
                errorText = dobVal.errorMessage,
                helperText = "Age computed: $computedAge yrs",
                placeholder = "1959-04-12",
                modifier = Modifier.weight(0.62f),
                testTag = "edit_profile_dob_input",
              )
              SehatTextField(
                label = "City (optional)",
                value = city,
                onValueChange = { city = it.take(48) },
                placeholder = "Pune",
                modifier = Modifier.weight(0.38f),
                testTag = "edit_profile_city_input",
              )
            }

            Text("Gender (optional)", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              ProfileDomain.GENDER_OPTIONS.forEach { option ->
                FilterChip(
                  selected = gender == option,
                  onClick = { gender = option },
                  label = { Text(option) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = c.mist,
                      selectedLabelColor = c.deepOcean,
                    ),
                  modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                )
              }
            }

            SehatTextField(
              label = "Phone number (optional)",
              value = phone,
              onValueChange = { phone = it.take(20) },
              errorText = phoneVal.errorMessage,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              placeholder = "+91 98200 00111",
              testTag = "edit_profile_phone_input",
            )

            // Read-only Email + Language note
            Surface(
              shape = ButtonShape,
              color = c.mistSoft,
              border = BorderStroke(1.dp, c.hairlineBorder),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    "Email (read-only): ${email.ifBlank { "sunita.demo@sehatsaathi.local" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.ink,
                    modifier = Modifier.weight(1f),
                  )
                  SehatGhostButton(
                    text = "Security settings",
                    onClick = {
                      onDismiss()
                      onGoToSecuritySettings()
                    },
                  )
                }
                Text(
                  "App language: English (fixed for safety clarity).",
                  style = MaterialTheme.typography.labelSmall,
                  color = c.inkSecondary,
                )
              }
            }

            HorizontalDivider(color = c.hairlineBorder)

            // 2. Health Conditions, Blood Group, Allergies, Height/Weight
            Text("Health details (optional)", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            Text("Conditions", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              ProfileDomain.PRESET_CONDITIONS.forEach { cond ->
                val selected = cond in selectedConditions
                FilterChip(
                  selected = selected,
                  onClick = {
                    selectedConditions = if (selected) selectedConditions - cond else selectedConditions + cond
                  },
                  label = { Text(cond) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = c.mist,
                      selectedLabelColor = c.deepOcean,
                    ),
                  modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                )
              }
              FilterChip(
                selected = hasOtherCondition,
                onClick = { hasOtherCondition = !hasOtherCondition },
                label = { Text("Other") },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = c.mist,
                    selectedLabelColor = c.deepOcean,
                  ),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
              )
            }
            if (hasOtherCondition) {
              SehatTextField(
                label = "Other condition(s)",
                value = otherConditionText,
                onValueChange = { otherConditionText = it.take(80) },
                placeholder = "e.g. Hypothyroidism",
              )
            }

            Text("Blood group (optional)", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              ProfileDomain.BLOOD_GROUPS.filter { it.isNotEmpty() }.forEach { bg ->
                FilterChip(
                  selected = bloodGroup == bg,
                  onClick = { bloodGroup = if (bloodGroup == bg) "" else bg },
                  label = { Text(bg) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = c.mist,
                      selectedLabelColor = c.deepOcean,
                    ),
                  modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                )
              }
            }

            Text("Medicine or food allergies (optional tags)", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
            if (allergiesList.isNotEmpty()) {
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allergiesList.forEach { tag ->
                  Surface(
                    onClick = { allergiesList = allergiesList - tag },
                    shape = PillShape,
                    color = c.mist,
                    border = BorderStroke(1.dp, c.hairlineBorder),
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                      Text(tag, style = MaterialTheme.typography.labelMedium, color = c.deepOcean)
                      Spacer(modifier = Modifier.width(6.dp))
                      Icon(Icons.Filled.Close, contentDescription = "Remove $tag", tint = c.deepOcean, modifier = Modifier.size(15.dp))
                    }
                  }
                }
              }
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              SehatTextField(
                label = "Add allergy tag",
                value = newAllergyInput,
                onValueChange = { newAllergyInput = it.take(36) },
                placeholder = "e.g. Penicillin",
                modifier = Modifier.weight(1f),
              )
              SehatSecondaryButton(
                text = "Add",
                minHeight = 52.dp,
                onClick = {
                  val clean = ProfileDomain.sanitizeText(newAllergyInput, 36)
                  if (clean.isNotEmpty() && clean !in allergiesList) {
                    allergiesList = allergiesList + clean
                    newAllergyInput = ""
                  }
                },
              )
            }

            // Height & Weight with unit toggles
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.weight(1f)) {
                SehatTextField(
                  label = "Height ($heightUnit)",
                  value = heightVal,
                  onValueChange = { heightVal = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                  listOf("cm", "ft").forEach { u ->
                    FilterChip(
                      selected = heightUnit == u,
                      onClick = { heightUnit = u },
                      label = { Text(u) },
                    )
                  }
                }
              }
              Column(modifier = Modifier.weight(1f)) {
                SehatTextField(
                  label = "Weight ($weightUnit)",
                  value = weightVal,
                  onValueChange = { weightVal = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                  listOf("kg", "lb").forEach { u ->
                    FilterChip(
                      selected = weightUnit == u,
                      onClick = { weightUnit = u },
                      label = { Text(u) },
                    )
                  }
                }
              }
            }

            HorizontalDivider(color = c.hairlineBorder)

            // 3. Pharmacy & Reminder Preferences
            Text("Preferred pharmacy & reminders", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            SehatTextField(
              label = "Preferred pharmacy name",
              value = pharmacyName,
              onValueChange = { pharmacyName = it.take(80) },
            )
            SehatTextField(
              label = "Pharmacy phone / WhatsApp",
              value = pharmacyPhone,
              onValueChange = { pharmacyPhone = it.take(20) },
              errorText = pharmacyPhoneVal.errorMessage,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              SehatTextField(
                label = "Morning",
                value = morningTime,
                onValueChange = { morningTime = it.take(12) },
                modifier = Modifier.weight(1f),
              )
              SehatTextField(
                label = "Afternoon",
                value = afternoonTime,
                onValueChange = { afternoonTime = it.take(12) },
                modifier = Modifier.weight(1f),
              )
              SehatTextField(
                label = "Night",
                value = nightTime,
                onValueChange = { nightTime = it.take(12) },
                modifier = Modifier.weight(1f),
              )
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Quiet hours (${quietStart} – ${quietEnd})", style = MaterialTheme.typography.bodyMedium, color = c.ink)
              Switch(
                checked = quietEnabled,
                onCheckedChange = { quietEnabled = it },
                colors =
                  SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = c.clinicalBlue,
                  ),
              )
            }

            HorizontalDivider(color = c.hairlineBorder)

            // 4. Emergency Card Contacts (up to 3)
            Text("Emergency card contacts (${emergencyContacts.size}/3)", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
            Text(
              "Used on your Emergency Card, SOS screen, and family alerts.",
              style = MaterialTheme.typography.labelMedium,
              color = c.inkSecondary,
            )
            emergencyContacts.forEach { ec ->
              Surface(
                shape = ButtonShape,
                color = c.mistSoft,
                border = BorderStroke(1.dp, if (ec.isPrimary) c.clinicalBlueAccent else c.hairlineBorder),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.padding(12.dp),
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      Text(ec.name, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
                      if (ec.isPrimary) {
                        StatusChip(level = HealthStatusLevel.INFO, label = "Primary")
                      }
                    }
                    Text("${ec.relationship} · ${ec.phone}", style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
                  }
                  Row {
                    if (!ec.isPrimary) {
                      IconButton(
                        onClick = {
                          emergencyContacts =
                            emergencyContacts.map { item -> item.copy(isPrimary = item.id == ec.id) }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                      ) {
                        Icon(Icons.Filled.Star, contentDescription = "Mark ${ec.name} primary", tint = c.clinicalBlueAccent)
                      }
                    }
                    IconButton(
                      onClick = {
                        val remaining = emergencyContacts.filterNot { it.id == ec.id }
                        emergencyContacts =
                          if (remaining.isNotEmpty() && remaining.none { it.isPrimary }) {
                            remaining.mapIndexed { idx, item -> item.copy(isPrimary = idx == 0) }
                          } else {
                            remaining
                          }
                      },
                      modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                      Icon(Icons.Filled.Delete, contentDescription = "Remove ${ec.name}", tint = c.statusUrgentFg)
                    }
                  }
                }
              }
            }

            if (emergencyContacts.size < 3) {
              SehatTextField(
                label = "New contact name",
                value = newEcName,
                onValueChange = { newEcName = it.take(50); ecError = null },
                placeholder = "e.g. Anita Sharma",
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SehatTextField(
                  label = "Relationship",
                  value = newEcRelation,
                  onValueChange = { newEcRelation = it.take(24) },
                  modifier = Modifier.weight(0.45f),
                )
                SehatTextField(
                  label = "Phone",
                  value = newEcPhone,
                  onValueChange = { newEcPhone = it.take(20); ecError = null },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                  placeholder = "+91 98...",
                  modifier = Modifier.weight(0.55f),
                )
              }
              ecError?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg) }
              SehatSecondaryButton(
                text = "Add emergency contact",
                icon = Icons.Filled.Add,
                minHeight = 48.dp,
                onClick = {
                  val cleanN = ProfileDomain.sanitizeText(newEcName, 50)
                  val phVal = ProfileDomain.validatePhone(newEcPhone)
                  if (cleanN.length < 2) {
                    ecError = "Enter at least 2 characters for the contact name."
                  } else if (newEcPhone.isBlank() || !phVal.isValid) {
                    ecError = "Enter a valid phone number."
                  } else {
                    val newEntry =
                      EmergencyContactEntry(
                        id = "ec-${System.currentTimeMillis()}",
                        name = cleanN,
                        relationship = ProfileDomain.sanitizeText(newEcRelation, 24).ifEmpty { "Family" },
                        phone = newEcPhone.trim(),
                        isPrimary = emergencyContacts.isEmpty(),
                      )
                    emergencyContacts = emergencyContacts + newEntry
                    newEcName = ""
                    newEcPhone = ""
                  }
                },
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }

          HorizontalDivider(color = c.hairlineBorder)

          // Sticky footer: Save & Cancel
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
          ) {
            SehatSecondaryButton(
              text = "Cancel",
              minHeight = 56.dp,
              onClick = { requestCancel() },
              modifier = Modifier.weight(0.42f),
              testTag = "edit_profile_cancel_btn",
            )
            SehatPrimaryButton(
              text = "Save changes",
              icon = Icons.Filled.Save,
              enabled = isDirty && isValid,
              onClick = {
                onSave(candidateProfile, candidateReminders)
                onDismiss()
              },
              modifier = Modifier.weight(0.58f),
              testTag = "edit_profile_save_btn",
            )
          }
        }
      }
    }
  }

  if (showDiscardConfirm) {
    Dialog(onDismissRequest = { showDiscardConfirm = false }) {
      Surface(
        shape = CardShape,
        color = c.surface,
        border = BorderStroke(1.5.dp, c.statusCautionBorder),
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth().testTag("discard_changes_dialog"),
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Discard changes?", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
          Text(
            "You have unsaved edits to your profile. Keep editing or discard your changes?",
            style = MaterialTheme.typography.bodyMedium,
            color = c.ink,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SehatPrimaryButton(
              text = "Keep editing",
              onClick = { showDiscardConfirm = false },
              modifier = Modifier.weight(0.55f),
              testTag = "keep_editing_btn",
            )
            SehatDestructiveButton(
              text = "Discard",
              onClick = {
                showDiscardConfirm = false
                onDismiss()
              },
              modifier = Modifier.weight(0.45f),
              testTag = "discard_changes_confirm_btn",
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoCropAndZoomDialog(
  isAvatar: Boolean,
  currentBitmap: Bitmap?,
  patientName: String,
  onSaveBitmap: (Bitmap?) -> Unit,
  onShowToast: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  var workingBitmap by remember(currentBitmap) { mutableStateOf(currentBitmap) }
  var zoom by remember { mutableFloatStateOf(1f) }
  var panX by remember { mutableFloatStateOf(0f) }
  var panY by remember { mutableFloatStateOf(0f) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  val pickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        val maxDim = if (isAvatar) ProfileDomain.AVATAR_MAX_PX * 2 else ProfileDomain.COVER_MAX_WIDTH_PX
        when (
          val res =
            ImageUploadHelper.loadAndCompressImage(
              context = context,
              uri = uri,
              maxDimensionPx = maxDim,
              maxBytes = ProfileDomain.MAX_PROFILE_PHOTO_BYTES,
              jpegQuality = ProfileDomain.PROFILE_JPEG_QUALITY,
            )
        ) {
          is ImageLoadResult.Success -> {
            workingBitmap = res.bitmap
            zoom = 1f
            panX = 0f
            panY = 0f
            errorMsg = null
          }
          is ImageLoadResult.Error -> {
            errorMsg = res.message
          }
        }
      }
    }

  val previewBitmap =
    remember(workingBitmap, zoom, panX, panY, isAvatar) {
      workingBitmap?.let { bmp ->
        ProfileDomain.cropAndScaleProfileBitmap(bmp, isAvatar = isAvatar, zoom = zoom, panX = panX, panY = panY).first
      }
    }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      shadowElevation = 14.dp,
      modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).testTag("photo_crop_dialog"),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = if (isAvatar) "Update profile photo" else "Update cover banner",
            style = MaterialTheme.typography.headlineMedium,
            color = c.deepOcean,
          )
          IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(Icons.Filled.Close, contentDescription = "Close photo editor", tint = c.deepOcean)
          }
        }

        Text(
          text =
            if (isAvatar) {
              "JPEG, PNG or WebP up to 8 MB. Resized client-side to 512px (80% quality) and stored only on this device."
            } else {
              "JPEG, PNG or WebP up to 8 MB. Resized client-side to 1600px wide (80% quality) and stored only on this device."
            },
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )

        // Live crop preview box with drag-to-reposition
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .fillMaxWidth()
              .height(if (isAvatar) 210.dp else 160.dp)
              .clip(CardShape)
              .background(c.mistSoft)
              .border(1.dp, c.hairlineBorder, CardShape)
              .pointerInput(workingBitmap) {
                if (workingBitmap != null) {
                  detectDragGestures { change, dragAmount ->
                    change.consume()
                    panX = (panX - dragAmount.x / 250f).coerceIn(-1f, 1f)
                    panY = (panY - dragAmount.y / 250f).coerceIn(-1f, 1f)
                  }
                }
              },
        ) {
          if (previewBitmap != null) {
            if (isAvatar) {
              Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Cropped avatar preview",
                contentScale = ContentScale.Crop,
                modifier =
                  Modifier
                    .size(164.dp)
                    .clip(CircleShape)
                    .border(3.dp, c.clinicalBlueAccent, CircleShape),
              )
            } else {
              Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Cropped cover preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CardShape),
              )
            }
          } else {
            Text(
              "No photo selected — default palette tint or initials will be shown.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
              textAlign = TextAlign.Start,
              modifier = Modifier.padding(16.dp),
            )
          }
        }

        if (workingBitmap != null) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              "Zoom (${String.format(Locale.US, "%.1fx", zoom)}) · Drag image to reposition",
              style = MaterialTheme.typography.labelLarge,
              color = c.deepOcean,
            )
            Slider(
              value = zoom,
              onValueChange = { zoom = it },
              valueRange = 1f..3f,
              colors = SliderDefaults.colors(thumbColor = c.clinicalBlue, activeTrackColor = c.clinicalBlue),
              modifier = Modifier.testTag("photo_zoom_slider"),
            )
          }
        }

        errorMsg?.let {
          Text(it, style = MaterialTheme.typography.labelMedium, color = c.statusUrgentFg)
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SehatSecondaryButton(
            text = "Choose from gallery",
            icon = Icons.Filled.PhotoLibrary,
            minHeight = 48.dp,
            onClick = {
              pickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
              )
            },
            testTag = "photo_choose_gallery_btn",
          )
          SehatSecondaryButton(
            text = if (isAvatar) "Use sample portrait (Demo)" else "Use sample cover (Demo)",
            icon = Icons.Filled.CameraAlt,
            minHeight = 48.dp,
            onClick = {
              workingBitmap = createSampleProfileBitmap(isAvatar = isAvatar, patientName = patientName)
              zoom = 1f
              panX = 0f
              panY = 0f
              errorMsg = null
            },
            testTag = "photo_sample_demo_btn",
          )
          if (workingBitmap != null) {
            SehatGhostButton(
              text = "Remove photo",
              onClick = {
                workingBitmap = null
                onSaveBitmap(null)
                onDismiss()
              },
              testTag = "photo_remove_btn",
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatSecondaryButton(
            text = "Cancel",
            onClick = onDismiss,
            modifier = Modifier.weight(0.45f),
          )
          SehatPrimaryButton(
            text = "Save photo",
            icon = Icons.Filled.Check,
            enabled = previewBitmap != null,
            onClick = {
              onSaveBitmap(previewBitmap)
              onDismiss()
            },
            modifier = Modifier.weight(0.55f),
            testTag = "photo_save_btn",
          )
        }
      }
    }
  }
}

@Composable
fun AvatarFullPreviewDialog(
  patient: PatientProfile,
  avatarBitmap: Bitmap?,
  onEditAvatar: () -> Unit,
  onRemoveAvatar: () -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  val initials = ProfileDomain.computeInitials(patient.name)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      shadowElevation = 14.dp,
      modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).testTag("avatar_full_preview_dialog"),
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(22.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(patient.name, style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(Icons.Filled.Close, contentDescription = "Close preview", tint = c.deepOcean)
          }
        }

        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier
              .size(220.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(c.mist, c.clinicalBlue.copy(alpha = 0.25f))),
              )
              .border(4.dp, c.clinicalBlueAccent, CircleShape),
        ) {
          if (avatarBitmap != null && !patient.useInitialsOnly) {
            Image(
              bitmap = avatarBitmap.asImageBitmap(),
              contentDescription = "${patient.name} full-size profile photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
          } else {
            Text(
              text = initials,
              style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
              color = c.deepOcean,
            )
          }
        }

        Text(
          "Only you and the people you choose can see this.",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
          textAlign = TextAlign.Start,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SehatPrimaryButton(
            text = "Change photo",
            icon = Icons.Filled.CameraAlt,
            onClick = {
              onDismiss()
              onEditAvatar()
            },
            modifier = Modifier.weight(1f),
          )
          if (avatarBitmap != null) {
            SehatSecondaryButton(
              text = "Remove",
              minHeight = 56.dp,
              onClick = {
                onRemoveAvatar()
                onDismiss()
              },
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShareCareCardDialog(
  uiState: SehatSaathiUiState,
  onShowToast: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  var selection by remember { mutableStateOf(CareCardSelection()) }
  val todayLabel = remember {
    SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
  }
  val careCardText =
    remember(uiState.patient, uiState.medicines, selection, todayLabel) {
      ProfileDomain.buildCareCardText(
        profile = uiState.patient,
        medicines = uiState.medicines,
        selection = selection,
        dateLabel = todayLabel,
      )
    }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      shadowElevation = 16.dp,
      modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp).testTag("care_card_dialog"),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column {
            Text("Share care card", style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
            Text(
              "Pick which fields to include. No public link or server upload.",
              style = MaterialTheme.typography.labelMedium,
              color = c.inkSecondary,
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(Icons.Filled.Close, contentDescription = "Close care card", tint = c.deepOcean)
          }
        }

        // Field selection chips
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = selection.includeAge,
            onClick = { selection = selection.copy(includeAge = !selection.includeAge) },
            label = { Text("Age & City") },
            modifier = Modifier.testTag("care_card_toggle_age"),
          )
          FilterChip(
            selected = selection.includeBloodGroup,
            onClick = { selection = selection.copy(includeBloodGroup = !selection.includeBloodGroup) },
            label = { Text("Blood group") },
            modifier = Modifier.testTag("care_card_toggle_blood"),
          )
          FilterChip(
            selected = selection.includeConditions,
            onClick = { selection = selection.copy(includeConditions = !selection.includeConditions) },
            label = { Text("Conditions") },
            modifier = Modifier.testTag("care_card_toggle_conditions"),
          )
          FilterChip(
            selected = selection.includeAllergies,
            onClick = { selection = selection.copy(includeAllergies = !selection.includeAllergies) },
            label = { Text("Allergies") },
            modifier = Modifier.testTag("care_card_toggle_allergies"),
          )
          FilterChip(
            selected = selection.includeMedicines,
            onClick = { selection = selection.copy(includeMedicines = !selection.includeMedicines) },
            label = { Text("Medicines (names)") },
            modifier = Modifier.testTag("care_card_toggle_medicines"),
          )
          FilterChip(
            selected = selection.includeEmergencyContacts,
            onClick = { selection = selection.copy(includeEmergencyContacts = !selection.includeEmergencyContacts) },
            label = { Text("Emergency contacts") },
            modifier = Modifier.testTag("care_card_toggle_contacts"),
          )
        }

        // Live Care Card Preview
        SehatCard(
          containerColor = c.mistSoft,
          borderColor = c.clinicalBlueAccent,
          modifier = Modifier.fillMaxWidth().testTag("care_card_live_preview"),
        ) {
          Text(
            text = careCardText,
            style = MaterialTheme.typography.bodyMedium.tabular(),
            color = c.ink,
          )
        }

        // Actions: Copy as text, Download PDF, Print
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SehatPrimaryButton(
            text = "Copy as text",
            icon = Icons.Filled.ContentCopy,
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              clipboard?.setPrimaryClip(ClipData.newPlainText("SehatSaathi Care Card", careCardText))
              onShowToast("Care card copied to clipboard.")
            },
            testTag = "care_card_copy_btn",
          )
          SehatSecondaryButton(
            text = "Download PDF",
            icon = Icons.Filled.Download,
            minHeight = 56.dp,
            onClick = {
              val pdfFile = DoctorSummaryAndDataExporter.generateCareCardPdf(context, careCardText)
              DoctorSummaryAndDataExporter.openPdfWithIntent(context, pdfFile)
              onShowToast("Saved ${pdfFile.name}.")
            },
            testTag = "care_card_pdf_btn",
          )
          SehatSecondaryButton(
            text = "Print",
            icon = Icons.Filled.Print,
            minHeight = 56.dp,
            onClick = {
              printCareCardHtml(context, careCardText, onShowToast)
            },
            testTag = "care_card_print_btn",
          )
        }
      }
    }
  }
}

private fun printCareCardHtml(
  context: Context,
  careCardText: String,
  onShowToast: (String) -> Unit,
) {
  runCatching {
    val webView = WebView(context)
    val escaped =
      careCardText
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br/>")
    val html =
      """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8"/>
        <title>SehatSaathi Care Card</title>
        <style>
          body { font-family: sans-serif; color: #000; background: #fff; padding: 24px; line-height: 1.6; font-size: 16px; }
          .box { border: 2px solid #000; border-radius: 12px; padding: 20px; max-width: 600px; }
        </style>
      </head>
      <body>
        <div class="box">$escaped</div>
      </body>
      </html>
      """.trimIndent()
    webView.webViewClient =
      object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String?) {
          val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
          if (printManager != null) {
            val adapter = view.createPrintDocumentAdapter("SehatSaathi_Care_Card")
            printManager.print("SehatSaathi_Care_Card", adapter, PrintAttributes.Builder().build())
          } else {
            onShowToast("Print service unavailable on this device.")
          }
        }
      }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    onShowToast("Opening print preview...")
  }.onFailure {
    onShowToast("Print preview could not open in this environment.")
  }
}

fun createSampleProfileBitmap(isAvatar: Boolean, patientName: String): Bitmap {
  val width = if (isAvatar) 512 else 1200
  val height = if (isAvatar) 512 else 450
  val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = AndroidCanvas(bmp)
  val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)

  // Deep Ocean -> Clinical Blue -> Mist palette background
  paint.color = AndroidColor.rgb(11, 79, 108)
  canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

  paint.color = AndroidColor.rgb(14, 110, 140)
  canvas.drawCircle(width * 0.75f, height * 0.35f, height * 0.55f, paint)

  paint.color = AndroidColor.rgb(232, 241, 245)
  canvas.drawCircle(width * 0.25f, height * 0.8f, height * 0.35f, paint)

  if (isAvatar) {
    paint.color = AndroidColor.WHITE
    paint.textSize = 160f
    paint.textAlign = AndroidPaint.Align.CENTER
    val initials = ProfileDomain.computeInitials(patientName)
    canvas.drawText(initials, width / 2f, height / 2f + 55f, paint)
  }
  return bmp
}

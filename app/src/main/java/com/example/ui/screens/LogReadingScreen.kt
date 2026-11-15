package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.HealthStatusLevel
import com.example.data.SeededCaregiver
import com.example.domain.risk.ReadingContext
import com.example.ui.schedule.ScheduleSpeechHelper
import com.example.domain.risk.RiskAssessment
import com.example.domain.risk.RiskLevel
import com.example.domain.risk.SymptomFlags
import com.example.domain.risk.VitalType
import com.example.ui.EscalationDemoState
import com.example.ui.PendingReadingConfirmation
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.ImageLoadResult
import com.example.ui.components.ImageUploadHelper
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatCardSkeleton
import com.example.ui.components.SehatDestructiveButton
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatInlineLoadingBanner
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.components.launchSafeDialer
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular
import java.io.File
import java.net.URLEncoder
import kotlin.math.cos
import kotlin.math.sin

private enum class LogInputMode(val label: String) {
  KEYPAD("Keypad"),
  PHOTO("Photo"),
  VOICE("Voice"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogReadingScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onStageKeypadConfirmation: (VitalType, Int?, Int?, Int?) -> Unit,
  onParsePhotoBitmap: (Bitmap) -> Unit,
  onParseVoicePhrase: (String) -> Unit,
  onClearPending: () -> Unit,
  onConfirmAndSave: (ReadingContext, SymptomFlags, Boolean) -> Unit,
  onExplainAi: (RiskAssessment) -> Unit,
  onAcknowledgeAlert: () -> Unit,
  onOpenEmergency: (RiskAssessment) -> Unit,
  onClearLatestResult: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  var inputMode by remember { mutableStateOf(LogInputMode.KEYPAD) }
  var vitalType by remember { mutableStateOf(VitalType.GLUCOSE) }
  var readingContext by remember { mutableStateOf(ReadingContext.FASTING) }
  var symptoms by remember { mutableStateOf(SymptomFlags()) }
  var isRecheck by remember { mutableStateOf(false) }

  // Keypad state
  var glucoseInput by remember { mutableStateOf("") }
  var systolicInput by remember { mutableStateOf("") }
  var diastolicInput by remember { mutableStateOf("") }
  var editingSystolic by remember { mutableStateOf(true) }

  // Photo & voice state
  var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
  var voicePhrase by remember { mutableStateOf("") }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        when (val result = ImageUploadHelper.loadAndCompressImage(context, uri)) {
          is ImageLoadResult.Success -> {
            previewBitmap = result.bitmap
            onParsePhotoBitmap(result.bitmap)
          }
          is ImageLoadResult.Error -> onShowToast(result.message)
        }
      }
    }

  val cameraLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
      val uri = pendingCameraUri
      if (saved && uri != null) {
        when (val result = ImageUploadHelper.loadAndCompressImage(context, uri)) {
          is ImageLoadResult.Success -> {
            previewBitmap = result.bitmap
            onParsePhotoBitmap(result.bitmap)
          }
          is ImageLoadResult.Error -> onShowToast(result.message)
        }
      }
    }

  val speechLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val spoken =
          result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
          voicePhrase = spoken
          onParseVoicePhrase(spoken)
        }
      }
    }

  val gaugeValue =
    when {
      uiState.pendingConfirmation?.glucoseMgDl != null -> uiState.pendingConfirmation.glucoseMgDl.toFloat()
      uiState.pendingConfirmation?.systolic != null -> uiState.pendingConfirmation.systolic.toFloat()
      vitalType == VitalType.GLUCOSE -> glucoseInput.toFloatOrNull() ?: 120f
      else -> systolicInput.toFloatOrNull() ?: 120f
    }

  LazyColumn(
    contentPadding =
      PaddingValues(16.dp, 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier.fillMaxSize().testTag("log_reading_scroll"),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Log sugar or blood pressure", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        Text(
          "Choose keypad, photo of your meter, or voice. You always confirm before saving.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
      }
    }

    // Animated Gauge Dial
    item {
      SehatCard(containerColor = c.surface) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = if (vitalType == VitalType.GLUCOSE) "Sugar gauge (mg/dL)" else "Systolic BP gauge (mmHg)",
            style = MaterialTheme.typography.titleMedium,
            color = c.deepOcean,
          )
          DemoPillBadge(text = "Rules-based")
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVitalsGauge(
          vitalType = uiState.pendingConfirmation?.vitalType ?: vitalType,
          value = gaugeValue,
          displayLabel =
            when {
              uiState.pendingConfirmation?.glucoseMgDl != null -> "${uiState.pendingConfirmation.glucoseMgDl} mg/dL"
              uiState.pendingConfirmation?.systolic != null ->
                "${uiState.pendingConfirmation.systolic}/${uiState.pendingConfirmation.diastolic} mmHg"
              vitalType == VitalType.GLUCOSE ->
                if (glucoseInput.isNotBlank()) "$glucoseInput mg/dL" else "Enter value"
              else ->
                if (systolicInput.isNotBlank() || diastolicInput.isNotBlank())
                  "${systolicInput.ifBlank { "--" }}/${diastolicInput.ifBlank { "--" }} mmHg"
                else "Enter BP"
            },
        )
      }
    }

    // Latest Saved Result Card (if just evaluated)
    uiState.latestAssessment?.let { assessment ->
      item {
        ReadingResultCard(
          assessment = assessment,
          caregivers = uiState.caregivers,
          isOnline = uiState.effectiveOnline,
          lastAlertSent = uiState.lastAlertSentForAssessment,
          cooldownSuppressedMsg = uiState.cooldownSuppressedMessage,
          escalationDemo = uiState.escalationDemo,
          isExplainingAi = uiState.isExplainingAi,
          aiExplanation = uiState.aiPlainExplanation,
          onExplainAi = { onExplainAi(assessment) },
          onAcknowledgeAlert = onAcknowledgeAlert,
          onOpenEmergency = { onOpenEmergency(assessment) },
          onDismissResult = onClearLatestResult,
        )
      }
    }

    // Pending Confirmation Step (Mandatory before saving!)
    uiState.pendingConfirmation?.let { pending ->
      item {
        ConfirmationBeforeSaveCard(
          pending = pending,
          readingContext = readingContext,
          symptoms = symptoms,
          isRecheck = isRecheck,
          onContextChange = { readingContext = it },
          onSymptomsChange = { symptoms = it },
          onRecheckChange = { isRecheck = it },
          onConfirm = { onConfirmAndSave(readingContext, symptoms, isRecheck) },
          onEdit = onClearPending,
        )
      }
    }

    // Input Mode Switcher: Keypad / Photo / Voice
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        LogInputMode.entries.forEach { mode ->
          val selected = inputMode == mode
          val icon =
            when (mode) {
              LogInputMode.KEYPAD -> Icons.Filled.Dialpad
              LogInputMode.PHOTO -> Icons.Filled.CameraAlt
              LogInputMode.VOICE -> Icons.Filled.Mic
            }
          Surface(
            onClick = { inputMode = mode },
            shape = ButtonShape,
            color = if (selected) c.clinicalBlue else c.surface,
            border = BorderStroke(1.5.dp, if (selected) c.clinicalBlue else c.hairlineBorder),
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("mode_${mode.name.lowercase()}"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PureWhite else c.deepOcean,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = mode.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) PureWhite else c.deepOcean,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }

    if (uiState.isParsingAi) {
      item {
        SehatInlineLoadingBanner(
          title = "Reading your meter value...",
          subtitle = "Checking numbers with Gemini. You will confirm before saving.",
        )
      }
    }

    uiState.aiParseError?.let { err ->
      item {
        Surface(
          shape = CardShape,
          color = c.statusCautionBg,
          border = BorderStroke(1.dp, c.statusCautionBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp),
          ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = c.statusCautionFg)
            Text(err, style = MaterialTheme.typography.bodyMedium, color = c.statusCautionFg)
          }
        }
      }
    }

    // Mode-specific input card
    item {
      when (inputMode) {
        LogInputMode.KEYPAD -> {
          KeypadInputCard(
            vitalType = vitalType,
            onVitalTypeChange = { vitalType = it },
            glucoseInput = glucoseInput,
            systolicInput = systolicInput,
            diastolicInput = diastolicInput,
            editingSystolic = editingSystolic,
            onToggleBpField = { editingSystolic = it },
            onDigit = { digit ->
              if (vitalType == VitalType.GLUCOSE) {
                if (glucoseInput.length < 3) glucoseInput += digit
              } else if (editingSystolic) {
                if (systolicInput.length < 3) systolicInput += digit
              } else {
                if (diastolicInput.length < 3) diastolicInput += digit
              }
            },
            onBackspace = {
              if (vitalType == VitalType.GLUCOSE) {
                if (glucoseInput.isNotEmpty()) glucoseInput = glucoseInput.dropLast(1)
              } else if (editingSystolic) {
                if (systolicInput.isNotEmpty()) systolicInput = systolicInput.dropLast(1)
              } else {
                if (diastolicInput.isNotEmpty()) diastolicInput = diastolicInput.dropLast(1)
              }
            },
            onClear = {
              glucoseInput = ""
              systolicInput = ""
              diastolicInput = ""
            },
            onReview = {
              if (vitalType == VitalType.GLUCOSE) {
                val g = glucoseInput.toIntOrNull()
                if (g == null || g !in 20..900) {
                  onShowToast("Please enter a valid sugar number between 20 and 900.")
                } else {
                  onStageKeypadConfirmation(VitalType.GLUCOSE, g, null, null)
                }
              } else {
                val s = systolicInput.toIntOrNull()
                val d = diastolicInput.toIntOrNull()
                if (s == null || d == null || s !in 40..300 || d !in 25..200) {
                  onShowToast("Please enter valid upper and lower blood pressure numbers.")
                } else {
                  onStageKeypadConfirmation(VitalType.BLOOD_PRESSURE, null, s, d)
                }
              }
            },
          )
        }

        LogInputMode.PHOTO -> {
          SehatCard(containerColor = c.surface) {
            Text("Photo of glucometer or BP monitor", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Take a clear photo of the screen. Gemini reads the digits and asks you to confirm before saving.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              SehatPrimaryButton(
                text = "Take photo",
                icon = Icons.Filled.CameraAlt,
                onClick = {
                  val uri = createTempPhotoUri(context)
                  pendingCameraUri = uri
                  cameraLauncher.launch(uri)
                },
                testTag = "photo_take_btn",
              )
              SehatSecondaryButton(
                text = "Choose from gallery",
                icon = Icons.Filled.PhotoLibrary,
                minHeight = 56.dp,
                onClick = {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                  )
                },
                testTag = "photo_gallery_btn",
              )
            }
            previewBitmap?.let { bmp ->
              Spacer(modifier = Modifier.height(14.dp))
              Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Selected meter photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(CardShape),
              )
            }
          }
        }

        LogInputMode.VOICE -> {
          SehatCard(containerColor = c.surface) {
            Text("Voice reading", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Say for example: \"Sugar 182\" or \"BP 150 by 95\". You will confirm the number before it is saved.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
            Spacer(modifier = Modifier.height(14.dp))
            SehatPrimaryButton(
              text = "Speak reading (en-IN)",
              icon = Icons.Filled.Mic,
              onClick = {
                val intent =
                  Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                      RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                      RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra(
                      RecognizerIntent.EXTRA_PROMPT,
                      "Say sugar or BP reading (e.g. Sugar 182)",
                    )
                  }
                try {
                  speechLauncher.launch(intent)
                } catch (_: ActivityNotFoundException) {
                  onShowToast(
                    "Voice input unavailable or microphone permission denied. Allow microphone access in Android Settings or enter the numbers below.",
                  )
                }
              },
              modifier = Modifier.fillMaxWidth(),
              testTag = "voice_mic_btn",
            )
            Spacer(modifier = Modifier.height(14.dp))
            SehatTextField(
              label = "Spoken or typed reading phrase",
              value = voicePhrase,
              onValueChange = { voicePhrase = it },
              placeholder = "e.g. Sugar 240 or BP 150 by 95",
              testTag = "voice_phrase_input",
            )
            Spacer(modifier = Modifier.height(10.dp))
            SehatSecondaryButton(
              text = "Extract numbers to confirm",
              onClick = { onParseVoicePhrase(voicePhrase) },
              enabled = voicePhrase.isNotBlank(),
              modifier = Modifier.fillMaxWidth(),
              testTag = "voice_parse_btn",
            )
          }
        }
      }
    }

    // Context & Symptom Toggles (always visible so user can tag before/during confirmation)
    item {
      ContextAndSymptomsCard(
        readingContext = readingContext,
        symptoms = symptoms,
        isRecheck = isRecheck,
        onContextChange = { readingContext = it },
        onSymptomsChange = { symptoms = it },
        onRecheckChange = { isRecheck = it },
      )
    }

    item {
      Text(
        text = uiState.thresholdsConfig.disclaimerNote,
        style = MaterialTheme.typography.labelSmall,
        color = c.inkSecondary,
      )
    }
  }
}

@Composable
private fun AnimatedVitalsGauge(
  vitalType: VitalType,
  value: Float,
  displayLabel: String,
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val minVal = if (vitalType == VitalType.GLUCOSE) 40f else 70f
  val maxVal = if (vitalType == VitalType.GLUCOSE) 340f else 210f
  val targetFraction = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)

  val animatedFraction by
    animateFloatAsState(
      targetValue = targetFraction,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 500, easing = FastOutSlowInEasing),
      label = "vitals_gauge",
    )

  val trackColor = c.mist
  val activeColor =
    if (vitalType == VitalType.GLUCOSE) {
      when {
        value < 70f || value > 300f -> c.statusUrgentFg
        value > 250f -> c.statusCautionFg
        else -> c.clinicalBlueAccent
      }
    } else {
      when {
        value >= 180f -> c.statusUrgentFg
        value >= 140f -> c.statusCautionFg
        else -> c.clinicalBlueAccent
      }
    }
  val needleOuter = c.surface
  val needleInner = c.deepOcean

  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxWidth().height(130.dp)) {
      Canvas(modifier = Modifier.width(240.dp).height(120.dp)) {
        val stroke = 16.dp.toPx()
        val arcSize = Size(size.width - stroke, (size.height - stroke) * 1.85f)
        val topLeft = Offset(stroke / 2f, stroke / 2f)

        // Track background
        drawArc(
          color = trackColor,
          startAngle = 180f,
          sweepAngle = 180f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        drawArc(
          color = activeColor,
          startAngle = 180f,
          sweepAngle = 180f * animatedFraction,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        // Needle indicator dot
        val angleRad = Math.toRadians(180.0 + 180.0 * animatedFraction)
        val cx = size.width / 2f
        val cy = topLeft.y + arcSize.height / 2f
        val r = arcSize.width / 2f
        val dotX = cx + (r * cos(angleRad)).toFloat()
        val dotY = cy + (r * sin(angleRad)).toFloat()
        drawCircle(color = needleOuter, radius = 9.dp.toPx(), center = Offset(dotX, dotY))
        drawCircle(color = needleInner, radius = 6.dp.toPx(), center = Offset(dotX, dotY))
      }

      Text(
        text = displayLabel,
        style = MaterialTheme.typography.headlineMedium.tabular(),
        color = c.deepOcean,
        modifier = Modifier.padding(bottom = 8.dp),
      )
    }
  }
}

@Composable
private fun KeypadInputCard(
  vitalType: VitalType,
  onVitalTypeChange: (VitalType) -> Unit,
  glucoseInput: String,
  systolicInput: String,
  diastolicInput: String,
  editingSystolic: Boolean,
  onToggleBpField: (Boolean) -> Unit,
  onDigit: (String) -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onReview: () -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(containerColor = c.surface) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      VitalTypeButton(
        label = "Blood Sugar (mg/dL)",
        selected = vitalType == VitalType.GLUCOSE,
        onClick = { onVitalTypeChange(VitalType.GLUCOSE) },
        modifier = Modifier.weight(1f),
        testTag = "vital_type_glucose",
      )
      VitalTypeButton(
        label = "Blood Pressure",
        selected = vitalType == VitalType.BLOOD_PRESSURE,
        onClick = { onVitalTypeChange(VitalType.BLOOD_PRESSURE) },
        modifier = Modifier.weight(1f),
        testTag = "vital_type_bp",
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (vitalType == VitalType.GLUCOSE) {
      Surface(
        shape = CardShape,
        color = c.mistSoft,
        border = BorderStroke(1.5.dp, c.clinicalBlueAccent),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
        ) {
          Text("Blood sugar", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
          Text(
            text = if (glucoseInput.isBlank()) "--- mg/dL" else "$glucoseInput mg/dL",
            style = MaterialTheme.typography.displayMedium.tabular(),
            color = c.deepOcean,
            modifier = Modifier.testTag("keypad_glucose_display"),
          )
        }
      }
    } else {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
          onClick = { onToggleBpField(true) },
          shape = CardShape,
          color = if (editingSystolic) c.mist else c.mistSoft,
          border = BorderStroke(if (editingSystolic) 2.dp else 1.dp, if (editingSystolic) c.clinicalBlueAccent else c.hairlineBorder),
          modifier = Modifier.weight(1f).testTag("bp_systolic_box"),
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(14.dp)) {
            Text("Upper (SYS)", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
            Text(
              text = systolicInput.ifBlank { "---" },
              style = MaterialTheme.typography.headlineLarge.tabular(),
              color = c.deepOcean,
            )
          }
        }
        Surface(
          onClick = { onToggleBpField(false) },
          shape = CardShape,
          color = if (!editingSystolic) c.mist else c.mistSoft,
          border = BorderStroke(if (!editingSystolic) 2.dp else 1.dp, if (!editingSystolic) c.clinicalBlueAccent else c.hairlineBorder),
          modifier = Modifier.weight(1f).testTag("bp_diastolic_box"),
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(14.dp)) {
            Text("Lower (DIA)", style = MaterialTheme.typography.labelMedium, color = c.inkSecondary)
            Text(
              text = diastolicInput.ifBlank { "---" },
              style = MaterialTheme.typography.headlineLarge.tabular(),
              color = c.deepOcean,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      rows.forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          row.forEach { digit ->
            KeypadDigitButton(label = digit, onClick = { onDigit(digit) }, modifier = Modifier.weight(1f))
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
          onClick = onClear,
          shape = ButtonShape,
          color = c.mistSoft,
          border = BorderStroke(1.dp, c.hairlineBorder),
          modifier = Modifier.weight(1f).defaultMinSize(minHeight = 56.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("Clear", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          }
        }
        KeypadDigitButton(label = "0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
        Surface(
          onClick = onBackspace,
          shape = ButtonShape,
          color = c.mistSoft,
          border = BorderStroke(1.dp, c.hairlineBorder),
          modifier = Modifier.weight(1f).defaultMinSize(minHeight = 56.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete digit", tint = c.deepOcean)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    SehatPrimaryButton(
      text = "Review & confirm",
      onClick = onReview,
      modifier = Modifier.fillMaxWidth(),
      testTag = "keypad_review_btn",
    )
  }
}

@Composable
private fun VitalTypeButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val c = SehatPalette.current
  Surface(
    onClick = onClick,
    shape = ButtonShape,
    color = if (selected) c.mist else c.surface,
    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.clinicalBlueAccent else c.hairlineBorder),
    modifier = modifier.defaultMinSize(minHeight = 48.dp).testTag(testTag),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = c.deepOcean,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun KeypadDigitButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Surface(
    onClick = onClick,
    shape = ButtonShape,
    color = c.surface,
    border = BorderStroke(1.dp, c.hairlineBorder),
    shadowElevation = 1.dp,
    modifier = modifier.defaultMinSize(minHeight = 56.dp).testTag("keypad_$label"),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        style = MaterialTheme.typography.headlineMedium.tabular(),
        color = c.deepOcean,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContextAndSymptomsCard(
  readingContext: ReadingContext,
  symptoms: SymptomFlags,
  isRecheck: Boolean,
  onContextChange: (ReadingContext) -> Unit,
  onSymptomsChange: (SymptomFlags) -> Unit,
  onRecheckChange: (Boolean) -> Unit,
) {
  val c = SehatPalette.current
  SehatCard(containerColor = c.surface) {
    Text("When was this taken?", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      ReadingContext.entries.forEach { ctx ->
        FilterChip(
          selected = readingContext == ctx,
          onClick = { onContextChange(ctx) },
          label = { Text(ctx.label) },
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = c.mist,
              selectedLabelColor = c.deepOcean,
            ),
          modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = c.hairlineBorder)
    Spacer(modifier = Modifier.height(10.dp))
    Text("Any symptoms right now?", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)

    SymptomCheckboxRow(
      checked = symptoms.dizziness,
      label = "Dizziness or light-headed",
      onCheckedChange = { onSymptomsChange(symptoms.copy(dizziness = it)) },
    )
    SymptomCheckboxRow(
      checked = symptoms.confusionOrFainting,
      label = "Confusion, fainting, or very sleepy",
      onCheckedChange = { onSymptomsChange(symptoms.copy(confusionOrFainting = it)) },
    )
    SymptomCheckboxRow(
      checked = symptoms.chestPainOrBreathlessness,
      label = "Chest pain or breathlessness",
      onCheckedChange = { onSymptomsChange(symptoms.copy(chestPainOrBreathlessness = it)) },
    )
    SymptomCheckboxRow(
      checked = symptoms.severeHeadacheOrWeakness,
      label = "Severe headache or sudden weakness",
      onCheckedChange = { onSymptomsChange(symptoms.copy(severeHeadacheOrWeakness = it)) },
    )
    SymptomCheckboxRow(
      checked = isRecheck,
      label = "This is a 5 or 15-minute recheck reading",
      onCheckedChange = onRecheckChange,
    )
  }
}

@Composable
private fun SymptomCheckboxRow(
  checked: Boolean,
  label: String,
  onCheckedChange: (Boolean) -> Unit,
) {
  val c = SehatPalette.current
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 48.dp)
        .clickable { onCheckedChange(!checked) }
        .padding(vertical = 2.dp),
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(checkedColor = c.clinicalBlue, checkmarkColor = PureWhite),
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(label, style = MaterialTheme.typography.bodyMedium, color = c.ink)
  }
}

@Composable
private fun ConfirmationBeforeSaveCard(
  pending: PendingReadingConfirmation,
  readingContext: ReadingContext,
  symptoms: SymptomFlags,
  isRecheck: Boolean,
  onContextChange: (ReadingContext) -> Unit,
  onSymptomsChange: (SymptomFlags) -> Unit,
  onRecheckChange: (Boolean) -> Unit,
  onConfirm: () -> Unit,
  onEdit: () -> Unit,
) {
  val c = SehatPalette.current
  val readingText =
    when (pending.vitalType) {
      VitalType.GLUCOSE -> "${pending.glucoseMgDl} mg/dL"
      VitalType.BLOOD_PRESSURE -> "${pending.systolic}/${pending.diastolic} mmHg"
    }

  SehatCard(
    containerColor = if (pending.needsVerification) c.statusCautionBg else c.mistSoft,
    borderColor = if (pending.needsVerification) c.statusCautionBorder else c.clinicalBlueAccent,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Please confirm before saving", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
      StatusChip(
        level = if (pending.needsVerification) HealthStatusLevel.CAUTION else HealthStatusLevel.INFO,
        label = if (pending.needsVerification) "Please verify" else pending.sourceLabel,
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = "Is this $readingText (${readingContext.label})?",
      style = MaterialTheme.typography.headlineMedium.tabular(),
      color = c.deepOcean,
    )
    if (pending.needsVerification) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Confidence was below 70%. Check the meter screen carefully before confirming.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.statusCautionFg,
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      SehatPrimaryButton(
        text = "Confirm & Check Safety",
        icon = Icons.Filled.CheckCircle,
        onClick = onConfirm,
        modifier = Modifier.weight(1f),
        testTag = "confirm_save_reading_btn",
      )
      SehatSecondaryButton(
        text = "Edit",
        icon = Icons.Filled.Edit,
        minHeight = 56.dp,
        onClick = onEdit,
        testTag = "edit_pending_reading_btn",
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingResultCard(
  assessment: RiskAssessment,
  caregivers: List<SeededCaregiver>,
  isOnline: Boolean = true,
  lastAlertSent: Boolean,
  cooldownSuppressedMsg: String?,
  escalationDemo: EscalationDemoState,
  isExplainingAi: Boolean,
  aiExplanation: String?,
  onExplainAi: () -> Unit,
  onAcknowledgeAlert: () -> Unit,
  onOpenEmergency: () -> Unit,
  onDismissResult: () -> Unit,
) {
  val c = SehatPalette.current
  val context = LocalContext.current
  val primaryCaregiver = caregivers.firstOrNull()
  val speechHelper = remember(context) { ScheduleSpeechHelper(context) }
  DisposableEffect(Unit) { onDispose { speechHelper.shutdown() } }

  val (bg, border) =
    when (assessment.riskLevel) {
      RiskLevel.NORMAL -> c.surface to c.hairlineBorder
      RiskLevel.CAUTION -> c.statusCautionBg to c.statusCautionBorder
      RiskLevel.URGENT, RiskLevel.EMERGENCY -> c.statusUrgentBg to c.statusUrgentBorder
    }

  SehatCard(containerColor = bg, borderColor = border) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      StatusChip(level = assessment.statusLevel, label = assessment.riskLevel.label)
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SehatGhostButton(
          text = "Listen",
          icon = Icons.AutoMirrored.Filled.VolumeUp,
          onClick = { speechHelper.speak("${assessment.headline}. ${assessment.fullGuidanceMessage}") },
          testTag = "result_listen_btn",
        )
        SehatGhostButton(text = "Done", onClick = onDismissResult)
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(assessment.headline, style = MaterialTheme.typography.headlineMedium, color = c.deepOcean)
    Spacer(modifier = Modifier.height(10.dp))

    assessment.guidanceSteps.forEach { step ->
      Text("• $step", style = MaterialTheme.typography.bodyLarge, color = c.ink, modifier = Modifier.padding(vertical = 2.dp))
    }

    if (assessment.show15MinRecheckReminder) {
      Spacer(modifier = Modifier.height(8.dp))
      StatusChip(level = HealthStatusLevel.CAUTION, label = "Recheck sugar in 15 minutes")
    }
    if (assessment.show5MinRecheckReminder) {
      Spacer(modifier = Modifier.height(8.dp))
      StatusChip(level = HealthStatusLevel.CAUTION, label = "Rest and recheck BP in 5 minutes")
    }

    Spacer(modifier = Modifier.height(12.dp))
    SehatSecondaryButton(
      text = if (isExplainingAi) "Loading simple explanation..." else "Explain simply with AI",
      icon = Icons.Filled.AutoAwesome,
      enabled = !isExplainingAi,
      isLoading = isExplainingAi,
      onClick = onExplainAi,
      modifier = Modifier.fillMaxWidth(),
      testTag = "explain_ai_btn",
    )

    if (isExplainingAi) {
      Spacer(modifier = Modifier.height(10.dp))
      SehatCardSkeleton(label = "Writing plain-language explanation...", lines = 3)
    }

    aiExplanation?.let { explanation ->
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        shape = CardShape,
        color = c.surface,
        border = BorderStroke(1.dp, c.hairlineBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Plain-language note (AI):", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          Text(explanation, style = MaterialTheme.typography.bodyMedium, color = c.ink)
        }
      }
    }

    if (assessment.shouldAlertFamily) {
      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = c.hairlineBorder)
      Spacer(modifier = Modifier.height(12.dp))

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = c.statusUrgentFg)
        Text("Family alert", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
        DemoPillBadge(text = "Demo")
      }
      Spacer(modifier = Modifier.height(6.dp))

      if (!isOnline) {
        Surface(
          shape = CardShape,
          color = c.surface,
          border = BorderStroke(1.dp, c.statusUrgentBorder),
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
          Text(
            text = "No internet right now: call your family directly using the Call button below.",
            style = MaterialTheme.typography.labelLarge,
            color = c.statusUrgentFg,
            modifier = Modifier.padding(10.dp),
          )
        }
      }

      if (cooldownSuppressedMsg != null) {
        Text(cooldownSuppressedMsg, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
      } else if (lastAlertSent && primaryCaregiver != null) {
        Text(
          "Ready to alert ${primaryCaregiver.name} (${primaryCaregiver.relationship}):",
          style = MaterialTheme.typography.bodyMedium,
          color = c.ink,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "\"${assessment.whatsappSummarySnippet}\"",
          style = MaterialTheme.typography.bodyMedium,
          color = c.deepOcean,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (primaryCaregiver != null) {
          SehatPrimaryButton(
            text = "WhatsApp ${primaryCaregiver.name} (Demo)",
            icon = Icons.AutoMirrored.Filled.Chat,
            onClick = { launchWhatsAppAlert(context, primaryCaregiver.phone, assessment.whatsappSummarySnippet) },
            testTag = "alert_whatsapp_btn",
          )
          SehatSecondaryButton(
            text = "Call ${primaryCaregiver.name}",
            icon = Icons.Filled.Call,
            minHeight = 56.dp,
            onClick = { launchDialer(context, primaryCaregiver.phone) },
            testTag = "alert_call_family_btn",
          )
        }
        SehatDestructiveButton(
          text = "Open Emergency Screen",
          onClick = onOpenEmergency,
          testTag = "open_emergency_btn",
        )
      }

      if (escalationDemo.active) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = CardShape,
          color = c.surface,
          border = BorderStroke(1.dp, c.statusUrgentBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Escalation timer (Demo)", style = MaterialTheme.typography.labelLarge, color = c.statusUrgentFg)
              DemoPillBadge(text = "Demo")
            }
            Text(
              text =
                if (escalationDemo.escalated) {
                  "Escalated to ${escalationDemo.secondaryCaregiverName} because ${escalationDemo.primaryCaregiverName} did not acknowledge."
                } else {
                  "Simulating 5-min escalation to ${escalationDemo.secondaryCaregiverName} in ${escalationDemo.secondsRemaining}s..."
                },
              style = MaterialTheme.typography.bodyMedium.tabular(),
              color = c.ink,
            )
            if (!escalationDemo.escalated) {
              SehatSecondaryButton(
                text = "Acknowledge alert (Demo)",
                onClick = onAcknowledgeAlert,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }
      }
    }
  }
}

fun launchWhatsAppAlert(context: Context, phone: String, message: String) {
  val digits = phone.filter { it.isDigit() }
  val encoded = URLEncoder.encode(message, "UTF-8")
  val url = "https://wa.me/$digits?text=$encoded"
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
  try {
    context.startActivity(intent)
  } catch (_: Throwable) {
    // Fallback to share sheet if WhatsApp/browser is not installed
    val share =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
      }
    context.startActivity(Intent.createChooser(share, "Send family alert"))
  }
}

fun launchDialer(context: Context, phone: String) {
  launchSafeDialer(context, phone)
}

private fun createTempPhotoUri(context: Context): Uri {
  val dir = File(context.cacheDir, "captures").apply { mkdirs() }
  val file = File(dir, "meter_${System.currentTimeMillis()}.jpg")
  return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

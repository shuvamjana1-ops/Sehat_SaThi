package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ButtonShape
import com.example.data.AuthSessionRepository
import com.example.data.HealthStatusLevel
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SafetyDisclaimerFooter
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatIconButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatProgressMeter
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.theme.CardShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency

@Composable
fun LogInScreen(
  onBack: () -> Unit,
  onLoginSubmit: (email: String, password: String) -> String?,
  onGoToSignUp: () -> Unit,
  onGoToForgotPassword: () -> Unit,
  onTryDemo: () -> Unit,
  onHomeClick: () -> Unit = onBack,
  onOpenInfo: (InfoSection) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  AuthScaffold(
    title = "Log in to SehatSaathi",
    subtitle = "Welcome back. Enter your email and password, or explore Demo mode right away.",
    onBack = onBack,
    onHomeClick = onHomeClick,
    onOpenInfo = onOpenInfo,
    modifier = modifier,
  ) {
    DemoAccountBanner()

    if (errorMsg != null) {
      AuthErrorBox(message = errorMsg!!)
    }

    SehatTextField(
      label = "Email address",
      value = email,
      onValueChange = {
        email = it
        errorMsg = null
      },
      placeholder = "e.g. sunita@mail.com",
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
      testTag = "login_email_input",
    )

    SehatTextField(
      label = "Password",
      value = password,
      onValueChange = {
        password = it
        errorMsg = null
      },
      placeholder = "At least 10 characters",
      isPassword = true,
      passwordVisible = passwordVisible,
      onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
      helperText = "Tip: check Caps Lock before submitting. Paste is allowed.",
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
      testTag = "login_password_input",
    )

    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
      SehatGhostButton(
        text = "Forgot password?",
        onClick = onGoToForgotPassword,
        testTag = "forgot_password_link",
      )
    }

    SehatPrimaryButton(
      text = "Log in",
      onClick = { errorMsg = onLoginSubmit(email, password) },
      modifier = Modifier.fillMaxWidth(),
      testTag = "login_submit_btn",
    )

    SehatSecondaryButton(
      text = "Try Demo mode (Sunita Devi)",
      onClick = onTryDemo,
      minHeight = 56.dp,
      modifier = Modifier.fillMaxWidth(),
      testTag = "login_demo_btn",
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("New to SehatSaathi?", style = MaterialTheme.typography.bodyMedium, color = SehatPalette.current.inkSecondary)
      SehatGhostButton(text = "Create account", onClick = onGoToSignUp, testTag = "go_to_signup_btn")
    }
  }
}

@Composable
fun SignUpScreen(
  onBack: () -> Unit,
  onSignUpSubmit: (name: String, email: String, password: String) -> String?,
  onGoToLogin: () -> Unit,
  onTryDemo: () -> Unit,
  onHomeClick: () -> Unit = onBack,
  onOpenInfo: (InfoSection) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var acceptedTerms by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  val strength = remember(password) { AuthSessionRepository.evaluatePassword(password) }
  val passwordsMatch = password.isNotEmpty() && password == confirmPassword

  AuthScaffold(
    title = "Create your account",
    subtitle = "Set up a simple profile for yourself or a family member.",
    onBack = onBack,
    onHomeClick = onHomeClick,
    onOpenInfo = onOpenInfo,
    modifier = modifier,
  ) {
    DemoAccountBanner()

    if (errorMsg != null) {
      AuthErrorBox(message = errorMsg!!)
    }

    SehatTextField(
      label = "Full name",
      value = name,
      onValueChange = {
        name = it
        errorMsg = null
      },
      placeholder = "e.g. Sunita Devi",
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      testTag = "signup_name_input",
    )

    SehatTextField(
      label = "Email address",
      value = email,
      onValueChange = {
        email = it
        errorMsg = null
      },
      placeholder = "e.g. sunita@mail.com",
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
      testTag = "signup_email_input",
    )

    SehatTextField(
      label = "Create password",
      value = password,
      onValueChange = {
        password = it
        errorMsg = null
      },
      placeholder = "Minimum 10 characters",
      isPassword = true,
      passwordVisible = passwordVisible,
      onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
      helperText = "Use letters and numbers. You can paste a password manager phrase.",
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
      testTag = "signup_password_input",
    )

    val c = SehatPalette.current
    // Live password strength meter + checklist
    Surface(
      shape = CardShape,
      color = c.mistSoft,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Password strength", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          Text(
            text = if (password.isEmpty()) "Enter password" else strength.label,
            style = MaterialTheme.typography.labelMedium,
            color =
              when {
                password.isEmpty() -> c.inkSecondary
                strength.isValid -> c.statusNormalFg
                !strength.notCommonPassword -> c.statusUrgentFg
                else -> c.statusCautionFg
              },
          )
        }
        SehatProgressMeter(
          progress = if (password.isEmpty()) 0f else strength.scoreFraction,
          color =
            when {
              strength.isValid -> c.statusNormalFg
              !strength.notCommonPassword -> c.statusUrgentFg
              else -> c.statusCautionFg
            },
        )
        ChecklistRow(met = strength.hasMinLength, text = "At least 10 characters")
        ChecklistRow(met = strength.hasLetterAndNumber, text = "Includes both letters and numbers")
        ChecklistRow(met = strength.notCommonPassword, text = "Avoids common passwords")
      }
    }

    SehatTextField(
      label = "Confirm password",
      value = confirmPassword,
      onValueChange = {
        confirmPassword = it
        errorMsg = null
      },
      placeholder = "Type password again",
      isPassword = true,
      passwordVisible = passwordVisible,
      onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
      errorText =
        if (confirmPassword.isNotEmpty() && !passwordsMatch) "Passwords do not match yet." else null,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
      testTag = "signup_confirm_input",
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .clickable { acceptedTerms = !acceptedTerms }
          .padding(vertical = 4.dp),
    ) {
      Checkbox(
        checked = acceptedTerms,
        onCheckedChange = { acceptedTerms = it },
        colors =
          CheckboxDefaults.colors(
            checkedColor = c.clinicalBlue,
            uncheckedColor = c.inkSecondary,
            checkmarkColor = PureWhite,
          ),
        modifier = Modifier.testTag("signup_terms_checkbox"),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "I understand SehatSaathi is a support tool, not a doctor.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.ink,
      )
    }

    SehatPrimaryButton(
      text = "Create account",
      onClick = {
        errorMsg =
          when {
            !acceptedTerms -> "Please accept the support tool notice before continuing."
            !passwordsMatch -> "Passwords must match."
            else -> onSignUpSubmit(name, email, password)
          }
      },
      modifier = Modifier.fillMaxWidth(),
      testTag = "signup_submit_btn",
    )

    SehatSecondaryButton(
      text = "Try Demo mode (Sunita Devi)",
      onClick = onTryDemo,
      minHeight = 56.dp,
      modifier = Modifier.fillMaxWidth(),
      testTag = "signup_demo_btn",
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Already have an account?", style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
      SehatGhostButton(text = "Log in", onClick = onGoToLogin, testTag = "go_to_login_btn")
    }
  }
}

@Composable
fun ForgotPasswordScreen(
  onBack: () -> Unit,
  onBackToLogin: () -> Unit,
  onHomeClick: () -> Unit = onBack,
  onOpenInfo: (InfoSection) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var email by remember { mutableStateOf("") }
  var submitted by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  AuthScaffold(
    title = "Reset password",
    subtitle = "Enter your email address and we will confirm reset steps.",
    onBack = onBack,
    onHomeClick = onHomeClick,
    onOpenInfo = onOpenInfo,
    modifier = modifier,
  ) {
    DemoAccountBanner()

    SehatTextField(
      label = "Email address",
      value = email,
      onValueChange = {
        email = it
        errorMsg = null
      },
      placeholder = "e.g. sunita@mail.com",
      errorText = errorMsg,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
      testTag = "forgot_email_input",
    )

    if (submitted) {
      StatusChip(
        level = HealthStatusLevel.INFO,
        label = "Demo: if this account exists, a reset link would be sent.",
      )
    }

    SehatPrimaryButton(
      text = "Send reset link (Demo)",
      onClick = {
        if (AuthSessionRepository.isValidEmail(email)) {
          errorMsg = null
          submitted = true
        } else {
          submitted = false
          errorMsg = "Please enter a valid email address."
        }
      },
      modifier = Modifier.fillMaxWidth(),
      testTag = "forgot_submit_btn",
    )

    SehatGhostButton(
      text = "Back to Log in",
      onClick = onBackToLogin,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
  }
}

@Composable
private fun AuthScaffold(
  title: String,
  subtitle: String,
  onBack: () -> Unit,
  onHomeClick: () -> Unit,
  onOpenInfo: (InfoSection) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
  val c = SehatPalette.current
  val reducedTransparency = rememberPrefersReducedTransparency()
  Column(modifier = modifier.fillMaxSize().background(c.background).imePadding()) {
    Surface(
      color = GlassTokens.chromeFill(scrolled = false, reducedTransparency = reducedTransparency, isDark = c.isDark),
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth().statusBarsPadding(),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
      ) {
        SehatIconButton(
          icon = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Go back",
          onClick = onBack,
          testTag = "auth_back_btn",
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier
              .clip(ButtonShape)
              .clickable(role = Role.Button, onClick = onHomeClick)
              .semantics { contentDescription = "SehatSaathi home" }
              .defaultMinSize(minHeight = 48.dp)
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("auth_logo_home"),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(30.dp).clip(ButtonShape).background(DeepOcean),
          ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text("SehatSaathi", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        }
      }
    }

    Box(
      contentAlignment = Alignment.TopCenter,
      modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
      SehatCard(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        Spacer(modifier = Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
      }
    }

    SafetyDisclaimerFooter(
      isDemoMode = false,
      onLogoClick = onHomeClick,
      onOpenInfo = onOpenInfo,
      showExtendedLinks = true,
      modifier = Modifier.navigationBarsPadding(),
    )
  }
}

@Composable
private fun DemoAccountBanner() {
  val c = SehatPalette.current
  Surface(
    shape = CardShape,
    color = c.mist,
    border = BorderStroke(1.dp, c.hairlineBorder),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.padding(12.dp),
    ) {
      Icon(Icons.Filled.Info, contentDescription = null, tint = c.deepOcean, modifier = Modifier.size(20.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Local Demo Account Layer", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          DemoPillBadge(text = "Demo")
        }
        Text(
          text = "No real medical data is sent to a server in Phase 1.",
          style = MaterialTheme.typography.labelMedium,
          color = c.inkSecondary,
        )
      }
    }
  }
}

@Composable
private fun AuthErrorBox(message: String) {
  val c = SehatPalette.current
  Surface(
    shape = CardShape,
    color = c.statusUrgentBg,
    border = BorderStroke(1.dp, c.statusUrgentBorder),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = c.statusUrgentFg,
      modifier = Modifier.padding(12.dp),
    )
  }
}

@Composable
private fun ChecklistRow(met: Boolean, text: String) {
  val c = SehatPalette.current
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Icon(
      imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
      contentDescription = null,
      tint = if (met) c.statusNormalFg else c.inkSecondary,
      modifier = Modifier.size(18.dp),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = if (met) c.statusNormalFg else c.inkSecondary,
    )
  }
}

package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.SehatPalette

@Composable
fun SehatTextField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  helperText: String? = null,
  errorText: String? = null,
  isPassword: Boolean = false,
  passwordVisible: Boolean = false,
  onTogglePasswordVisibility: (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  enabled: Boolean = true,
  reserveSupportingSpace: Boolean = false,
  testTag: String = "text_field",
) {
  val c = SehatPalette.current
  Column(
    horizontalAlignment = Alignment.Start,
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = c.deepOcean,
      textAlign = TextAlign.Start,
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      enabled = enabled,
      placeholder =
        if (placeholder.isNotEmpty()) {
          {
            Text(
              text = placeholder,
              style = MaterialTheme.typography.bodyLarge,
              color = c.inkSecondary,
              textAlign = TextAlign.Start,
            )
          }
        } else null,
      leadingIcon = leadingIcon,
      trailingIcon =
        if (isPassword && onTogglePasswordVisibility != null) {
          {
            IconButton(
              onClick = onTogglePasswordVisibility,
              modifier = Modifier.minimumInteractiveComponentSize().defaultMinSize(48.dp, 48.dp).testTag("${testTag}_toggle"),
            ) {
              Icon(
                imageVector =
                  if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = c.deepOcean,
              )
            }
          }
        } else null,
      visualTransformation =
        if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None,
      isError = errorText != null,
      singleLine = singleLine,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      shape = ButtonShape,
      textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink, textAlign = TextAlign.Start),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = c.surface,
          unfocusedContainerColor = c.surface,
          focusedBorderColor = c.clinicalBlueAccent,
          unfocusedBorderColor = c.hairlineBorder,
          errorBorderColor = c.statusUrgentFg,
          cursorColor = c.clinicalBlueAccent,
          focusedTextColor = c.ink,
          unfocusedTextColor = c.ink,
        ),
      modifier =
        Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 56.dp)
          .semantics {
            if (errorText != null) {
              error(errorText)
            }
          }
          .testTag(testTag),
    )
    if (errorText != null || helperText != null || reserveSupportingSpace) {
      Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 24.dp),
      ) {
        if (errorText != null) {
          Text(
            text = errorText,
            style = MaterialTheme.typography.labelMedium,
            color = c.statusUrgentFg,
            textAlign = TextAlign.Start,
          )
        } else if (helperText != null) {
          Text(
            text = helperText,
            style = MaterialTheme.typography.labelMedium,
            color = c.inkSecondary,
            textAlign = TextAlign.Start,
          )
        }
      }
    }
  }
}

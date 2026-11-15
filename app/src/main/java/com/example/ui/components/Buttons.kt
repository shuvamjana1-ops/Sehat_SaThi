package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.StatusUrgentFg

@Composable
fun SehatPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  minHeight: Dp = 56.dp,
  testTag: String = "primary_button",
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (pressed && !reducedMotion) 0.97f else 1f,
      animationSpec = tween(durationMillis = 150),
      label = "primary_btn_scale",
    )

  Button(
    onClick = { if (!isLoading) onClick() },
    enabled = enabled,
    interactionSource = interactionSource,
    shape = ButtonShape,
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = c.clinicalBlue,
        contentColor = PureWhite,
        disabledContainerColor = c.clinicalBlue.copy(alpha = 0.4f),
        disabledContentColor = PureWhite.copy(alpha = 0.9f),
      ),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    modifier =
      modifier
        .defaultMinSize(minHeight = minHeight)
        .scale(scale)
        .then(
          if (enabled) {
            Modifier.shadow(
              elevation = if (pressed) 2.dp else 8.dp,
              shape = ButtonShape,
              ambientColor = DeepOcean.copy(alpha = 0.3f),
              spotColor = c.clinicalBlue.copy(alpha = 0.45f),
            )
          } else {
            Modifier
          },
        )
        .testTag(testTag),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = PureWhite,
          strokeWidth = 2.5.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
      } else if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = PureWhite,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun SehatSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  minHeight: Dp = 48.dp,
  testTag: String = "secondary_button",
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (pressed && !reducedMotion) 0.97f else 1f,
      animationSpec = tween(durationMillis = 150),
      label = "secondary_btn_scale",
    )

  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    shape = ButtonShape,
    border = BorderStroke(1.5.dp, if (enabled) c.clinicalBlueAccent else c.hairlineBorder),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor = if (pressed) c.mistSoft else c.surface,
        contentColor = c.deepOcean,
      ),
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    modifier =
      modifier
        .defaultMinSize(minHeight = minHeight)
        .scale(scale)
        .then(
          if (enabled) {
            Modifier.shadow(
              elevation = if (pressed) 1.dp else 3.dp,
              shape = ButtonShape,
              ambientColor = DeepOcean.copy(alpha = 0.14f),
              spotColor = DeepOcean.copy(alpha = 0.2f),
            )
          } else {
            Modifier
          },
        )
        .testTag(testTag),
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = if (enabled) c.deepOcean else c.deepOcean.copy(alpha = 0.45f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun SehatGhostButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  testTag: String = "ghost_button",
) {
  val c = SehatPalette.current
  TextButton(
    onClick = onClick,
    enabled = enabled,
    shape = ButtonShape,
    colors = ButtonDefaults.textButtonColors(contentColor = c.clinicalBlueAccent),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    modifier = modifier.defaultMinSize(minHeight = 48.dp).testTag(testTag),
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = c.clinicalBlueAccent,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun SehatDestructiveButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  testTag: String = "destructive_button",
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    shape = ButtonShape,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = StatusUrgentFg,
        contentColor = PureWhite,
      ),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    modifier = modifier.defaultMinSize(minHeight = 48.dp).testTag(testTag),
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = PureWhite,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun SehatIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = Color.Unspecified,
  testTag: String = "icon_button",
) {
  val resolvedTint = if (tint == Color.Unspecified) SehatPalette.current.deepOcean else tint
  IconButton(
    onClick = onClick,
    modifier = modifier.minimumInteractiveComponentSize().testTag(testTag),
  ) {
    Icon(imageVector = icon, contentDescription = contentDescription, tint = resolvedTint)
  }
}

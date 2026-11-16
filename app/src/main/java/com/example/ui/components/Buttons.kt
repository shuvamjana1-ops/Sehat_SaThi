package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.StatusUrgentFg
import com.example.ui.theme.withNonBreakingUnits
import kotlinx.coroutines.delay

@Composable
private fun rememberButtonMorphState(
  interactionSource: MutableInteractionSource,
  reducedMotion: Boolean,
): ButtonMorphMetrics {
  val pressed by interactionSource.collectIsPressedAsState()
  var clickTrigger by remember { mutableIntStateOf(0) }
  var morphPulse by remember { mutableStateOf(false) }

  LaunchedEffect(clickTrigger) {
    if (clickTrigger > 0 && !reducedMotion) {
      morphPulse = true
      delay(210)
      morphPulse = false
    }
  }

  val active = (pressed || morphPulse) && !reducedMotion

  val cornerRadius by
    animateDpAsState(
      targetValue = if (active) 28.dp else 16.dp,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      label = "btn_morph_corner",
    )

  val scaleX by
    animateFloatAsState(
      targetValue = if (active) 1.035f else 1f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      label = "btn_morph_scale_x",
    )

  val scaleY by
    animateFloatAsState(
      targetValue = if (active) 0.93f else 1f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      label = "btn_morph_scale_y",
    )

  val rotationX by
    animateFloatAsState(
      targetValue = if (active) -7f else 0f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      label = "btn_morph_rot_x",
    )

  val iconRotation by
    animateFloatAsState(
      targetValue = if (active) -12f else 0f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      label = "btn_morph_icon_rot",
    )

  return ButtonMorphMetrics(
    active = active,
    cornerRadius = cornerRadius,
    scaleX = scaleX,
    scaleY = scaleY,
    rotationX = rotationX,
    iconRotation = iconRotation,
    triggerPulse = { clickTrigger++ },
  )
}

private data class ButtonMorphMetrics(
  val active: Boolean,
  val cornerRadius: Dp,
  val scaleX: Float,
  val scaleY: Float,
  val rotationX: Float,
  val iconRotation: Float,
  val triggerPulse: () -> Unit,
)

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
  val haptics = LocalHapticFeedback.current
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val morph = rememberButtonMorphState(interactionSource, reducedMotion)
  val morphedShape = RoundedCornerShape(morph.cornerRadius)

  Button(
    onClick = {
      if (!isLoading) {
        morph.triggerPulse()
        runCatching { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
        onClick()
      }
    },
    enabled = enabled,
    interactionSource = interactionSource,
    shape = morphedShape,
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = if (morph.active) c.deepOcean else c.clinicalBlue,
        contentColor = PureWhite,
        disabledContainerColor = c.clinicalBlue.copy(alpha = 0.4f),
        disabledContentColor = PureWhite.copy(alpha = 0.9f),
      ),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    modifier =
      modifier
        .defaultMinSize(minWidth = 48.dp, minHeight = minHeight)
        .graphicsLayer {
          scaleX = morph.scaleX
          scaleY = morph.scaleY
          rotationX = morph.rotationX
          cameraDistance = 12f * density
        }
        .then(
          if (enabled) {
            Modifier.shadow(
              elevation = if (morph.active) 2.dp else 8.dp,
              shape = morphedShape,
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
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = PureWhite,
          strokeWidth = 2.5.dp,
        )
      } else if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier =
            Modifier
              .size(20.dp)
              .graphicsLayer { rotationZ = morph.iconRotation },
        )
      }
      Text(
        text = text.withNonBreakingUnits(),
        style = MaterialTheme.typography.labelLarge.copy(lineBreak = LineBreak.Heading),
        color = PureWhite,
        textAlign = TextAlign.Center,
        maxLines = 2,
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
  val haptics = LocalHapticFeedback.current
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val morph = rememberButtonMorphState(interactionSource, reducedMotion)
  val morphedShape = RoundedCornerShape(morph.cornerRadius)

  OutlinedButton(
    onClick = {
      morph.triggerPulse()
      runCatching { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
      onClick()
    },
    enabled = enabled,
    interactionSource = interactionSource,
    shape = morphedShape,
    border = BorderStroke(if (morph.active) 2.dp else 1.5.dp, if (enabled) c.clinicalBlueAccent else c.hairlineBorder),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor = if (morph.active) c.mist else c.surface,
        contentColor = c.deepOcean,
      ),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    modifier =
      modifier
        .defaultMinSize(minWidth = 48.dp, minHeight = minHeight)
        .graphicsLayer {
          scaleX = morph.scaleX
          scaleY = morph.scaleY
          rotationX = morph.rotationX
          cameraDistance = 12f * density
        }
        .then(
          if (enabled) {
            Modifier.shadow(
              elevation = if (morph.active) 1.dp else 3.dp,
              shape = morphedShape,
              ambientColor = DeepOcean.copy(alpha = 0.14f),
              spotColor = DeepOcean.copy(alpha = 0.2f),
            )
          } else {
            Modifier
          },
        )
        .testTag(testTag),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier =
            Modifier
              .size(20.dp)
              .graphicsLayer { rotationZ = morph.iconRotation },
        )
      }
      Text(
        text = text.withNonBreakingUnits(),
        style = MaterialTheme.typography.labelLarge.copy(lineBreak = LineBreak.Heading),
        color = if (enabled) c.deepOcean else c.deepOcean.copy(alpha = 0.45f),
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
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
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val morph = rememberButtonMorphState(interactionSource, reducedMotion)
  val morphedShape = RoundedCornerShape(morph.cornerRadius)

  TextButton(
    onClick = {
      morph.triggerPulse()
      onClick()
    },
    enabled = enabled,
    interactionSource = interactionSource,
    shape = morphedShape,
    colors = ButtonDefaults.textButtonColors(contentColor = c.clinicalBlueAccent),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    modifier =
      modifier
        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
        .graphicsLayer {
          scaleX = morph.scaleX
          scaleY = morph.scaleY
          rotationX = morph.rotationX
          cameraDistance = 12f * density
        }
        .testTag(testTag),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier =
            Modifier
              .size(20.dp)
              .graphicsLayer { rotationZ = morph.iconRotation },
        )
      }
      Text(
        text = text.withNonBreakingUnits(),
        style = MaterialTheme.typography.labelLarge.copy(lineBreak = LineBreak.Heading),
        color = c.clinicalBlueAccent,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
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
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val morph = rememberButtonMorphState(interactionSource, reducedMotion)
  val morphedShape = RoundedCornerShape(morph.cornerRadius)

  Button(
    onClick = {
      morph.triggerPulse()
      onClick()
    },
    enabled = enabled,
    interactionSource = interactionSource,
    shape = morphedShape,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = StatusUrgentFg,
        contentColor = PureWhite,
      ),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    modifier =
      modifier
        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
        .graphicsLayer {
          scaleX = morph.scaleX
          scaleY = morph.scaleY
          rotationX = morph.rotationX
          cameraDistance = 12f * density
        }
        .testTag(testTag),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier =
            Modifier
              .size(20.dp)
              .graphicsLayer { rotationZ = morph.iconRotation },
        )
      }
      Text(
        text = text.withNonBreakingUnits(),
        style = MaterialTheme.typography.labelLarge.copy(lineBreak = LineBreak.Heading),
        color = PureWhite,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
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
  val reducedMotion = rememberPrefersReducedMotion()
  val interactionSource = remember { MutableInteractionSource() }
  val morph = rememberButtonMorphState(interactionSource, reducedMotion)

  IconButton(
    onClick = {
      morph.triggerPulse()
      onClick()
    },
    interactionSource = interactionSource,
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .defaultMinSize(48.dp, 48.dp)
        .graphicsLayer {
          scaleX = morph.scaleX
          scaleY = morph.scaleY
          rotationZ = morph.iconRotation
        }
        .testTag(testTag),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = resolvedTint,
      modifier = Modifier.size(24.dp),
    )
  }
}

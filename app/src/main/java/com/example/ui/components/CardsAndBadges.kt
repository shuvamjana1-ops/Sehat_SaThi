package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.HealthStatusLevel
import com.example.ui.theme.CardShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.Mist
import com.example.ui.theme.MistSoft
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency

@Composable
fun SehatCard(
  modifier: Modifier = Modifier,
  containerColor: Color = PureWhite,
  borderColor: Color = HairlineBorder,
  contentPadding: PaddingValues = PaddingValues(20.dp),
  useGlass: Boolean = false,
  highReadabilityGlass: Boolean = false,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val c = SehatPalette.current
  val isDark = c.isDark
  val reducedMotion = rememberPrefersReducedMotion()
  val reducedTransparency = rememberPrefersReducedTransparency()
  var pointerOffset by remember { mutableStateOf<Offset?>(null) }

  val resolvedBaseContainer =
    when {
      !isDark -> containerColor
      containerColor == PureWhite -> c.surface
      containerColor == Mist -> c.mist
      containerColor == MistSoft -> c.mistSoft
      else -> containerColor
    }

  val resolvedBorderColor =
    if (isDark && borderColor == HairlineBorder) c.hairlineBorder else borderColor

  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val pressScale by
    animateFloatAsState(
      targetValue = if (onClick != null && pressed && !reducedMotion) 0.97f else 1f,
      animationSpec = tween(durationMillis = 150),
      label = "card_press_scale",
    )

  val shape = if (useGlass) GlassTokens.GlassCornerShape else CardShape
  val effectiveContainer =
    if (useGlass) {
      GlassTokens.cardFill(
        baseColor = containerColor,
        highReadability = highReadabilityGlass,
        reducedTransparency = reducedTransparency,
        isDark = isDark,
      )
    } else {
      resolvedBaseContainer
    }

  val ambientShadow = if (isDark) Color.Black.copy(alpha = 0.36f) else DeepOcean.copy(alpha = if (useGlass) 0.14f else 0.16f)
  val spotShadow = if (isDark) Color.Black.copy(alpha = 0.44f) else DeepOcean.copy(alpha = if (useGlass) 0.20f else 0.22f)
  val sculptedShadow =
    Modifier.shadow(
      elevation = if (onClick != null) 8.dp else 5.dp,
      shape = shape,
      clip = false,
      ambientColor = ambientShadow,
      spotColor = spotShadow,
    )

  val pointerModifier =
    if (useGlass && !reducedMotion) {
      Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull()
            if (change != null && (change.type == PointerType.Mouse || change.type == PointerType.Stylus)) {
              pointerOffset =
                when (event.type) {
                  PointerEventType.Move, PointerEventType.Enter -> change.position
                  PointerEventType.Exit -> null
                  else -> pointerOffset
                }
            }
          }
        }
      }
    } else {
      Modifier
    }

  val sheenOverlay =
    Modifier.drawWithContent {
      if (useGlass) {
        drawRoundRect(
          brush = GlassTokens.sheenBrush(if (highReadabilityGlass) 0.42f else 0.60f, isDark = isDark),
          cornerRadius = CornerRadius(GlassTokens.GlassRadius.toPx(), GlassTokens.GlassRadius.toPx()),
        )
        val ptr = pointerOffset
        if (ptr != null) {
          drawRoundRect(
            brush = GlassTokens.pointerSpecularBrush(ptr, size.maxDimension * 0.45f, isDark = isDark),
            cornerRadius = CornerRadius(GlassTokens.GlassRadius.toPx(), GlassTokens.GlassRadius.toPx()),
          )
        }
      }
      drawContent()
      val cornerPx = (if (useGlass) GlassTokens.GlassRadius else 20.dp).toPx()
      val topHighlightColor =
        if (isDark) c.clinicalBlueAccent.copy(alpha = if (useGlass) 0.16f else 0.10f)
        else PureWhite.copy(alpha = if (useGlass) 0.68f else 0.55f)
      drawRoundRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(topHighlightColor, Color.Transparent),
            startY = 0f,
            endY = 26.dp.toPx(),
          ),
        topLeft = Offset.Zero,
        size = Size(size.width, 26.dp.toPx().coerceAtMost(size.height)),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
      )
    }

  val sculptedBorder =
    if (useGlass) {
      GlassTokens.borderStroke(resolvedBorderColor, isDark = isDark)
    } else {
      BorderStroke(
        width = 1.dp,
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                if (isDark) c.clinicalBlueAccent.copy(alpha = 0.25f) else PureWhite.copy(alpha = 0.9f),
                resolvedBorderColor,
              ),
          ),
      )
    }

  if (onClick != null) {
    Card(
      onClick = onClick,
      interactionSource = interactionSource,
      shape = shape,
      colors = CardDefaults.cardColors(containerColor = effectiveContainer, contentColor = c.ink),
      border = sculptedBorder,
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
      modifier = modifier.scale(pressScale).then(sculptedShadow).then(pointerModifier),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().then(sheenOverlay).padding(contentPadding),
        content = content,
      )
    }
  } else {
    Card(
      shape = shape,
      colors = CardDefaults.cardColors(containerColor = effectiveContainer, contentColor = c.ink),
      border = sculptedBorder,
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = modifier.then(sculptedShadow).then(pointerModifier),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().then(sheenOverlay).padding(contentPadding),
        content = content,
      )
    }
  }
}

@Composable
fun StatusChip(
  level: HealthStatusLevel,
  label: String = level.label,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val (fg, bg, border, icon) =
    when (level) {
      HealthStatusLevel.NORMAL ->
        Quad(c.statusNormalFg, c.statusNormalBg, c.statusNormalBorder, Icons.Filled.CheckCircle)
      HealthStatusLevel.CAUTION ->
        Quad(c.statusCautionFg, c.statusCautionBg, c.statusCautionBorder, Icons.Filled.Warning)
      HealthStatusLevel.URGENT ->
        Quad(c.statusUrgentFg, c.statusUrgentBg, c.statusUrgentBorder, Icons.Filled.Error)
      HealthStatusLevel.INFO ->
        Quad(c.statusInfoFg, c.statusInfoBg, c.statusInfoBorder, Icons.Filled.Info)
    }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier =
      modifier
        .background(color = bg, shape = PillShape)
        .border(width = 1.dp, color = border, shape = PillShape)
        .padding(horizontal = 12.dp, vertical = 6.dp),
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = fg,
    )
  }
}

@Composable
fun DemoPillBadge(
  text: String = "Demo",
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Surface(
    color = c.mist,
    shape = PillShape,
    border =
      BorderStroke(
        width = 1.dp,
        brush = Brush.horizontalGradient(listOf(c.clinicalBlueAccent.copy(alpha = 0.35f), c.hairlineBorder)),
      ),
    shadowElevation = 1.dp,
    modifier = modifier,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = c.statusInfoFg,
      modifier =
        Modifier
          .background(Brush.horizontalGradient(listOf(c.mist, c.mistSoft)))
          .padding(horizontal = 11.dp, vertical = 4.dp),
    )
  }
}

private data class Quad(
  val fg: Color,
  val bg: Color,
  val border: Color,
  val icon: ImageVector,
)

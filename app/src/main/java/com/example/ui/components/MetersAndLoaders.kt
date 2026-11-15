package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardShape
import com.example.ui.theme.DarkMist
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.tabular

@Composable
fun SehatProgressMeter(
  progress: Float,
  modifier: Modifier = Modifier,
  color: Color = Color.Unspecified,
  trackColor: Color = Color.Unspecified,
  height: Dp = 10.dp,
) {
  val c = SehatPalette.current
  val resolvedColor = if (color == Color.Unspecified) c.clinicalBlueAccent else color
  val resolvedTrack = if (trackColor == Color.Unspecified) c.mist else trackColor
  val reducedMotion = rememberPrefersReducedMotion()
  val clamped = progress.coerceIn(0f, 1f)
  val animatedProgress by
    animateFloatAsState(
      targetValue = clamped,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 650, easing = FastOutSlowInEasing),
      label = "progress_meter",
    )

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(height)
        .clip(PillShape)
        .background(resolvedTrack),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth(animatedProgress)
          .height(height)
          .clip(PillShape)
          .background(resolvedColor),
    )
  }
}

@Composable
fun SehatRingProgress(
  completed: Int,
  total: Int,
  modifier: Modifier = Modifier,
  size: Dp = 108.dp,
  strokeWidth: Dp = 12.dp,
) {
  val c = SehatPalette.current
  val trackTint = c.mist
  val arcTint = c.clinicalBlueAccent
  val reducedMotion = rememberPrefersReducedMotion()
  val targetFraction = if (total <= 0) 0f else (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
  var trigger by remember { mutableStateOf(false) }
  LaunchedEffect(targetFraction) { trigger = true }

  val animatedFraction by
    animateFloatAsState(
      targetValue = if (trigger) targetFraction else 0f,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 700, easing = FastOutSlowInEasing),
      label = "ring_progress",
    )

  Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
    Canvas(modifier = Modifier.size(size)) {
      val strokePx = strokeWidth.toPx()
      val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
      val topLeft = Offset(strokePx / 2f, strokePx / 2f)

      drawArc(
        color = trackTint,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round),
      )

      drawArc(
        color = arcTint,
        startAngle = -90f,
        sweepAngle = 360f * animatedFraction,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round),
      )
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "$completed/$total",
        style = MaterialTheme.typography.headlineMedium.tabular(),
        color = c.deepOcean,
      )
      Text(
        text = "taken",
        style = MaterialTheme.typography.labelSmall,
        color = c.deepOcean,
      )
    }
  }
}

@Composable
fun SehatSparkline(
  values: List<Float>,
  modifier: Modifier = Modifier,
  lineColor: Color = Color.Unspecified,
  lowThreshold: Float = 70f,
  highThreshold: Float = 180f,
) {
  val c = SehatPalette.current
  val resolvedLineColor = if (lineColor == Color.Unspecified) c.clinicalBlueAccent else lineColor
  val bandColor = c.mist.copy(alpha = 0.7f)
  val dotOuter = c.surface
  val dotInner = c.deepOcean
  val reducedMotion = rememberPrefersReducedMotion()
  var started by remember { mutableStateOf(false) }
  LaunchedEffect(values) { started = true }

  val drawFraction by
    animateFloatAsState(
      targetValue = if (started) 1f else 0f,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 700, easing = FastOutSlowInEasing),
      label = "sparkline_draw",
    )

  Canvas(modifier = modifier.fillMaxWidth().height(84.dp)) {
    if (values.size < 2) return@Canvas
    val minVal = (values.minOrNull() ?: 50f).coerceAtMost(lowThreshold - 10f)
    val maxVal = (values.maxOrNull() ?: 250f).coerceAtLeast(highThreshold + 20f)
    val range = (maxVal - minVal).coerceAtLeast(1f)

    val padV = 10.dp.toPx()
    val usableHeight = size.height - padV * 2f

    fun yFor(v: Float): Float {
      val norm = ((v - minVal) / range).coerceIn(0f, 1f)
      return size.height - padV - (norm * usableHeight)
    }

    val yHigh = yFor(highThreshold)
    val yLow = yFor(lowThreshold)
    drawRect(
      color = bandColor,
      topLeft = Offset(0f, yHigh),
      size = Size(size.width, (yLow - yHigh).coerceAtLeast(2f)),
    )

    val stepX = size.width / (values.size - 1).toFloat()
    val visibleCount = ((values.size - 1) * drawFraction).toInt().coerceAtLeast(1)

    val path = Path()
    values.forEachIndexed { idx, value ->
      if (idx <= visibleCount) {
        val x = idx * stepX
        val y = yFor(value)
        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
    }

    drawPath(
      path = path,
      color = resolvedLineColor,
      style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    if (drawFraction > 0.9f) {
      val lastX = (values.size - 1) * stepX
      val lastY = yFor(values.last())
      drawCircle(color = dotOuter, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
      drawCircle(color = dotInner, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
    }
  }
}

@Composable
fun SehatSkeletonBox(
  modifier: Modifier = Modifier,
  height: Dp = 20.dp,
) {
  val c = SehatPalette.current
  val reducedMotion = rememberPrefersReducedMotion()
  val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
  val alpha by
    transition.animateFloat(
      initialValue = 0.45f,
      targetValue = if (reducedMotion) 0.45f else 0.9f,
      animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
      label = "skeleton_alpha",
    )

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(height)
        .clip(CardShape)
        .background(
          Brush.horizontalGradient(
            listOf(c.mist.copy(alpha = alpha), c.mist.copy(alpha = 0.28f), c.mist.copy(alpha = alpha)),
          ),
        ),
  )
}

@Composable
fun SehatCardSkeleton(
  label: String = "Loading...",
  lines: Int = 3,
  modifier: Modifier = Modifier,
) {
  SehatCard(
    useGlass = true,
    highReadabilityGlass = true,
    modifier =
      modifier
        .fillMaxWidth()
        .semantics {
          liveRegion = LiveRegionMode.Polite
          contentDescription = label
        }
        .testTag("sehat_card_skeleton"),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      SehatSkeletonBox(modifier = Modifier.fillMaxWidth(0.55f), height = 22.dp)
      repeat(lines.coerceIn(1, 5)) { index ->
        val fraction = if (index == lines - 1) 0.72f else 1f
        SehatSkeletonBox(modifier = Modifier.fillMaxWidth(fraction), height = 16.dp)
      }
    }
  }
}

@Composable
fun SehatInlineLoadingBanner(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  SehatCard(
    containerColor = c.mist,
    useGlass = true,
    highReadabilityGlass = true,
    modifier =
      modifier
        .fillMaxWidth()
        .semantics {
          liveRegion = LiveRegionMode.Polite
          contentDescription = "$title. $subtitle"
        }
        .testTag("sehat_inline_loader"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = c.clinicalBlueAccent,
        strokeWidth = 2.75.dp,
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = c.inkSecondary)
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    SehatSkeletonBox(modifier = Modifier.fillMaxWidth(0.85f), height = 14.dp)
    Spacer(modifier = Modifier.height(8.dp))
    SehatSkeletonBox(modifier = Modifier.fillMaxWidth(0.60f), height = 14.dp)
  }
}

@Composable
fun SehatToastBanner(
  message: String?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  AnimatedVisibility(
    visible = message != null,
    enter = slideInVertically { -it } + fadeIn(tween(250)),
    exit = slideOutVertically { -it } + fadeOut(tween(200)),
    modifier = modifier,
  ) {
    if (message != null) {
      val isError =
        remember(message) {
          val lower = message.lowercase()
          lower.contains("could not") ||
            lower.contains("incorrect") ||
            lower.contains("blocked") ||
            lower.contains("unavailable") ||
            lower.contains("too large") ||
            lower.contains("invalid") ||
            lower.contains("please enter") ||
            lower.contains("please choose") ||
            lower.contains("error") ||
            lower.contains("failed") ||
            lower.contains("no internet")
        }
      val container = if (isError) c.statusUrgentBg else if (c.isDark) DarkMist else DeepOcean
      val contentTint = if (isError) c.statusUrgentFg else PureWhite
      val borderTint = if (isError) c.statusUrgentBorder else c.hairlineBorder.copy(alpha = 0.45f)

      Surface(
        shape = CardShape,
        color = container,
        contentColor = contentTint,
        border = BorderStroke(1.dp, borderTint),
        shadowElevation = 6.dp,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
              liveRegion = if (isError) LiveRegionMode.Assertive else LiveRegionMode.Polite
            },
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = if (isError) Icons.Filled.ErrorOutline else Icons.Filled.Info,
              contentDescription = null,
              tint = if (isError) c.statusUrgentFg else PureWhite,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = message,
              style = MaterialTheme.typography.bodyMedium,
              color = contentTint,
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Dismiss notification",
              tint = contentTint,
            )
          }
        }
      }
    }
  }
}

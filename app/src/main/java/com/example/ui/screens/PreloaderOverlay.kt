package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.components.SehatProgressMeter
import com.example.ui.components.rememberPrefersReducedMotion
import com.example.ui.theme.SehatPalette

@Composable
fun PreloaderOverlay(
  visible: Boolean,
  progress: Float,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val ecgTint = c.clinicalBlueAccent
  val reducedMotion = rememberPrefersReducedMotion()
  var drawStarted by remember { mutableStateOf(false) }
  LaunchedEffect(visible) { if (visible) drawStarted = true }

  val lineDraw by
    animateFloatAsState(
      targetValue = if (drawStarted) 1f else 0f,
      animationSpec = tween(durationMillis = if (reducedMotion) 0 else 700, easing = FastOutSlowInEasing),
      label = "preloader_ecg",
    )

  AnimatedVisibility(
    visible = visible,
    exit = fadeOut(tween(250)),
    modifier = modifier,
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier
          .fillMaxSize()
          .background(Brush.verticalGradient(listOf(c.mist, c.background)))
          .semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = "Loading SehatSaathi"
          },
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.widthIn(max = 320.dp).padding(24.dp),
      ) {
        Canvas(modifier = Modifier.width(180.dp).height(64.dp)) {
          val w = size.width
          val h = size.height
          val mid = h * 0.55f
          val points =
            listOf(
              0f to mid,
              0.24f * w to mid,
              0.32f * w to mid - h * 0.14f,
              0.38f * w to mid + h * 0.12f,
              0.46f * w to mid - h * 0.44f,
              0.55f * w to mid + h * 0.34f,
              0.63f * w to mid - h * 0.10f,
              0.70f * w to mid,
              w to mid,
            )
          val path = Path()
          val maxIdx = ((points.size - 1) * lineDraw).toInt().coerceAtLeast(1)
          points.forEachIndexed { idx, (px, py) ->
            if (idx <= maxIdx) {
              if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
          }
          drawPath(
            path = path,
            color = ecgTint,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "SehatSaathi",
          style = MaterialTheme.typography.displayMedium,
          color = c.deepOcean,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Simple medicine & health safety",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        SehatProgressMeter(progress = progress, modifier = Modifier.width(200.dp), height = 6.dp)
      }
    }
  }
}

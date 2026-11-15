package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.components.SafetyDisclaimerFooter
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotFoundScreen(
  isAuthenticated: Boolean,
  isDemoMode: Boolean,
  onGoHome: () -> Unit,
  onGoBack: () -> Unit,
  onOpenInfo: (InfoSection) -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val reducedTransparency = rememberPrefersReducedTransparency()
  Column(modifier = modifier.fillMaxSize().background(c.background).testTag("not_found_screen")) {
    Surface(
      color = GlassTokens.chromeFill(scrolled = false, reducedTransparency = reducedTransparency, isDark = c.isDark),
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth().statusBarsPadding(),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier
              .clip(ButtonShape)
              .clickable(role = Role.Button, onClick = onGoHome)
              .semantics { contentDescription = "SehatSaathi home" }
              .defaultMinSize(minHeight = 48.dp)
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).clip(ButtonShape).background(DeepOcean),
          ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = PureWhite, modifier = Modifier.size(17.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text("SehatSaathi", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
        }
      }
    }

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
      SehatCard(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(56.dp).clip(ButtonShape).background(c.mist),
        ) {
          Icon(Icons.Filled.SearchOff, contentDescription = null, tint = c.clinicalBlueAccent, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("Page not found", style = MaterialTheme.typography.headlineLarge, color = c.deepOcean)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text =
            if (isAuthenticated) {
              "We could not find that page inside SehatSaathi. Let us take you back to your daily dashboard."
            } else {
              "We could not find that page. Return to the SehatSaathi home screen to sign in or try the demo."
            },
          style = MaterialTheme.typography.bodyLarge,
          color = c.ink,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Nothing in your health records was changed.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(18.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          SehatPrimaryButton(
            text = "Go to home",
            icon = Icons.Filled.Home,
            onClick = onGoHome,
            testTag = "not_found_home_btn",
          )
          SehatSecondaryButton(
            text = "Go back",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            minHeight = 56.dp,
            onClick = onGoBack,
            testTag = "not_found_back_btn",
          )
        }
      }
    }

    SafetyDisclaimerFooter(
      isDemoMode = isDemoMode,
      onLogoClick = onGoHome,
      onOpenInfo = onOpenInfo,
      modifier = Modifier.navigationBarsPadding(),
    )
  }
}

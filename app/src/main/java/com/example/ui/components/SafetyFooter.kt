package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.SiteConfig
import com.example.ui.screens.InfoSection
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MistSoft
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.rememberPrefersReducedTransparency

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafetyDisclaimerFooter(
  isDemoMode: Boolean,
  modifier: Modifier = Modifier,
  onLogoClick: (() -> Unit)? = null,
  onOpenInfo: ((InfoSection) -> Unit)? = null,
  showExtendedLinks: Boolean = false,
) {
  val c = SehatPalette.current
  val reducedTransparency = rememberPrefersReducedTransparency()
  val footerFill =
    GlassTokens.cardFill(
      baseColor = MistSoft,
      highReadability = true,
      reducedTransparency = reducedTransparency,
      isDark = c.isDark,
    )

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(footerFill)
        .border(border = GlassTokens.borderStroke(c.hairlineBorder, isDark = c.isDark))
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .testTag("safety_footer"),
  ) {
    if (showExtendedLinks && (onLogoClick != null || onOpenInfo != null)) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (onLogoClick != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .clip(ButtonShape)
                .clickable(role = Role.Button, onClick = onLogoClick)
                .semantics { contentDescription = "SehatSaathi home" }
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .testTag("footer_logo_home"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(22.dp).clip(ButtonShape).background(DeepOcean),
            ) {
              Icon(Icons.Filled.Favorite, contentDescription = null, tint = PureWhite, modifier = Modifier.size(12.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("SehatSaathi", style = MaterialTheme.typography.labelLarge, color = c.deepOcean)
          }
        }
        if (onOpenInfo != null) {
          InfoSection.entries.forEach { section ->
            Box(
              contentAlignment = Alignment.CenterStart,
              modifier =
                Modifier
                  .clip(ButtonShape)
                  .clickable(role = Role.Button) { onOpenInfo(section) }
                  .defaultMinSize(minHeight = 48.dp)
                  .padding(horizontal = 6.dp, vertical = 4.dp)
                  .testTag("footer_link_${section.name.lowercase()}"),
            ) {
              Text(
                text = section.title,
                style = MaterialTheme.typography.labelLarge,
                color = c.clinicalBlueAccent,
              )
            }
          }
        }
      }
      Text(
        text = SiteConfig.copyrightNotice(),
        style = MaterialTheme.typography.labelSmall,
        color = c.inkSecondary,
      )
      Spacer(modifier = Modifier.height(6.dp))
      HorizontalDivider(color = c.hairlineBorder)
      Spacer(modifier = Modifier.height(8.dp))
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Icon(
        imageVector = Icons.Filled.HealthAndSafety,
        contentDescription = null,
        tint = c.deepOcean,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = stringResource(R.string.safety_footer_disclaimer),
        style = MaterialTheme.typography.labelMedium,
        color = c.ink,
        modifier = Modifier.weight(1f),
      )
      if (isDemoMode) {
        Spacer(modifier = Modifier.width(6.dp))
        DemoPillBadge(text = "Demo")
      }
    }
  }
}

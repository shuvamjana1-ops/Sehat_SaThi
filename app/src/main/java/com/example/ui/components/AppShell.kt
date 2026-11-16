package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.data.SiteConfig
import com.example.ui.screens.InfoSection
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatAmbientBackdrop
import com.example.ui.theme.SehatPalette
import com.example.ui.theme.StatusUrgentFg
import com.example.ui.theme.rememberPrefersReducedTransparency
import kotlinx.coroutines.launch

data class ShellDestination(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val badgeCount: Int = 0,
  val isPrimaryAction: Boolean = false,
)

object AppRoutes {
  const val LANDING = "landing"
  const val SIGN_UP = "sign_up"
  const val LOG_IN = "log_in"
  const val FORGOT_PASSWORD = "forgot_password"
  const val DASHBOARD = "dashboard"
  const val LOG_READING = "log_reading"
  const val SCAN_RX = "scan_rx"
  const val MEDICINES = "medicines"
  const val TRENDS = "trends"
  const val FAMILY = "family"
  const val ACCOUNT = "account"
  const val INFO_ROUTE = "info/{section}"
  const val NOT_FOUND = "not_found"

  fun infoRoute(section: InfoSection): String = "info/${section.name.lowercase()}"
}

@Composable
fun SehatAppShell(
  currentRoute: String,
  isDemoMode: Boolean,
  isOnline: Boolean,
  offlineQueueCount: Int,
  refillAlertCount: Int,
  toastMessage: String?,
  onDismissToast: () -> Unit,
  onSyncOfflineQueue: () -> Unit,
  onNavigate: (String) -> Unit,
  onOpenEmergency: () -> Unit = {},
  modifier: Modifier = Modifier,
  onToggleTheme: (() -> Unit)? = null,
  content: @Composable (PaddingValues) -> Unit,
) {
  var showCommandPalette by remember { mutableStateOf(false) }
  var sidebarPinned by remember { mutableStateOf(true) }
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val destinations =
    remember(refillAlertCount) {
      listOf(
        ShellDestination(AppRoutes.DASHBOARD, "Home", Icons.Filled.Home),
        ShellDestination(
          AppRoutes.MEDICINES,
          "Medicines",
          Icons.Filled.Medication,
          badgeCount = refillAlertCount,
        ),
        ShellDestination(
          AppRoutes.LOG_READING,
          "Log reading",
          Icons.Filled.Add,
          isPrimaryAction = true,
        ),
        ShellDestination(AppRoutes.TRENDS, "Trends", Icons.AutoMirrored.Filled.ShowChart),
        ShellDestination(AppRoutes.FAMILY, "Family", Icons.Filled.Groups),
      )
    }

  val extraCommands =
    remember(destinations) {
      destinations +
        listOf(
          ShellDestination(AppRoutes.SCAN_RX, "Scan prescription", Icons.Filled.DocumentScanner),
          ShellDestination(AppRoutes.ACCOUNT, "Account & security", Icons.Filled.Person),
        )
    }

  val density = LocalDensity.current
  val imeBottomPx = WindowInsets.ime.getBottom(density)
  val isKeyboardOpen = imeBottomPx > 0
  val reducedMotion = rememberPrefersReducedMotion()
  val reducedTransparency = rememberPrefersReducedTransparency()

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .onPreviewKeyEvent { keyEvent ->
          if (
            keyEvent.type == KeyEventType.KeyDown &&
              keyEvent.key == Key.K &&
              (keyEvent.isCtrlPressed || keyEvent.isMetaPressed)
          ) {
            showCommandPalette = true
            true
          } else {
            false
          }
        },
  ) {
    val isPhone = maxWidth < 600.dp
    val isDesktop = maxWidth >= 840.dp

    if (isPhone && drawerState.isOpen) {
      BackHandler { scope.launch { drawerState.close() } }
    }

    SehatAmbientBackdrop(reducedMotion = reducedMotion) {
      ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isPhone,
        drawerContent = {
          if (isPhone) {
            MobileDrawerContent(
              currentRoute = currentRoute,
              isDemoMode = isDemoMode,
              reducedTransparency = reducedTransparency,
              destinations = extraCommands,
              onNavigate = { route ->
                scope.launch { drawerState.close() }
                onNavigate(route)
              },
              onClose = { scope.launch { drawerState.close() } },
            )
          }
        },
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
          if (!isPhone) {
            if (isDesktop) {
              DesktopSidebar(
                expanded = sidebarPinned,
                onToggleExpand = { sidebarPinned = !sidebarPinned },
                currentRoute = currentRoute,
                destinations = extraCommands,
                onNavigate = onNavigate,
              )
            } else {
              TabletNavRail(
                currentRoute = currentRoute,
                destinations = extraCommands,
                onNavigate = onNavigate,
              )
            }
          }

          Column(modifier = Modifier.weight(1f).fillMaxHeight().imePadding()) {
            ShellTopBar(
              isPhone = isPhone,
              isDemoMode = isDemoMode,
              isDrawerOpen = drawerState.isOpen,
              reducedTransparency = reducedTransparency,
              onOpenMenu = { scope.launch { drawerState.open() } },
              onOpenCommandPalette = { showCommandPalette = true },
              onToggleTheme = onToggleTheme,
              onOpenAccount = { onNavigate(AppRoutes.ACCOUNT) },
              onOpenEmergency = onOpenEmergency,
              onLogoClick = { onNavigate(AppRoutes.DASHBOARD) },
            )

            if (!isOnline || offlineQueueCount > 0) {
              OfflineStatusBar(
                isOnline = isOnline,
                queueCount = offlineQueueCount,
                onSyncNow = onSyncOfflineQueue,
                onCallFamily = { onNavigate(AppRoutes.FAMILY) },
              )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
              Box(modifier = Modifier.widthIn(max = 1200.dp).fillMaxSize()) {
                content(PaddingValues(bottom = if (isPhone && !isKeyboardOpen) 20.dp else 16.dp))
                SehatToastBanner(
                  message = toastMessage,
                  onDismiss = onDismissToast,
                  modifier = Modifier.align(Alignment.TopCenter),
                )
              }
            }

            SafetyDisclaimerFooter(
              isDemoMode = isDemoMode,
              onLogoClick = { onNavigate(AppRoutes.DASHBOARD) },
              onOpenInfo = { section -> onNavigate(AppRoutes.infoRoute(section)) },
              modifier =
                if (isPhone && !isKeyboardOpen) {
                  Modifier
                } else {
                  Modifier.navigationBarsPadding()
                },
            )

            if (isPhone && !isKeyboardOpen) {
              Spacer(modifier = Modifier.navigationBarsPadding().height(92.dp))
            }
          }
        }

        if (isPhone && !isKeyboardOpen) {
          FloatingBottomPillNav(
            currentRoute = currentRoute,
            destinations = destinations,
            reducedMotion = reducedMotion,
            reducedTransparency = reducedTransparency,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp),
          )
        }
      }
    }
    }
  }

  if (showCommandPalette) {
    CommandPaletteDialog(
      commands = extraCommands,
      onSelect = { route ->
        showCommandPalette = false
        onNavigate(route)
      },
      onDismiss = { showCommandPalette = false },
    )
  }
}

@Composable
private fun MobileDrawerContent(
  currentRoute: String,
  isDemoMode: Boolean,
  reducedTransparency: Boolean,
  destinations: List<ShellDestination>,
  onNavigate: (String) -> Unit,
  onClose: () -> Unit,
) {
  val c = SehatPalette.current
  ModalDrawerSheet(
    drawerContainerColor = GlassTokens.chromeFill(scrolled = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
    drawerContentColor = c.ink,
    modifier = Modifier.widthIn(max = 310.dp).testTag("mobile_nav_drawer"),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxHeight()
          .statusBarsPadding()
          .navigationBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier
              .clip(ButtonShape)
              .clickable(role = Role.Button) { onNavigate(AppRoutes.DASHBOARD) }
              .semantics { contentDescription = "SehatSaathi home" }
              .defaultMinSize(minHeight = 48.dp)
              .padding(horizontal = 6.dp),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).clip(ButtonShape).background(DeepOcean),
          ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = PureWhite, modifier = Modifier.size(17.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text("SehatSaathi", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          if (isDemoMode) {
            Spacer(modifier = Modifier.width(6.dp))
            DemoPillBadge(text = "Demo")
          }
        }
        IconButton(
          onClick = onClose,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("mobile_drawer_close_btn"),
        ) {
          Icon(Icons.Filled.Close, contentDescription = "Close menu", tint = c.deepOcean)
        }
      }

      HorizontalDivider(color = c.hairlineBorder, modifier = Modifier.padding(vertical = 8.dp))

      destinations.forEach { dest ->
        val isSelected = currentRoute == dest.route
        NavigationDrawerItem(
          selected = isSelected,
          onClick = { onNavigate(dest.route) },
          icon = {
            BadgedBox(
              badge = {
                if (dest.badgeCount > 0) {
                  Badge(containerColor = StatusUrgentFg, contentColor = PureWhite) {
                    Text(dest.badgeCount.toString())
                  }
                }
              },
            ) {
              Icon(imageVector = dest.icon, contentDescription = null)
            }
          },
          label = { Text(dest.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
          colors =
            NavigationDrawerItemDefaults.colors(
              selectedContainerColor = c.mist,
              selectedIconColor = c.deepOcean,
              selectedTextColor = c.deepOcean,
              unselectedContainerColor = Color.Transparent,
              unselectedIconColor = c.inkSecondary,
              unselectedTextColor = c.ink,
            ),
          modifier =
            Modifier
              .padding(vertical = 2.dp)
              .defaultMinSize(minHeight = 48.dp)
              .semantics { selected = isSelected }
              .testTag("drawer_${dest.route}"),
        )
      }

      HorizontalDivider(color = c.hairlineBorder, modifier = Modifier.padding(vertical = 10.dp))

      Text(
        text = "Safety & legal",
        style = MaterialTheme.typography.labelMedium,
        color = c.inkSecondary,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
      )

      val infoLinks =
        listOf(
          InfoSection.SAFETY to Icons.Filled.HealthAndSafety,
          InfoSection.PRIVACY to Icons.Filled.Lock,
          InfoSection.TERMS to Icons.Filled.Policy,
          InfoSection.ABOUT to Icons.Filled.Info,
        )
      infoLinks.forEach { (section, icon) ->
        NavigationDrawerItem(
          selected = false,
          onClick = { onNavigate(AppRoutes.infoRoute(section)) },
          icon = { Icon(icon, contentDescription = null) },
          label = { Text(section.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
          colors =
            NavigationDrawerItemDefaults.colors(
              unselectedContainerColor = Color.Transparent,
              unselectedIconColor = c.clinicalBlueAccent,
              unselectedTextColor = c.ink,
            ),
          modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = SiteConfig.copyrightNotice(),
        style = MaterialTheme.typography.labelSmall,
        color = c.inkSecondary,
        modifier = Modifier.padding(horizontal = 12.dp),
      )
    }
  }
}

@Composable
private fun ShellTopBar(
  isPhone: Boolean,
  isDemoMode: Boolean,
  isDrawerOpen: Boolean,
  reducedTransparency: Boolean,
  onOpenMenu: () -> Unit,
  onOpenCommandPalette: () -> Unit,
  onToggleTheme: (() -> Unit)?,
  onOpenAccount: () -> Unit,
  onOpenEmergency: () -> Unit,
  onLogoClick: () -> Unit,
) {
  val c = SehatPalette.current
  Surface(
    color = GlassTokens.chromeFill(scrolled = false, reducedTransparency = reducedTransparency, isDark = c.isDark),
    shadowElevation = 0.dp,
    modifier =
      Modifier
        .fillMaxWidth()
        .shadow(
          elevation = 5.dp,
          ambientColor = if (c.isDark) Color.Black.copy(alpha = 0.35f) else DeepOcean.copy(alpha = 0.14f),
          spotColor = if (c.isDark) Color.Black.copy(alpha = 0.4f) else DeepOcean.copy(alpha = 0.18f),
        )
        .statusBarsPadding(),
  ) {
    Column {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          if (isPhone) {
            SehatIconButton(
              icon = Icons.Filled.Menu,
              contentDescription = if (isDrawerOpen) "Close menu" else "Open menu",
              onClick = onOpenMenu,
              testTag = "mobile_menu_btn",
            )
            Spacer(modifier = Modifier.width(2.dp))
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .clip(ButtonShape)
                .clickable(role = Role.Button, onClick = onLogoClick)
                .semantics { contentDescription = "SehatSaathi home" }
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("shell_logo_home"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(34.dp).clip(ButtonShape).background(DeepOcean),
            ) {
              Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(18.dp),
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "SehatSaathi",
              style = MaterialTheme.typography.titleLarge,
              color = c.deepOcean,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (isDemoMode) {
              Spacer(modifier = Modifier.width(6.dp))
              DemoPillBadge(text = "Demo")
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (!isPhone) {
            Surface(
              onClick = onOpenCommandPalette,
              shape = PillShape,
              color = c.mistSoft,
              border = BorderStroke(1.dp, c.hairlineBorder),
              modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("command_palette_btn"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Search,
                  contentDescription = null,
                  tint = c.deepOcean,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Quick jump · Ctrl+K",
                  style = MaterialTheme.typography.labelMedium,
                  color = c.inkSecondary,
                )
              }
            }
          } else {
            SehatIconButton(
              icon = Icons.Filled.Search,
              contentDescription = "Quick jump menu",
              onClick = onOpenCommandPalette,
              testTag = "command_palette_btn",
            )
          }

          if (onToggleTheme != null) {
            SehatIconButton(
              icon = if (c.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
              contentDescription = if (c.isDark) "Switch to light mode" else "Switch to dark mode",
              onClick = onToggleTheme,
              testTag = "shell_theme_toggle_btn",
            )
          }

          Surface(
            onClick = onOpenEmergency,
            shape = PillShape,
            color = StatusUrgentFg,
            modifier = Modifier.defaultMinSize(minHeight = 40.dp).testTag("sos_emergency_btn"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("SOS", style = MaterialTheme.typography.titleSmall, color = PureWhite)
            }
          }

          SehatIconButton(
            icon = Icons.Filled.Person,
            contentDescription = "Account and security",
            onClick = onOpenAccount,
            testTag = "account_btn",
          )
        }
      }
      HorizontalDivider(color = c.hairlineBorder, thickness = 1.dp)
    }
  }
}

@Composable
private fun FloatingBottomPillNav(
  currentRoute: String,
  destinations: List<ShellDestination>,
  reducedMotion: Boolean,
  reducedTransparency: Boolean,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Surface(
    shape = PillShape,
    color = GlassTokens.cardFill(PureWhite, highReadability = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
    shadowElevation = 0.dp,
    border = GlassTokens.borderStroke(c.hairlineBorder, isDark = c.isDark),
    modifier =
      modifier
        .padding(horizontal = 12.dp)
        .fillMaxWidth()
        .widthIn(max = 520.dp)
        .shadow(
          elevation = 14.dp,
          shape = PillShape,
          ambientColor = if (c.isDark) Color.Black.copy(alpha = 0.45f) else DeepOcean.copy(alpha = 0.22f),
          spotColor = if (c.isDark) Color.Black.copy(alpha = 0.55f) else DeepOcean.copy(alpha = 0.28f),
        ),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
    ) {
      destinations.forEach { dest ->
        val isSelected = currentRoute == dest.route
        if (dest.isPrimaryAction) {
          val interactionSource = remember { MutableInteractionSource() }
          val pressed by interactionSource.collectIsPressedAsState()
          val pressScale by
            animateFloatAsState(
              targetValue = if (pressed && !reducedMotion) 0.97f else 1f,
              animationSpec = tween(150),
              label = "floating_log_scale",
            )
          Surface(
            onClick = { onNavigate(dest.route) },
            interactionSource = interactionSource,
            shape = PillShape,
            color = c.clinicalBlue,
            shadowElevation = 0.dp,
            modifier =
              Modifier
                .offset(y = (-4).dp)
                .scale(pressScale)
                .shadow(
                  elevation = 8.dp,
                  shape = PillShape,
                  ambientColor = DeepOcean.copy(alpha = 0.3f),
                  spotColor = c.clinicalBlue.copy(alpha = 0.45f),
                )
                .defaultMinSize(minWidth = 56.dp, minHeight = 52.dp)
                .semantics { selected = isSelected }
                .testTag("nav_${dest.route}"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
              Icon(
                imageVector = dest.icon,
                contentDescription = dest.label,
                tint = PureWhite,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Log",
                style = MaterialTheme.typography.labelLarge,
                color = PureWhite,
                maxLines = 1,
              )
            }
          }
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
              Modifier
                .clip(PillShape)
                .clickable(role = Role.Tab) { onNavigate(dest.route) }
                .background(if (isSelected) c.mist else Color.Transparent)
                .defaultMinSize(minWidth = 52.dp, minHeight = 48.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .semantics { selected = isSelected }
                .testTag("nav_${dest.route}"),
          ) {
            BadgedBox(
              badge = {
                if (dest.badgeCount > 0) {
                  Badge(containerColor = StatusUrgentFg, contentColor = PureWhite) {
                    Text(dest.badgeCount.toString())
                  }
                }
              },
            ) {
              Icon(
                imageVector = dest.icon,
                contentDescription = dest.label,
                tint = if (isSelected) c.deepOcean else c.inkSecondary,
                modifier = Modifier.size(22.dp),
              )
            }
            Text(
              text = dest.label,
              style = MaterialTheme.typography.labelSmall,
              color = if (isSelected) c.deepOcean else c.inkSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TabletNavRail(
  currentRoute: String,
  destinations: List<ShellDestination>,
  onNavigate: (String) -> Unit,
) {
  val c = SehatPalette.current
  val reducedTransparency = rememberPrefersReducedTransparency()
  NavigationRail(
    containerColor = GlassTokens.chromeFill(scrolled = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
    modifier = Modifier.fillMaxHeight().statusBarsPadding().navigationBarsPadding(),
  ) {
    Spacer(modifier = Modifier.height(12.dp))
    destinations.forEach { dest ->
      val isSelected = currentRoute == dest.route
      NavigationRailItem(
        selected = isSelected,
        onClick = { onNavigate(dest.route) },
        icon = {
          BadgedBox(
            badge = {
              if (dest.badgeCount > 0) {
                Badge(containerColor = StatusUrgentFg, contentColor = PureWhite) {
                  Text(dest.badgeCount.toString())
                }
              }
            },
          ) {
            Icon(imageVector = dest.icon, contentDescription = dest.label)
          }
        },
        label = { Text(dest.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors =
          NavigationRailItemDefaults.colors(
            selectedIconColor = c.deepOcean,
            selectedTextColor = c.deepOcean,
            indicatorColor = c.mist,
            unselectedIconColor = c.inkSecondary,
            unselectedTextColor = c.inkSecondary,
          ),
      )
    }
  }
}

@Composable
private fun DesktopSidebar(
  expanded: Boolean,
  onToggleExpand: () -> Unit,
  currentRoute: String,
  destinations: List<ShellDestination>,
  onNavigate: (String) -> Unit,
) {
  val c = SehatPalette.current
  val reducedTransparency = rememberPrefersReducedTransparency()
  Surface(
    color = GlassTokens.chromeFill(scrolled = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
    border = GlassTokens.borderStroke(c.hairlineBorder, isDark = c.isDark),
    modifier =
      Modifier
        .width(if (expanded) 232.dp else 82.dp)
        .fillMaxHeight()
        .statusBarsPadding()
        .navigationBarsPadding(),
  ) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (expanded) Arrangement.SpaceBetween else Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      ) {
        if (expanded) {
          Text("Menu", style = MaterialTheme.typography.titleMedium, color = c.deepOcean)
        }
        IconButton(onClick = onToggleExpand, modifier = Modifier.minimumInteractiveComponentSize()) {
          Icon(
            imageVector =
              if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
            contentDescription = if (expanded) "Collapse sidebar" else "Expand sidebar",
            tint = c.deepOcean,
          )
        }
      }
      destinations.forEach { dest ->
        val isSelected = currentRoute == dest.route
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp)
              .clip(ButtonShape)
              .background(
                when {
                  dest.isPrimaryAction -> c.clinicalBlue
                  isSelected -> c.mist
                  else -> Color.Transparent
                },
              )
              .clickable(role = Role.Button) { onNavigate(dest.route) }
              .defaultMinSize(minHeight = 48.dp)
              .padding(horizontal = 12.dp, vertical = 10.dp)
              .semantics { selected = isSelected }
              .testTag("sidebar_${dest.route}"),
        ) {
          val tint =
            when {
              dest.isPrimaryAction -> PureWhite
              isSelected -> c.deepOcean
              else -> c.inkSecondary
            }
          BadgedBox(
            badge = {
              if (dest.badgeCount > 0) {
                Badge(containerColor = StatusUrgentFg, contentColor = PureWhite) {
                  Text(dest.badgeCount.toString())
                }
              }
            },
          ) {
            Icon(imageVector = dest.icon, contentDescription = dest.label, tint = tint)
          }
          if (expanded) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = dest.label,
              style = MaterialTheme.typography.labelLarge,
              color = tint,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CommandPaletteDialog(
  commands: List<ShellDestination>,
  onSelect: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  var query by remember { mutableStateOf("") }
  val filtered =
    remember(query, commands) {
      if (query.isBlank()) commands
      else commands.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }

  val reducedTransparency = rememberPrefersReducedTransparency()
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = GlassTokens.GlassCornerShape,
      color = GlassTokens.cardFill(PureWhite, highReadability = true, reducedTransparency = reducedTransparency, isDark = c.isDark),
      border = GlassTokens.borderStroke(c.hairlineBorder, isDark = c.isDark),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Quick Jump", style = MaterialTheme.typography.titleLarge, color = c.deepOcean)
          IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(Icons.Filled.Close, contentDescription = "Close quick jump", tint = c.deepOcean)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SehatTextField(
          label = "Search screen or action",
          value = query,
          onValueChange = { query = it },
          placeholder = "e.g. Log reading, Medicines, Family",
          testTag = "command_search_input",
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.height(260.dp)) {
          items(filtered, key = { it.route }) { item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(ButtonShape)
                  .clickable { onSelect(item.route) }
                  .defaultMinSize(minHeight = 48.dp)
                  .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
              Icon(imageVector = item.icon, contentDescription = null, tint = c.clinicalBlueAccent)
              Spacer(modifier = Modifier.width(12.dp))
              Text(item.label, style = MaterialTheme.typography.bodyMedium, color = c.ink)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OfflineStatusBar(
  isOnline: Boolean,
  queueCount: Int,
  onSyncNow: () -> Unit,
  onCallFamily: () -> Unit,
) {
  val c = SehatPalette.current
  Surface(
    color = if (!isOnline) c.statusCautionBg else c.mist,
    border =
      BorderStroke(
        1.dp,
        if (!isOnline) c.statusCautionBorder else c.hairlineBorder,
      ),
    modifier = Modifier.fillMaxWidth().testTag("offline_status_bar"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f),
      ) {
        Icon(
          imageVector = if (!isOnline) Icons.Filled.CloudOff else Icons.Filled.Sync,
          contentDescription = null,
          tint = if (!isOnline) c.statusCautionFg else c.clinicalBlueAccent,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text =
            when {
              !isOnline && queueCount > 0 ->
                "Offline ($queueCount queued) · Rules & keypad still work"
              !isOnline ->
                "Offline · Safety rules, schedule & Call Family work without net"
              else -> "$queueCount queued offline scan(s) ready"
            },
          style = MaterialTheme.typography.labelMedium,
          color = if (!isOnline) c.statusCautionFg else c.deepOcean,
        )
      }
      if (!isOnline) {
        SehatGhostButton(text = "Call family", onClick = onCallFamily)
      } else {
        SehatGhostButton(text = "Sync now", onClick = onSyncNow, testTag = "sync_offline_queue_btn")
      }
    }
  }
}

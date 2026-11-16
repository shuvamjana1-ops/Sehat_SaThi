package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ai.AdvisorUiState
import com.example.ai.CareChatMessage
import com.example.ai.CareGuidanceResult
import com.example.ai.ChatRole
import com.example.data.HealthStatusLevel
import com.example.ui.SehatSaathiUiState
import com.example.ui.components.DemoPillBadge
import com.example.ui.components.SehatCard
import com.example.ui.components.SehatDestructiveButton
import com.example.ui.components.SehatGhostButton
import com.example.ui.components.SehatPrimaryButton
import com.example.ui.components.SehatSecondaryButton
import com.example.ui.components.SehatTextField
import com.example.ui.components.StatusChip
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.CardShape
import com.example.ui.theme.ClinicalBlue
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.DeepOceanDark
import com.example.ui.theme.Mist
import com.example.ui.theme.PillShape
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SehatPalette

@Composable
fun CareAssistantScreen(
  uiState: SehatSaathiUiState,
  contentPadding: PaddingValues,
  onSelectTab: (Int) -> Unit,
  onTopicInputChange: (String) -> Unit,
  onRequestDoDont: (String) -> Unit,
  onAskFollowUpInChat: (String) -> Unit,
  onSendChatMessage: (String) -> Unit,
  onChatDraftChange: (String) -> Unit = {},
  onRetryChat: () -> Unit,
  onResetChat: () -> Unit,
  onDismissChatError: () -> Unit,
  onDismissEmergencyShortcut: () -> Unit,
  onOpenEmergency: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  val care = uiState.careAssistant

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("care_assistant_screen"),
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Compact Hero Header + Mode Switcher
    Surface(
      shape = CardShape,
      color = DeepOcean,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier =
          Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(DeepOceanDark, DeepOcean, ClinicalBlue)))
            .padding(16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = PureWhite,
              modifier = Modifier.size(22.dp),
            )
            Text(
              text = "Care AI Companion",
              style = MaterialTheme.typography.titleLarge,
              color = PureWhite,
            )
          }
          if (uiState.session.isDemoMode) {
            DemoPillBadge(text = "Demo")
          }
        }
        Text(
          text = "Clear Do's & Don'ts and care chat tailored to your readings. Never diagnoses or changes medicine doses.",
          style = MaterialTheme.typography.bodySmall,
          color = Mist,
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          AssistantTabButton(
            text = "Do's & Don'ts",
            icon = Icons.Filled.FactCheck,
            selected = care.activeTab == 0,
            onClick = { onSelectTab(0) },
            testTag = "ai_tab_dodont",
            modifier = Modifier.weight(1f),
          )
          AssistantTabButton(
            text = "Care Chat",
            icon = Icons.AutoMirrored.Filled.Chat,
            selected = care.activeTab == 1,
            onClick = { onSelectTab(1) },
            testTag = "ai_tab_chat",
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    if (care.showEmergencyShortcutBanner) {
      Spacer(modifier = Modifier.height(10.dp))
      RedFlagEmergencyBanner(
        onOpenEmergency = onOpenEmergency,
        onDismiss = onDismissEmergencyShortcut,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      if (care.activeTab == 0) {
        DoDontAdvisorTab(
          topicInput = care.topicInput,
          advisorState = care.advisorState,
          bottomPadding = contentPadding.calculateBottomPadding(),
          onTopicInputChange = onTopicInputChange,
          onRequestDoDont = onRequestDoDont,
          onAskFollowUpInChat = onAskFollowUpInChat,
        )
      } else {
        CareChatTab(
          messages = care.chatMessages,
          isStreaming = care.isChatStreaming,
          chatError = care.chatError,
          canRetry = care.lastFailedChatPrompt != null,
          bottomPadding = contentPadding.calculateBottomPadding(),
          onSendMessage = onSendChatMessage,
          onDraftChange = onChatDraftChange,
          onRetryChat = onRetryChat,
          onResetChat = onResetChat,
          onDismissError = onDismissChatError,
          onOpenEmergency = onOpenEmergency,
        )
      }
    }
  }
}

@Composable
private fun AssistantTabButton(
  text: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val c = SehatPalette.current
  Surface(
    onClick = onClick,
    shape = ButtonShape,
    color = if (selected) PureWhite else PureWhite.copy(alpha = 0.14f),
    border = BorderStroke(1.dp, if (selected) PureWhite else PureWhite.copy(alpha = 0.28f)),
    modifier = modifier.defaultMinSize(minHeight = 48.dp).testTag(testTag),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) DeepOcean else PureWhite,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) DeepOcean else PureWhite,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun RedFlagEmergencyBanner(
  onOpenEmergency: () -> Unit,
  onDismiss: () -> Unit,
) {
  val c = SehatPalette.current
  Surface(
    shape = CardShape,
    color = c.statusUrgentBg,
    border = BorderStroke(1.5.dp, c.statusUrgentBorder),
    modifier = Modifier.fillMaxWidth().testTag("ai_red_flag_banner"),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.padding(14.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = c.statusUrgentFg,
            modifier = Modifier.size(22.dp),
          )
          Text(
            text = "Urgent warning signs noticed",
            style = MaterialTheme.typography.titleMedium,
            color = c.statusUrgentFg,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Dismiss emergency banner",
            tint = c.statusUrgentFg,
          )
        }
      }
      Text(
        text = "If you have chest pain, trouble breathing, fainting, confusion, or severe weakness, call 112 or reach your nearest hospital right away.",
        style = MaterialTheme.typography.bodyMedium,
        color = c.ink,
      )
      SehatDestructiveButton(
        text = "Open Emergency Help / 112",
        icon = Icons.Filled.LocalHospital,
        onClick = onOpenEmergency,
        modifier = Modifier.fillMaxWidth(),
        testTag = "ai_emergency_shortcut_btn",
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoDontAdvisorTab(
  topicInput: String,
  advisorState: AdvisorUiState,
  bottomPadding: androidx.compose.ui.unit.Dp,
  onTopicInputChange: (String) -> Unit,
  onRequestDoDont: (String) -> Unit,
  onAskFollowUpInChat: (String) -> Unit,
) {
  val c = SehatPalette.current
  val focusManager = LocalFocusManager.current
  var draftTopic by rememberSaveable { mutableStateOf(topicInput) }

  LaunchedEffect(topicInput) {
    if (topicInput.isEmpty() && draftTopic.isNotEmpty()) {
      draftTopic = ""
    }
  }

  val presetSituations =
    listOf(
      "Low blood sugar (under 70 mg/dL)" to "Low blood sugar",
      "High blood pressure today" to "High BP today",
      "Missed a scheduled medicine dose" to "Missed a dose",
      "Feeling dizzy or shaky" to "Feeling dizzy",
      "High blood sugar after meals" to "High blood sugar",
      "Mild fever or body ache with diabetes" to "Fever or ache",
    )

  LazyColumn(
    contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = Modifier.fillMaxSize().testTag("ai_dodont_list"),
  ) {
    item(key = "situations_card") {
      SehatCard(
        containerColor = c.surface,
        useGlass = true,
        highReadabilityGlass = true,
      ) {
        Text(
          text = "What should I do or avoid?",
          style = MaterialTheme.typography.titleLarge,
          color = c.deepOcean,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Tap a common situation or type what you feel to get clear Do's and Don'ts.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.inkSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          presetSituations.forEach { (query, label) ->
            Surface(
              onClick = {
                focusManager.clearFocus()
                draftTopic = query
                onTopicInputChange(query)
                onRequestDoDont(query)
              },
              shape = PillShape,
              color = if (draftTopic == query) c.clinicalBlue else c.mistSoft,
              border =
                BorderStroke(
                  1.dp,
                  if (draftTopic == query) c.clinicalBlue else c.hairlineBorder,
                ),
              modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("ai_chip_${label.lowercase().replace(' ', '_')}"),
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelLarge,
                  color = if (draftTopic == query) PureWhite else c.deepOcean,
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
        SehatTextField(
          label = "Or describe your situation",
          value = draftTopic,
          onValueChange = { next ->
            draftTopic = next
            onTopicInputChange(next)
          },
          placeholder = "e.g. Sugar is 265 before dinner, feeling tired",
          singleLine = false,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions =
            KeyboardActions(
              onDone = {
                focusManager.clearFocus()
                onRequestDoDont(draftTopic)
              },
            ),
          testTag = "ai_topic_input",
        )
        Spacer(modifier = Modifier.height(12.dp))
        SehatPrimaryButton(
          text = "Get Do's & Don'ts",
          icon = Icons.Filled.AutoAwesome,
          isLoading = advisorState is AdvisorUiState.Loading,
          enabled = advisorState !is AdvisorUiState.Loading,
          onClick = {
            focusManager.clearFocus()
            onRequestDoDont(draftTopic)
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "ai_get_dodont_btn",
        )
      }
    }

    when (advisorState) {
      AdvisorUiState.Idle -> {
        item(key = "idle_note") {
          SehatCard(containerColor = c.mistSoft) {
            Text(
              text = "How this helper works",
              style = MaterialTheme.typography.titleMedium,
              color = c.deepOcean,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "• Gives practical everyday steps (like water, rest, safe rechecking, and warning signs).\n• Never prescribes new tablets or changes your medicine doses.\n• Uses your saved readings & medicine names as reference only.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
          }
        }
      }

      AdvisorUiState.Loading -> {
        item(key = "loading_card") {
          SehatCard(containerColor = c.surface) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              CircularProgressIndicator(
                color = c.clinicalBlueAccent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp),
              )
              Column {
                Text(
                  text = "Preparing Do's & Don'ts...",
                  style = MaterialTheme.typography.titleMedium,
                  color = c.deepOcean,
                )
                Text(
                  text = "Checking safe steps for your situation.",
                  style = MaterialTheme.typography.bodySmall,
                  color = c.inkSecondary,
                )
              }
            }
          }
        }
      }

      is AdvisorUiState.Error -> {
        item(key = "error_card") {
          SehatCard(
            containerColor = c.statusUrgentBg,
            borderColor = c.statusUrgentBorder,
          ) {
            StatusChip(level = HealthStatusLevel.URGENT, label = "Care AI unavailable")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = advisorState.message,
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
              modifier = Modifier.testTag("ai_dodont_error_text"),
            )
            Spacer(modifier = Modifier.height(10.dp))
            SehatSecondaryButton(
              text = "Try again",
              icon = Icons.Filled.Refresh,
              onClick = { onRequestDoDont(draftTopic.ifBlank { topicInput }) },
              modifier = Modifier.fillMaxWidth(),
              testTag = "ai_retry_dodont_btn",
            )
          }
        }
      }

      is AdvisorUiState.Success -> {
        item(key = "result_cards") {
          GuidanceResultSection(
            result = advisorState.result,
            onAskFollowUp = {
              onAskFollowUpInChat(
                "I just read your Do's and Don'ts for \"${advisorState.result.topic}\". Can I ask a follow-up question?",
              )
            },
          )
        }
      }
    }
  }
}

@Composable
private fun GuidanceResultSection(
  result: CareGuidanceResult,
  onAskFollowUp: () -> Unit,
) {
  val c = SehatPalette.current
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SehatCard(containerColor = c.surface, useGlass = true, highReadabilityGlass = true) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = result.topic,
          style = MaterialTheme.typography.titleLarge,
          color = c.deepOcean,
          modifier = Modifier.weight(1f),
        )
        StatusChip(level = HealthStatusLevel.INFO, label = "Care guide")
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = result.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = c.ink,
      )
    }

    // What TO DO (Green + Check Icon + Label)
    SehatCard(
      containerColor = c.statusNormalBg,
      borderColor = c.statusNormalBorder,
    ) {
      StatusChip(level = HealthStatusLevel.NORMAL, label = "What to Do")
      Spacer(modifier = Modifier.height(10.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        result.whatToDo.forEach { step ->
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Do",
              tint = c.statusNormalFg,
              modifier = Modifier.padding(top = 2.dp).size(20.dp),
            )
            Text(
              text = step,
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    // What NOT TO DO (Urgent Red + Cancel Icon + Label)
    SehatCard(
      containerColor = c.statusUrgentBg,
      borderColor = c.statusUrgentBorder,
    ) {
      StatusChip(level = HealthStatusLevel.URGENT, label = "What NOT to Do")
      Spacer(modifier = Modifier.height(10.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        result.whatNotToDo.forEach { step ->
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(
              imageVector = Icons.Filled.Cancel,
              contentDescription = "Do not",
              tint = c.statusUrgentFg,
              modifier = Modifier.padding(top = 2.dp).size(20.dp),
            )
            Text(
              text = step,
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    // When to seek urgent help + follow-up CTA
    SehatCard(
      containerColor = c.statusCautionBg,
      borderColor = c.statusCautionBorder,
    ) {
      StatusChip(level = HealthStatusLevel.CAUTION, label = "When to call 112 or a doctor")
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = result.whenToCallHelp,
        style = MaterialTheme.typography.bodyMedium,
        color = c.ink,
      )
      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = c.statusCautionBorder)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = result.endingReminder,
        style = MaterialTheme.typography.labelLarge,
        color = c.statusCautionFg,
      )
      Spacer(modifier = Modifier.height(10.dp))
      SehatSecondaryButton(
        text = "Ask a follow-up in Care Chat",
        icon = Icons.AutoMirrored.Filled.Chat,
        onClick = onAskFollowUp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "ai_followup_chat_btn",
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CareChatTab(
  messages: List<CareChatMessage>,
  isStreaming: Boolean,
  chatError: String?,
  canRetry: Boolean,
  bottomPadding: androidx.compose.ui.unit.Dp,
  onSendMessage: (String) -> Unit,
  onDraftChange: (String) -> Unit,
  onRetryChat: () -> Unit,
  onResetChat: () -> Unit,
  onDismissError: () -> Unit,
  onOpenEmergency: () -> Unit,
) {
  val c = SehatPalette.current
  val listState = rememberLazyListState()
  val focusManager = LocalFocusManager.current
  var draft by rememberSaveable { mutableStateOf("") }

  val starterQuestions =
    listOf(
      "What should I do if my sugar feels low?",
      "What should I avoid when BP is high?",
      "Explain my medicines today simply",
      "What questions should I ask my doctor?",
    )

  val lastMessage = messages.lastOrNull()
  val lastMessageLen = lastMessage?.text?.length ?: 0
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      runCatching { listState.animateScrollToItem(messages.lastIndex) }
    }
  }
  LaunchedEffect(lastMessageLen) {
    if (messages.isNotEmpty() && lastMessage?.isStreaming == true) {
      runCatching { listState.scrollToItem(messages.lastIndex) }
    }
  }

  Column(modifier = Modifier.fillMaxSize().testTag("ai_chat_tab")) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = "Multi-turn Care Chat",
        style = MaterialTheme.typography.titleMedium,
        color = c.deepOcean,
      )
      if (messages.isNotEmpty()) {
        SehatGhostButton(
          text = "New chat",
          icon = Icons.Filled.Refresh,
          onClick = {
            draft = ""
            onDraftChange("")
            onResetChat()
          },
          testTag = "ai_new_chat_btn",
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    LazyColumn(
      state = listState,
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 10.dp),
      modifier = Modifier.weight(1f).fillMaxWidth().testTag("ai_chat_messages"),
    ) {
      if (messages.isEmpty()) {
        item(key = "chat_welcome") {
          SehatCard(containerColor = c.surface, useGlass = true, highReadabilityGlass = true) {
            Text(
              text = "Ask SehatSaathi Care AI",
              style = MaterialTheme.typography.titleLarge,
              color = c.deepOcean,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Ask about daily do's and don'ts, meals and water habits, your saved reading values, or what to ask at your next clinic visit.",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Try asking:",
              style = MaterialTheme.typography.labelLarge,
              color = c.deepOcean,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              starterQuestions.forEachIndexed { index, q ->
                Surface(
                  onClick = { onSendMessage(q) },
                  enabled = !isStreaming,
                  shape = PillShape,
                  color = c.mistSoft,
                  border = BorderStroke(1.dp, c.hairlineBorder),
                  modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("ai_chat_starter_$index"),
                ) {
                  Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                  ) {
                    Text(
                      text = q,
                      style = MaterialTheme.typography.labelLarge,
                      color = c.deepOcean,
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        items(messages, key = { it.id }) { msg ->
          ChatBubble(
            message = msg,
            onOpenEmergency = onOpenEmergency,
          )
        }
      }
    }

    if (chatError != null) {
      Surface(
        shape = ButtonShape,
        color = c.statusUrgentBg,
        border = BorderStroke(1.dp, c.statusUrgentBorder),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("ai_chat_error_banner"),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = c.statusUrgentFg,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = chatError,
            style = MaterialTheme.typography.bodySmall,
            color = c.ink,
            modifier = Modifier.weight(1f),
          )
          if (canRetry && !isStreaming) {
            SehatGhostButton(
              text = "Retry",
              onClick = onRetryChat,
              testTag = "ai_chat_retry_btn",
            )
          }
          IconButton(
            onClick = onDismissError,
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Dismiss error",
              tint = c.statusUrgentFg,
            )
          }
        }
      }
    }

    // Sticky Bottom Composer
    Surface(
      shape = CardShape,
      color = c.surface,
      border = BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding.coerceAtLeast(4.dp)),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      ) {
        OutlinedTextField(
          value = draft,
          onValueChange = { next ->
            draft = next
            onDraftChange(next)
          },
          placeholder = {
            Text(
              text = "Ask about do's, don'ts, or symptoms...",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
          },
          maxLines = 3,
          shape = ButtonShape,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions =
            KeyboardActions(
              onSend = {
                if (draft.isNotBlank() && !isStreaming) {
                  val toSend = draft
                  draft = ""
                  onDraftChange("")
                  focusManager.clearFocus()
                  onSendMessage(toSend)
                }
              },
            ),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedContainerColor = c.mistSoft,
              unfocusedContainerColor = c.mistSoft,
              focusedBorderColor = c.clinicalBlueAccent,
              unfocusedBorderColor = c.hairlineBorder,
              focusedTextColor = c.ink,
              unfocusedTextColor = c.ink,
            ),
          modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("ai_chat_input"),
        )

        Surface(
          onClick = {
            if (draft.isNotBlank() && !isStreaming) {
              val toSend = draft
              draft = ""
              onDraftChange("")
              focusManager.clearFocus()
              onSendMessage(toSend)
            }
          },
          enabled = draft.isNotBlank() && !isStreaming,
          shape = PillShape,
          color = if (draft.isNotBlank() && !isStreaming) c.clinicalBlue else c.mist,
          modifier = Modifier.minimumInteractiveComponentSize().size(48.dp).testTag("ai_chat_send_btn"),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isStreaming) {
              CircularProgressIndicator(
                color = c.deepOcean,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
              )
            } else {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send question",
                tint = if (draft.isNotBlank()) PureWhite else c.inkSecondary,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChatBubble(
  message: CareChatMessage,
  onOpenEmergency: () -> Unit,
) {
  val c = SehatPalette.current
  val isUser = message.role == ChatRole.USER

  Row(
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Surface(
      shape =
        RoundedCornerShape(
          topStart = 18.dp,
          topEnd = 18.dp,
          bottomStart = if (isUser) 18.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 18.dp,
        ),
      color = if (isUser) c.clinicalBlue else c.surface,
      border = if (isUser) null else BorderStroke(1.dp, c.hairlineBorder),
      modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.90f),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(14.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (!isUser) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = c.clinicalBlueAccent,
                modifier = Modifier.size(16.dp),
              )
            }
            Text(
              text = if (isUser) "You" else "Care AI",
              style = MaterialTheme.typography.labelMedium,
              color = if (isUser) Mist else c.deepOcean,
            )
          }
          if (message.timestampLabel.isNotBlank()) {
            Text(
              text = message.timestampLabel,
              style = MaterialTheme.typography.labelSmall,
              color = if (isUser) Mist.copy(alpha = 0.85f) else c.inkSecondary,
            )
          }
        }

        if (message.isStreaming && message.text.isBlank()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            CircularProgressIndicator(
              color = c.clinicalBlueAccent,
              strokeWidth = 2.dp,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "Thinking carefully...",
              style = MaterialTheme.typography.bodyMedium,
              color = c.inkSecondary,
            )
          }
        } else {
          Text(
            text = if (message.isStreaming) "${message.text} ▍" else message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) PureWhite else c.ink,
          )
        }

        if (!isUser && message.hasRedFlagBanner) {
          SehatDestructiveButton(
            text = "Call 112 / Emergency Help",
            icon = Icons.Filled.LocalHospital,
            onClick = onOpenEmergency,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          )
        }
      }
    }
  }
}

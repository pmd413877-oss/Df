package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GlowingOrb
import com.example.ui.components.NavTab
import com.example.ui.components.QuickPromptChip
import com.example.ui.components.ZaraBottomNav
import com.example.ui.components.ZaraTopBar
import com.example.ui.state.VoiceState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.BorderCard
import com.example.ui.theme.BorderGlow
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun HomeScreen(
    assistantViewModel: AssistantViewModel,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateApiKey: () -> Unit = {},
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by assistantViewModel.uiState.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        assistantViewModel.setMicPermissionGranted(isGranted)
        if (isGranted) {
            assistantViewModel.clearError()
            assistantViewModel.startVoiceListening()
        } else {
            assistantViewModel.showMicPermissionDeniedError()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "home_mic_pulse")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF080B16),
                        Color(0xFF0D1224),
                        Color(0xFF080B17)
                    )
                )
            )
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar
            ZaraTopBar(
                onMenuClick = onOpenDrawer,
                onSettingsClick = onNavigateSettings
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Central Glowing AI Voice Orb
                Box(
                    modifier = Modifier.padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlowingOrb(
                        voiceState = uiState.voiceState,
                        amplitude = uiState.audioAmplitude,
                        size = 220.dp
                    )
                }

                // Speech Bubble Card (Greeting / Recognized Question / AI Response)
                SpeechBubbleCard(
                    uiState = uiState,
                    onCopy = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Zara AI", text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSpeak = { assistantViewModel.speakCurrentResponse() },
                    onStopSpeech = { assistantViewModel.stopSpeaking() },
                    onDismissError = { assistantViewModel.clearError() },
                    onCancelProcessing = { assistantViewModel.cancelProcessing() },
                    onRequestMicPermission = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onOpenApiKeySetup = onNavigateApiKey,
                    onOpenTextInput = {
                        assistantViewModel.openTextInput()
                    },
                    onRetryVoice = {
                        val hasMicPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasMicPermission) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            assistantViewModel.startVoiceListening()
                        }
                    }
                )

                // Audio Waveform Visualizer
                AudioWaveform(
                    voiceState = uiState.voiceState,
                    amplitude = uiState.audioAmplitude,
                    modifier = Modifier.fillMaxWidth()
                )

                // Controls Row: [History Button]  [Center Mic Button]  [Type Button]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History Shortcut Button
                    CircleActionButton(
                        icon = Icons.Default.History,
                        label = "History",
                        onClick = onNavigateHistory,
                        testTag = "home_history_button"
                    )

                    // Large Glowing Center Microphone Button
                    val isListeningOrSpeaking = uiState.voiceState == VoiceState.LISTENING ||
                            uiState.voiceState == VoiceState.SPEAKING
                    val micScale = if (isListeningOrSpeaking) micPulse else 1.0f

                    Box(
                        modifier = Modifier
                            .scale(micScale)
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isListeningOrSpeaking) AccentCyanBright else PrimaryPurpleLight,
                                        Color(0xFF3B82F6),
                                        PrimaryPurple
                                    )
                                )
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    listOf(AccentCyanBright, PrimaryPurpleLight)
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                val hasMicPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasMicPermission) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    assistantViewModel.onMicButtonClicked()
                                }
                            }
                            .testTag("home_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.voiceState == VoiceState.PROCESSING) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (uiState.voiceState == VoiceState.SPEAKING) {
                                    Icons.Default.Stop
                                } else {
                                    Icons.Default.Mic
                                },
                                contentDescription = "Microphone",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Type Shortcut Button
                    CircleActionButton(
                        icon = Icons.Default.Keyboard,
                        label = "Type",
                        onClick = { assistantViewModel.openTextInput() },
                        testTag = "home_type_button"
                    )
                }

                // Quick Prompt Chips Section (Matching reference mockup)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickPromptChip(
                            text = "Tell me a story",
                            icon = Icons.Default.Book,
                            onClick = { assistantViewModel.submitQuickPrompt("Tell me an engaging short story with a memorable message.") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickPromptChip(
                            text = "Explain a topic",
                            icon = Icons.Default.Lightbulb,
                            onClick = { assistantViewModel.submitQuickPrompt("Explain quantum computing in simple terms.") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickPromptChip(
                            text = "Help me learn English",
                            icon = Icons.Default.School,
                            onClick = { assistantViewModel.submitQuickPrompt("Give me 3 practical English conversation idioms with Bengali translation and examples.") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickPromptChip(
                            text = "Give me a coding example",
                            icon = Icons.Default.Code,
                            onClick = { assistantViewModel.submitQuickPrompt("Show me a clean Kotlin coroutines flow example with explanation.") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickPromptChip(
                            text = "কল সহকারী (Call)",
                            icon = Icons.Default.Call,
                            onClick = { assistantViewModel.openCallAssistant() },
                            modifier = Modifier.weight(1f)
                        )
                        QuickPromptChip(
                            text = "মেসেজ সহকারী (SMS)",
                            icon = Icons.Default.Chat,
                            onClick = { assistantViewModel.openMessageAssistant() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickPromptChip(
                            text = "ইউটিউব গান সার্চ",
                            icon = Icons.Default.PlayArrow,
                            onClick = { assistantViewModel.submitQuickPrompt("ইউটিউবে রবীন্দ্রসংগীত সার্চ করো") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickPromptChip(
                            text = "ক্যামেরা ওপেন",
                            icon = Icons.Default.CameraAlt,
                            onClick = { assistantViewModel.submitQuickPrompt("ক্যামেরা ওপেন করো") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    QuickPromptChip(
                        text = "Answer a general question",
                        icon = Icons.Default.QuestionMark,
                        onClick = { assistantViewModel.submitQuickPrompt("What are the most exciting breakthroughs in Artificial Intelligence this year?") },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }

            // Bottom Navigation Bar
            ZaraBottomNav(
                selectedTab = NavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.HOME -> {}
                        NavTab.HISTORY -> onNavigateHistory()
                        NavTab.SETTINGS -> onNavigateSettings()
                    }
                }
            )
        }

        // Text Input Bottom Sheet Overlay
        AnimatedVisibility(
            visible = uiState.showTextInputSheet,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TextInputSheet(
                onDismiss = { assistantViewModel.closeTextInput() },
                onSubmit = { text -> assistantViewModel.submitTextQuestion(text) }
            )
        }

        // Call Assistant Dialog Overlay
        AnimatedVisibility(
            visible = uiState.showCallAssistantDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CallAssistantDialog(
                callManager = assistantViewModel.callManager,
                ttsManager = assistantViewModel.ttsManager,
                onDismiss = { assistantViewModel.closeCallAssistant() }
            )
        }

        // Message Assistant Dialog Overlay
        AnimatedVisibility(
            visible = uiState.showMessageAssistantDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MessageAssistantDialog(
                messageManager = assistantViewModel.messageManager,
                automationManager = assistantViewModel.automationManager,
                ttsManager = assistantViewModel.ttsManager,
                onDismiss = { assistantViewModel.closeMessageAssistant() }
            )
        }
    }
}

@Composable
private fun SpeechBubbleCard(
    uiState: com.example.ui.state.AssistantUiState,
    onCopy: (String) -> Unit,
    onSpeak: () -> Unit,
    onStopSpeech: () -> Unit,
    onDismissError: () -> Unit,
    onCancelProcessing: () -> Unit,
    onRequestMicPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenApiKeySetup: () -> Unit,
    onOpenTextInput: () -> Unit,
    onRetryVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF131A33).copy(alpha = 0.90f),
                        Color(0xFF0F1528).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        PrimaryPurpleLight.copy(alpha = 0.6f),
                        AccentCyanBright.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("speech_bubble_card"),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Error State
            uiState.errorMessage != null -> {
                val isMicPermissionError = uiState.errorMessage.contains("Microphone", ignoreCase = true) ||
                        uiState.errorMessage.contains("মাইক্রোফোন", ignoreCase = true) ||
                        uiState.errorMessage.contains("permission", ignoreCase = true)

                val isApiError = uiState.errorMessage.contains("API", ignoreCase = true) ||
                        uiState.errorMessage.contains("key", ignoreCase = true) ||
                        uiState.errorMessage.contains("কী", ignoreCase = true) ||
                        uiState.errorMessage.contains("মডেল", ignoreCase = true) ||
                        uiState.errorMessage.contains("404") ||
                        uiState.errorMessage.contains("401") ||
                        uiState.errorMessage.contains("403")

                if (isMicPermissionError) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = AccentCyanBright,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "মাইক্রোফোন পারমিশন প্রয়োজন",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = onDismissError,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "জারার সাথে সরাসরি কথা বলতে এবং আপনার কাজগুলো করাতে মাইক্রোফোন পারমিশন চালু করুন।",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action buttons: [অনুমতি দিন] [অ্যাপ সেটিংস]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onRequestMicPermission,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bubble_allow_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "অনুমতি দিন",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenSettings,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bubble_open_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AccentCyanBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "অ্যাপ সেটিংস",
                                    color = AccentCyanBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenTextInput() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("bubble_type_fallback_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = PrimaryPurpleLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "অথবা কীবোর্ডে টাইপ করে কথা বলুন",
                                color = PrimaryPurpleLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (isApiError) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = AccentCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini API সমস্যা",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = onDismissError,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.errorMessage,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onOpenApiKeySetup,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bubble_open_api_key_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "API কী দিন",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = onRetryVoice,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bubble_retry_api_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AccentCyanBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "আবার চেষ্টা",
                                    fontSize = 12.5.sp,
                                    color = AccentCyanBright,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenTextInput() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = PrimaryPurpleLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "অথবা কীবোর্ডে টাইপ করে কথা বলুন",
                                color = PrimaryPurpleLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Error Occurred",
                                color = ErrorRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = onDismissError,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.errorMessage,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onRetryVoice,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "আবার চেষ্টা",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenTextInput,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AccentCyanBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "টাইপ করুন",
                                    fontSize = 12.5.sp,
                                    color = AccentCyanBright,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Listening State
            uiState.voiceState == VoiceState.LISTENING -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Listening...",
                        color = AccentCyanBright,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.recognizedTextPreview.isNotBlank()) {
                            "\"${uiState.recognizedTextPreview}\""
                        } else {
                            "Speak now in your chosen language"
                        },
                        color = TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Processing State
            uiState.voiceState == VoiceState.PROCESSING -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Thinking...",
                        color = PrimaryPurpleLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${uiState.userQuestion}\"",
                        color = TextSecondary,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Answer Displayed State
            uiState.aiResponse.isNotBlank() -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.userQuestion.isNotBlank()) {
                        Text(
                            text = "You: ${uiState.userQuestion}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = uiState.aiResponse,
                        color = TextPrimary,
                        fontSize = 14.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons: Speak/Stop & Copy
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (uiState.voiceState == VoiceState.SPEAKING) {
                                    onStopSpeech()
                                } else {
                                    onSpeak()
                                }
                            },
                            modifier = Modifier.size(34.dp).testTag("bubble_speak_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.voiceState == VoiceState.SPEAKING) {
                                    Icons.Default.Stop
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                },
                                contentDescription = if (uiState.voiceState == VoiceState.SPEAKING) "Stop" else "Speak",
                                tint = AccentCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { onCopy(uiState.aiResponse) },
                            modifier = Modifier.size(34.dp).testTag("bubble_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = PrimaryPurpleLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Default Initial State (Exactly matching reference mockup)
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hello!",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "How can I help you today?",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF131A33))
                .border(1.2.dp, BorderCard, CircleShape)
                .clickable(onClick = onClick)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.BorderCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun VoiceSettingsScreen(
    settingsViewModel: SettingsViewModel,
    assistantViewModel: AssistantViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPrefs by settingsViewModel.userPreferences.collectAsState()

    var speechRate by remember(userPrefs.speechRate) { mutableFloatStateOf(userPrefs.speechRate) }
    var speechPitch by remember(userPrefs.speechPitch) { mutableFloatStateOf(userPrefs.speechPitch) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080B16))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp)
            .testTag("voice_settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("voice_settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voice & Speech Settings",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Speech Speed Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F1528))
                        .border(1.dp, BorderCard, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Speech Speed (Rate)",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${"%.2f".format(speechRate)}x",
                                color = AccentCyanBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Slider(
                            value = speechRate,
                            onValueChange = { speechRate = it },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyanBright,
                                activeTrackColor = PrimaryPurple,
                                inactiveTrackColor = Color(0xFF1E284A)
                            ),
                            modifier = Modifier.testTag("speech_rate_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "0.5x Slow", color = TextMuted, fontSize = 11.5.sp)
                            Text(text = "1.0x Normal", color = TextMuted, fontSize = 11.5.sp)
                            Text(text = "2.0x Fast", color = TextMuted, fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Speech Pitch Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F1528))
                        .border(1.dp, BorderCard, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Voice Pitch",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${"%.2f".format(speechPitch)}x",
                                color = PrimaryPurpleLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Slider(
                            value = speechPitch,
                            onValueChange = { speechPitch = it },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryPurpleLight,
                                activeTrackColor = PrimaryPurple,
                                inactiveTrackColor = Color(0xFF1E284A)
                            ),
                            modifier = Modifier.testTag("speech_pitch_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "0.5x Low", color = TextMuted, fontSize = 11.5.sp)
                            Text(text = "1.0x Normal", color = TextMuted, fontSize = 11.5.sp)
                            Text(text = "2.0x High", color = TextMuted, fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Test Voice Button
                OutlinedButton(
                    onClick = {
                        assistantViewModel.ttsManager.setSpeechRate(speechRate)
                        assistantViewModel.ttsManager.setSpeechPitch(speechPitch)

                        val samplePhrase = if (userPrefs.selectedLanguage == "bn") {
                            "নমস্কার! আমি জারা এআই, আপনার ব্যক্তিগত ভয়েস অ্যাসিস্ট্যান্ট।"
                        } else {
                            "Hello! I am Zara AI, your personal voice assistant."
                        }
                        assistantViewModel.ttsManager.speak(samplePhrase)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyanBright),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("test_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = AccentCyanBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play Test Voice Sample",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Settings Button
            Button(
                onClick = {
                    settingsViewModel.setSpeechRate(speechRate)
                    settingsViewModel.setSpeechPitch(speechPitch)
                    Toast.makeText(context, "Voice settings saved", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_voice_settings_button")
            ) {
                Text(
                    text = "Save Voice Settings",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

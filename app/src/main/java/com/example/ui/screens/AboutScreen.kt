package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.BorderCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080B16))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .testTag("about_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "About Zara AI",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Identity Card
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, AccentCyanBright, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.zara_avatar),
                    contentDescription = "Zara AI Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Zara AI",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Your Personal Voice Assistant",
                color = PrimaryPurpleLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version 1.0.0 (Native Android Edition)",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Architecture Stack Cards
            TechStackCard(
                icon = Icons.Default.Psychology,
                title = "AI Intelligence",
                description = "Google Gemini 1.5 Flash API with multi-turn conversational context and bilingual support."
            )

            Spacer(modifier = Modifier.height(12.dp))

            TechStackCard(
                icon = Icons.Default.Mic,
                title = "Speech Recognition",
                description = "Native Android SpeechRecognizer with live RMS audio amplitude and partial word recognition."
            )

            Spacer(modifier = Modifier.height(12.dp))

            TechStackCard(
                icon = Icons.Default.VolumeUp,
                title = "Speech Synthesis (TTS)",
                description = "Android TextToSpeech engine with adjustable playback rate, pitch, and instant interrupt capability."
            )

            Spacer(modifier = Modifier.height(12.dp))

            TechStackCard(
                icon = Icons.Default.Storage,
                title = "Local Persistence",
                description = "Room SQLite Database for local conversation history and Jetpack DataStore for user preferences."
            )

            Spacer(modifier = Modifier.height(12.dp))

            TechStackCard(
                icon = Icons.Default.Code,
                title = "UI Framework",
                description = "100% Jetpack Compose with Material 3 design tokens, fluid animations, and custom glowing neon shaders."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Terms Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1528))
                    .border(1.dp, BorderCard, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentCyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Policy Commitment",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Zara AI strictly upholds Google Play Developer Program policies. No raw microphone recordings are ever saved or uploaded. All conversation history is maintained on-device in a private SQLite database.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TechStackCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F1528))
            .border(1.dp, BorderCard, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentCyanBright,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

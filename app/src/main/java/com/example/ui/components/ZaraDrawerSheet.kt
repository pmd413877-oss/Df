package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
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
fun ZaraDrawerSheet(
    onDismiss: () -> Unit,
    onNewConversation: () -> Unit,
    onOpenCallAssistant: () -> Unit,
    onOpenMessageAssistant: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateLanguage: () -> Unit,
    onNavigateApiKey: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .background(Color(0xFF0F1528))
                .border(1.dp, BorderCard)
                .clickable(enabled = false) {}
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
                .testTag("zara_drawer_sheet")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Header with close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, AccentCyanBright, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.zara_avatar),
                                    contentDescription = "Zara AI",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Zara AI",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Voice Assistant",
                                    color = AccentCyanBright,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp).testTag("close_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // New Chat Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryPurple, PrimaryPurpleLight)
                                )
                            )
                            .clickable {
                                onNewConversation()
                                onDismiss()
                            }
                            .padding(14.dp)
                            .testTag("drawer_new_chat"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Conversation",
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    DrawerMenuItem(
                        icon = Icons.Default.Call,
                        title = "কল সহকারী (Call Assistant)",
                        onClick = {
                            onDismiss()
                            onOpenCallAssistant()
                        },
                        testTag = "drawer_call_assistant"
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Chat,
                        title = "মেসেজ সহকারী (Message Center)",
                        onClick = {
                            onDismiss()
                            onOpenMessageAssistant()
                        },
                        testTag = "drawer_message_center"
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.History,
                        title = "Conversation History",
                        onClick = {
                            onDismiss()
                            onNavigateHistory()
                        },
                        testTag = "drawer_history"
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Language,
                        title = "Language Selection",
                        onClick = {
                            onDismiss()
                            onNavigateLanguage()
                        },
                        testTag = "drawer_language"
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Key,
                        title = "Gemini API Setup",
                        onClick = {
                            onDismiss()
                            onNavigateApiKey()
                        },
                        testTag = "drawer_api"
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Settings,
                        title = "All Settings",
                        onClick = {
                            onDismiss()
                            onNavigateSettings()
                        },
                        testTag = "drawer_settings"
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Info,
                        title = "About Zara AI",
                        onClick = {
                            onDismiss()
                            onNavigateAbout()
                        },
                        testTag = "drawer_about"
                    )
                }

                // Footer
                Column {
                    Text(
                        text = "Zara AI Voice Edition",
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = "Powered by Google Gemini",
                        color = PrimaryPurpleLight,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentCyanBright,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.MessageAssistantManager
import com.example.automation.SmartMessage
import com.example.automation.ZaraAutomationManager
import com.example.speech.TextToSpeechManager
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.BorderCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageAssistantDialog(
    messageManager: MessageAssistantManager,
    automationManager: ZaraAutomationManager,
    ttsManager: TextToSpeechManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by messageManager.messages.collectAsState()
    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080B16).copy(alpha = 0.96f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp)
            .testTag("message_assistant_dialog")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = AccentCyanBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মেসেজ সহকারী (Message Manager)",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_messages_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "জারা আপনার আগত বার্তাগুলো পড়ে শোনাবে এবং প্রয়োজনীয় উত্তর তৈরি করবে।",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageCardItem(
                        message = msg,
                        formattedTime = dateFormat.format(Date(msg.timestamp)),
                        onReadAloud = {
                            messageManager.markAsRead(msg.id)
                            val speechText = "${msg.sender} লিখেছেন: ${msg.content}"
                            ttsManager.speak(speechText)
                            Toast.makeText(context, "জারা বার্তাটি পড়ছে...", Toast.LENGTH_SHORT).show()
                        },
                        onSendReply = {
                            automationManager.composeSms("01700000000", msg.suggestedReply)
                            Toast.makeText(context, "রিপ্লাই ড্রাফট করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simulate incoming test message action
            OutlinedButton(
                onClick = {
                    messageManager.addIncomingMessage(
                        sender = "বস (Office)",
                        content = "জরুরি: ক্লায়েন্ট আগামীকাল সকাল ১০টায় জুম মিটিং চাচ্ছে। প্রস্তুত থেকো।",
                        suggestedReply = "হ্যাঁ স্যার, আমি সব প্রেজেন্টেশন ফাইল রেডি রাখছি।"
                    )
                    Toast.makeText(context, "নতুন টেস্ট মেসেজ যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyanBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("নতুন মেসেজ সিমুলেট করুন", fontSize = 13.5.sp)
            }
        }
    }
}

@Composable
private fun MessageCardItem(
    message: SmartMessage,
    formattedTime: String,
    onReadAloud: () -> Unit,
    onSendReply: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F1528))
            .border(1.dp, BorderCard, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender,
                    color = AccentCyanBright,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedTime,
                    color = TextMuted,
                    fontSize = 11.5.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.content,
                color = TextPrimary,
                fontSize = 13.5.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Read Aloud and Reply with AI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReadAloud,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2347)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Read",
                        tint = AccentCyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "পড়ে শোনাও",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onSendReply,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Reply",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI রিপ্লাই পাঠাও",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

package com.example.automation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SmartMessage(
    val id: Long,
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val suggestedReply: String = ""
)

class MessageAssistantManager {

    private val sampleMessages = listOf(
        SmartMessage(
            id = 1,
            sender = "মা (Home)",
            content = "তুমি কখন বাসায় ফিরবে? রাতের খাবার কী খাবে জানাও।",
            suggestedReply = "মা, আমি ১ ঘণ্টার মধ্যে আসছি। সাধারণ খাবার হলেই চলবে।"
        ),
        SmartMessage(
            id = 2,
            sender = "Bank Alert",
            content = "আপনার অ্যাকাউন্টে ৫০,০০০ টাকা জমা হয়েছে। বর্তমান ব্যালেন্স চেক করতে লগইন করুন।",
            suggestedReply = "ধন্যবাদ, আপডেট পেয়েছি।"
        ),
        SmartMessage(
            id = 3,
            sender = "অফিস টিম (সিয়াম)",
            content = "জারা, ক্লায়েন্ট প্রেজেন্টেশনের স্লাইডগুলো কি রেডি হয়েছে?",
            suggestedReply = "হ্যাঁ সিয়াম ভাই, সব স্লাইড রেডি করা আছে। ইমেইলে পাঠিয়ে দিচ্ছি।"
        )
    )

    private val _messages = MutableStateFlow(sampleMessages)
    val messages: StateFlow<List<SmartMessage>> = _messages.asStateFlow()

    fun markAsRead(id: Long) {
        _messages.value = _messages.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun addIncomingMessage(sender: String, content: String, suggestedReply: String = "ধন্যবাদ, বার্তা পেয়েছি।") {
        val newMsg = SmartMessage(
            id = System.currentTimeMillis(),
            sender = sender,
            content = content,
            suggestedReply = suggestedReply
        )
        _messages.value = listOf(newMsg) + _messages.value
    }
}

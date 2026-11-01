package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userQuestion: String,
    val aiResponse: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en",
    val isVoice: Boolean = true,
    val durationMs: Long = 0L
)

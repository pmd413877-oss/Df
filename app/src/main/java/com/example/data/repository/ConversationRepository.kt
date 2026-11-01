package com.example.data.repository

import com.example.data.local.dao.ConversationDao
import com.example.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversationDao: ConversationDao) {

    val allConversations: Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    val conversationCount: Flow<Int> =
        conversationDao.getConversationCount()

    suspend fun saveConversation(
        question: String,
        answer: String,
        language: String,
        isVoice: Boolean,
        durationMs: Long = 0L
    ): Long {
        val entity = ConversationEntity(
            userQuestion = question,
            aiResponse = answer,
            timestamp = System.currentTimeMillis(),
            language = language,
            isVoice = isVoice,
            durationMs = durationMs
        )
        return conversationDao.insertConversation(entity)
    }

    suspend fun deleteById(id: Long) {
        conversationDao.deleteById(id)
    }

    suspend fun clearAll() {
        conversationDao.clearAll()
    }

    fun search(query: String): Flow<List<ConversationEntity>> {
        return conversationDao.searchConversations(query)
    }
}

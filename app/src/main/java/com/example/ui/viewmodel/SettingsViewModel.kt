package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.network.GeminiClient
import com.example.data.network.GeminiResult
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.UserPreferences
import com.example.data.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val conversationDao = AppDatabase.getInstance(application).conversationDao()
    private val conversationRepository = ConversationRepository(conversationDao)
    private val geminiClient = GeminiClient()

    val userPreferences: StateFlow<UserPreferences> =
        preferencesRepository.userPreferencesFlow
            .let { flow ->
                val state = MutableStateFlow(UserPreferences())
                viewModelScope.launch {
                    flow.collect { state.value = it }
                }
                state.asStateFlow()
            }

    private val _testConnectionState = MutableStateFlow<GeminiResult?>(null)
    val testConnectionState: StateFlow<GeminiResult?> = _testConnectionState.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            preferencesRepository.saveApiKey(apiKey)
        }
    }

    fun deleteApiKey() {
        viewModelScope.launch {
            preferencesRepository.deleteApiKey()
            _testConnectionState.value = null
        }
    }

    fun testConnection(apiKey: String, model: String = "gemini-2.5-flash") {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testConnectionState.value = null
            val resolvedKey = geminiClient.resolveApiKey(apiKey)
            val result = geminiClient.testConnection(resolvedKey, model)
            _testConnectionState.value = result
            _isTestingConnection.value = false
        }
    }

    fun clearTestConnectionState() {
        _testConnectionState.value = null
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(languageCode)
        }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            preferencesRepository.setSpeechRate(rate)
        }
    }

    fun setSpeechPitch(pitch: Float) {
        viewModelScope.launch {
            preferencesRepository.setSpeechPitch(pitch)
        }
    }

    fun setAutoSpeak(autoSpeak: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSpeak(autoSpeak)
        }
    }

    fun setSelectedVoiceName(voiceName: String) {
        viewModelScope.launch {
            preferencesRepository.setSelectedVoiceName(voiceName)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            conversationRepository.clearAll()
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            preferencesRepository.resetAllSettings()
        }
    }

    fun completeWelcome() {
        viewModelScope.launch {
            preferencesRepository.setWelcomeCompleted(true)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }
}

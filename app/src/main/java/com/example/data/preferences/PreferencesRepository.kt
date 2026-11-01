package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zara_user_preferences")

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val HAS_COMPLETED_WELCOME = booleanPreferencesKey("has_completed_welcome")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val API_KEY = stringPreferencesKey("gemini_api_key")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
        val SELECTED_VOICE_NAME = stringPreferencesKey("selected_voice_name")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            hasCompletedWelcome = preferences[PreferencesKeys.HAS_COMPLETED_WELCOME] ?: false,
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
            apiKey = preferences[PreferencesKeys.API_KEY] ?: "",
            selectedLanguage = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "bn",
            speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
            speechPitch = preferences[PreferencesKeys.SPEECH_PITCH] ?: 1.0f,
            autoSpeak = preferences[PreferencesKeys.AUTO_SPEAK] ?: true,
            selectedVoiceName = preferences[PreferencesKeys.SELECTED_VOICE_NAME] ?: "",
            selectedModel = preferences[PreferencesKeys.SELECTED_MODEL]?.let {
                if (it == "gemini-2.5-flash" || it.isBlank()) "gemini-1.5-flash" else it
            } ?: "gemini-1.5-flash"
        )
    }

    suspend fun setWelcomeCompleted(completed: Boolean = true) {
        context.dataStore.edit { it[PreferencesKeys.HAS_COMPLETED_WELCOME] = completed }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { it[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { it[PreferencesKeys.API_KEY] = apiKey.trim() }
    }

    suspend fun deleteApiKey() {
        context.dataStore.edit { it.remove(PreferencesKeys.API_KEY) }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_LANGUAGE] = languageCode }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { it[PreferencesKeys.SPEECH_RATE] = rate }
    }

    suspend fun setSpeechPitch(pitch: Float) {
        context.dataStore.edit { it[PreferencesKeys.SPEECH_PITCH] = pitch }
    }

    suspend fun setAutoSpeak(autoSpeak: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_SPEAK] = autoSpeak }
    }

    suspend fun setSelectedVoiceName(voiceName: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_VOICE_NAME] = voiceName }
    }

    suspend fun setSelectedModel(model: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_MODEL] = model }
    }

    suspend fun resetAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SELECTED_LANGUAGE)
            preferences.remove(PreferencesKeys.SPEECH_RATE)
            preferences.remove(PreferencesKeys.SPEECH_PITCH)
            preferences.remove(PreferencesKeys.AUTO_SPEAK)
            preferences.remove(PreferencesKeys.SELECTED_VOICE_NAME)
        }
    }
}

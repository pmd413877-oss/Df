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
import com.example.speech.SpeechRecognizerManager
import com.example.speech.TextToSpeechManager
import com.example.ui.state.AssistantUiState
import com.example.ui.state.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val conversationDao = AppDatabase.getInstance(application).conversationDao()
    private val conversationRepository = ConversationRepository(conversationDao)
    private val geminiClient = GeminiClient()
    val speechManager = SpeechRecognizerManager(application)
    val ttsManager = TextToSpeechManager(application)
    val automationManager = com.example.automation.ZaraAutomationManager(application)
    val callManager = com.example.automation.CallAssistantManager()
    val messageManager = com.example.automation.MessageAssistantManager()

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    val userPreferences = preferencesRepository.userPreferencesFlow

    private var activeGeminiJob: Job? = null
    private var recentTurns = mutableListOf<Pair<String, String>>()

    init {
        // Collect preferences to configure TTS and current language
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                ttsManager.setSpeechRate(prefs.speechRate)
                ttsManager.setSpeechPitch(prefs.speechPitch)
                ttsManager.setLanguage(prefs.selectedLanguage)
                if (prefs.selectedVoiceName.isNotBlank()) {
                    ttsManager.setVoiceByName(prefs.selectedVoiceName)
                }
                val resolvedKey = geminiClient.resolveApiKey(prefs.apiKey)
                _uiState.update {
                    it.copy(
                        isApiKeyConfigured = resolvedKey.isNotBlank(),
                        languageCode = prefs.selectedLanguage,
                        autoSpeak = prefs.autoSpeak
                    )
                }
            }
        }

        // Collect speech recognition events
        viewModelScope.launch {
            speechManager.speechState.collect { state ->
                when (state) {
                    is SpeechRecognizerManager.SpeechState.Idle -> {
                        if (_uiState.value.voiceState == VoiceState.LISTENING) {
                            _uiState.update { it.copy(voiceState = VoiceState.IDLE, audioAmplitude = 0f) }
                        }
                    }
                    is SpeechRecognizerManager.SpeechState.Ready -> {
                        _uiState.update {
                            it.copy(
                                voiceState = VoiceState.LISTENING,
                                recognizedTextPreview = "",
                                errorMessage = null
                            )
                        }
                    }
                    is SpeechRecognizerManager.SpeechState.Listening -> {
                        _uiState.update {
                            it.copy(
                                voiceState = VoiceState.LISTENING,
                                recognizedTextPreview = state.partialText,
                                audioAmplitude = state.amplitude
                            )
                        }
                    }
                    is SpeechRecognizerManager.SpeechState.Result -> {
                        val text = state.text
                        _uiState.update {
                            it.copy(
                                voiceState = VoiceState.PROCESSING,
                                recognizedTextPreview = text,
                                audioAmplitude = 0f
                            )
                        }
                        processQuestion(text, isVoice = true)
                    }
                    is SpeechRecognizerManager.SpeechState.Error -> {
                        _uiState.update {
                            it.copy(
                                voiceState = VoiceState.ERROR,
                                errorMessage = state.message,
                                audioAmplitude = 0f
                            )
                        }
                    }
                }
            }
        }

        // Collect TTS speaking state to update UI
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    _uiState.update { it.copy(voiceState = VoiceState.SPEAKING) }
                } else {
                    if (_uiState.value.voiceState == VoiceState.SPEAKING) {
                        _uiState.update { it.copy(voiceState = VoiceState.IDLE) }
                    }
                }
            }
        }
    }

    fun setMicPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(isMicPermissionGranted = granted) }
    }

    fun onMicButtonClicked() {
        when (_uiState.value.voiceState) {
            VoiceState.IDLE, VoiceState.ERROR -> {
                ttsManager.stop()
                val lang = _uiState.value.languageCode
                speechManager.startListening(lang)
            }
            VoiceState.LISTENING -> {
                speechManager.stopListening()
            }
            VoiceState.SPEAKING -> {
                stopSpeaking()
            }
            VoiceState.PROCESSING -> {
                cancelProcessing()
            }
        }
    }

    fun cancelListening() {
        speechManager.cancel()
        _uiState.update { it.copy(voiceState = VoiceState.IDLE, audioAmplitude = 0f) }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _uiState.update { it.copy(voiceState = VoiceState.IDLE) }
    }

    fun cancelProcessing() {
        activeGeminiJob?.cancel()
        activeGeminiJob = null
        _uiState.update { it.copy(voiceState = VoiceState.IDLE) }
    }

    fun openTextInput() {
        ttsManager.stop()
        speechManager.cancel()
        _uiState.update { it.copy(showTextInputSheet = true) }
    }

    fun closeTextInput() {
        _uiState.update { it.copy(showTextInputSheet = false) }
    }

    fun submitTextQuestion(question: String) {
        closeTextInput()
        ttsManager.stop()
        processQuestion(question, isVoice = false)
    }

    fun submitQuickPrompt(promptText: String) {
        ttsManager.stop()
        speechManager.cancel()
        processQuestion(promptText, isVoice = false)
    }

    fun speakCurrentResponse() {
        val response = _uiState.value.aiResponse
        if (response.isNotBlank()) {
            ttsManager.speak(response)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(voiceState = VoiceState.IDLE, errorMessage = null) }
    }

    fun openCallAssistant() {
        ttsManager.stop()
        speechManager.cancel()
        callManager.triggerSimulatedCall()
        _uiState.update { it.copy(showCallAssistantDialog = true) }
    }

    fun closeCallAssistant() {
        callManager.endCall()
        _uiState.update { it.copy(showCallAssistantDialog = false) }
    }

    fun openMessageAssistant() {
        ttsManager.stop()
        speechManager.cancel()
        _uiState.update { it.copy(showMessageAssistantDialog = true) }
    }

    fun closeMessageAssistant() {
        _uiState.update { it.copy(showMessageAssistantDialog = false) }
    }

    private fun handleDirectAction(
        question: String,
        confirmationText: String,
        isVoice: Boolean,
        action: () -> Unit
    ) {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            conversationRepository.saveConversation(
                question = question,
                answer = confirmationText,
                language = prefs.selectedLanguage,
                isVoice = isVoice,
                durationMs = 250L
            )

            _uiState.update {
                it.copy(
                    voiceState = VoiceState.IDLE,
                    userQuestion = question,
                    aiResponse = confirmationText,
                    errorMessage = null
                )
            }

            if (prefs.autoSpeak) {
                ttsManager.speak(confirmationText)
            }

            // Execute the intent
            action()
        }
    }

    private fun processQuestion(question: String, isVoice: Boolean) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return

        val lower = trimmed.lowercase()

        // 1. YouTube Search Automation
        if (lower.contains("ইউটিউব") || lower.contains("youtube")) {
            val query = trimmed
                .replace("ইউটিউবে", "", ignoreCase = true)
                .replace("ইউটিউব", "", ignoreCase = true)
                .replace("সার্চ করো", "", ignoreCase = true)
                .replace("সার্চ", "", ignoreCase = true)
                .replace("চালাও", "", ignoreCase = true)
                .replace("দেখাও", "", ignoreCase = true)
                .replace("search youtube for", "", ignoreCase = true)
                .replace("on youtube", "", ignoreCase = true)
                .trim()
            val finalQuery = if (query.isNotBlank()) query else "Trending"
            val confirmation = "অবশ্যই, আমি ইউটিউবে '$finalQuery' সার্চ করে দিচ্ছি।"
            handleDirectAction(trimmed, confirmation, isVoice) {
                automationManager.searchYouTube(finalQuery)
            }
            return
        }

        // 2. Web Search Automation
        if ((lower.contains("সার্চ") && (lower.contains("গুগল") || lower.contains("ওয়েব") || lower.contains("web"))) || lower.startsWith("search ")) {
            val query = trimmed
                .replace("গুগলে", "", ignoreCase = true)
                .replace("গুগল", "", ignoreCase = true)
                .replace("ওয়েবে", "", ignoreCase = true)
                .replace("সার্চ করো", "", ignoreCase = true)
                .replace("search web for", "", ignoreCase = true)
                .replace("search for", "", ignoreCase = true)
                .replace("search", "", ignoreCase = true)
                .trim()
            val finalQuery = if (query.isNotBlank()) query else "Google AI"
            val confirmation = "গুগলে '$finalQuery' অনুসন্ধান করা হচ্ছে..."
            handleDirectAction(trimmed, confirmation, isVoice) {
                automationManager.searchWeb(finalQuery)
            }
            return
        }

        // 3. App Launch Automation
        if (lower.contains("ওপেন করো") || lower.contains("খুলো") || lower.contains("খোলো") || lower.contains("চালু করো") || lower.startsWith("open ")) {
            val appTarget = trimmed
                .replace("ওপেন করো", "", ignoreCase = true)
                .replace("খোলো", "", ignoreCase = true)
                .replace("খুলো", "", ignoreCase = true)
                .replace("চালু করো", "", ignoreCase = true)
                .replace("open app", "", ignoreCase = true)
                .replace("open", "", ignoreCase = true)
                .replace("অ্যাপ", "", ignoreCase = true)
                .trim()
            val confirmation = "$appTarget অ্যাপটি চালু করা হচ্ছে।"
            handleDirectAction(trimmed, confirmation, isVoice) {
                automationManager.openApp(appTarget)
            }
            return
        }

        // 4. App Install Automation
        if (lower.contains("ইনস্টল") || lower.contains("install")) {
            val appTarget = trimmed
                .replace("ইনস্টল করো", "", ignoreCase = true)
                .replace("ইনস্টল", "", ignoreCase = true)
                .replace("install", "", ignoreCase = true)
                .replace("অ্যাপ", "", ignoreCase = true)
                .trim()
            val confirmation = "গুগল প্লে স্টোরে '$appTarget' অ্যাপ খোঁজা হচ্ছে।"
            handleDirectAction(trimmed, confirmation, isVoice) {
                automationManager.installApp(appTarget)
            }
            return
        }

        // 5. Call Screening / Call Management
        if (lower.contains("কল সহকারী") || lower.contains("কল ম্যানেজমেন্ট") || lower.contains("ইনকামিং কল") || lower.contains("call assistant")) {
            val confirmation = "কল সহকারী চালু করা হচ্ছে। আপনার ইনকামিং কল স্ক্রিনিং প্রস্তুত।"
            handleDirectAction(trimmed, confirmation, isVoice) {
                openCallAssistant()
            }
            return
        }

        // 6. Message Management
        if (lower.contains("মেসেজ সহকারী") || lower.contains("মেসেজ পড়ো") || lower.contains("বার্তা দেখাও") || lower.contains("message assistant")) {
            val confirmation = "মেসেজ সহকারী ওপেন করা হচ্ছে। বার্তাগুলো পড়ে শোনানো হচ্ছে।"
            handleDirectAction(trimmed, confirmation, isVoice) {
                openMessageAssistant()
            }
            return
        }

        val startTime = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                voiceState = VoiceState.PROCESSING,
                userQuestion = trimmed,
                errorMessage = null
            )
        }

        activeGeminiJob = viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val result = geminiClient.askGemini(
                question = trimmed,
                userApiKey = prefs.apiKey,
                languageCode = prefs.selectedLanguage,
                recentHistory = recentTurns,
                model = prefs.selectedModel
            )

            when (result) {
                is GeminiResult.Success -> {
                    val answer = result.text
                    val duration = System.currentTimeMillis() - startTime

                    // Save to Room DB
                    conversationRepository.saveConversation(
                        question = trimmed,
                        answer = answer,
                        language = prefs.selectedLanguage,
                        isVoice = isVoice,
                        durationMs = duration
                    )

                    recentTurns.add(trimmed to answer)
                    if (recentTurns.size > 8) {
                        recentTurns.removeAt(0)
                    }

                    _uiState.update {
                        it.copy(
                            voiceState = VoiceState.IDLE,
                            aiResponse = answer,
                            errorMessage = null
                        )
                    }

                    // Auto-speak response if enabled
                    if (prefs.autoSpeak) {
                        ttsManager.speak(answer)
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            voiceState = VoiceState.ERROR,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeGeminiJob?.cancel()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}

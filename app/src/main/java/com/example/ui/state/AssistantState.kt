package com.example.ui.state

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class AssistantUiState(
    val voiceState: VoiceState = VoiceState.IDLE,
    val userQuestion: String = "",
    val aiResponse: String = "",
    val recognizedTextPreview: String = "",
    val audioAmplitude: Float = 0f,
    val errorMessage: String? = null,
    val isMicPermissionGranted: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val showTextInputSheet: Boolean = false,
    val showCallAssistantDialog: Boolean = false,
    val showMessageAssistantDialog: Boolean = false,
    val isApiKeyConfigured: Boolean = false,
    val languageCode: String = "bn",
    val autoSpeak: Boolean = true
)

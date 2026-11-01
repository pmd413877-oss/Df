package com.example.data.preferences

data class UserPreferences(
    val hasCompletedWelcome: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val apiKey: String = "",
    val selectedLanguage: String = "bn", // Default to Bengali as featured in prompt, with instant selector
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val autoSpeak: Boolean = true,
    val selectedVoiceName: String = "",
    val selectedModel: String = "gemini-1.5-flash"
)

data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val speechLocaleTag: String
)

val AVAILABLE_LANGUAGES = listOf(
    SupportedLanguage("bn", "Bengali", "বাংলা", "🇧🇩", "bn-BD"),
    SupportedLanguage("en", "English", "English", "🇺🇸", "en-US"),
    SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳", "hi-IN"),
    SupportedLanguage("ar", "Arabic", "العربية", "🇸🇦", "ar-SA"),
    SupportedLanguage("es", "Spanish", "Español", "🇪🇸", "es-ES")
)

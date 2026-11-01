package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object ApiKeySetup : Screen("api_key_setup")
    object MicrophonePermission : Screen("microphone_permission")
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")
    object LanguageSelection : Screen("language_selection")
    object VoiceSettings : Screen("voice_settings")
    object About : Screen("about")
}

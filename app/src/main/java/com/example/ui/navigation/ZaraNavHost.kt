package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.ZaraDrawerSheet
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ApiKeySetupScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.MicrophonePermissionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceSettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun ZaraNavHost(
    assistantViewModel: AssistantViewModel,
    settingsViewModel: SettingsViewModel,
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val userPrefs by settingsViewModel.userPreferences.collectAsState()
    var isDrawerOpen by remember { mutableStateOf(false) }

    val startDestination = when {
        !userPrefs.hasCompletedWelcome -> Screen.Welcome.route
        !userPrefs.hasCompletedOnboarding -> Screen.Onboarding.route
        else -> Screen.Home.route
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = {
                        settingsViewModel.completeWelcome()
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        settingsViewModel.completeOnboarding()
                        // Check if mic permission needed or go to permission screen
                        navController.navigate(Screen.MicrophonePermission.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.MicrophonePermission.route) {
                MicrophonePermissionScreen(
                    onPermissionGranted = {
                        assistantViewModel.setMicPermissionGranted(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.MicrophonePermission.route) { inclusive = true }
                        }
                    },
                    onContinueWithoutPermission = {
                        assistantViewModel.setMicPermissionGranted(false)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.MicrophonePermission.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    assistantViewModel = assistantViewModel,
                    onNavigateHistory = { navController.navigate(Screen.History.route) },
                    onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenDrawer = { isDrawerOpen = true }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    historyViewModel = historyViewModel,
                    assistantViewModel = assistantViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateHistory = { navController.navigate(Screen.History.route) },
                    onNavigateLanguage = { navController.navigate(Screen.LanguageSelection.route) },
                    onNavigateVoice = { navController.navigate(Screen.VoiceSettings.route) },
                    onNavigateApiKey = { navController.navigate(Screen.ApiKeySetup.route) },
                    onNavigateAbout = { navController.navigate(Screen.About.route) }
                )
            }

            composable(Screen.ApiKeySetup.route) {
                ApiKeySetupScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LanguageSelection.route) {
                LanguageSelectionScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VoiceSettings.route) {
                VoiceSettingsScreen(
                    settingsViewModel = settingsViewModel,
                    assistantViewModel = assistantViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Side Drawer Sheet
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ZaraDrawerSheet(
                onDismiss = { isDrawerOpen = false },
                onNewConversation = {
                    assistantViewModel.clearError()
                    assistantViewModel.stopSpeaking()
                },
                onOpenCallAssistant = {
                    assistantViewModel.openCallAssistant()
                },
                onOpenMessageAssistant = {
                    assistantViewModel.openMessageAssistant()
                },
                onNavigateHistory = { navController.navigate(Screen.History.route) },
                onNavigateLanguage = { navController.navigate(Screen.LanguageSelection.route) },
                onNavigateApiKey = { navController.navigate(Screen.ApiKeySetup.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateAbout = { navController.navigate(Screen.About.route) }
            )
        }
    }
}

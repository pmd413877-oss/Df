package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ui.navigation.ZaraNavHost
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val assistantViewModel: AssistantViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check initial microphone permission state
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        assistantViewModel.setMicPermissionGranted(hasMicPermission)

        setContent {
            MyApplicationTheme {
                ZaraNavHost(
                    assistantViewModel = assistantViewModel,
                    settingsViewModel = settingsViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        assistantViewModel.setMicPermissionGranted(hasMicPermission)
        if (hasMicPermission && assistantViewModel.uiState.value.errorMessage?.contains("Microphone", ignoreCase = true) == true) {
            assistantViewModel.clearError()
        }
    }

    override fun onPause() {
        super.onPause()
        assistantViewModel.ttsManager.stop()
        assistantViewModel.speechManager.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        assistantViewModel.ttsManager.shutdown()
        assistantViewModel.speechManager.destroy()
    }
}

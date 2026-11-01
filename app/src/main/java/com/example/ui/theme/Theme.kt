package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZaraDarkColorScheme = darkColorScheme(
    primary = PrimaryPurpleLight,
    onPrimary = Color.White,
    primaryContainer = AccentViolet,
    onPrimaryContainer = Color.White,
    secondary = AccentCyanBright,
    onSecondary = Color.Black,
    secondaryContainer = BackgroundCardElevated,
    onSecondaryContainer = AccentCyanBright,
    tertiary = AccentBlue,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundCard,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderCard,
    outlineVariant = BorderSubtle,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Zara AI is dark theme first
    dynamicColor: Boolean = false, // Keep signature Zara AI purple-blue aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZaraDarkColorScheme,
        typography = Typography,
        content = content
    )
}

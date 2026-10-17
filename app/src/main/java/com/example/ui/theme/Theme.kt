package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberViolet,
    onPrimary = Color.White,
    primaryContainer = CyberVioletContainer,
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = CyberCyan,
    onSecondary = Color(0xFF00382E),
    secondaryContainer = CyberCyanContainer,
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}

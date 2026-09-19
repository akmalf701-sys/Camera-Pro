package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CameraColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    secondary = AmberAccent,
    onSecondary = Color.Black,
    tertiary = EmeraldGreen,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CameraColorScheme,
        typography = Typography,
        content = content
    )
}


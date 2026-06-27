package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentGlow,
    tertiary = NeonGreen,
    background = DeepDarkBg,
    surface = GlassBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextSlate100,
    onSurface = TextSlate400,
    surfaceVariant = GlassBg,
    onSurfaceVariant = TextSlate100
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

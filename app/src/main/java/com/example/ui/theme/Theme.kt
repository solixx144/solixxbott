package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SolixxDarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = AccentPurpleContainer,
    onPrimaryContainer = AccentPurpleLight,
    secondary = AccentPurpleLight,
    onSecondary = BackgroundNearBlack,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentIndigo,
    onTertiary = Color.White,
    background = BackgroundNearBlack,
    onBackground = TextPrimary,
    surface = SurfaceDarkCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    surfaceTint = AccentPurple,
    outline = BorderSubtle,
    outlineVariant = Color(0xFF1F212D),
    error = AccentRose,
    onError = Color.White,
    errorContainer = AccentRoseContainer,
    onErrorContainer = Color(0xFFFF8998)
)

@Composable
fun SolixxBotTheme(
    darkTheme: Boolean = true, // SolixxBot is designed for a premium dark interface
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SolixxDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SolixxBotTheme(darkTheme = true, content = content)
}

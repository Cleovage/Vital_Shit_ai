package com.example.vitaai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GlebPurple,
    secondary = GlebEmerald,
    tertiary = GlebAmber,
    background = InkBlack,
    surface = InkDeep,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = GlebRose,
    onError = Color.White,
    outlineVariant = GlebSlate800,
    surfaceVariant = InkMedium,
    primaryContainer = GlebPurple.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White
)

// Legacy / Compatibility mapping for old code
val Primary = GlebPurple
val Secondary = GlebEmerald
val Tertiary = GlebAmber
val Error = GlebRose
val Background = InkBlack
val OnBackground = Color.White
val OnPrimary = Color.White
val OnSurfaceVariant = GlebSlate400
val OutlineVariant = GlebSlate800
val PrimaryContainer = GlebPurple.copy(alpha = 0.2f)
val GlowPrimary = GlebPurple.copy(alpha = 0.4f)
val SurfaceContainer = InkMedium
val SurfaceContainerHigh = InkMedium
val SurfaceContainerHighest = InkDeep
val Slate400 = GlebSlate400
val Slate500 = GlebSlate400
val Slate700 = GlebSlate600
val Slate900 = Color.White

@Composable
fun VitaAITheme(
    content: @Composable () -> Unit
) {
    val glebColors = GlebColors()

    CompositionLocalProvider(
        LocalGlebColors provides glebColors
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = VitaTypography,
            content = content
        )
    }
}

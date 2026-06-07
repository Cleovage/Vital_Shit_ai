package com.example.vitaai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── GLEB KUZNETSOV - ATMOSPHERIC INK PALETTE ───

val InkBlack = Color(0xFF0A0A0A)
val InkDeep = Color(0xFF121212)
val InkMedium = Color(0xFF1A1A1A)

// Vibrant Accents
val GlebPurple = Color(0xFF6366F1)
val GlebEmerald = Color(0xFF10B981)
val GlebAmber = Color(0xFFF59E0B)
val GlebBlue = Color(0xFF3B82F6)
val GlebCyan = Color(0xFF06B6D4)
val GlebRose = Color(0xFFF43F5E)

// Neutrals
val GlebSlate400 = Color(0xFF94A3B8)
val GlebSlate600 = Color(0xFF475569)
val GlebSlate800 = Color(0xFF1E293B)
val GlebCream = Color(0xFFFFFAF0)

@Immutable
data class GlebColors(
    val background: Color = InkBlack,
    val surface: Color = InkDeep,
    val cardFill: Color = Color.White.copy(alpha = 0.05f),
    val cardBorderSharp: Color = Color.White.copy(alpha = 0.15f),
    val cardBorderSoft: Color = Color.White.copy(alpha = 0.02f),
    val textPrimary: Color = Color.White,
    val textSecondary: Color = GlebSlate400,
    val accentPrimary: Color = GlebPurple,
    val accentSecondary: Color = GlebEmerald,
    val auraGlow: Color = GlebPurple.copy(alpha = 0.15f),
    
    // Legacy compatibility aliases
    val glassFill: Color = Color.White.copy(alpha = 0.05f),
    val glassBorderLight: Color = Color.White.copy(alpha = 0.15f),
    val glassBorderDark: Color = Color.White.copy(alpha = 0.02f)
)

val LocalGlebColors = staticCompositionLocalOf { GlebColors() }
val LocalVitaColors = LocalGlebColors

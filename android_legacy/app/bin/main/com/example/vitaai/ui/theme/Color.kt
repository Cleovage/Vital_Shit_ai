package com.example.vitaai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Primary (Cyan 500)
val Primary = Color(0xFF06B6D4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF06B6D4).copy(alpha = 0.1f)
val OnPrimaryContainer = Color(0xFF06B6D4)
val InversePrimary = Color(0xFF06B6D4)
val PrimaryFixed = Color(0xFF06B6D4)
val PrimaryFixedDim = Color(0xFF06B6D4)
val OnPrimaryFixed = Color(0xFFFFFFFF)
val OnPrimaryFixedVariant = Color(0xFF06B6D4)

// Secondary (Blue 500)
val Secondary = Color(0xFF3B82F6)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF3B82F6).copy(alpha = 0.1f)
val OnSecondaryContainer = Color(0xFF3B82F6)
val SecondaryFixed = Color(0xFF3B82F6)
val SecondaryFixedDim = Color(0xFF3B82F6)
val OnSecondaryFixed = Color(0xFFFFFFFF)
val OnSecondaryFixedVariant = Color(0xFF3B82F6)

// Tertiary (Rose 500)
val Tertiary = Color(0xFFF43F5E)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFF43F5E).copy(alpha = 0.1f)
val OnTertiaryContainer = Color(0xFFF43F5E)
val TertiaryFixed = Color(0xFFF43F5E)
val TertiaryFixedDim = Color(0xFFF43F5E)
val OnTertiaryFixed = Color(0xFFFFFFFF)
val OnTertiaryFixedVariant = Color(0xFFF43F5E)

// Error (High Contrast Alert Red)
val Error = Color(0xFFEF4444)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFEF4444).copy(alpha = 0.1f)
val OnErrorContainer = Color(0xFFEF4444)

// Surface & Background (Pure White Canvas)
val Background = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF0F172A) // Slate 900
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF0F172A)
val SurfaceDim = Color(0xFFF8FAFC)
val SurfaceBright = Color(0xFFFFFFFF)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF8FAFC)
val SurfaceContainer = Color(0xFFF1F5F9) // slate-100
val SurfaceContainerHigh = Color(0xFFE2E8F0) // slate-200
val SurfaceContainerHighest = Color(0xFFCBD5E1) // slate-300
val SurfaceVariant = Color(0xFFF1F5F9)
val OnSurfaceVariant = Color(0xFF475569) // slate-600
val InverseSurface = Color(0xFF0F172A)
val InverseOnSurface = Color(0xFFFFFFFF)
val SurfaceTint = Color(0xFF06B6D4)

// Outline (Technical borders)
val Outline = Color(0x12000000) // 7% black border
val OutlineVariant = Color(0x0F000000)

// Custom Glassmorphic Tokens
val ApexGlow = Color(0xFF0F172A).copy(alpha = 0.08f)
val ApexBorder = Color(0xFF0F172A).copy(alpha = 0.07f)
val GlassFill = Color(0xFFFFFFFF).copy(alpha = 0.78f)
val GlassBorderLight = Color(0x12000000)
val GlassBorderDark = Color(0x12000000)
val GlowPrimary = Color(0xFF06B6D4).copy(alpha = 0.08f)
val GlowSecondary = Color(0xFF3B82F6).copy(alpha = 0.08f)
val AuraGradientPrimary = Color(0xFF34D399).copy(alpha = 0.06f)
val AuraGradientSecondary = Color(0xFF60A5FA).copy(alpha = 0.06f)
val AccentAmber = Color(0xFFF59E0B)
val AccentYellow = Color(0xFFEAB308)
val AccentRose = Color(0xFFF43F5E)
val AccentGreen = Color(0xFF10B981)

@Immutable
data class VitaColors(
    val glassFill: Color = GlassFill,
    val glassBorderLight: Color = GlassBorderLight,
    val glassBorderDark: Color = GlassBorderDark,
    val glowPrimary: Color = GlowPrimary,
    val glowSecondary: Color = GlowSecondary,
    val auraGradientPrimary: Color = AuraGradientPrimary,
    val auraGradientSecondary: Color = AuraGradientSecondary,
    val apexGlow: Color = ApexGlow,
    val apexBorder: Color = ApexBorder,
    val accentAmber: Color = AccentAmber,
    val accentYellow: Color = AccentYellow,
    val accentRose: Color = AccentRose,
    val accentGreen: Color = AccentGreen
)

val LocalVitaColors = staticCompositionLocalOf { VitaColors() }

package com.example.vitaai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Apex Vitality — High Performance Palette ──────────────────────────────

// Primary (Electric Lime)
val Primary = Color(0xFFB0D500)
val OnPrimary = Color(0xFF171E00)
val PrimaryContainer = Color(0xFFCAF300)
val OnPrimaryContainer = Color(0xFF596C00)
val InversePrimary = Color(0xFF536600)
val PrimaryFixed = Color(0xFFCAF300)
val PrimaryFixedDim = Color(0xFFB0D500)
val OnPrimaryFixed = Color(0xFF171E00)
val OnPrimaryFixedVariant = Color(0xFF3E4C00)

// Secondary (Technical Zinc)
val Secondary = Color(0xFFC8C6C9)
val OnSecondary = Color(0xFF303033)
val SecondaryContainer = Color(0xFF47464A)
val OnSecondaryContainer = Color(0xFFB6B4B8)
val SecondaryFixed = Color(0xFFE4E1E5)
val SecondaryFixedDim = Color(0xFFC8C6C9)
val OnSecondaryFixed = Color(0xFF1B1B1E)
val OnSecondaryFixedVariant = Color(0xFF47464A)

// Tertiary (Structured Slate)
val Tertiary = Color(0xFFFFFFFF)
val OnTertiary = Color(0xFF303037)
val TertiaryContainer = Color(0xFFE3E1EA)
val OnTertiaryContainer = Color(0xFF64646B)
val TertiaryFixed = Color(0xFFE3E1EA)
val TertiaryFixedDim = Color(0xFFC7C5CE)
val OnTertiaryFixed = Color(0xFF1B1B21)
val OnTertiaryFixedVariant = Color(0xFF46464D)

// Error (High Saturation)
val Error = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Surface & Background (Deep Performance Black)
val Background = Color(0xFF131315)
val OnBackground = Color(0xFFE5E1E4)
val Surface = Color(0xFF131315)
val OnSurface = Color(0xFFE5E1E4)
val SurfaceDim = Color(0xFF131315)
val SurfaceBright = Color(0xFF39393B)
val SurfaceContainerLowest = Color(0xFF0E0E10)
val SurfaceContainerLow = Color(0xFF1C1B1D)
val SurfaceContainer = Color(0xFF201F22)
val SurfaceContainerHigh = Color(0xFF2A2A2C)
val SurfaceContainerHighest = Color(0xFF353437)
val SurfaceVariant = Color(0xFF353437)
val OnSurfaceVariant = Color(0xFFC5C9AC)
val InverseSurface = Color(0xFFE5E1E4)
val InverseOnSurface = Color(0xFF313032)
val SurfaceTint = Color(0xFFB0D500)

// Outline
val Outline = Color(0xFF8F9378)
val OutlineVariant = Color(0xFF444932)

// ─── Custom Tokens (Apex) ──────────────────────────────────────────────────

/** Subtle technical glow */
val ApexGlow = Primary.copy(alpha = 0.1f)

/** Technical border for cards */
val ApexBorder = OutlineVariant.copy(alpha = 0.5f)

// ─── Legacy/Compatibility Tokens ───────────────────────────────────────────
// Kept for initial compilation, will be removed as components are updated
val GlassFill = Color.White.copy(alpha = 0.03f)
val GlassBorderLight = Color.White.copy(alpha = 0.15f)
val GlassBorderDark = Color.White.copy(alpha = 0.05f)
val GlowPrimary = Primary.copy(alpha = 0.15f)
val GlowSecondary = Secondary.copy(alpha = 0.15f)
val AuraGradientPrimary = Primary.copy(alpha = 0.20f)
val AuraGradientSecondary = Secondary.copy(alpha = 0.20f)

// ─── VitaColors Composition Local ──────────────────────────────────────────

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
    val apexBorder: Color = ApexBorder
)

val LocalVitaColors = staticCompositionLocalOf { VitaColors() }

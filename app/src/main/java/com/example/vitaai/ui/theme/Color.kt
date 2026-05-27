package com.example.vitaai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Luminous Sanctuary — Full Stitch Palette ───────────────────────────────

// Primary (Neon Teal)
val Primary = Color(0xFF8AEBFF)
val OnPrimary = Color(0xFF00363E)
val PrimaryContainer = Color(0xFF22D3EE)
val OnPrimaryContainer = Color(0xFF005763)
val InversePrimary = Color(0xFF006877)
val PrimaryFixed = Color(0xFFA2EEFF)
val PrimaryFixedDim = Color(0xFF2FD9F4)
val OnPrimaryFixed = Color(0xFF001F25)
val OnPrimaryFixedVariant = Color(0xFF004E5A)

// Secondary (Soft Violet)
val Secondary = Color(0xFFDDB7FF)
val OnSecondary = Color(0xFF490080)
val SecondaryContainer = Color(0xFF6F00BE)
val OnSecondaryContainer = Color(0xFFD6A9FF)
val SecondaryFixed = Color(0xFFF0DBFF)
val SecondaryFixedDim = Color(0xFFDDB7FF)
val OnSecondaryFixed = Color(0xFF2C0051)
val OnSecondaryFixedVariant = Color(0xFF6900B3)

// Tertiary (Energetic Coral)
val Tertiary = Color(0xFFFFD2D5)
val OnTertiary = Color(0xFF67001F)
val TertiaryContainer = Color(0xFFFFAAB2)
val OnTertiaryContainer = Color(0xFF94223A)
val TertiaryFixed = Color(0xFFFFDADC)
val TertiaryFixedDim = Color(0xFFFFB2B9)
val OnTertiaryFixed = Color(0xFF400010)
val OnTertiaryFixedVariant = Color(0xFF891933)

// Error
val Error = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Surface & Background (Deep Space)
val Background = Color(0xFF0B1326)
val OnBackground = Color(0xFFDAE2FD)
val Surface = Color(0xFF0B1326)
val OnSurface = Color(0xFFDAE2FD)
val SurfaceDim = Color(0xFF0B1326)
val SurfaceBright = Color(0xFF31394D)
val SurfaceContainerLowest = Color(0xFF060E20)
val SurfaceContainerLow = Color(0xFF131B2E)
val SurfaceContainer = Color(0xFF171F33)
val SurfaceContainerHigh = Color(0xFF222A3D)
val SurfaceContainerHighest = Color(0xFF2D3449)
val SurfaceVariant = Color(0xFF2D3449)
val OnSurfaceVariant = Color(0xFFBBC9CD)
val InverseSurface = Color(0xFFDAE2FD)
val InverseOnSurface = Color(0xFF283044)
val SurfaceTint = Color(0xFF2FD9F4)

// Outline
val Outline = Color(0xFF859397)
val OutlineVariant = Color(0xFF3C494C)

// ─── Custom Tokens (Non-M3) ────────────────────────────────────────────────

/** Glassmorphism card fill */
val GlassFill = Color.White.copy(alpha = 0.03f)

/** Glass card border — top-left (brighter) */
val GlassBorderLight = Color.White.copy(alpha = 0.15f)

/** Glass card border — bottom-right (dimmer) */
val GlassBorderDark = Color.White.copy(alpha = 0.05f)

/** Aura glow — Primary at 15% for outer glow effects */
val GlowPrimary = Color(0xFF22D3EE).copy(alpha = 0.15f)

/** Aura glow — Secondary at 15% */
val GlowSecondary = Color(0xFFA855F7).copy(alpha = 0.15f)

/** Aura gradient blob — primary at 20% */
val AuraGradientPrimary = Color(0xFF22D3EE).copy(alpha = 0.20f)

/** Aura gradient blob — secondary at 20% */
val AuraGradientSecondary = Color(0xFFA855F7).copy(alpha = 0.20f)

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
    val neonTeal: Color = PrimaryContainer,
    val softViolet: Color = Secondary,
    val energeticCoral: Color = TertiaryContainer
)

val LocalVitaColors = staticCompositionLocalOf { VitaColors() }

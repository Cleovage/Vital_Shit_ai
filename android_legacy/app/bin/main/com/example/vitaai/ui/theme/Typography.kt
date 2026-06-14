package com.example.vitaai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.example.vitaai.R

// ─── Google Fonts Provider ──────────────────────────────────────────────────

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val InterFont = GoogleFont("Inter")
val PlusJakartaSansFont = GoogleFont("Plus Jakarta Sans")
val SpaceGroteskFont = GoogleFont("Space Grotesk")

// ─── Font Families ──────────────────────────────────────────────────────────

val DisplayFamily = FontFamily(
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Bold)
)

val BodyFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Bold)
)

val LabelFamily = FontFamily(
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.Bold)
)


// ─── Typography Scale ───────────────────────────────────────────────────────

val VitaTypography = Typography(
    // Display
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.8.sp,
        letterSpacing = (-0.96).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 39.6.sp,
        letterSpacing = (-0.36).sp
    ),

    // Headlines
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 21.6.sp,
        letterSpacing = 0.sp
    ),

    // Titles
    titleLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // Labels
    labelLarge = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.7.sp
    ),
    labelMedium = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.6.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    )
)

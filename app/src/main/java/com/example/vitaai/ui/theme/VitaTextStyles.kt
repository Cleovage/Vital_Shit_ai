package com.example.vitaai.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Semantic typography tokens for VitaAI.
 *
 * Scale: 10 / 11 / 12 / 13 / 14 / 16 / 18 / 20 / 24 / 28 / 32 / 40 / 44 / 64 sp
 * Weights: Regular (body), Medium (labels), SemiBold (headings), Bold (titles/metrics), ExtraBold (nav)
 */
object VitaTextStyles {

    // ─── Page & screen headers ───────────────────────────────────────────────

    /** Uppercase kicker above page titles (e.g. "PERSONALIZATION"). */
    val pageKicker = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 4.4.sp
    )

    /** Primary screen title (e.g. "Profile", "Today"). */
    val pageTitle = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-2).sp
    )

    /** Secondary screen title on detail panels. */
    val screenSubtitle = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    /** Detail panel metric name kicker (uppercase). */
    val detailKicker = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp
    )

    // ─── Section & card headers ──────────────────────────────────────────────

    /** Uppercase section label (e.g. "YOUR PROGRESS"). */
    val sectionLabel = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.sp
    )

    /** Optional subtitle below a section label. */
    val sectionSubtitle = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    /** Card or insight block title. */
    val cardTitle = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    /** Section header inside cards (e.g. "Today's Insights"). */
    val cardSectionTitle = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.03).sp
    )

    /** Uppercase label inside cards (e.g. "READINESS", "TODAY"). */
    val cardOverline = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.95.sp
    )

    /** ApexCard header bar label. */
    val cardHeaderBar = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.2.sp
    )

    // ─── Metrics & numbers ───────────────────────────────────────────────────

    /** Hero metric — readiness score, large dashboard numbers. */
    val metricHero = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 64.sp,
        lineHeight = 68.sp,
        letterSpacing = (-3.84).sp
    )

    /** Detail screen primary value. */
    val metricLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.5).sp
    )

    /** Vitals hero cards (steps, hydration total). */
    val metricMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-1.28).sp
    )

    /** Dashboard metric card values. */
    val metricCompact = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.96).sp
    )

    /** Hydration / secondary hero values. */
    val metricProminent = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.8).sp
    )

    /** Percent sign, units beside hero metrics. */
    val metricUnit = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    /** Uppercase unit label (e.g. "ML", "BPM"). */
    val metricUnitLabel = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

    /** Profile hero name, prominent inline titles. */
    val profileName = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.8).sp
    )

    /** Profile stat value (level, workout count). */
    val statValue = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.5).sp
    )

    /** Profile stat label, metric sub-labels. */
    val statLabel = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )

    /** Inline metric row label (Sleep, Heart). */
    val metricRowLabel = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    /** Inline metric row value. */
    val metricRowValue = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // ─── Body & captions ─────────────────────────────────────────────────────

    /** Primary body copy in cards and tips. */
    val bodyPrimary = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    /** Secondary descriptive text. */
    val bodySecondary = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    )

    /** Metric card label (Steps, Protein). */
    val metricLabel = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    /** Small helper / goal text. */
    val caption = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    /** Tiny badges and status chips — minimum readable size. */
    val badge = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.5.sp
    )

    /** Status chip text (Health Connect, Premium). */
    val chip = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )

    // ─── Interactive & navigation ────────────────────────────────────────────

    /** ActionRow primary title. */
    val actionTitle = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.48).sp
    )

    /** ActionRow subtitle. */
    val actionSubtitle = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    /** Primary button / dialog CTA label. */
    val buttonLabel = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    /** Bottom nav selected tab label. */
    val navLabel = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = (-0.03).sp
    )

    /** PageHeader "AI Sync" badge. */
    val syncBadge = TextStyle(
        fontFamily = LabelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )

    /** Edit affordance hint (e.g. "Tap to edit name"). */
    val editHint = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )

    /** Dialog title. */
    val dialogTitle = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
}

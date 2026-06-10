package com.example.vitaai.data

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Shared formatting and score helpers so dashboard, activity, and detail
 * screens show the same values from the same [HealthSnapshot].
 */
object HealthMetrics {

    fun computeReadinessScore(snapshot: HealthSnapshot): Int {
        val sleepScore = (snapshot.sleepDurationHours / 8.0).coerceIn(0.0, 1.0)
        val heartScore = heartRecoveryFraction(snapshot)
        return ((sleepScore * 0.5 + heartScore * 0.5) * 100).roundToInt()
    }

    fun sleepProgressPercent(hours: Double): Int =
        ((hours / 8.0) * 100).roundToInt().coerceIn(0, 100)

    fun heartRecoveryPercent(snapshot: HealthSnapshot): Int =
        (heartRecoveryFraction(snapshot) * 100).roundToInt().coerceIn(0, 100)

    fun readinessLabel(score: Int): String = when {
        score <= 0 -> "NO DATA"
        score >= 80 -> "OPTIMAL"
        score >= 60 -> "GOOD"
        score >= 40 -> "MODERATE"
        else -> "LOW"
    }

    fun formatActiveEnergyKcal(calories: Double): String {
        val kcal = calories.roundToInt()
        return if (kcal > 0) "$kcal kcal" else "-- kcal"
    }

    fun formatTrainingMinutes(minutes: Double): String {
        val min = minutes.roundToInt()
        return if (min > 0) "$min min" else "-- min"
    }

    fun formatProteinGrams(grams: Double): String {
        val g = grams.roundToInt()
        return if (g > 0) "$g g" else "-- g"
    }

    fun formatSleepHours(hours: Double): String =
        if (hours > 0.0) String.format(Locale.US, "%.1f h", hours) else "-- h"

    fun formatHeartRateBpm(rate: Double): String =
        if (rate > 0.0) rate.roundToInt().toString() else "--"

    fun formatRestingHeartRate(resting: Double, avg: Double): String {
        val rate = when {
            resting > 0.0 -> resting
            avg > 0.0 -> avg
            else -> 0.0
        }
        return if (rate > 0.0) "RESTING: ${rate.roundToInt()} BPM" else "RESTING: -- BPM"
    }

    fun hydrationPaceMessage(hydrationLiters: Double, goalLiters: Double): String {
        if (goalLiters <= 0.0) return "Set a hydration goal in your profile to track daily pace."
        val remainingMl = ((goalLiters - hydrationLiters).coerceAtLeast(0.0) * 1000).roundToInt()
        return if (remainingMl > 0) {
            "You're currently ${remainingMl}ml behind your daily hydration pace. Grab a glass of water now to stay on track for your ${String.format(Locale.US, "%.1f", goalLiters)}L goal."
        } else {
            "Great job — you've met your ${String.format(Locale.US, "%.1f", goalLiters)}L hydration goal for today."
        }
    }

    fun recoveryTip(sleepHours: Double): String = when {
        sleepHours <= 0.0 -> "No sleep data synced yet. Connect Health Connect or start a sleep session tonight."
        sleepHours < 6.5 -> "Your deep sleep was slightly lower last night (${String.format(Locale.US, "%.1f", sleepHours)} h). Consider winding down 30 mins earlier today and avoiding screens before bed."
        else -> "Sleep looks solid at ${String.format(Locale.US, "%.1f", sleepHours)} h. Keep your bedtime routine consistent to protect recovery."
    }

    fun trendValues(trend: Map<java.time.LocalDate, Double>): List<Float> =
        trend.values.map { it.toFloat() }

    private fun heartRecoveryFraction(snapshot: HealthSnapshot): Double {
        val hr = when {
            snapshot.restingHeartRate > 0.0 -> snapshot.restingHeartRate
            snapshot.avgHeartRate > 0.0 -> snapshot.avgHeartRate
            else -> return 0.0
        }
        // Lower resting/average HR maps to a higher recovery contribution.
        return ((90.0 - hr) / 30.0).coerceIn(0.0, 1.0)
    }
}

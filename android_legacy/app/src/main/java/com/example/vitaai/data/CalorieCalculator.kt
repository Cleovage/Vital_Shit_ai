package com.example.vitaai.data

import java.util.Locale
import kotlin.math.roundToInt

object CalorieCalculator {

    /**
     * Estimates calorie burn of a strength/bodyweight workout based on elapsed time,
     * completed sets, and total reps.
     * 
     * Formula:
     * - Baseline metabolic cost: METs = 3.5. Baseline calories = minutes * 3.5 * 3.5 * weightKg / 200.
     * - Sets EPOC cost: completedSets * 1.8 kcal * MovementFactor.
     * - Reps contraction cost: totalReps * 0.15 kcal * MovementFactor.
     */
    fun estimateStrengthCalories(
        templateId: String,
        durationSeconds: Long,
        completedSets: Int,
        totalReps: Int,
        weightKg: Double = 75.0
    ): Double {
        val minutes = durationSeconds / 60.0
        val factor = getCategoryWeightFactor(templateId)
        
        // 1. Basal/MET cost of lifting active time
        val baseline = minutes * 3.5 * 3.5 * weightKg / 200.0
        
        // 2. Setup/recovery set cost
        val setsCost = completedSets * 1.8 * factor
        
        // 3. Mechanical muscle fiber contraction cost per rep
        val repsCost = totalReps * 0.15 * factor
        
        return baseline + setsCost + repsCost
    }

    /**
     * Estimates cardio calorie burn dynamically using speed/pace.
     * If speed is available, utilizes standardized ACSM metabolic equations.
     * If manual, falls back to optimized MET coefficients.
     */
    fun estimateCardioCalories(
        templateId: String,
        durationSeconds: Long,
        distanceMeters: Double,
        weightKg: Double = 75.0
    ): Double {
        val minutes = durationSeconds / 60.0
        if (minutes <= 0.0) return 0.0

        val speedMps = distanceMeters / durationSeconds.toDouble().coerceAtLeast(1.0)
        val speedKmph = speedMps * 3.6

        val met = if (distanceMeters <= 0.0) {
            // Static cardio fallback METs
            when (templateId) {
                "running" -> 10.0
                "cycling" -> 8.0
                "swimming_pool", "swimming_open" -> 8.0
                "walking" -> 3.8
                else -> 4.5
            }
        } else {
            // Dynamic MET calculation based on actual speed (ACSM equations)
            when (templateId) {
                "running" -> {
                    // Running: METs = 3.5 + 0.2 * speed (m/min) / 3.5
                    (3.5 + 0.2 * (speedMps * 60.0)) / 3.5
                }
                "walking" -> {
                    // Walking: METs = 3.5 + 0.1 * speed (m/min) / 3.5
                    (3.5 + 0.1 * (speedMps * 60.0)) / 3.5
                }
                "cycling" -> {
                    when {
                        speedKmph < 15 -> 4.0
                        speedKmph < 20 -> 6.0
                        speedKmph < 25 -> 8.0
                        speedKmph < 30 -> 10.0
                        else -> 12.0
                    }
                }
                else -> 8.0
            }
        }

        // METs * 3.5 * weightKg / 200 = kcal/min
        return met * 3.5 * weightKg / 200.0 * minutes
    }

    private fun getCategoryWeightFactor(templateId: String): Double {
        return when (templateId) {
            "deadlift", "squat" -> 1.25 // Heavy compound leg moves
            "bench_press", "overhead_press", "rows" -> 1.0 // Compound upper moves
            "push_up", "pull_up", "lunges", "burpees" -> 0.95 // Bodyweight HIIT
            "plank" -> 0.5 // Static isometric hold
            "yoga" -> 0.45 // Low impact mobility
            else -> 0.8
        }
    }

    /**
     * Estimates Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation.
     */
    fun estimateBmr(
        weightKg: Double,
        heightMeters: Double,
        ageYears: Int,
        gender: String
    ): Double {
        val heightCm = heightMeters * 100.0
        return when (gender.lowercase()) {
            "male" -> 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears + 5.0
            "female" -> 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears - 161.0
            else -> 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears - 78.0 // average offset
        }
    }

    /**
     * Estimates dynamic idle calories burned based on current elapsed time today.
     * Activity level multiplier represents Sedentary (1.1), Light (1.2), Active (1.3), Very Active (1.4).
     */
    fun estimateIdleCalories(
        bmr: Double,
        activityLevel: String,
        minutesElapsedToday: Int
    ): Double {
        val multiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.1
            "light" -> 1.2
            "active" -> 1.3
            "very active" -> 1.4
            else -> 1.2
        }
        val dayFraction = minutesElapsedToday / 1440.0
        return bmr * multiplier * dayFraction
    }

    /**
     * Estimates active calories burned from walking steps.
     */
    fun estimateActiveCaloriesFromSteps(steps: Long, weightKg: Double): Double {
        val baseFactor = 0.04 // average kcal per step for a 70kg person
        val weightAdjustment = weightKg / 70.0
        return steps * baseFactor * weightAdjustment
    }

    /**
     * Estimates active calories from heart rate elevation.
     */
    fun estimateHeartRateActiveCalories(
        avgHeartRate: Double,
        restingHeartRate: Double = 70.0,
        weightKg: Double = 75.0,
        activeMinutes: Double
    ): Double {
        if (avgHeartRate <= restingHeartRate || activeMinutes <= 0.0) return 0.0
        val excessHr = avgHeartRate - restingHeartRate
        // Linear MET increase: +10 bpm above resting ~ +0.8 METs
        val addedMets = (excessHr / 10.0) * 0.8
        val kcalPerMin = addedMets * 3.5 * weightKg / 200.0
        return kcalPerMin * activeMinutes
    }
}

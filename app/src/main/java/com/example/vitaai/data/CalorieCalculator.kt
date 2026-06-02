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
}

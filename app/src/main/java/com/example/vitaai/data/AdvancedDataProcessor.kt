package com.example.vitaai.data

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvancedDataProcessor @Inject constructor() {

    private var emaHeartRate: Double? = null
    private val emaAlpha: Double = 0.3

    fun smoothHeartRate(bpm: Float): Float? {
        if (bpm <= 0) return emaHeartRate?.toFloat()
        
        val current = bpm.toDouble()
        emaHeartRate = if (emaHeartRate == null) {
            current
        } else {
            (emaAlpha * current) + ((1.0 - emaAlpha) * emaHeartRate!!)
        }
        return emaHeartRate?.toFloat()
    }

    fun resetHeartRateSmoothing() {
        emaHeartRate = null
    }

    private var lastStepCount: Float? = null
    private var lastStepTime: Instant? = null
    private val minStepIntervalMs: Long = 100 // 10 steps per second max

    fun filterSteps(rawSteps: Float): Float? {
        val now = Instant.now()
        
        if (lastStepCount == null) {
            lastStepCount = rawSteps
            lastStepTime = now
            return rawSteps
        }

        val delta = rawSteps - lastStepCount!!
        if (delta <= 0) return lastStepCount // No new steps or reset

        val timeDelta = java.time.Duration.between(lastStepTime, now).toMillis()
        
        // Basic debounce: if steps come in too fast, they might be noise or multi-reporting
        if (timeDelta < minStepIntervalMs) {
            return lastStepCount
        }

        // Sanity check: no one takes 100 steps in 100ms
        if (delta > 50) {
            return lastStepCount
        }

        lastStepCount = rawSteps
        lastStepTime = now
        return rawSteps
    }

    fun estimateCaloriesFromSteps(steps: Long): Double {
        // Very basic estimation: ~0.04 calories per step
        return steps * 0.04
    }
}

package com.example.vitaai.data

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvancedDataProcessor @Inject constructor() {

    // Exponential Moving Average state
    private var emaHeartRate: Double? = null
    private val emaAlpha = 0.2 // Smoothing factor (0 < alpha <= 1). Lower = smoother, higher = more responsive.

    /**
     * Applies an Exponential Moving Average (EMA) filter to heart rate data.
     * Also filters out biologically impossible anomalies.
     */
    fun smoothHeartRate(rawHeartRate: Float): Float? {
        if (rawHeartRate < 30f || rawHeartRate > 220f) {
            // Anomaly detected, discard this sample
            return null
        }

        val hrDouble = rawHeartRate.toDouble()
        val smoothed = if (emaHeartRate == null) {
            hrDouble // Initialize EMA with the first valid reading
        } else {
            (emaAlpha * hrDouble) + ((1 - emaAlpha) * emaHeartRate!!)
        }
        
        emaHeartRate = smoothed
        return smoothed.toFloat()
    }

    /**
     * Resets the EMA state, useful when a new session starts or after a long gap in data.
     */
    fun resetHeartRateSmoothing() {
        emaHeartRate = null
    }

    private var lastStepCount: Float? = null
    private var lastStepTime: Instant? = null
    private val minStepIntervalMs = 200L // 5 steps per second max (walking/running)
    
    /**
     * Debounces and validates step count updates to prevent false positives from random movements.
     * 
     * @param rawSteps The cumulative step count from the sensor.
     * @return The validated cumulative step count, or null if the update is deemed invalid (e.g., too fast).
     */
    fun filterSteps(rawSteps: Float): Float? {
        val now = Instant.now()
        
        // Handle initialization or negative anomalies
        if (rawSteps < 0) return null
        
        if (lastStepCount == null || lastStepTime == null) {
            lastStepCount = rawSteps
            lastStepTime = now
            return rawSteps
        }

        val timeDeltaMs = now.toEpochMilli() - lastStepTime!!.toEpochMilli()
        val stepsDelta = rawSteps - lastStepCount!!

        // If the step count decreased, the sensor likely reset (e.g. device reboot)
        if (stepsDelta < 0) {
            lastStepCount = rawSteps
            lastStepTime = now
            return rawSteps
        }

        // If no new steps, just return the current value
        if (stepsDelta == 0f) {
            return rawSteps
        }

        // Check for impossibly fast steps (anomaly)
        if (timeDeltaMs > 0 && (timeDeltaMs / stepsDelta) < minStepIntervalMs) {
            // Update too fast, likely a false positive (shaking device)
            // We ignore this update and keep the old state
            return null
        }

        // Valid update
        lastStepCount = rawSteps
        lastStepTime = now
        return rawSteps
    }
    
    /**
     * Calculates a more accurate calorie estimation based on steps if actual active calories are unavailable.
     * This is a basic implementation that could be expanded with user BMR later.
     */
    fun estimateCaloriesFromSteps(steps: Long): Double {
        // A more nuanced heuristic: 
        // ~0.04 calories per step is okay for walking, but we can cap it or adjust it based on volume
        // For a more advanced app, we'd need user weight, height, and step cadence (speed).
        // For now, we'll use a slightly more conservative baseline of 0.035 to prevent overestimation.
        return steps * 0.035
    }
}

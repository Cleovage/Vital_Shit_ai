package com.example.vitaai.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveSensorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val advancedDataProcessor: AdvancedDataProcessor
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getStepCountFlow(): Flow<Float> = callbackFlow {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.get(0)?.let { rawSteps -> 
                    advancedDataProcessor.filterSteps(rawSteps)?.let { filteredSteps ->
                        trySend(filteredSteps)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            runCatching {
                sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun getHeartRateFlow(): Flow<Float> = callbackFlow {
        val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.get(0)?.let { rawHr ->
                    advancedDataProcessor.smoothHeartRate(rawHr)?.let { smoothedHr ->
                        trySend(smoothedHr)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (hrSensor != null) {
            runCatching {
                // Reset smoothing state when starting a new flow
                advancedDataProcessor.resetHeartRateSmoothing()
                sensorManager.registerListener(listener, hrSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}

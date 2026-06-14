package com.example.vitaai.tracking

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.vitaai.R
import com.example.vitaai.data.local.AmbientLightLogEntity
import com.example.vitaai.data.local.ScreenStateEventEntity
import com.example.vitaai.data.local.SleepSessionEntity
import com.example.vitaai.data.local.VitaDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.vitaai.ui.MainActivity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SleepTrackingService : Service() {

    @Inject
    lateinit var vitaDao: VitaDao

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    
    private var lightSensorListener: AmbientLightSensorListener? = null
    private var screenStateReceiver: ScreenStateReceiver? = null

    private var startTimeMillis: Long = 0
    private val CHANNEL_ID = "sleep_tracking_channel"
    private val NOTIFICATION_ID = 2002

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    companion object {
        val isServiceRunning = MutableStateFlow(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning.value = true
        startTimeMillis = System.currentTimeMillis()

        // Acquire WakeLock to keep CPU running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VitaAI::SleepTrackingWakeLock").apply {
            acquire(10 * 60 * 60 * 1000L) // 10 hours max
        }

        // Start Foreground Notification
        val notification = createNotification("Sleep tracking active. Aligning your circadian rhythm...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Initialize and start light sensor
        lightSensorListener = AmbientLightSensorListener(this) { lux ->
            serviceScope.launch {
                vitaDao.insertAmbientLightLog(
                    AmbientLightLogEntity(
                        timestampMillis = System.currentTimeMillis(),
                        luxValue = lux
                    )
                )
            }
        }.apply { start() }

        // Initialize and register screen receiver
        screenStateReceiver = ScreenStateReceiver { isScreenOn ->
            serviceScope.launch {
                val eventType = if (isScreenOn) "SCREEN_ON" else "SCREEN_OFF"
                vitaDao.insertScreenStateEvent(
                    ScreenStateEventEntity(
                        timestampMillis = System.currentTimeMillis(),
                        eventType = eventType
                    )
                )
            }
        }.apply { register(this@SleepTrackingService) }

        // Log initial screen state
        serviceScope.launch {
            val isScreenOn = isInteractive()
            vitaDao.insertScreenStateEvent(
                ScreenStateEventEntity(
                    timestampMillis = System.currentTimeMillis(),
                    eventType = if (isScreenOn) "SCREEN_ON" else "SCREEN_OFF"
                )
            )
        }

        return START_STICKY
    }

    private fun isInteractive(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }

    override fun onDestroy() {
        isServiceRunning.value = false
        val endTimeMillis = System.currentTimeMillis()
        
        // Stop sensor and receiver
        lightSensorListener?.stop()
        screenStateReceiver?.unregister(this)

        // Calculate and save sleep session
        serviceScope.launch {
            processAndSaveSleepSession(startTimeMillis, endTimeMillis)
            
            // Release wake lock
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            serviceScope.cancel()
        }

        super.onDestroy()
    }

    private suspend fun processAndSaveSleepSession(startMs: Long, endMs: Long) {
        val durationMillis = endMs - startMs
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        
        if (durationMinutes < 10) {
            // Sleep sessions must be at least 10 minutes to register
            return
        }

        // Retrieve light and screen logs for this window
        val lightLogs = vitaDao.getAmbientLightLogs(startMs, endMs)
        val screenEvents = vitaDao.getScreenStateEvents(startMs, endMs)

        // Apply simplified Cole-Kripke sleep heuristics
        // We divide the sleep window into 1-minute epochs
        val totalEpochs = durationMinutes.toInt()
        var asleepEpochs = 0
        var totalLux = 0f

        for (i in 0 until totalEpochs) {
            val epochStart = startMs + TimeUnit.MINUTES.toMillis(i.toLong())
            val epochEnd = epochStart + TimeUnit.MINUTES.toMillis(1)

            // Check screen-on duration in this epoch
            val isScreenOn = screenEvents.any { it.timestampMillis in epochStart..epochEnd && it.eventType == "SCREEN_ON" }
            val avgLux = lightLogs.filter { it.timestampMillis in epochStart..epochEnd }.map { it.luxValue }.average().toFloat()

            val isAsleep = !isScreenOn && (avgLux.isNaN() || avgLux < 5.0f)
            if (isAsleep) {
                asleepEpochs++
            }
            if (!avgLux.isNaN()) {
                totalLux += avgLux
            }
        }

        val sleepEfficiency = if (totalEpochs > 0) (asleepEpochs.toDouble() / totalEpochs) * 100.0 else 0.0
        
        // Calculate Sleep Quality Score based on duration and efficiency
        // Target: 8 hours (480 mins)
        val durationScore = ((durationMinutes.toDouble() / 480.0) * 100.0).coerceAtMost(100.0)
        val efficiencyScore = sleepEfficiency
        val sleepQualityScore = ((durationScore * 0.5) + (efficiencyScore * 0.5)).toInt().coerceIn(0, 100)

        val sleepSession = SleepSessionEntity(
            startTimeMillis = startMs,
            endTimeMillis = endMs,
            durationMinutes = durationMinutes,
            sleepQualityScore = sleepQualityScore,
            source = "Phone Sensors",
            notes = "Avg Light: ${String.format("%.1f", totalLux / Math.max(1, lightLogs.size))} lux, Efficiency: ${String.format("%.1f", sleepEfficiency)}%"
        )

        vitaDao.insertSleepSession(sleepSession)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground notifications for sleep tracking service"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        // Safe fallback for pending intent to launch main activity
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VitaAI Circadian Engine")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Standard system icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}

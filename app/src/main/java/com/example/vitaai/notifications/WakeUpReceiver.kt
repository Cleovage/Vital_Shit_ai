package com.example.vitaai.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.vitaai.tracking.SleepTrackingService
import com.example.vitaai.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WakeUpReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "wakeup_notifications_channel"
    private val NOTIFICATION_ID = 2002

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)

        // Intent to open Main Activity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_circadian", true)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent to stop Sleep Tracking Service
        val stopTrackingIntent = Intent(context, SleepTrackingService::class.java)
        val stopTrackingPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                1003,
                stopTrackingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                1003,
                stopTrackingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notificationBody = buildNotificationBody(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("☀️ Good Morning!")
            .setContentText("Time to wake up. Check your sleep tracking summary.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Tracking",
                stopTrackingPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotificationBody(context: Context): String {
        var body = "Time to wake up and start your day!\n\n"
        
        // Try to fetch sleep metrics
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val prefs = context.getSharedPreferences("vita_app_prefs", Context.MODE_PRIVATE)
                val lastSleepHours = prefs.getFloat("last_sleep_duration_hours", 0f)
                val sleepQuality = prefs.getInt("last_sleep_quality", 0)
                val lightExposure = prefs.getInt("morning_light_exposure", 0)
                
                if (lastSleepHours > 0) {
                    body += "Last Night Sleep: ${String.format("%.1f", lastSleepHours)}h\n"
                }
                if (sleepQuality > 0) {
                    body += "Quality: $sleepQuality/100\n"
                }
                if (lightExposure > 0) {
                    body += "Light Exposure: ${lightExposure}lux\n"
                }
                body += "\nTap to view full tracking summary."
            } catch (e: Exception) {
                body += "\nView your sleep summary in the app."
            }
        }
        
        return body
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wake-up Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily wake-up alerts with sleep tracking summary"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

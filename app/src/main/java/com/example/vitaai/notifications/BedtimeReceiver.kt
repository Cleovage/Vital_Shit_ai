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

class BedtimeReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "bedtime_notifications_channel"
    private val NOTIFICATION_ID = 2001

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)

        // Intent to open Main Activity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to start Sleep Tracking Foreground Service
        val trackingIntent = Intent(context, SleepTrackingService::class.java)
        val trackingPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                1002,
                trackingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                1002,
                trackingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Time to Wind Down")
            .setContentText("Your biological clock suggests sleeping. Minimize bright screen light.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_play,
                "Start Sleep Tracking",
                trackingPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bedtime Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily alerts to prepare for bed and align circadian cycles"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

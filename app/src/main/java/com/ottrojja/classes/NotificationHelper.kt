package com.ottrojja.classes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ottrojja.R

object NotificationHelper {

    private const val CHANNEL_ID = "quran_reminders"

    val notificationVibrationLengths= longArrayOf(0, 500, 250, 500)

    fun showNotification(
        context: Context,
        id: Int,
        title: String,
        message: String
    ) {
        println("show noti for $id $title $message")
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(manager)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setVibrate(notificationVibrationLengths)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(id, notification)
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Qur'an Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminder Notification Channel with sound and vibration"

                // Enable vibration
                enableVibration(true)
                vibrationPattern = notificationVibrationLengths

                // Enable sound
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                setSound(soundUri, audioAttributes)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
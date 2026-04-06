package com.ottrojja.classes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ottrojja.R

object NotificationHelper {

    private const val CHANNEL_ID = "quran_reminders"

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
            )
            manager.createNotificationChannel(channel)
        }
    }
}
package com.ottrojja.classes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ottrojja.classes.DataStore.DataStoreHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "PLAYER_CHANNEL",
                "Playing Media",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(false)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            notificationManager.createNotificationChannel(channel)
        }
        DataStoreHolder.init(this)
        OverlayBootstrap.init(this, appScope)

    }
}
package com.ottrojja.classes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ottrojja.classes.DataStore.DataStoreHolder
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.prayerTimesScreen.data.PrayerTimesModule
import com.ottrojja.services.page.PagePlaybackDependencies
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
        PrayerTimesModule.init(this)
        OverlayBootstrap.init(this, appScope)
        // the page player builds its playlists itself; same database instance as the screens
        PagePlaybackDependencies.init { context ->
            val db = DatabaseProvider.getDatabase(context)
            QuranRepository(db.quranDao(), db.khitmahDao(), db.tasabeehDao())
        }

    }
}
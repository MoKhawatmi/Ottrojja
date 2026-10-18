package com.ottrojja.screens.prayerTimesScreen.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ottrojja.R
import com.ottrojja.screens.prayerTimesScreen.PrayerFormat
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import java.time.ZoneId

/** Data an alarm carries into the notification or the adhan. Primitive-friendly on purpose. */
data class PrayerAlert(
    val type: PrayerType,
    val epochMillis: Long,
    val isJumuah: Boolean,
    val locationName: String,
    val timeZoneId: String,
)

object PrayerNotifications {

    // v2: lock screen visibility can't be changed on an existing channel, so new ids
    const val CHANNEL_PRAYER = "prayer_times_v2"
    const val CHANNEL_ADHAN = "prayer_times_adhan_v2"
    private val OLD_CHANNELS = listOf("prayer_times", "prayer_times_adhan")

    /** Replace with your reminder notification icon: small icons must be a white silhouette. */
    private val SMALL_ICON = R.drawable.logo

    private const val NOTIFICATION_ID_BASE = 47_000

    fun notificationId(type: PrayerType) = NOTIFICATION_ID_BASE + type.ordinal

    /** Covers both the Android 13+ runtime permission and notifications switched off in settings. */
    fun canPostNotifications(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * True while Do Not Disturb is on: the adhan is then replaced by a notification.
     * Silent and vibrate modes don't count: the adhan plays on the alarm volume, which
     * Android lets through in both, like an alarm clock.
     */
    fun isDoNotDisturbOn(context: Context): Boolean {
        val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notifications.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    @RawRes
    fun adhanSound(type: PrayerType): Int =
        if (type == PrayerType.FAJR) R.raw.adhan_fajr else R.raw.adhan

    /**
     * Channels can't change their sound after creation, hence two: one with the default
     * notification sound, one silent for the adhan (the service plays the audio itself).
     * Both show their full content on the lock screen.
     */
    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        OLD_CHANNELS.forEach { id ->
            if (manager.getNotificationChannel(id) != null) manager.deleteNotificationChannel(id)
        }
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PRAYER,
                context.getString(R.string.prayer_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.prayer_channel_description)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ADHAN,
                context.getString(R.string.prayer_adhan_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.prayer_adhan_channel_description)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }

    /** The plain "حان الآن وقت صلاة الظهر" notification. [silent] is used after an adhan finished. */
    fun showPrayerNotification(context: Context, alert: PrayerAlert, silent: Boolean = false) {
        if (!canPostNotifications(context)) return
        ensureChannels(context)
        val notification = baseBuilder(context, alert, if (silent) CHANNEL_ADHAN else CHANNEL_PRAYER)
            .setAutoCancel(true)
            .setSilent(silent)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId(alert.type), notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check and the call.
        }
    }

    /**
     * Shown while the adhan plays. Stop action, body tap ([tapIntent]) and swipe all stop it,
     * none of them needs the phone unlocked.
     *
     * Deliberately not ongoing: on Android 14+ an ongoing notification can't be swiped away while
     * the phone is locked. Below 14 the system keeps foreground service notifications on screen
     * whatever this flag says, so there the stop action and the tap are the ways out.
     */
    fun buildAdhanNotification(
        context: Context,
        alert: PrayerAlert,
        stopIntent: PendingIntent,
        tapIntent: PendingIntent,
    ) = baseBuilder(context, alert, CHANNEL_ADHAN)
        .setOngoing(false)
        .setSilent(true)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setContentIntent(tapIntent)
        .setDeleteIntent(stopIntent)
        .addAction(0, context.getString(R.string.prayer_adhan_stop), stopIntent)
        .build()

    private fun baseBuilder(context: Context, alert: PrayerAlert, channel: String): NotificationCompat.Builder {
        val prayerName = context.getString(alert.type.displayNameRes(alert.isJumuah))
        val time = PrayerFormat.time(context, alert.epochMillis, zoneOf(alert.timeZoneId))
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(SMALL_ICON)
            .setContentTitle(context.getString(R.string.prayer_notification_title, prayerName))
            .setContentText(context.getString(R.string.prayer_notification_text, alert.locationName, time))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // full content on the lock screen (API < 26)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(alert.epochMillis)
            .setShowWhen(true)
            .setContentIntent(openAppIntent(context))
    }

    private fun openAppIntent(context: Context): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context, 0, launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun zoneOf(id: String): ZoneId = runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())
}

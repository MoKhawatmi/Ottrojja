package com.ottrojja.screens.prayerTimesScreen.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.ottrojja.screens.prayerTimesScreen.data.PrayerTimesRepository
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps one alarm per notifiable prayer: the next occurrence of each prayer whose mode is
 * not OFF. Every call cancels and re-registers all of them, so it is safe to call as often
 * as needed (settings change, alarm fired, boot, time or zone change, periodic worker).
 */
class PrayerAlarmScheduler(
    context: Context,
    private val repository: PrayerTimesRepository,
) {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val mutex = Mutex()

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    suspend fun rescheduleAll(nowMillis: Long = System.currentTimeMillis()) = mutex.withLock {
        val settings = repository.settingsFlow.first()
        val modes = repository.notificationModesFlow.first()

        notifiablePrayers.forEach(::cancel)

        val location = settings.location ?: return@withLock
        if (!PrayerNotifications.canPostNotifications(appContext)) return@withLock

        for (type in notifiablePrayers) {
            if ((modes[type] ?: PrayerNotificationMode.OFF) == PrayerNotificationMode.OFF) continue
            val (day, time) = repository.nextOccurrence(type, nowMillis, settings) ?: continue
            schedule(
                PrayerAlert(
                    type = type,
                    epochMillis = time.epochMillis,
                    isJumuah = type == PrayerType.DHUHR && day.isFriday,
                    locationName = location.displayName,
                    timeZoneId = location.timeZoneId,
                )
            )
        }
    }

    private fun schedule(alert: PrayerAlert) {
        val pendingIntent = pendingIntent(alert.type, PrayerAlarmReceiver.intent(appContext, alert))
        if (canScheduleExactAlarms()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, alert.epochMillis, pendingIntent,
            )
        } else {
            // Without the exact alarm permission Android may deliver this a few minutes late.
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, alert.epochMillis, pendingIntent,
            )
        }
    }

    private fun cancel(type: PrayerType) {
        alarmManager.cancel(pendingIntent(type, Intent(appContext, PrayerAlarmReceiver::class.java)))
    }

    /** One request code per prayer; extras don't take part in PendingIntent matching. */
    private fun pendingIntent(type: PrayerType, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE_BASE + type.ordinal,
            intent.setAction(PrayerAlarmReceiver.ACTION_PRAYER_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private companion object {
        const val REQUEST_CODE_BASE = 47_100
        val notifiablePrayers = PrayerType.entries.filter { it.isNotifiable }
    }
}

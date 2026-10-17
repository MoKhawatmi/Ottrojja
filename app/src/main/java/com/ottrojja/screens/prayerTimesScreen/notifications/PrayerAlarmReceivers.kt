package com.ottrojja.screens.prayerTimesScreen.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.screens.prayerTimesScreen.data.PrayerTimesModule
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/** Runs [block] off the main thread while keeping the receiver alive (up to ~10 s). */
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportException(exception = e, file = "PrayerAlarmReceivers")
        } finally {
            pending.finish()
        }
    }
}

/**
 * Fires at a prayer time: shows the notification or starts the adhan, then schedules
 * the next occurrence.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PRAYER_ALARM) return
        val alert = intent.toAlert() ?: return

        runAsync {
            val now = System.currentTimeMillis()
            // A phone that was off at prayer time shouldn't announce it hours later.
            if (now - alert.epochMillis <= STALE_AFTER_MILLIS) {
                announce(context, alert)
            }
            // Strictly after this prayer, so it is never scheduled twice.
            PrayerTimesModule.alarmScheduler.rescheduleAll(maxOf(now, alert.epochMillis) + 1_000)
        }
    }

    private suspend fun announce(context: Context, alert: PrayerAlert) {
        // Read the mode now, not when the alarm was set: the user may have changed it since.
        val mode = PrayerTimesModule.repository.notificationModesFlow.first()[alert.type]
        when (mode) {
            PrayerNotificationMode.ADHAN ->
                if (PrayerNotifications.isPhoneSilenced(context)) {
                    PrayerNotifications.showPrayerNotification(context, alert)
                } else {
                    ContextCompat.startForegroundService(context, PrayerAdhanService.playIntent(context, alert))
                }

            PrayerNotificationMode.NOTIFICATION -> PrayerNotifications.showPrayerNotification(context, alert)
            PrayerNotificationMode.OFF, null -> Unit
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.ottrojja.action.PRAYER_ALARM"
        private val STALE_AFTER_MILLIS = TimeUnit.MINUTES.toMillis(15)

        private const val EXTRA_TYPE = "prayer_type"
        private const val EXTRA_TIME = "prayer_time_millis"
        private const val EXTRA_JUMUAH = "prayer_is_jumuah"
        private const val EXTRA_LOCATION = "prayer_location_name"
        private const val EXTRA_ZONE = "prayer_time_zone"

        fun intent(context: Context, alert: PrayerAlert): Intent =
            Intent(context, PrayerAlarmReceiver::class.java).putAlert(alert)

        internal fun Intent.putAlert(alert: PrayerAlert): Intent = apply {
            putExtra(EXTRA_TYPE, alert.type.name)
            putExtra(EXTRA_TIME, alert.epochMillis)
            putExtra(EXTRA_JUMUAH, alert.isJumuah)
            putExtra(EXTRA_LOCATION, alert.locationName)
            putExtra(EXTRA_ZONE, alert.timeZoneId)
        }

        internal fun Intent.toAlert(): PrayerAlert? {
            val type = getStringExtra(EXTRA_TYPE)
                ?.let { name -> PrayerType.entries.firstOrNull { it.name == name } } ?: return null
            val time = getLongExtra(EXTRA_TIME, -1L).takeIf { it > 0 } ?: return null
            return PrayerAlert(
                type = type,
                epochMillis = time,
                isJumuah = getBooleanExtra(EXTRA_JUMUAH, false),
                locationName = getStringExtra(EXTRA_LOCATION).orEmpty(),
                timeZoneId = getStringExtra(EXTRA_ZONE) ?: java.time.ZoneId.systemDefault().id,
            )
        }
    }
}

/**
 * Alarms are lost on reboot and are wrong after a clock or time zone change, so these
 * broadcasts re-register them. Also listens for the exact alarm permission being granted.
 */
class PrayerRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        runAsync { PrayerTimesModule.alarmScheduler.rescheduleAll() }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}

/**
 * Safety net, every 12 hours: re-registers the alarms in case something cleared them
 * (some vendors' battery savers do). Enqueued by PrayerTimesModule.
 */
class PrayerRescheduleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        PrayerTimesModule.alarmScheduler.rescheduleAll()
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "prayer_times_reschedule"
    }
}

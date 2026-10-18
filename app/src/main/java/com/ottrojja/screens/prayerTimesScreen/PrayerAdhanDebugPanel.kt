package com.ottrojja.screens.prayerTimesScreen.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ApplicationInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import com.ottrojja.composables.dialogs.OttrojjaAlertDialog
import com.ottrojja.screens.prayerTimesScreen.data.PrayerTimesModule
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * DEV ONLY: renders nothing in release (non-debuggable) builds.
 *
 * "Diagnose" explains which path the next prayer alarm will take and why.
 * "Play now" starts the adhan service directly, skipping the Do Not Disturb check.
 * "Alarm in 10 s" goes through the real alarm → receiver → service path; lock the
 * screen right after tapping it to test the background start.
 */
@Composable
fun PrayerAdhanDebugPanel() {
    val context = LocalContext.current
    if (!context.isDebuggable()) return

    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DebugButton("Diagnose") { scope.launch { report = diagnose(context) } }
        DebugButton("Play now") {
            scope.launch {
                val alert = testAlert(delayMillis = 0) ?: run { report = NO_LOCATION; return@launch }
                report = try {
                    ContextCompat.startForegroundService(context, PrayerAdhanService.playIntent(context, alert))
                    null
                } catch (e: Exception) {
                    "Service start failed:\n$e"
                }
            }
        }
        DebugButton("Alarm in 10 s") {
            scope.launch {
                val alert = testAlert(delayMillis = 10_000) ?: run { report = NO_LOCATION; return@launch }
                scheduleTestAlarm(context, alert)
                report = "Test alarm set for ${alert.type} in 10 s via the real receiver.\n" +
                    "It uses the saved mode of ${alert.type}: " +
                    "${PrayerTimesModule.repository.notificationModesFlow.first()[alert.type]}.\n" +
                    "Lock the screen now."
            }
        }
    }

    report?.let { text ->
        OttrojjaAlertDialog(
            onDismiss = { report = null },
            onConfirm = { report = null },
            title = "Adhan debug",
            text = text,
            confirmText = "OK",
            cancelText = "Close",
        )
    }
}

@Composable
private fun DebugButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Text(label, color = MaterialTheme.colorScheme.error)
    }
}

private const val NO_LOCATION = "No location set yet."
private const val TEST_REQUEST_CODE = 47_199

private fun Context.isDebuggable(): Boolean =
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

/** An alert for the next obligatory prayer's type, but timed now (+ delay). */
private suspend fun testAlert(delayMillis: Long): PrayerAlert? {
    val repository = PrayerTimesModule.repository
    val settings = repository.settingsFlow.first()
    val location = settings.location ?: return null
    val now = System.currentTimeMillis()
    val next = repository.nextPrayer(now, settings)
    return PrayerAlert(
        type = next?.type ?: PrayerType.DHUHR,
        epochMillis = now + delayMillis,
        isJumuah = next?.isJumuah ?: false,
        locationName = location.displayName,
        timeZoneId = location.timeZoneId,
    )
}

private fun scheduleTestAlarm(context: Context, alert: PrayerAlert) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        TEST_REQUEST_CODE,
        PrayerAlarmReceiver.intent(context, alert).setAction(PrayerAlarmReceiver.ACTION_PRAYER_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    if (PrayerTimesModule.alarmScheduler.canScheduleExactAlarms()) {
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, alert.epochMillis, pendingIntent)
    } else {
        AlarmManagerCompat.setAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, alert.epochMillis, pendingIntent)
    }
}

private suspend fun diagnose(context: Context): String {
    val repository = PrayerTimesModule.repository
    val settings = repository.settingsFlow.first()
    val modes = repository.notificationModesFlow.first()
    val next = settings.location?.let { repository.nextPrayer(System.currentTimeMillis(), settings) }

    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    val ringer = when (audio.ringerMode) {
        AudioManager.RINGER_MODE_NORMAL -> "NORMAL"
        AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
        AudioManager.RINGER_MODE_SILENT -> "SILENT"
        else -> audio.ringerMode.toString()
    }
    val dnd = when (notifications.currentInterruptionFilter) {
        NotificationManager.INTERRUPTION_FILTER_ALL -> "off (ALL)"
        NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "PRIORITY"
        NotificationManager.INTERRUPTION_FILTER_ALARMS -> "ALARMS only"
        NotificationManager.INTERRUPTION_FILTER_NONE -> "NONE"
        else -> notifications.currentInterruptionFilter.toString()
    }
    val alarmVolume = "${audio.getStreamVolume(AudioManager.STREAM_ALARM)}/${audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)}"
    val notificationsAllowed = PrayerNotifications.canPostNotifications(context)
    val exact = PrayerTimesModule.alarmScheduler.canScheduleExactAlarms()
    val doNotDisturb = PrayerNotifications.isDoNotDisturbOn(context)
    val adhanOk = playable(context, PrayerType.DHUHR)
    val fajrOk = playable(context, PrayerType.FAJR)
    val batteryUnrestricted = power.isIgnoringBatteryOptimizations(context.packageName)

    val modeLines = PrayerType.entries.filter { it.isNotifiable }
        .joinToString("\n") { "  ${it.name}: ${modes[it] ?: PrayerNotificationMode.OFF}" }

    val nextMode = next?.let { modes[it.type] } ?: PrayerNotificationMode.OFF
    val verdict = when {
        settings.location == null -> "No location: no alarms are scheduled."
        !notificationsAllowed -> "Notifications not allowed: no alarms are scheduled."
        next == null -> "No next prayer could be calculated."
        nextMode == PrayerNotificationMode.OFF -> "${next.type} is OFF: nothing will happen."
        nextMode == PrayerNotificationMode.NOTIFICATION -> "${next.type} is set to NOTIFICATION, not ADHAN."
        doNotDisturb -> "${next.type} is ADHAN, but Do Not Disturb is on, so a plain notification is shown instead."
        !exact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            "Exact alarms not allowed: Android 12+ may refuse to start the adhan from the background; you'd get a notification instead."
        !adhanOk || !fajrOk -> "An adhan file can't be played: check res/raw/adhan and adhan_fajr."
        audio.getStreamVolume(AudioManager.STREAM_ALARM) == 0 -> "Alarm volume is 0: the adhan plays silently."
        else -> "${next.type} should play the adhan."
    }

    return buildString {
        appendLine("Verdict: $verdict")
        appendLine()
        appendLine("Next prayer: ${next?.type ?: "-"}")
        appendLine("Modes:")
        appendLine(modeLines)
        appendLine("Notifications allowed: $notificationsAllowed")
        appendLine("Exact alarms allowed: $exact")
        appendLine("Ringer: $ringer")
        appendLine("Do Not Disturb: $dnd")
        appendLine("Alarm volume: $alarmVolume")
        appendLine("adhan playable: $adhanOk, adhan_fajr playable: $fajrOk")
        append("Battery optimisation off: $batteryUnrestricted")
    }
}

private fun playable(context: Context, type: PrayerType): Boolean = try {
    MediaPlayer.create(context, PrayerNotifications.adhanSound(type))?.also { it.release() } != null
} catch (e: Exception) {
    false
}

package com.ottrojja.screens.reminderScreen.classes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ottrojja.broadcaseReceivers.ReminderReceiver
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import com.ottrojja.room.entities.Reminder
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: Reminder) {
        println("scheduling reminder")
        println(reminder)
        if (!reminder.isEnabled) return

        val triggerTime = reminder.nextTrigger

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) // FLAG_UPDATE_CURRENT handles rescheduling by auto updating intent with new reminder data that has same id (requestCode)
        println("setting alarm")
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    fun getIntervalDays(type: ReminderRepeatType): Int {
        return when (type) {
            ReminderRepeatType.DAILY -> 1
            ReminderRepeatType.DAYS_2 -> 2
            ReminderRepeatType.DAYS_3 -> 3
            ReminderRepeatType.DAYS_4 -> 4
            ReminderRepeatType.DAYS_5 -> 5
            ReminderRepeatType.DAYS_6 -> 6
            ReminderRepeatType.WEEKLY -> 7
        }
    }

    fun calculateInitialTrigger(reminder: Reminder): Long {
        println("calculate initial ${reminder.id}")
        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val intervalDays = getIntervalDays(reminder.repeatType)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
        }

        return calendar.timeInMillis
    }

    fun resolveNextTrigger(
        reminder: Reminder
    ): Pair<Boolean, Long> {

        val now = System.currentTimeMillis()

        val interval = getIntervalDays(reminder.repeatType)

        var next = reminder.nextTrigger
        var missed = false

        // move forward until it's in the future
        while (next <= now) {
            missed = true
            next = advanceTrigger(next, interval)
        }

        return Pair(missed, next)
    }

    fun advanceTrigger(current: Long, intervalDays: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = current
            add(Calendar.DAY_OF_YEAR, intervalDays)
        }
        return calendar.timeInMillis
    }

}

/*fun calculateNextTrigger(reminder: Reminder): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = reminder.lastTrigger
    }

    if (reminder.repeatType == ReminderRepeatType.ONCE) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminder.lastTrigger
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return if (calendar.timeInMillis > System.currentTimeMillis()) {
            calendar.timeInMillis
        } else {
            -1L // already passed, no next trigger
        }
    }

    // Set target time (hour/minute)
    calendar.set(Calendar.HOUR_OF_DAY, reminder.hour)
    calendar.set(Calendar.MINUTE, reminder.minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val now = System.currentTimeMillis()

    // Determine interval in days
    val intervalDays = when (reminder.repeatType) {
        ReminderRepeatType.DAILY -> 1
        ReminderRepeatType.DAYS_2 -> 2
        ReminderRepeatType.DAYS_3 -> 3
        ReminderRepeatType.DAYS_4 -> 4
        ReminderRepeatType.DAYS_5 -> 5
        ReminderRepeatType.DAYS_6 -> 6
        ReminderRepeatType.WEEKLY -> 7
        ReminderRepeatType.ONCE -> return 0L
    }

    // Move forward until it's in the future
    while (calendar.timeInMillis <= now) {
        calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
    }

    return calendar.timeInMillis
}*/

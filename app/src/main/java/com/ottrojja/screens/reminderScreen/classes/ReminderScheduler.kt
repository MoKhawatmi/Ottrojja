package com.ottrojja.screens.reminderScreen.classes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ottrojja.broadcaseReceivers.ReminderReceiver
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import com.ottrojja.room.entities.Reminder
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: Reminder) {
        println("doing reminder")
        println(reminder)
        if (!reminder.isEnabled) return

        val triggerTime = getNextTriggerTime(reminder)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) // FLAG_UPDATE_CURRENT handles rescheduling by auto updating intent with new reminder data that has same id
        println("setting alarm")
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
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

    private fun getNextTriggerTime(reminder: Reminder): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            when (reminder.repeatType) {
                ReminderRepeatType.ONCE -> return -1
                ReminderRepeatType.DAILY -> target.add(Calendar.DAY_OF_YEAR, 1)
                ReminderRepeatType.WEEKLY -> target.add(Calendar.WEEK_OF_YEAR, 1)
                ReminderRepeatType.DAYS_2 -> target.add(Calendar.DAY_OF_YEAR, 2)
                ReminderRepeatType.DAYS_3 -> target.add(Calendar.DAY_OF_YEAR, 3)
                ReminderRepeatType.DAYS_4 -> target.add(Calendar.DAY_OF_YEAR, 4)
                ReminderRepeatType.DAYS_5 -> target.add(Calendar.DAY_OF_YEAR, 5)
                ReminderRepeatType.DAYS_6 -> target.add(Calendar.DAY_OF_YEAR, 6)
            }
        }

        return target.timeInMillis
    }

    fun calculateNextTrigger(reminder: Reminder): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminder.lastTrigger
        }
        println("received to calc")
        println(reminder.lastTrigger)

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
            calendar.add(java.util.Calendar.DAY_OF_YEAR, intervalDays)
        }

        return calendar.timeInMillis
    }
}
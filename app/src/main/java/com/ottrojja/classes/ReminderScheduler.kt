package com.ottrojja.classes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ottrojja.broadcaseReceivers.ReminderReceiver
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
}
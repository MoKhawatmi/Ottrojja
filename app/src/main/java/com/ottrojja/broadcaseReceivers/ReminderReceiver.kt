package com.ottrojja.broadcaseReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.ottrojja.classes.NotificationHelper.showNotification
import com.ottrojja.classes.ReminderRepeatType
import com.ottrojja.classes.ReminderScheduler
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.database.QuranDatabase
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        println("received notification")

        val id = intent.getIntExtra("REMINDER_ID", -1)
        if (id == -1) {
            println("false id -1"); return
        }
        println("sent id $id")
        val db = DatabaseProvider.getDatabase(context)
        val repository = ReminderRepository(db.reminderDao())
        val scheduler = ReminderScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {

            val reminder = repository.getById(id)
            if (reminder == null) {
                println("reminder not found")
                return@launch
            }

            println("found reminder")
            println(reminder)

            if (!reminder.isEnabled) return@launch
            println("is enabled")

            showNotification(context, reminder.id, reminder.title, "${reminder.customMessage}")

            if (reminder.repeatType != ReminderRepeatType.ONCE) {
                scheduler.scheduleReminder(reminder)
            }
        }
    }
}


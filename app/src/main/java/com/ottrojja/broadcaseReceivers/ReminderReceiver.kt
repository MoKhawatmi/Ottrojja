package com.ottrojja.broadcaseReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ottrojja.screens.reminderScreen.classes.ReminderWorker

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getIntExtra("REMINDER_ID", -1)
        if (id == -1) return

        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workDataOf("REMINDER_ID" to id))
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }
    /*
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

                val reminderMessage = if (reminder.isMain) DynamicMessageProvider.getMessage() else reminder.customMessage

                showNotification(context, reminder.id, reminder.title, "$reminderMessage")

                // update lastTrigger in reminder to allow calculation of next trigger time
                repository.updateReminder(reminder.copy(lastTrigger = System.currentTimeMillis()))

                if (reminder.repeatType != ReminderRepeatType.ONCE) {
                    // schedule repetition
                    scheduler.scheduleReminder(reminder)
                } else {
                    // disable no repetition reminder after firing
                    repository.updateReminder(reminder.copy(isEnabled = false))
                }
            }
        }*/
}


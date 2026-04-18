package com.ottrojja.broadcaseReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ottrojja.classes.Helpers.validateReminders
import com.ottrojja.screens.reminderScreen.classes.ReminderScheduler
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.reminderScreen.classes.RescheduleRemindersWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {


        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            validateReminders(context)
        }
    }
    /* if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

         val db = DatabaseProvider.getDatabase(context)
         val repository = ReminderRepository(db.reminderDao())
         val scheduler = ReminderScheduler(context)
         CoroutineScope(Dispatchers.IO).launch {
             val reminders = repository.getEnabledReminders()

             reminders.forEach {
                 scheduler.scheduleReminder(it)
             }
         }
     }*/
}

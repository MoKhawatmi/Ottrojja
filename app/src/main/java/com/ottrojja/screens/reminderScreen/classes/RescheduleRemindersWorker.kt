package com.ottrojja.screens.reminderScreen.classes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.repositories.ReminderRepository

class RescheduleRemindersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {

            val db = DatabaseProvider.getDatabase(applicationContext)
            val repository = ReminderRepository(db.reminderDao())
            val scheduler = ReminderScheduler(applicationContext)

            val reminders = repository.getEnabledReminders()

            reminders.forEach {
                try {
                    scheduler.scheduleReminder(it)
                } catch (e: Exception) {
                    reportException(e, "RescheduleRemindersWorker", "error trying scheduler.scheduleReminder")
                }
            }

            Result.success()
        } catch (e: Exception) {
            reportException(e, "RescheduleRemindersWorker", "error in process")
            Result.retry()
        }
    }
}
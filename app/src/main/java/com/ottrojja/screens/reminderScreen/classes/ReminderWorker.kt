package com.ottrojja.screens.reminderScreen.classes

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.ReminderNotificationHelper.showReminderNotification
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.repositories.ReminderRepository

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getLong("REMINDER_ID", -1L)

            //Result.failure()= “This work permanently failed. Do NOT retry.”
            if (id == -1L) {
                Log.e("ReminderWorker", "Missing or invalid REMINDER_ID")
                return Result.failure()
            }


            val db = DatabaseProvider.getDatabase(applicationContext)
            val repository = ReminderRepository(db.reminderDao())
            val scheduler = ReminderScheduler(applicationContext)

            val reminder = repository.getById(id)

            if (reminder == null) {
                Log.e("ReminderWorker", "Reminder not found: $id")
                return Result.failure()
            }

            if (!reminder.isEnabled) return Result.success()

            val (shouldFireNow, nextTrigger) = scheduler.resolveNextTrigger(reminder)


            val message = if (reminder.isMain || reminder.customMessage?.isBlank() == true) {
                DynamicMessageProvider.getMessage()
            } else reminder.customMessage

            if (shouldFireNow) {
                showReminderNotification(
                    applicationContext,
                    reminder.id,
                    reminder.title,
                    message ?: ""
                )
            }

            if (nextTrigger == -1L) {
                repository.updateReminder(reminder.copy(isEnabled = false))
            } else {
                val updatedReminder = reminder.copy(nextTrigger = nextTrigger)
                repository.updateReminder(updatedReminder)
                scheduler.scheduleReminder(updatedReminder)
            }

            Result.success()

        } catch (e: Exception) {
            reportException(e, "ReminderWorker")
            Result.retry()
        }
    }
}
package com.ottrojja.screens.reminderScreen.classes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.NotificationHelper.showNotification
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.repositories.ReminderRepository

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getInt("REMINDER_ID", -1)

            //Result.failure()= “This work permanently failed. Do NOT retry.”
            if (id == -1) return Result.failure()

            val db = DatabaseProvider.getDatabase(applicationContext)
            val repository = ReminderRepository(db.reminderDao())
            val scheduler = ReminderScheduler(applicationContext)

            val reminder = repository.getById(id)
                ?: return Result.failure()

            if (!reminder.isEnabled) return Result.success()

            val message = if (reminder.isMain || reminder.customMessage?.isBlank() == true) {
                DynamicMessageProvider.getMessage()
            } else reminder.customMessage

            showNotification(
                applicationContext,
                reminder.id,
                reminder.title,
                message ?: ""
            )

            repository.updateReminder(
                reminder.copy(lastTrigger = System.currentTimeMillis())
            )

            if (reminder.repeatType != ReminderRepeatType.ONCE) {
                scheduler.scheduleReminder(reminder)
            } else {
                repository.updateReminder(
                    reminder.copy(isEnabled = false)
                )
            }

            Result.success()

        } catch (e: Exception) {
            reportException(e, "ReminderWorker")
            Result.retry() // 🔥 huge advantage
        }
    }
}
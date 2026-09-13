package com.ottrojja.screens.reminderScreen.classes

import android.content.Context
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository

class ReminderManager(
    private val repository: ReminderRepository,
    private val context: Context
) {
    private val scheduler = ReminderScheduler(context)

    suspend fun createReminder(
        title: String,
        customMessage: String?,
        hour: Int,
        minute: Int,
        repeatType: ReminderRepeatType
    ): Long {
        val reminder = Reminder(
            title = title,
            customMessage = customMessage,
            hour = hour,
            minute = minute,
            repeatType = repeatType,
            nextTrigger = 0L
        )

        val initialTrigger = scheduler.calculateInitialTrigger(reminder)
        reminder.nextTrigger = initialTrigger

        val reminderId = repository.insertReminder(reminder)

        val insertedReminder = repository.getById(reminderId)
            ?: throw Exception("Could not retrieve newly inserted reminder")

        scheduler.scheduleReminder(insertedReminder)

        return reminderId
    }

    suspend fun upsertReminder(reminder: Reminder): Reminder {
        val initialTrigger = scheduler.calculateInitialTrigger(reminder)
        reminder.nextTrigger = initialTrigger

        val result = repository.upsertReminder(reminder)

        val reminderId = if (reminder.id != 0L) {
            reminder.id as Long
        } else {
            result
        }

        val savedReminder = repository.getById(reminderId)
            ?: throw Exception("Could not retrieve newly upserted reminder")

        scheduler.scheduleReminder(savedReminder)

        return savedReminder
    }

    suspend fun setReminderEnabled(
        reminder: Reminder,
        enabled: Boolean
    ) {
        val updatedReminder = reminder.copy(
            isEnabled = enabled
        )

        repository.updateReminder(updatedReminder)

        if (enabled) {
            scheduler.scheduleReminder(updatedReminder)
        } else {
            scheduler.cancelReminder(updatedReminder)
        }
    }

    suspend fun deleteReminder(reminder: Reminder) {
        scheduler.cancelReminder(reminder)
        repository.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        repository.getById(id)?.let {
            deleteReminder(it)
        }
    }

    suspend fun insertMainReminderIfNotExists() {
        val mainReminder = Reminder(
            title = "المذكر اليومي",
            customMessage = null,
            hour = 12,
            minute = 0,
            repeatType = ReminderRepeatType.DAILY,
            isEnabled = true,
            isMain = true,
            nextTrigger = 0L
        )

        val initialTrigger = scheduler.calculateInitialTrigger(mainReminder)
        mainReminder.nextTrigger = initialTrigger

        repository.insertMainIfNotExists(mainReminder)
    }
}
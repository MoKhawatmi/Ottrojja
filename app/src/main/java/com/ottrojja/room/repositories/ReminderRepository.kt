package com.ottrojja.room.repositories

import androidx.room.Transaction
import com.ottrojja.room.dao.ReminderDao
import com.ottrojja.room.entities.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: ReminderDao
) {

    suspend fun upsertReminder(reminder: Reminder): Long {
        return dao.upsertReminder(reminder)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return dao.insertReminder(reminder)
    }

    suspend fun getMainReminder(): Reminder? {
        return dao.getMainReminder()
    }


    suspend fun updateReminder(reminder: Reminder) {
        return dao.updateReminder(reminder)
    }

    fun getAllReminders(): Flow<List<Reminder>> = dao.getAll()

    suspend fun getById(id: Int): Reminder? {
        return dao.getById(id)
    }

    suspend fun getEnabledReminders() = dao.getEnabled()

    suspend fun deleteReminder(reminder: Reminder) {
        dao.delete(reminder)
    }

    suspend fun insertMainIfNotExists(mainReminder: Reminder) {
        dao.insertMainIfNotExists(mainReminder)
    }
}
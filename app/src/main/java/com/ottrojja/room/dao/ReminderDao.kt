package com.ottrojja.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.ottrojja.room.entities.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Upsert
    suspend fun upsertReminder(reminder: Reminder): Long

    @Query("SELECT * FROM Reminder WHERE isMain = 1 LIMIT 1")
    suspend fun getMainReminder(): Reminder?

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("SELECT * FROM reminder ORDER BY id ASC")
    fun getAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getById(id: Int): Reminder?

    @Query("SELECT * FROM reminder WHERE isEnabled = 1")
    suspend fun getEnabled(): List<Reminder>

    @Delete
    suspend fun delete(reminder: Reminder)

    @Transaction
    suspend fun insertMainIfNotExists(mainReminder: Reminder) {
        val existing = getMainReminder()
        if (existing == null) {
            insertReminder(mainReminder)
        }
    }


}
package com.ottrojja.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.ottrojja.room.entities.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder): Long

    @Upsert
    suspend fun upsertReminder(reminder: Reminder): Long

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


}
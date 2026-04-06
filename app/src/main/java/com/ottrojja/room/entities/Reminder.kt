package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ottrojja.classes.ReminderRepeatType

@Entity
data class Reminder(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    val title: String,
    val customMessage: String?,
    val hour: Int,
    val minute: Int,
    val repeatType: ReminderRepeatType, // DAILY, WEEKLY, ONCE
    val isEnabled: Boolean= true,
    val isMain: Boolean = false
)
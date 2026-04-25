package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType

@Entity
data class Reminder(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    val title: String,
    val customMessage: String?,
    val hour: Int,
    val minute: Int,
    val repeatType: ReminderRepeatType,
    val isEnabled: Boolean = true,
    var nextTrigger: Long,
    val isMain: Boolean = false
)
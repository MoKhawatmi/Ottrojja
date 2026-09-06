package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ottrojja.screens.Khitmah.classes.KhitmahStatus


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Reminder::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("reminderId")
    ]
)
data class Khitmah(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "targetDate") val targetDate: String? = null, //yyyy-MM-dd
    @ColumnInfo(name = "reminderId") val reminderId: Long? = null, //Optional reference to an existing reminder.
    @ColumnInfo(name = "status") val status: KhitmahStatus = KhitmahStatus.IN_PROGRESS,
    @ColumnInfo(name = "pagesRead") val pagesRead: Int = 0
)
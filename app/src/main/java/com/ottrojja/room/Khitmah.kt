package com.ottrojja.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Khitmah(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "latestPage") val latestPage: String = "1",
    @ColumnInfo(name = "isComplete") val isComplete: Boolean = false
)

package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey @ColumnInfo(name = "pageNum") val pageNum: String,
    @ColumnInfo(name = "timeStamp") val timeStamp: Long = System.currentTimeMillis()
)

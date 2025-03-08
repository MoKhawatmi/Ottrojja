package com.ottrojja.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Khitmah::class, // Parent table
            parentColumns = arrayOf("id"), //primary key in Khitmah
            childColumns = arrayOf("khitmahId"), // Foreign key in KhitmahMark
            onDelete = ForeignKey.CASCADE // if a student is deleted, their subjects are deleted
        )
    ]
)
data class KhitmahMark(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "khitmahId") val khitmahId: Int,
    @ColumnInfo(name = "timeStamp") val timeStamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "pageNum") val pageNum: String
)

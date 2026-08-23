package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Khitmah::class,
            parentColumns = ["id"],
            childColumns = ["khitmahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("khitmahId"),
        Index("readingDate")
    ]
)
data class KhitmahSession(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "khitmahId") val khitmahId: Long,
    @ColumnInfo(name = "startPage") val startPage: Int,
    @ColumnInfo(name = "startVerseId") val startVerseId: Int,
    @ColumnInfo(name = "startSurahId") val startSurahId: Int,
    @ColumnInfo(name = "endPage") val endPage: Int,
    @ColumnInfo(name = "endVerseId") val endVerseId: Int,
    @ColumnInfo(name = "endSurahId") val endSurahId: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "readingDate") val readingDate: String, //yyyy-MM-dd
    @ColumnInfo(name = "reflection") val reflection: String? = null
)

package com.ottrojja.screens.mainScreen

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ChapterData(
    @ColumnInfo(name = "chapterName") val chapterName: String,
    @ColumnInfo(name = "chapterStartPage") val chapterStartPage: String,
    @PrimaryKey @ColumnInfo(name = "surahId") val surahId: Int,
    @ColumnInfo(name = "chapterType") val chapterType: String,
    @ColumnInfo(name = "verseCount") val verseCount: Int
)

package com.ottrojja.room.entities

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
    @ColumnInfo(name = "nameMeaning") val nameMeaning: String,
    @ColumnInfo(name = "namingReason") val namingReason: String,
    @ColumnInfo(name = "otherNames") val otherNames: String,
    @ColumnInfo(name = "generalPurpose") val generalPurpose: String,
    @ColumnInfo(name = "reasonOfRevelation") val reasonOfRevelation: String,
    @ColumnInfo(name = "virtues") val virtues: String,
    @ColumnInfo(name = "surahEvents") val surahEvents: String,
    @ColumnInfo(name = "verseCount") val verseCount: Int
)


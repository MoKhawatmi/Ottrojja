package com.ottrojja.screens.quranScreen

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class TafseerData(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "sura") val sura: String,
    @ColumnInfo(name = "aya") val aya: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "tafseer") val tafseer: String
)

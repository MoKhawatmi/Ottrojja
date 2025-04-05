package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CauseOfRevelation
    (
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "sura") val sura: Int,
    @ColumnInfo(name = "verses") val verses: String,
    @ColumnInfo(name = "text") val text: String,
)

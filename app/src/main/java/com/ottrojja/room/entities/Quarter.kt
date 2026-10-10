package com.ottrojja.room.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Keep
@Entity
@Serializable
data class Quarter(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "sura") val sura: String,
    @ColumnInfo(name = "aya") val aya: String,
    @ColumnInfo(name = "pageNum") val pageNum: String,
    @ColumnInfo(name = "part") val part: String,
    @ColumnInfo(name = "hizb") val hizb: String
)


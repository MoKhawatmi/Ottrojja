package com.ottrojja.room.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Keep
@Entity
@Serializable
data class Azkar(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "azkarTitle")
    val azkarTitle: String,

    @ColumnInfo(name = "ytLink")
    val ytLink: String,

    @ColumnInfo(name = "zikrAddress")
    val zikrAddress: String,

    @ColumnInfo(name = "azkarText")
    val azkarText: String,

    @ColumnInfo(name = "image")
    val image: String
)
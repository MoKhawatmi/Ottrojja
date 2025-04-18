package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Azkar(
    @PrimaryKey @ColumnInfo(name = "azkarTitle") val azkarTitle: String,
    @ColumnInfo(name = "ytLink") val ytLink: String,
    @ColumnInfo(name = "firebaseAddress") val firebaseAddress: String,
    @ColumnInfo(name = "azkarText") val azkarText: String,
    @ColumnInfo(name = "image") val image: String
)

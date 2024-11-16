package com.ottrojja.screens.mainScreen

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class PartData(
    @PrimaryKey @ColumnInfo(name = "partId") val partId: String,
    @ColumnInfo(name = "partName") val partName: String,
    @ColumnInfo(name = "partStartPage") val partStartPage: String,
    @ColumnInfo(name = "firstWords") val firstWords: String
)

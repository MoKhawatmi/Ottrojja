package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TasabeehList::class, // Parent table
            parentColumns = arrayOf("id"), //primary key in TasabeehList
            childColumns = arrayOf("listId"), // Foreign key in CustomTasbeeh
            onDelete = ForeignKey.CASCADE // if a list is deleted, their items are deleted
        )
    ]
)
data class CustomTasbeeh(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "listId") val listId: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "count") val count: Int,
)

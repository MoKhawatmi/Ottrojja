package com.ottrojja.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList

data class ListWithTasabeeh(
    @Embedded val tasabeehList: TasabeehList,
    @Relation(
        parentColumn = "id",  // Refers to the PRIMARY KEY in the TasabeehList table
        entityColumn = "listId"   // Refers to the FOREIGN KEY in the CustomTasbeeh table
    )
    val customTasabeeh: List<CustomTasbeeh>  // This will automatically fetch all tasabeeh related to the list
)

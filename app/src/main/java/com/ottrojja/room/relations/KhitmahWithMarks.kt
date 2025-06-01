package com.ottrojja.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahMark

data class KhitmahWithMarks(
    @Embedded val khitmah: Khitmah,  // Embeds the Khitmah data in this class
    @Relation(
        parentColumn = "id",  // Refers to the PRIMARY KEY in the Khitmah table
        entityColumn = "khitmahId"   // Refers to the FOREIGN KEY in the KhitmahMark table
    )
    val marks: List<KhitmahMark>  // This will automatically fetch all marks related to the khitmah

)
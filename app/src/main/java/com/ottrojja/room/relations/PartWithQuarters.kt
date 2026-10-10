package com.ottrojja.room.relations

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.ottrojja.room.entities.Quarter
import com.ottrojja.screens.mainScreen.PartData

@Keep
data class PartWithQuarters(
    @Embedded val part: PartData,  // Embeds the part data in this class
    @Relation(
        parentColumn = "partId",  // Refers to the PRIMARY KEY in the PartData table
        entityColumn = "part"   // Refers to the FOREIGN KEY in the Quarter table
    )
    val quarters: List<Quarter>  // This will automatically fetch all quarters related to the part

)

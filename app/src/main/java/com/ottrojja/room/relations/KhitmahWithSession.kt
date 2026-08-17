package com.ottrojja.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession

data class KhitmahWithSession(
    @Embedded
    val khitmah: Khitmah,

    @Relation(
        parentColumn = "id",
        entityColumn = "khitmahId"
    )
    val sessions: List<KhitmahSession>
)

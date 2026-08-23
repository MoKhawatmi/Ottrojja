package com.ottrojja.screens.Khitmah.classes

import androidx.room.Embedded
import com.ottrojja.room.entities.Khitmah

data class KhitmahListItem(
    @Embedded
    val khitmah: Khitmah,
    val latestEndPage: Int?,
    val latestEndVerseId: Int?,
    val latestReadingDate: String?,
    val sessionCount: Int
)
package com.ottrojja.screens.Khitmah.classes

import com.ottrojja.room.entities.KhitmahSession

data class DayReadingInfo(
    val sessionCount: Int,
    val sessions: List<KhitmahSession>
)
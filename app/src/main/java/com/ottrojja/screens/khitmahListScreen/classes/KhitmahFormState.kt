package com.ottrojja.screens.khitmahListScreen.classes

import com.ottrojja.room.entities.Khitmah

data class KhitmahFormState(
    val khitmah: Khitmah = Khitmah(title = "", targetDate = ""),
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "12:00",
    val mode: KhitmahFormMode = KhitmahFormMode.CREATE
)
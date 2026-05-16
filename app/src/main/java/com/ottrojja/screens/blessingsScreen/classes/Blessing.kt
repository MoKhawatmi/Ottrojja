package com.ottrojja.screens.blessingsScreen.classes

import kotlinx.serialization.Serializable

@Serializable
data class Blessing(
    val id: Int,
    val text: String,
    val category: BlessingCategory = BlessingCategory.GENERAL)
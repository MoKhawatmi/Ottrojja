package com.ottrojja.screens.blessingsScreen.classes

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Keep
@Serializable
data class Blessing(
    val id: Int,
    val text: String,
    val category: BlessingCategory = BlessingCategory.GENERAL,
)
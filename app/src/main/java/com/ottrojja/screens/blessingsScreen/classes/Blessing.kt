package com.ottrojja.screens.blessingsScreen.classes

import com.ottrojja.classes.TextParser.TextBlock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Blessing(
    val id: Int,
    val text: String,
    val category: BlessingCategory = BlessingCategory.GENERAL,

    @Transient
    val content: List<TextBlock> = emptyList()
)
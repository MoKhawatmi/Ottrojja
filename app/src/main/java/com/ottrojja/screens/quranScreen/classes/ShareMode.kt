package com.ottrojja.screens.quranScreen.classes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ottrojja.composables.selectableChips.SelectableChip

enum class ShareMode(
    override val title: String,
    override val icon: ImageVector,
    override val color: Color,
    override val subtitle: String? = null
) : SelectableChip {

    TEXT(
        title = "نص",
        icon = Icons.Outlined.ShortText,
        color = Color(0xFF1976D2)
    ),


    IMAGE(
        title = "صورة",
        icon = Icons.Outlined.Image,
        color = Color(0xFF1976D2)
    ),
}
package com.ottrojja.screens.blessingsScreen.classes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ottrojja.R
import com.ottrojja.composables.selectableChips.SelectableChip

enum class BlessingCategory(
    override val title: String,
    override val icon: ImageVector,
    override val color: Color,
    override val subtitle: String? = null
) : SelectableChip {

    ALL(
        title = "الكل",
        icon = Icons.Outlined.Hub,
        color = Color(0xFF1976D2)
    ),


    GENERAL(
        title = "عام",
        icon = Icons.Outlined.Dashboard,
        color = Color(0xFF1976D2)
    ),

    STORY(
        title = "قَصص",
        icon = Icons.Outlined.AutoStories,
        color = Color(0xFF2E7D32)
    ),

    HISTORIC(
        title = "تاريخي",
        icon = Icons.Outlined.HistoryEdu,
        color = Color(0xFF8D6E63)
    )
}
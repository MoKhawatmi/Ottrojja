package com.ottrojja.composables.selectableChips

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

interface SelectableChip {
    val title: String
    val subtitle: String?
    val icon: ImageVector
    val color: Color
}

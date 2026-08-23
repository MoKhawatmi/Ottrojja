package com.ottrojja.composables.ottrojjaFlexibleActions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class FlexibleAction(
    val text: String,
    val bgColor: Color,
    val textColor: Color,
    val action: () -> Unit,
    val icon: ImageVector? = null
)

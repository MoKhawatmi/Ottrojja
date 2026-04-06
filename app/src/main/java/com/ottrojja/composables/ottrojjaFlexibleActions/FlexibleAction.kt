package com.ottrojja.composables.ottrojjaFlexibleActions

import androidx.compose.ui.graphics.Color

data class FlexibleAction(
    val text: String,
    val bgColor: Color,
    val textColor: Color,
    val action: () -> Unit
)

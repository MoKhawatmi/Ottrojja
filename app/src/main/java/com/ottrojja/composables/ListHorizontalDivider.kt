package com.ottrojja.composables

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ListHorizontalDivider(){
    HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
}
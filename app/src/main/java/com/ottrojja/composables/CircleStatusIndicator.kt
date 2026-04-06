package com.ottrojja.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.complete_green

@Composable
fun CircleStatusIndicator(status: Boolean,
                          truthyColor: Color,
                          falsyColor: Color,
                          iconDescription: String) {
    Icon(
        imageVector = Icons.Filled.Circle,
        contentDescription = iconDescription,
        tint = if (status) truthyColor else falsyColor,
        modifier = Modifier
            .padding(end = 4.dp)
            .size(16.dp)
    )
}
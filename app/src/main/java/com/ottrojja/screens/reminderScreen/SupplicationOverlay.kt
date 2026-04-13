package com.ottrojja.screens.reminderScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SupplicationOverlay(onDismiss: () -> Unit) {

    Box(
        modifier = Modifier
            .width(300.dp)
            .height(100.dp)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Reminder 🤲",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
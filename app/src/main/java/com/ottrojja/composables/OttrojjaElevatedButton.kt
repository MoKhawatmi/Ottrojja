package com.ottrojja.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults.elevatedButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OttrojjaElevatedButton(onClick: () -> Unit, icon: ImageVector, iconSize: Dp = 24.dp) {
    ElevatedButton(
        onClick = { onClick() },
        elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape,
        modifier = Modifier
            .padding(4.dp, 0.dp)
            .clip(CircleShape)
    ) {
        Icon(
            icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
    }
}
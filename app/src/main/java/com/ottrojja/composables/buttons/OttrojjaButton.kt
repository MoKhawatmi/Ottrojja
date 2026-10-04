package com.ottrojja.composables.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconBefore: ImageVector? = null,
    iconAfter: ImageVector? = null
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            if (iconBefore != null) {
                Icon(
                    imageVector = iconBefore,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "button icon before"
                )
            }
            OttrojjaText(text, style = OttrojjaTheme.typography.bodyMedium)
            if (iconAfter != null) {
                Icon(
                    imageVector = iconAfter,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "button icon after"
                )
            }

        }
    }
}
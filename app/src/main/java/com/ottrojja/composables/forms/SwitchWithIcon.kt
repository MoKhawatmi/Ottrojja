package com.ottrojja.composables.forms

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SwitchWithIcon(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector = Icons.Default.Check,
    enabled: Boolean = true
) {
    Switch(
        enabled = enabled,
        checked = checked,
        onCheckedChange = {
            onCheckedChange(it)
        },
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            null
        },
    )
}

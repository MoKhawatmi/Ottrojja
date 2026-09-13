package com.ottrojja.composables.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaSelect(label: String? = null,
                   value: String?,
                   onClick: () -> Unit,
                   disabled: Boolean = false,
                   error: String? = null,
                   clearable: Boolean = false,
                   onClear: () -> Unit = {}
) {
    val backgroundColor = if (!disabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
    val contentColor = if (!disabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary


    OttrojjaFormFieldContainer(
        modifier = Modifier.clickable(enabled = !disabled, onClick = { onClick() }),
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OttrojjaText(
                text = if (label != null && label.isNotEmpty()) "$label: ${value ?: ""}" else value ?: "",
                style = OttrojjaTheme.typography.bodyMedium,
                color = contentColor
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.End)) {
                if (clearable && value?.isNotBlank() == true) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "clear",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable(onClick = { onClear() })
                    )
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Open select options",
                    tint = contentColor
                )
            }

        }
        if (error != null && !error.isEmpty()) {
            OttrojjaFieldError(error)
        }

    }
}
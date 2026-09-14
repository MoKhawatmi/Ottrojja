package com.ottrojja.composables.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    disabled: Boolean = false
) {
    val backgroundColor = if (!disabled) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.outline
    val contentColor = if (!disabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiary

    OttrojjaFormFieldContainer(
        modifier = Modifier.toggleable(
            value = checked,
            enabled = !disabled,
            onValueChange = onCheckedChange
        ),
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OttrojjaText(
                text = label,
                textAlign = TextAlign.Center,
                style = OttrojjaTheme.typography.bodyMedium,
                color = contentColor
            )

            SwitchWithIcon(
                checked = checked,
                onCheckedChange = onCheckedChange,
                disabled = disabled
            )
        }
    }
}
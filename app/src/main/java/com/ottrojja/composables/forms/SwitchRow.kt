package com.ottrojja.composables.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    enabled: Boolean = true
) {

    val colors = OttrojjaFieldDefaults.colors(enabled)


    OttrojjaFormFieldContainer(
        modifier = Modifier.toggleable(
            value = checked,
            enabled = enabled,
            onValueChange = onCheckedChange,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ),
        backgroundColor = colors.background,
        contentColor = colors.content,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OttrojjaText(
                text = label,
                textAlign = TextAlign.Start,
                style = OttrojjaTheme.typography.bodyMedium,
                color = colors.content,
                modifier = Modifier.weight(1f)
            )

            SwitchWithIcon(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
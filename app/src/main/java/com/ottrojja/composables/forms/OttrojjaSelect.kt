package com.ottrojja.composables.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaSelect(
    value: String?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    clearable: Boolean = false,
    onClear: () -> Unit = {},
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clearInteractionSource = remember { MutableInteractionSource() }

    val colors = OttrojjaFieldDefaults.colors(enabled)
    val shape = OttrojjaFieldDefaults.shape

    val hasValue = !value.isNullOrBlank()
    val showClear = clearable && hasValue && enabled

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.background, shape)
            .border(2.dp, colors.border, shape)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = colors.content,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                OttrojjaText(
                    text = if (hasValue) value else "إختيار",
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = colors.content,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear selection",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable(
                            enabled = enabled,
                            indication = null,
                            interactionSource = clearInteractionSource,
                            onClick = onClear
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open select options",
                    tint = colors.content
                )
            }
        }
    }
}


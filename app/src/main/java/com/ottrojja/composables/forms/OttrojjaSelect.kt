package com.ottrojja.composables.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OttrojjaSelect(value: String?, onClick: () -> Unit, disabled: Boolean = false, error: String?) {
    val backgroundColor = if (!disabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
    val contentColor = if (!disabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(2.dp, contentColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !disabled) { onClick() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = value ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                color = contentColor
            )
            Icon(Icons.Default.ArrowDropDown,
                contentDescription = "Open select options",
                tint = contentColor
            )
        }
        if (error != null && !error.isEmpty()) {
            OttrojjaFieldError(error)
        }
    }
}
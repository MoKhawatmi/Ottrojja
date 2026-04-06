package com.ottrojja.composables.forms

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OttrojjaSelect(value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .border(2.dp,
                MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onSecondary
        )
        Icon(Icons.Default.ArrowDropDown,
            contentDescription = "Open select options",
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }

}
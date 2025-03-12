package com.ottrojja.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OttrojjaTopBar(content: @Composable() () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ) {
            content()
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.secondary)
    }
}
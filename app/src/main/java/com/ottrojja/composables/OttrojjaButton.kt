package com.ottrojja.composables

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun OttrojjaButton(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
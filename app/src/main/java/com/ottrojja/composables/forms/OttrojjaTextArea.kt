package com.ottrojja.composables.forms

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OttrojjaTextArea(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it) },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Adjust height as needed
            .padding(vertical = 16.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = 6, // Allow multiple lines
        singleLine = false,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            cursorColor = MaterialTheme.colorScheme.onSecondary,
            focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
            focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
package com.ottrojja.screens.CustomTasabeehListScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaDialog

@Composable
fun AddCustomTasbeehDialog(onDismiss: () -> Unit, onConfirm: (text: String, count: Int) -> Unit) {
    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
    ) {
        var tasbeehText by remember { mutableStateOf("") }
        var tasbeehCount by remember { mutableStateOf("") }


        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "إضافة ذكر", textAlign = TextAlign.Center)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = tasbeehText,
                    onValueChange = { tasbeehText = it },
                    label = { Text("نص الذكر") },
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
                    shape = RoundedCornerShape(8.dp) // Rounded corners for a softer look
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = tasbeehCount,
                    onValueChange = { newText ->
                        val filteredText = newText.filter { it.isDigit() } // Allow only digits
                        val intValue = filteredText.toIntOrNull() ?: 0 // Convert to integer safely
                        if (intValue <= 1000000) {
                            tasbeehCount = filteredText // Update only if within limit
                        }
                    },
                    label = { Text("مرات الذكر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
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
                    )
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (tasbeehText.isNotBlank() && tasbeehCount.isNotBlank()) {
                            onConfirm(tasbeehText, tasbeehCount.toInt())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "إضافة",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

        }
    }

}
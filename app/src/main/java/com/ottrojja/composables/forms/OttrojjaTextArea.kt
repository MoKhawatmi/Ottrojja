package com.ottrojja.composables.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
fun OttrojjaTextArea(value: String?,
                     onChange: (String) -> Unit,
                     label: String,
                     maxLength: Int? = null,
                     disabled: Boolean = false,
                     error: String? = ""
) {
    val backgroundColor = if (!disabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
    val contentColor = if (!disabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary


    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = {
                val newValue = if (maxLength != null) {
                    it.take(maxLength)
                } else {
                    it
                }
                onChange(newValue)
            },
            label = {
                Text(label,
                    modifier = Modifier
                        .background(backgroundColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    color = contentColor
                )
            },
            enabled = !disabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust height as needed
                .padding(vertical = 16.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            maxLines = 6, // Allow multiple lines
            singleLine = false,
            colors = TextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                cursorColor = contentColor,
                focusedIndicatorColor = contentColor,
                unfocusedIndicatorColor = contentColor,
                focusedLabelColor = contentColor,
                unfocusedLabelColor = contentColor,
                disabledTextColor = contentColor,
                disabledLabelColor = contentColor,
                disabledContainerColor = backgroundColor,
                focusedSupportingTextColor = MaterialTheme.colorScheme.onSecondary,
                disabledSupportingTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSecondary

            ),
            shape = RoundedCornerShape(8.dp),
            supportingText = {
                maxLength?.let {
                    Text("${value?.length ?: 0} / $maxLength")
                }
            }
        )
        if (error != null && !error.isEmpty()) {
            OttrojjaFieldError(error)
        }
    }


}
package com.ottrojja.composables.forms

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
fun OttrojjaTextField(value: String?,
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
            modifier = Modifier.fillMaxWidth(),
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
                if (disabled && value?.isNotBlank()==true) null else Text(label,
                    modifier = Modifier
                        .background(color = Color.Transparent, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    color = contentColor
                )
            },
            enabled = !disabled,
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
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
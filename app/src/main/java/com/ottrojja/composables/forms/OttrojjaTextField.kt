package com.ottrojja.composables.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.OttrojjaTheme


enum class TextFieldInputType {
    TEXT,
    NUMBER
}

@Composable
fun OttrojjaTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    maxValue: Long? = null,
    enabled: Boolean = true,
    inputType: TextFieldInputType = TextFieldInputType.TEXT,
    leadingIcon: ImageVector? = Icons.Default.EditNote
) {
    val colors = OttrojjaFieldDefaults.colors(enabled)
    val shape = OttrojjaFieldDefaults.shape

    val keyboardType = when (inputType) {
        TextFieldInputType.TEXT -> KeyboardType.Text
        TextFieldInputType.NUMBER -> KeyboardType.Number
    }

    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.background)
            .border(
                width = 2.dp,
                color = colors.border,
                shape = shape
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        value = value,
        onValueChange = { newValue ->
            val filteredValue = when (inputType) {
                TextFieldInputType.TEXT -> newValue
                TextFieldInputType.NUMBER -> newValue.filter(Char::isDigit)
            }

            val limitedValue = maxLength?.let {
                filteredValue.take(it)
            } ?: filteredValue

            if (
                inputType != TextFieldInputType.NUMBER ||
                maxValue == null ||
                limitedValue.toLongOrNull()?.let { it <= maxValue } != false
            ) {
                onChange(limitedValue)
            }
        },
        enabled = enabled,
        singleLine = true,
        textStyle = OttrojjaTheme.typography.bodyMedium.copy(
            color = colors.content
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        cursorBrush = SolidColor(colors.content),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = colors.content
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    innerTextField()
                }
            }
        }
    )
}


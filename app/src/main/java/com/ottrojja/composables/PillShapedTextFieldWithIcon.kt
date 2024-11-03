package com.ottrojja.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillShapedTextFieldWithIcon(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: Painter,
    modifier: Modifier = Modifier,
    placeHolder: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }

    val textFieldModifier = modifier
        .background(
            Color.White,
            shape = CircleShape
        )
        .border(1.dp, Color.Black.copy(alpha = 0.2f), shape = CircleShape)
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .onFocusChanged { isFocused = it.isFocused }

    Row(
        modifier = textFieldModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            leadingIcon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        /*if (value.isEmpty()) {
            Text(
                text = placeHolder,
                modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outlineVariant),
            )
        }*/
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
            visualTransformation = VisualTransformation.None,
            singleLine = true,
            modifier = Modifier
                .weight(0.9f)
                .padding(vertical = 4.dp)
                .onFocusChanged { isFocused = it.isFocused },
        )
    }
}
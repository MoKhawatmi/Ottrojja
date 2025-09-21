package com.ottrojja.screens.teacherScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.TeacherAnswer

@Composable
fun SolutionInputTextField(
    value: TeacherAnswer,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    desiredWidth: Dp,
    clearFocusTrigger: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(clearFocusTrigger) {
        if (clearFocusTrigger) {
            println("clear focus")
            focusManager.clearFocus(force = true) // Clear and prevent re-focusing
        }
    }

    val textFieldModifier = modifier
        .padding(horizontal = 2.dp, vertical = 1.dp)
        .width(desiredWidth)
        .background(
            if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.background else if (value.status == AnswerStatus.RIGHT) Color(0xFFE2FFD6) else MaterialTheme.colorScheme.errorContainer
        )
        .border(
            1.dp,
            if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.primary else if (value.status == AnswerStatus.RIGHT) Color(0xFF29712C) else MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(6.dp)
        )
        .onFocusChanged { isFocused = it.isFocused }

    Row(
        modifier = textFieldModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value.answer,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.labelLarge.copy(
                fontSize = 24.sp,
                color = if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.primary else if (value.status == AnswerStatus.RIGHT)
                    Color(0xFF29712C) else MaterialTheme.colorScheme.error
            ),
            visualTransformation = VisualTransformation.None,
            singleLine = true,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .onFocusChanged { isFocused = it.isFocused },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next // Choose appropriate IME action (Done, Search, etc.)
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        focusDirection = FocusDirection.Next,
                    )
                }
            )
        )
    }
}

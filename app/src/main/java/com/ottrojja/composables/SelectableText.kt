package com.ottrojja.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectableText(text: String) {
    SelectionContainer(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = text,
            color = Color.Black,
            modifier = Modifier
                .padding(10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 21.sp),
            textAlign = TextAlign.Right,
        )
    }
}
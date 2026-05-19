package com.ottrojja.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun EmptyListMessage(text: String){
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        OttrojjaText(
            text = text,
            style = OttrojjaTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xFF9E9E9E)
        )
    }
}
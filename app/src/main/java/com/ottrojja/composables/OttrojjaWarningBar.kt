package com.ottrojja.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaWarningBar(text: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.errorContainer)
        .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OttrojjaText(
            text = text,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = OttrojjaTheme.typography.bodyMedium
        )
    }
}
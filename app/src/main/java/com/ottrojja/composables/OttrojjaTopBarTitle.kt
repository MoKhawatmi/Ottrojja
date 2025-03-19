package com.ottrojja.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun OttrojjaTopBarTitle(title: String) {
    Text(text = title,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp, lineHeight = 28.sp),
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(0.6f)
    )
}
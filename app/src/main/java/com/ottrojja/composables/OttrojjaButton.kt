package com.ottrojja.composables

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
        disabledContainerColor = MaterialTheme.colorScheme.outline,
        disabledContentColor = MaterialTheme.colorScheme.onTertiary
    )) {
        OttrojjaText(text, style = OttrojjaTheme.typography.bodyMedium)
    }
}
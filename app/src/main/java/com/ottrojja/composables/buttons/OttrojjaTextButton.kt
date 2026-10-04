package com.ottrojja.composables.buttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    TextButton(
        modifier = modifier,
        enabled = enabled,
        onClick = { onClick() }
    ) {
        OttrojjaText(
            text,
            style = OttrojjaTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
        )
    }
}
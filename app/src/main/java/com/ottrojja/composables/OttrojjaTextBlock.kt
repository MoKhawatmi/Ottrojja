package com.ottrojja.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.TextParser.TextBlock
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaTextBlock(
    block: TextBlock
) {
    when (block) {
        is TextBlock.Paragraph -> OttrojjaText(
            text = block.text,
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        is TextBlock.Quote -> OttrojjaText(
            text = block.text,
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        is TextBlock.Poetry -> OttrojjaText(
            text = block.text,
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        is TextBlock.Heading -> OttrojjaText(
            text = block.text,
            style = OttrojjaTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Right,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}
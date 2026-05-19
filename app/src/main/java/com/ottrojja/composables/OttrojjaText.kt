package com.ottrojja.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.ottrojja.classes.Helpers.truncate
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = OttrojjaTheme.typography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    textAlign: TextAlign = TextAlign.Start,
    truncate: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Normal,
    textDecoration: TextDecoration= TextDecoration.None
) {
    Text(
        text = text.truncate(truncate),
        modifier = modifier,
        style = style,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        textDecoration= textDecoration
    )
}
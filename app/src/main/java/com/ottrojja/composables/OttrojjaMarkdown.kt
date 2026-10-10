package com.ottrojja.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.State as ParsedMarkdown
import com.mikepenz.markdown.model.rememberMarkdownState
import com.ottrojja.ui.theme.OttrojjaTheme

/**
 * Renders an already-parsed markdown document (preferred for lists:
 * parse once in the ViewModel, no loading flash when items scroll back in).
 */
@Composable
fun OttrojjaMarkdown(
    state: ParsedMarkdown,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    layoutDirection: LayoutDirection = LayoutDirection.Rtl,
) {
    val textColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    val base = OttrojjaTheme.typography.bodyMedium

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Markdown(
            state,
            colors = markdownColor(
                text = textColor,
                dividerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            ),
            typography = markdownTypography(
                h1 = base.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                h2 = base.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                h3 = base.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                h4 = base.copy(fontWeight = FontWeight.Bold),
                h5 = base.copy(fontWeight = FontWeight.Bold),
                h6 = base.copy(fontWeight = FontWeight.Bold),
                text = base,
                paragraph = base,
                ordered = base,
                bullet = base,
                list = base,
            ),
            modifier = modifier.fillMaxWidth(),
        )
    }
}

/**
 * Convenience overload for a single, one-off markdown string
 * (it is parsed asynchronously inside the composition).
 */
@Composable
fun OttrojjaMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    layoutDirection: LayoutDirection = LayoutDirection.Rtl,
) {
    val markdownState = rememberMarkdownState(content, retainState = true)
    // Collect the parsed state from the hoisted holder.
    val parsed = markdownState.state.collectAsState().value
    OttrojjaMarkdown(parsed, modifier, color, layoutDirection)
}
package com.ottrojja.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToIndianNumbers
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun VerseTextWithNumber(
    modifier: Modifier = Modifier,
    text: String,
    number: Int,
    numberAfter: Boolean = false,
    color: Color = Color.Black,
    textStyle: TextStyle = OttrojjaTheme.typography.quranTextMedium,
    numberTextStyle: TextStyle = OttrojjaTheme.typography.bodySpecialMedium,
    textAlign: TextAlign = TextAlign.Start
) {

    val verseNumberStr="\uFD3F${convertToIndianNumbers("$number")}\uFD3E"

    OttrojjaText(
        text = buildAnnotatedString {
            if (!numberAfter) {
                withStyle(
                    style = numberTextStyle.toSpanStyle().copy(color = color),
                ) {
                    append(verseNumberStr)
                }
            }

            withStyle(
                style = textStyle.toSpanStyle().copy(color = color)
            ) {
                append("\uFD3F$text\uFD3E")
            }

            if (numberAfter) {
                withStyle(
                    style = numberTextStyle.toSpanStyle().copy(color = color),
                ) {
                    append(verseNumberStr)
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        textAlign = textAlign
    )
}
package com.ottrojja.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun VerseTextWithNumber(modifier: Modifier = Modifier,
                        text: String,
                        number: Int,
                        numberAfter: Boolean = false,
                        color: Color = Color.Black,
                        textStyle: TextStyle= OttrojjaTheme.typography.quranTextMedium
) {

    val numberContent = OttrojjaText(
        text = buildString {
            append("\uFD3F")
            append(Helpers.convertToIndianNumbers("$number"))
            append("\uFD3E")
        },
        style = OttrojjaTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.offset(y = 2.dp)
    )


    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {

        if (!numberAfter) {
            numberContent
        }

        OttrojjaText(
            text = text,
            style = textStyle,
            color = color,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        if (numberAfter) {
            numberContent
        }

    }
}
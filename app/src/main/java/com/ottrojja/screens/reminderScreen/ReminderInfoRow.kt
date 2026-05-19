package com.ottrojja.screens.reminderScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ReminderInfoRow(
    modifier: Modifier= Modifier,
    label: String,
    value: String,
    labelColor: Color = Color.Black,
    valueColor: Color = Color.Black,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OttrojjaText(
            text = label,
            style = OttrojjaTheme.typography.bodyMedium,
            color = labelColor,
            textAlign = TextAlign.Right,
        )

        if (trailing != null) {
            trailing()
        } else {
            OttrojjaText(
                text = value,
                style = OttrojjaTheme.typography.bodyMedium,
                color = valueColor,
                textAlign = TextAlign.Right,
            )
        }
    }
}
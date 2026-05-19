package com.ottrojja.composables.ottrojjaFlexibleActions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun RowScope.FlexActionOption(
    text: String,
    onClick: () -> Unit,
    bgColor: Color,
    textColor: Color) {

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1.0f)
            .background(bgColor)
            .clickable { onClick() }
            .padding(4.dp, 6.dp)
    ) {
        OttrojjaText(
            text = text,
            style = OttrojjaTheme.typography.bodyMedium,
            color = textColor
        )
    }

}
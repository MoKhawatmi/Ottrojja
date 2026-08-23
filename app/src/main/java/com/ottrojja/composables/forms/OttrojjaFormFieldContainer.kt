package com.ottrojja.composables.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OttrojjaFormFieldContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, contentColor, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        content()
    }
}
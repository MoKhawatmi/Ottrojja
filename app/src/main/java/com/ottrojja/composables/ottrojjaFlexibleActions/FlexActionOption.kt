package com.ottrojja.composables.ottrojjaFlexibleActions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun RowScope.FlexActionOption(
    text: String,
    onClick: () -> Unit,
    bgColor: Color,
    textColor: Color,
    icon: ImageVector? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1.0f)
            .background(bgColor)
            .clickable { onClick() }
            .padding(4.dp, 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            OttrojjaText(
                text = text,
                style = OttrojjaTheme.typography.bodyMedium,
                color = textColor
            )

            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = text,
                    tint = textColor
                )
            }
        }
    }
}
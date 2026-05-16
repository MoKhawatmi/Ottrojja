package com.ottrojja.composables.floatingNavigationDock

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DockItem(
    optionText: String,
    isCurrent: Boolean,
    onClick: () -> Unit,
    iconId: Int,
    alternateIcon: Int?, // used when route is not current
    modifier: Modifier = Modifier,
    overrideColor: Color? = null
) {
    val unselectedColor = Color(0xFF9E9E9E)
    val selectedColor= MaterialTheme.colorScheme.primary;


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!isCurrent) {
                        onClick()
                    }
                }
            )
            .padding(6.dp, 2.dp)
    ) {
        Image(
            painter = painterResource(
                id = if (isCurrent) iconId else if (alternateIcon != null) alternateIcon else iconId
            ),
            contentDescription = "",
            colorFilter = if (overrideColor != null) ColorFilter.tint(overrideColor
            ) else if (isCurrent) ColorFilter.tint(selectedColor) else ColorFilter.tint(
                unselectedColor
            )
        )
        Text(
            text = optionText,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (overrideColor != null) overrideColor else if (isCurrent) selectedColor else unselectedColor,
        )
    }
}

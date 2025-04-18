package com.ottrojja.composables

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T : Enum<T>> OttrojjaTabs(
    items: List<T>,
    selectedItem: T,
    onClickTab: (T) -> Unit,
    tabPrefix: String = ""
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, option ->
            Column() {
                Text(
                    text = "$tabPrefix$option",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (selectedItem == option) MaterialTheme.colorScheme.onPrimary else primaryColor,
                    modifier = Modifier
                        .padding(2.dp, 0.dp)
                        .clip(shape = RoundedCornerShape(50))
                        .drawBehind {
                            if (selectedItem == option) {
                                drawCircle(
                                    color = primaryColor,
                                    radius = this.size.maxDimension
                                )
                            }
                        }
                        .clickable { onClickTab(option) }
                        .defaultMinSize(minWidth = 90.dp)
                        .padding(0.dp, 6.dp, 0.dp, 6.dp)
                )
            }
        }
    }
}

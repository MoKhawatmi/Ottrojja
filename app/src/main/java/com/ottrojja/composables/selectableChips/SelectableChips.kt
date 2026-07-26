package com.ottrojja.composables.selectableChips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme


@Composable
fun <T : SelectableChip> ChipSelector(
    items: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { item ->

            val isSelected = item == selected

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                        .clickable(onClick = { onSelected(item) })
                        .padding(8.dp, 16.dp)
                        .defaultMinSize(minHeight = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                    OttrojjaText(
                        text = item.title,
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )

                    item.subtitle?.let {
                        OttrojjaText(
                            text = it,
                            style = OttrojjaTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(4.dp)
                            .fillMaxWidth(0.75f)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
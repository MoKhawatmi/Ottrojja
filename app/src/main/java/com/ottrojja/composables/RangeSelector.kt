package com.ottrojja.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionItem
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionSegment


@Composable
fun RangeSelector(startSegments: List<RangeSelectionSegment>,
                  endSegments: List<RangeSelectionSegment>) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            RangeSelectionItem(segments = startSegments)
        }

        Column(modifier = Modifier.weight(1f)) {
            RangeSelectionItem(segments = endSegments)
        }
    }
}
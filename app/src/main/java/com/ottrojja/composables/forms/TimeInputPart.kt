package com.ottrojja.composables.forms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeInputPart(
    range: IntRange,
    modifier: Modifier = Modifier,
    startNumber: Int = range.first,
    visibleItemsCount: Int = 3,
    onValueChange: (Int) -> Unit
) {
    val itemHeight = 48.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startNumber - range.first
    )

    val flingBehavior = rememberSnapFlingBehavior(listState)

    val numbers = remember(range) { range.toList() }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // Selected item
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportEndOffset / 2

            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }?.index
        }.collect { index ->
            index ?: return@collect

            selectedIndex = index

            onValueChange(numbers[index!!])
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {

        // Center highlight
        Box(
            modifier = Modifier
                .height(itemHeight)
                .fillMaxWidth()
                .background(
                    Color.LightGray.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .border(width = 1.dp, color = MaterialTheme.colorScheme.onSecondary, shape = RoundedCornerShape(16.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                vertical = itemHeight * (visibleItemsCount / 2)
            )
        ) {
            items(numbers.size) { index ->
                val number = numbers[index]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString().padStart(2, '0'),
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

    }
}
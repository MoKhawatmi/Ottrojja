package com.ottrojja.composables.ottrojjaFlexibleActions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OttrojjaFlexibleActions(
    modifier: Modifier = Modifier,
    actionList: List<FlexibleAction>,
    bgColor: Color = MaterialTheme.colorScheme.background
) {

    val MAX_PER_ROW = 3
    val remainder = actionList.size % MAX_PER_ROW
    val fillers = if (remainder == 0) 0 else MAX_PER_ROW - remainder

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(8.dp),
        maxItemsInEachRow = MAX_PER_ROW,
    ) {
        actionList.forEach {
            FlexActionOption(
                text = it.text,
                bgColor = it.bgColor,
                textColor = it.textColor,
                onClick = {
                    it.action()
                },
                icon = it.icon
            )
        }
        repeat(fillers) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                //empty column as filler
            }
        }
    }
}
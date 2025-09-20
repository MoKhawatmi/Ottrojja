package com.ottrojja.screens.mainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ottrojja.classes.Helpers.convertToIndianNumbers
import com.ottrojja.classes.Screen
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.room.entities.Quarter
import com.ottrojja.room.relations.PartWithQuarters

@Composable
fun PartsMenu(
    items: List<PartWithQuarters> = listOf<PartWithQuarters>(),
    navController: NavController
) {
    val keyboardController = LocalSoftwareKeyboardController.current;

    LazyColumn(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { item ->
            PartItem(part = item,
                itemClicked = { pageNum ->
                    keyboardController!!.hide();
                    navController.navigate(Screen.QuranScreen.invokeRoute(pageNum))
                })
        }
    }
}


@Composable
fun PartItem(part: PartWithQuarters, itemClicked: (String) -> Unit) {

    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 200)
    )


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier
                .padding(0.dp, 2.dp)
                .fillMaxWidth(0.9f)
                .clickable { itemClicked(part.part.partStartPage) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = part.part.partName, color = Color.Black)
                }
                Row(
                    modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "{${part.part.firstWords}}",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                modifier = Modifier
                    .rotate(rotation)
                    .padding(6.dp, 0.dp)
                    .size(32.dp)
                    .clickable(onClick = { expanded = !expanded })
            )
        }
        ListHorizontalDivider()


        AnimatedVisibility(expanded) {

            Column {
                part.quarters.forEachIndexed { index, it ->
                    var hizbText = "";
                    when (index % 4) {
                        0 -> hizbText = "الحزب ${it.hizb}"
                        1 -> hizbText = "ربع الحزب ${it.hizb}"
                        2 -> hizbText = "نصف الحزب ${it.hizb}"
                        else -> hizbText = "ثلاث ارباع الحزب ${it.hizb}"
                    }
                    QuarterItem(
                        quarter = it,
                        hizbText = hizbText,
                        itemClicked = { pageNum -> itemClicked(pageNum) },
                        isLastItem = (index == 7)
                    )
                }

            }

        }
    }
}

@Composable
fun QuarterItem(quarter: Quarter,
                hizbText: String,
                itemClicked: (String) -> Unit,
                isLastItem: Boolean) {
    Column(
        modifier = Modifier
            .padding(top = 2.dp, bottom = 2.dp, start = 32.dp, end = 0.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                itemClicked(quarter.pageNum)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 12.dp, 12.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "\u06DE $hizbText", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = convertToIndianNumbers(quarter.pageNum),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (!isLastItem) {
            ListHorizontalDivider()
        }
    }
}

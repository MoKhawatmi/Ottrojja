package com.ottrojja.screens.listeningScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.convertToIndianNumbers
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon

@Composable
fun VerseSelectionDialog(onDismiss: () -> Unit,
                         versesNum: Int,
                         selectVerse: (Int) -> Unit
) {
    val versesArray = Array(versesNum) { (it + 1).toString() }

    var searchFilter by remember { mutableStateOf("") };

    OttrojjaDialog(onDismissRequest = { onDismiss() },
        contentModifier = Modifier
            .padding(8.dp)
            .fillMaxHeight(0.75f)
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        useDefaultWidth = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            )
            {
                PillShapedTextFieldWithIcon(
                    value = searchFilter, //no hoisting this search filter as it is just a simple Int filter
                    onValueChange = { newValue -> searchFilter = newValue },
                    leadingIcon = painterResource(id = R.drawable.search),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    placeHolder = "الاية"
                )
            }

            LazyColumn(
                Modifier
                    .fillMaxHeight()
            ) {
                items(versesArray.filter { it.contains(searchFilter) || it.contains(convertToArabicNumbers(searchFilter)) }) { item ->
                    Column(modifier = Modifier
                        .padding(12.dp, 2.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectVerse(item.toInt());
                            onDismiss();
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = item,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }

}
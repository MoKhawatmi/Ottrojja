package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon

@Composable
fun PageSelectionDialog(
    onDismissRequest: () -> Unit,
    pages: List<String>,
    scrollPageNumber: Int,
    onSelect: (String) -> Unit,
    searchFilter: String,
    searchFilterChanged: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(scrollPageNumber-1)
    }

    OttrojjaDialog(onDismissRequest = { onDismissRequest() }) {
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
                    value = searchFilter,
                    onValueChange = { newValue -> searchFilterChanged(newValue) },
                    leadingIcon = painterResource(id = R.drawable.search),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    placeHolder = "رقم الصفحة"
                )
            }
            LazyColumn(state = listState) {
                items(items = pages, key = { it }) { item ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item) }
                        .padding(6.dp)) {
                        Text(
                            text = "ص${item}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}
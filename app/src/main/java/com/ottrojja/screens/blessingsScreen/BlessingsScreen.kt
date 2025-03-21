package com.ottrojja.screens.blessingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.composables.FillerItem
import com.ottrojja.composables.Header
import com.ottrojja.composables.ListHorizontalDivider

@Composable
fun BlessingsScreen(blessingsViewModel: BlessingsViewModel = viewModel()) {
    val context= LocalContext.current
    val blessings by blessingsViewModel.blessingsList.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        blessingsViewModel.fetchBlessings()
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() &&
                    visibleItems.last().index == blessings.lastIndex
                ) {
                    blessingsViewModel.fetchBlessings()
                }
            }
    }


    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary)) {
        Header()
        LazyColumn(state = listState, modifier = Modifier.padding(horizontal = 6.dp).fillMaxHeight()) {
            items(blessings) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "share blessing",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, end = 4.dp, start = 4.dp, bottom = 0.dp).clickable { copyToClipboard(context, item.text) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text =item.text,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(6.dp, 8.dp)
                        )
                    }
                    ListHorizontalDivider()
                }
            }

            if (blessingsViewModel.loading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(34.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        }
    }

}

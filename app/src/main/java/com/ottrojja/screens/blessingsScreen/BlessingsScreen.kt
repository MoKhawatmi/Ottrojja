package com.ottrojja.screens.blessingsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.stripMarkdown
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaLoadingIndicator
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.selectableChips.ChipSelector
import com.ottrojja.screens.blessingsScreen.classes.BlessingCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlessingsScreen(blessingsViewModel: BlessingsViewModel = viewModel()) {
    val context = LocalContext.current
    val blessings by blessingsViewModel.blessingsList.collectAsState()
    val searchFilter by blessingsViewModel.searchQuery.collectAsState()
    val selectedCategory by blessingsViewModel.filterCategory.collectAsState()
    var showSearch by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Pagination: load the next page when the last item becomes visible.
    // (The initial load now happens in the ViewModel's init block.)
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

    val pullRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary)
        ) {
            TopBar(
                title = "إشراقات",
                mainAction = ButtonAction(icon = Icons.Default.Search, title = "search", action = { showSearch = !showSearch })
            )

            ChipSelector(
                items = BlessingCategory.entries,
                selected = selectedCategory,
                onSelected = { value -> blessingsViewModel.updateCategory(value) },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            PullToRefreshBox(
                isRefreshing = blessingsViewModel.isRefreshing,
                onRefresh = { blessingsViewModel.refresh() },
                state = pullRefreshState
            ) {
                LazyColumn(state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .fillMaxHeight()
                ) {
                    items(blessings) { item ->
                        BlessingItem(
                            item = item,
                            // Copy clean plain text instead of raw markdown syntax
                            onShareClick = { copyToClipboard(context, item.blessing.text.stripMarkdown()) }
                        )
                    }

                    if (blessingsViewModel.loading) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                OttrojjaLoadingIndicator(
                                    size = 34.dp,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 4.dp
                                )
                            }
                        }
                    }
                }
            }
        }
        // Scrim — sits above the content, below the search field
        AnimatedVisibility(
            visible = showSearch,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { showSearch = false }
                    )
            )
        }

        AnimatedVisibility(showSearch) {
            OttrojjaDetailsContainer(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {} // no-op: just absorbs the tap so it doesn't reach the scrim
                    )
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PillShapedTextFieldWithIcon(
                        value = searchFilter,
                        onValueChange = { newText -> blessingsViewModel.updateSearch(newText) },
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.padding(vertical = 0.dp).fillMaxWidth(0.9f)
                    )
                }
            }
        }
    }
}
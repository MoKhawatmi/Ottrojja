package com.ottrojja.screens.quranScreen

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaItemSelectionDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.RangeSelector
import com.ottrojja.composables.ottrojjaFlexibleActions.FlexibleAction
import com.ottrojja.composables.ottrojjaFlexibleActions.OttrojjaFlexibleActions
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionSegment
import com.ottrojja.room.entities.PageContent
import com.ottrojja.screens.listeningScreen.ListeningViewModel.SelectionPhase

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersesSection(
    items: List<PageContent>,
    onSheetRequest: (PageContent, TafseerSheetMode) -> Unit,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val versesSectionViewModel: VersesSectionViewModel = viewModel(
        factory = VersesSectionViewModelFactory(repository, application)
    )

    LaunchedEffect(items) {
        versesSectionViewModel.setItems(items);
    }
    val versesList = versesSectionViewModel.items.collectAsState();

    if (versesSectionViewModel.showVerseShareDialog) {
        ShareVersesBottomSheet(
            onDismissRequest = { versesSectionViewModel.showVerseShareDialog = false },
            title = "مشاركة ايات من سورة ${versesSectionViewModel.shareChapterName}",
            startSegments = listOf(RangeSelectionSegment(
                title = "من اية",
                value = "${versesSectionViewModel.startingVerse?.verseNum}",
                onClick = {
                    versesSectionViewModel.selectionPhase = SelectionPhase.START
                    versesSectionViewModel.showVerseSelectionDialog = true
                }
            )),
            endSegments = listOf(RangeSelectionSegment(
                title = "الى اية",
                value = "${versesSectionViewModel.endVerse?.verseNum}",
                onClick = {
                    versesSectionViewModel.selectionPhase = SelectionPhase.END
                    versesSectionViewModel.showVerseSelectionDialog = true
                }
            )),
            onShareClick = { versesSectionViewModel.shareVerses() }
        )
    }

    if (versesSectionViewModel.showVerseSelectionDialog) {
        SelectVerseDialog(
            onDismissRequest = { versesSectionViewModel.showVerseSelectionDialog = false },
            searchFilter = versesSectionViewModel.searchFilter,
            onSearchFilterChange = { value -> versesSectionViewModel.searchFilter = value },
            versesList = versesSectionViewModel.filteredVerses,
            onVerseSelected = { verse ->
                versesSectionViewModel.verseSelected(verse)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(versesList.value) { item ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable { versesSectionViewModel.updateExpanded(item) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${
                            Helpers.convertToIndianNumbers("${item.pageContent.verseNum!!}")
                        } ${item.pageContent.verseText}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )

                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Expand Verse Options",
                        modifier = Modifier.clickable {
                            versesSectionViewModel.updateExpanded(item)
                        }
                    )
                }
                AnimatedVisibility(
                    visible = item.expanded,
                ) {
                    OttrojjaFlexibleActions(
                        listOf(
                            FlexibleAction(
                                text = "مشاركة",
                                bgColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.Black,
                                action = { versesSectionViewModel.fetchChapterVerses(item.pageContent) }
                            ),
                            FlexibleAction(
                                text = "التفسير",
                                bgColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.التفسير) }
                            ),
                            FlexibleAction(
                                text = "الإعراب",
                                bgColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.Black,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.الإعراب) }
                            ),
                            FlexibleAction(
                                text = "سبب النزول",
                                bgColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.أسباب_النزول) }
                            ),
                            FlexibleAction(
                                text = "المفردات",
                                bgColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.Black,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.معاني_المفردات) }
                            ),
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(14.dp),
                thickness = 1.dp,
                color = Color.Black.copy(alpha = 0.1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareVersesBottomSheet(onDismissRequest: () -> Unit,
                           title: String,
                           startSegments: List<RangeSelectionSegment>,
                           endSegments: List<RangeSelectionSegment>,
                           onShareClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            RangeSelector(
                startSegments = startSegments,
                endSegments = endSegments
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                OttrojjaButton("مشاركة", onClick = { onShareClick() })
            }
        }
    }
}

@Composable
fun SelectVerseDialog(onDismissRequest: () -> Unit,
                      searchFilter: String,
                      onSearchFilterChange: (String) -> Unit,
                      versesList: List<PageContent>,
                      onVerseSelected: (PageContent) -> Unit
) {
    OttrojjaItemSelectionDialog(onDismiss = { onDismissRequest() },
        searchBar = {
            PillShapedTextFieldWithIcon(
                value = searchFilter,
                onValueChange = { newValue -> onSearchFilterChange(newValue) },
                leadingIcon = painterResource(id = R.drawable.search),
                modifier = Modifier.fillMaxWidth(0.9f),
                placeHolder = "الاية"
            )
        },
        selectionItems = versesList,
        onSelect = { verse -> onVerseSelected(verse) },
        itemContent = { item ->
            Row {
                Text("الاية ${item.verseNum}")
            }
        }
    )
}


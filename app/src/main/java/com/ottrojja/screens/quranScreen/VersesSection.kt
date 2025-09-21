package com.ottrojja.screens.quranScreen

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TafseerSheetMode
import com.ottrojja.room.entities.PageContent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersesSection(
    items: List<PageContent>,
    onSheetRequest: (String, TafseerSheetMode) -> Unit,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val versesSectionViewModel: VersesSectionViewModel = viewModel(
        factory = VersesSectionViewModelFactory(repository, application)
    )


    LaunchedEffect(null) {
        versesSectionViewModel.setItems(items);
    }
    val versesList = versesSectionViewModel.items.collectAsState();

    Column() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(10.dp)
        ) {
            items(versesList.value) { item ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        versesSectionViewModel.updateExpanded(
                            item
                        )
                    }) {
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
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp), maxItemsInEachRow = 3
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable {
                                        versesSectionViewModel.shareVerse(item.pageContent
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "مشاركة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        onSheetRequest(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}",
                                            TafseerSheetMode.TAFSEER
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "التفسير",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable {
                                        onSheetRequest(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}",
                                            TafseerSheetMode.E3RAB
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "الإعراب",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        onSheetRequest(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}",
                                            TafseerSheetMode.CAUSES_OF_REVELATION
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "سبب النزول",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable {
                                        onSheetRequest(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}",
                                            TafseerSheetMode.VERSE_MEANINGS
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "المفردات",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(Color.Transparent)
                                    .padding(4.dp, 6.dp)
                            ) {}
                        }
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
}

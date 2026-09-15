package com.ottrojja.composables.quranRangeSelector

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.RangeSelector
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionSegment
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.listeningScreen.dialogs.SurahSelectionDialog
import com.ottrojja.screens.listeningScreen.dialogs.VerseSelectionDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun QuranRangeSelector(
    modifier: Modifier = Modifier,
    range: QuranRangeState,
    onRangeChange: (QuranRangeState) -> Unit,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: QuranRangeSelectorViewModel = viewModel(
        factory = QuranRangeSelectorViewModelFactory(
            repository,
            application
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initializeRange(range)
    }

    // Send only actual VM edits back to parent
    LaunchedEffect(Unit) {
        viewModel.rangeChanges.collect { updatedRange ->
            onRangeChange(updatedRange)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            println("resetting range state")
            viewModel.reset()
        }
    }

    val filteredChapters = remember(
        uiState.chapters,
        uiState.searchQuery
    ) {
        viewModel.getFilteredChapters()
    }

    val currentRange = uiState.range

    if (uiState.showSurahDialog) {
        SurahSelectionDialog(
            onDismiss = viewModel::dismissSurahDialog,
            chapters = filteredChapters,
            searchFilter = uiState.searchQuery,
            searchFilterChanged = viewModel::updateSearchQuery,
            selectSurah = { surah ->
                viewModel.surahUpdated(surah)
            },
        )
    }

    if (uiState.showVerseDialog) {

        val selectedSurah = when (
            uiState.selectionTarget
        ) {
            QuranRangeSelectorViewModel.SelectionTarget.START ->
                currentRange.startSurah

            QuranRangeSelectorViewModel.SelectionTarget.END ->
                currentRange.endSurah
        }

        selectedSurah?.let { surah ->
            VerseSelectionDialog(
                onDismiss = viewModel::dismissVerseDialog,
                versesNum = surah.verseCount,
                selectVerse = { verseNum ->
                    viewModel.verseUpdated(verseNum)
                }
            )
        }
    }

    Column(
        modifier = modifier
    ) {
        RangeSelector(
            startSegments = listOf(
                RangeSelectionSegment(
                    title = "من",
                    value = buildString {
                        append(currentRange.startSurah?.surahId ?: "-")
                        append("\n")
                        append(
                            currentRange.startSurah?.chapterName ?: "-"
                        )
                    },
                    onClick = {
                        viewModel.openSurahSelection(
                            QuranRangeSelectorViewModel
                                .SelectionTarget.START
                        )
                    }
                ),

                RangeSelectionSegment(
                    title = "الاية",
                    value = currentRange.startVerse?.verseNum?.toString() ?: "-",
                    onClick = {
                        viewModel.openVerseSelection(
                            QuranRangeSelectorViewModel
                                .SelectionTarget.START
                        )
                    }
                )
            ),

            endSegments = listOf(
                RangeSelectionSegment(
                    title = "الى",
                    value = buildString {
                        append(currentRange.endSurah?.surahId ?: "-")
                        append("\n")
                        append(
                            currentRange.endSurah?.chapterName ?: "-"
                        )
                    },
                    onClick = {
                        viewModel.openSurahSelection(
                            QuranRangeSelectorViewModel
                                .SelectionTarget.END
                        )
                    }
                ),
                RangeSelectionSegment(
                    title = "الاية",
                    value = currentRange.endVerse?.verseNum?.toString() ?: "-",
                    onClick = {
                        viewModel.openVerseSelection(
                            QuranRangeSelectorViewModel
                                .SelectionTarget.END
                        )
                    }
                )
            )
        )
    }
}
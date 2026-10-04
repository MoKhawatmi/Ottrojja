package com.ottrojja.screens.teacherScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.quranRangeSelector.QuranRangeSelector
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.repositories.QuranRepository

@Composable
fun TrainingSelection(
    startTraining: () -> Unit,
    quranRange: QuranRangeState? = null,
    onQuranRangeChange: (QuranRangeState) -> Unit,
    repository: QuranRepository
) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        if (quranRange != null) {
            QuranRangeSelector(
                range = quranRange,
                onRangeChange = { onQuranRangeChange(it) },
                repository = repository
            )
        }
    }


    /*Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RangeSelector(
            startSegments = listOf(RangeSelectionSegment(
                title = "من",
                value = "${startingSurah?.surahId}.\n${startingSurah?.chapterName}",
                onClick = {
                    setShowSurahSelectionDialog(true);
                    setSelectionPhase(ListeningViewModel.SelectionPhase.START);
                }
            ), RangeSelectionSegment(
                title = "الاية",
                value = startingVerse.toString(),
                onClick = {
                    setShowVerseSelectionDialog(true);
                    setSelectionPhase(ListeningViewModel.SelectionPhase.START);
                }
            )),
            endSegments = listOf(RangeSelectionSegment(
                title = "الى",
                value = "${endSurah?.surahId}.\n${endSurah?.chapterName}",
                onClick = {
                    setShowSurahSelectionDialog(true);
                    setSelectionPhase(ListeningViewModel.SelectionPhase.END);
                }
            ), RangeSelectionSegment(
                title = "الاية",
                value = endVerse.toString(),
                onClick = {
                    setShowVerseSelectionDialog(true);
                    setSelectionPhase(ListeningViewModel.SelectionPhase.END);
                }
            ))
        )
    }*/

    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OttrojjaButton(
            onClick = { startTraining() },
            text = "ابدأ",
        )
    }
}
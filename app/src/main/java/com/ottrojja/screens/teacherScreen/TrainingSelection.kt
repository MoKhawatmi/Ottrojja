package com.ottrojja.screens.teacherScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.RangeSelector
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionSegment
import com.ottrojja.screens.listeningScreen.ListeningViewModel
import com.ottrojja.screens.mainScreen.ChapterData

@Composable
fun TrainingSelection(
    setShowSurahSelectionDialog: (Boolean) -> Unit,
    setSelectionPhase: (ListeningViewModel.SelectionPhase) -> Unit,
    setShowVerseSelectionDialog: (Boolean) -> Unit,
    startingSurah: ChapterData?,
    startingVerse: Int,
    endSurah: ChapterData?,
    endVerse: Int,
    startTraining: () -> Unit
) {

    Row(modifier = Modifier
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
    }

    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = { startTraining() }) {
            Text(
                text = "ابدأ",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }


}
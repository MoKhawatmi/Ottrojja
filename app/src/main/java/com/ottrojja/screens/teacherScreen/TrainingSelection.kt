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
import com.ottrojja.composables.RangeSelectionItem
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
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RangeSelectionItem(
                    surahItem = startingSurah,
                    selectSurahClicked = {
                        setShowSurahSelectionDialog(true);
                        setSelectionPhase(ListeningViewModel.SelectionPhase.START);
                    },
                    verseItem = startingVerse,
                    selectVerseClicked = {
                        setShowVerseSelectionDialog(true);
                        setSelectionPhase(ListeningViewModel.SelectionPhase.START);
                    },
                    header = "من"
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                RangeSelectionItem(
                    surahItem = endSurah,
                    selectSurahClicked = {
                        setShowSurahSelectionDialog(true);
                        setSelectionPhase(ListeningViewModel.SelectionPhase.END);
                    },
                    verseItem = endVerse,
                    selectVerseClicked = {
                        setShowVerseSelectionDialog(true);
                        setSelectionPhase(ListeningViewModel.SelectionPhase.END);
                    },
                    header = "إلى"
                )
            }
        }
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
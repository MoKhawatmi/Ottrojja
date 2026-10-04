package com.ottrojja.screens.teacherScreen

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.ButtonAction
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.screens.listeningScreen.ListeningViewModel
import com.ottrojja.screens.listeningScreen.dialogs.SurahSelectionDialog
import com.ottrojja.screens.listeningScreen.dialogs.VerseSelectionDialog
import com.ottrojja.screens.teacherScreen.classes.TeacherMode
import com.ottrojja.screens.teacherScreen.dialogs.InstructionsDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun TeacherScreen(
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val teacherScreenViewModel: TeacherScreenViewModel = viewModel(
        factory = TeacherScreenViewModelFactory(repository, application)
    )

    val quranRange by teacherScreenViewModel.quranRange.collectAsState()

    LaunchedEffect(Unit) {
        teacherScreenViewModel.initChaptersList()
        teacherScreenViewModel.initQuranRange()
    }

    DisposableEffect(Unit) {
        onDispose {
            teacherScreenViewModel.releasePlayer();
        }
    }


    if (teacherScreenViewModel.showInstructionsDialog) {
        InstructionsDialog(onDismiss = { teacherScreenViewModel.showInstructionsDialog = false })
    }


    Column() {
        TopBar(
            title = "المعلم",
            mainAction = ButtonAction(
                icon = Icons.Default.Info,
                action = { teacherScreenViewModel.showInstructionsDialog = true }
            ),
            secondaryActions = if (teacherScreenViewModel.mode != TeacherMode.SELECTION) {
                listOf(
                    ButtonAction(
                        icon = Icons.Filled.ArrowBack,
                        action = { teacherScreenViewModel.backToSelection() },
                        title = "إنهاء"
                    )
                )
            } else {
                emptyList()
            }
        )

        when (teacherScreenViewModel.mode) {
            TeacherMode.SELECTION -> TrainingSelection(
                quranRange = quranRange,
                onQuranRangeChange = { teacherScreenViewModel.updateQuranRange(it) },
                repository = repository,
                startTraining = { teacherScreenViewModel.startTraining() },
            )

            TeacherMode.TRAINING -> PageTraining(
                currentVerse = teacherScreenViewModel.currentVerse,
                checkVerse = { teacherScreenViewModel.checkVerse() },
                currentTry = teacherScreenViewModel.currentTry,
                proceedVerse = { teacherScreenViewModel.proceedVerse() },
                solutionMap = teacherScreenViewModel.solutionMap,
                inputSolutions = teacherScreenViewModel.inputSolutions,
                onInputSolutionChanged = { value, index ->
                    teacherScreenViewModel.inputSolutions.set(
                        index,
                        TeacherAnswer(value, AnswerStatus.UNCHECKED)
                    )
                },
                maxTries = teacherScreenViewModel.MAX_TRIES,
                maxTriesReached = teacherScreenViewModel.reachedMaxTries,
                allRight = teacherScreenViewModel.allRight,
                showResults = {
                    teacherScreenViewModel.showResults()
                },
                correctVersesAnswered = teacherScreenViewModel.correctVersesAnswered,
                lastVerseReached = teacherScreenViewModel.lastVerseReached,
                isDownloading = teacherScreenViewModel.isDownloading,
                playVerse = { teacherScreenViewModel.playVerse() },
                isPlaying = teacherScreenViewModel.isPlaying,
                onPauseClicked = { teacherScreenViewModel.pauseVerse() },
                onDispose = { teacherScreenViewModel.resetMedia(); },
                selectedTrainingVerses = teacherScreenViewModel.selectedTrainingVerses,
                getChapterName = { chapterId -> teacherScreenViewModel.getChapterName(chapterId) }
            )

            TeacherMode.RESULTS -> FinalResults(
                selectedTrainingVerses = teacherScreenViewModel.selectedTrainingVerses,
                backToSelection = { teacherScreenViewModel.backToSelection() }
            )
        }
    }
}




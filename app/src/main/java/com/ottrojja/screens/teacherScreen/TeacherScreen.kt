package com.ottrojja.screens.teacherScreen

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.ButtonAction
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.composables.TopBar
import com.ottrojja.screens.teacherScreen.classes.TeacherMode
import com.ottrojja.screens.teacherScreen.dialogs.InstructionsDialog

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




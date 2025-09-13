package com.ottrojja.screens.teacherScreen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.screens.listeningScreen.ListeningViewModel
import com.ottrojja.screens.listeningScreen.SurahSelectionDialog
import com.ottrojja.screens.listeningScreen.VerseSelectionDialog
import com.ottrojja.screens.mainScreen.ChapterData

@Composable
fun TeacherScreen(
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val teacherScreenViewModel: TeacherScreenViewModel = viewModel(
        factory = TeacherScreenViewModelFactory(repository, application)
    )

    var filteredChapters by remember { mutableStateOf(emptyList<ChapterData>()) }

    LaunchedEffect(Unit) {
        teacherScreenViewModel.initChaptersList()
    }

    LaunchedEffect(teacherScreenViewModel.searchFilter) {
        filteredChapters = teacherScreenViewModel.getChaptersList()
    }

    DisposableEffect(Unit) {
        onDispose {
            teacherScreenViewModel.releasePlayer();
        }
    }

    if (teacherScreenViewModel.showSurahSelectionDialog) {
        SurahSelectionDialog(
            onDismiss = { teacherScreenViewModel.showSurahSelectionDialog = false },
            chapters = filteredChapters,
            searchFilter = teacherScreenViewModel.searchFilter,
            searchFilterChanged = { value -> teacherScreenViewModel.searchFilter = value },
            selectSurah = { value ->
                teacherScreenViewModel.surahSelected(value);
                teacherScreenViewModel.searchFilter = ""
            },
            selectionPhase = teacherScreenViewModel.selectionPhase,
            checkIfChapterDownloaded = { false },
            downloadChapter = { },
            isDownloading = false
        )
    }

    if (teacherScreenViewModel.showVerseSelectionDialog) {
        VerseSelectionDialog(
            onDismiss = { teacherScreenViewModel.showVerseSelectionDialog = false },
            versesNum = if (teacherScreenViewModel.selectionPhase == ListeningViewModel.SelectionPhase.START) {
                teacherScreenViewModel.startingSurah!!.verseCount
            } else {
                teacherScreenViewModel.endSurah!!.verseCount
            },
            selectVerse = { value -> teacherScreenViewModel.verseSelected(value) })
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
            secondaryActions = if (teacherScreenViewModel.mode == TeacherScreenViewModel.TeacherMode.TRAINING) {
                listOf(
                    ButtonAction(
                        icon = Icons.Filled.ArrowBack,
                        action = { teacherScreenViewModel.backToPages() },
                        title = "إنهاء"
                    )
                )
            } else {
                emptyList()
            }
        )

        when (teacherScreenViewModel.mode) {
            TeacherScreenViewModel.TeacherMode.SELECTION -> TrainingSelection(
                setShowSurahSelectionDialog = { value -> teacherScreenViewModel.showSurahSelectionDialog = value },
                setSelectionPhase = { value -> teacherScreenViewModel.selectionPhase = value },
                setShowVerseSelectionDialog = { value -> teacherScreenViewModel.showVerseSelectionDialog = value },
                startingSurah = teacherScreenViewModel.startingSurah,
                startingVerse = teacherScreenViewModel.startingVerse,
                endSurah = teacherScreenViewModel.endSurah,
                endVerse = teacherScreenViewModel.endVerse,
                startTraining = { teacherScreenViewModel.startTraining() }
            )

            TeacherScreenViewModel.TeacherMode.TRAINING -> PageTraining(
                currentVerse = teacherScreenViewModel.currentVerse,
                checkVerse = { teacherScreenViewModel.checkVerse() },
                currentTry = teacherScreenViewModel.currentTry,
                proceedVerse = { teacherScreenViewModel.proceedVerse() },
                //startTeaching = { teacherScreenViewModel.startTeaching() },
                //hasStarted = teacherScreenViewModel.hasStarted,
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
                backToPageSelection = {
                    teacherScreenViewModel.backToPages()
                },
                correctVersesAnswered = teacherScreenViewModel.correctVersesAnswered,
                lastVerseReached = teacherScreenViewModel.lastVerseReached,
                isDownloading = teacherScreenViewModel.isDownloading,
                playVerse = { teacherScreenViewModel.playVerse() },
                isPlaying = teacherScreenViewModel.isPlaying,
                onPauseClicked = { teacherScreenViewModel.pauseVerse() },
                onDispose = { teacherScreenViewModel.resetMedia(); },
                selectedTrainingVerses = teacherScreenViewModel.selectedTrainingVerses,
                getChapterName= {chapterId-> teacherScreenViewModel.getChapterName(chapterId)}
            )
        }
    }
}

@Composable
fun InstructionsDialog(onDismiss: () -> Unit) {
    val instructionsText = mutableListOf<String>("المعلم")
    instructionsText.add("إختبر حفظك للقرآن الكريم من خلال هذه الميزة")
    instructionsText.add("الإرشادات:")
    instructionsText.add("بعد اختيار ايات التدريب قم بالضغط على زر البدء")
    instructionsText.add(
        "سيتم عرض الايات بالترتيب وبها فراغات لاكمالها حيث يمثل كل فراغ كلمة واحدة فقط من الاية"
    )
    instructionsText.add("قم بملئ الفراغات بالكلمات بدون استخدام اي تشكيل او حركات للحروف")
    instructionsText.add("ثم اضغط على زر التحقق للتحقق من اجاباتك")
    instructionsText.add("سيتم اظهار الاجابات الصحيحة باللون الاخضر والخاطئة باللون الاحمر")
    instructionsText.add(
        "امامك ثلاث محاولات للوصول للاجابات الصحيحة وبعدها يتم إظهار النص الصحيح لمقارنته بالحل ويمكنك عندها الاستماع للاية صوتيا"
    )

    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
            ) {
                items(instructionsText) { item ->

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.lightbulb),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(0.dp, 2.dp, 4.dp, 2.dp)
                                .fillMaxWidth(0.08f)
                        )
                        Text(
                            item,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSecondary,
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        }
    }
}


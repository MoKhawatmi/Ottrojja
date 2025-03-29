package com.ottrojja.screens.teacherScreen

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.elevatedButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.PageContentItemType
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.composables.Header
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.LoadingDialog
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.OttrojjaTopBar
import com.ottrojja.composables.PillShapedTextFieldWithIcon

@Composable
fun TeacherScreen(
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val teacherScreenViewModel: TeacherScreenViewModel = viewModel(
        factory = TeacherScreenViewModelFactory(repository, application)
    )

    DisposableEffect(Unit) {
        onDispose {
            teacherScreenViewModel.releasePlayer();
        }
    }

    Column() {
        when (teacherScreenViewModel.mode) {
            TeacherScreenViewModel.TeacherMode.PAGE_SELECTION -> PageSelection(
                searchFilter = teacherScreenViewModel.searchFilter,
                searchFilterChanged = { value ->
                    teacherScreenViewModel.searchFilter = value
                },
                pagesList = teacherScreenViewModel.getPagesList(),
                pageSelected = { value -> teacherScreenViewModel.pageSelected(value) }
            )

            TeacherScreenViewModel.TeacherMode.PAGE_TRAINING -> PageTraining(
                currentVerse = teacherScreenViewModel.currentVerse,
                checkVerse = { teacherScreenViewModel.checkVerse() },
                currentTry = teacherScreenViewModel.currentTry,
                proceedVerse = { teacherScreenViewModel.proceedVerse() },
                currentPage = teacherScreenViewModel.selectedPage,
                startTeaching = { teacherScreenViewModel.startTeaching() },
                hasStarted = teacherScreenViewModel.hasStarted,
                solutionMap = teacherScreenViewModel.solutionMap,
                inputSolutions = teacherScreenViewModel.inputSolutions,
                onInputSolutionChanged = { value, index ->
                    teacherScreenViewModel.inputSolutions.set(
                        index,
                        TeacherAnswer(value, AnswerStatus.UNCHECKED)
                    )
                },
                maxTries = teacherScreenViewModel.maxTries,
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
                showInstructions = teacherScreenViewModel.showInstructionsDialog,
                infoClicked = { teacherScreenViewModel.showInstructionsDialog = true },
                hideInstructions = { teacherScreenViewModel.showInstructionsDialog = false }

            )
        }
    }
}


@Composable
fun PageSelection(
    searchFilter: String,
    searchFilterChanged: (String) -> Unit,
    pagesList: List<String>,
    pageSelected: (String) -> (Unit)
) {
    Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
        Header()
        OttrojjaTopBar {
            Column {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إختر صفحة",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                )
                {
                    PillShapedTextFieldWithIcon(
                        value = searchFilter,
                        onValueChange = { newValue -> searchFilterChanged(newValue) },
                        leadingIcon = painterResource(id = R.drawable.search),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        }
        BrowseMenu(
            pagesList,
            { value -> pageSelected(value) })

    }

}

@Composable
fun BrowseMenu(
    items: List<String> = listOf<String>(),
    pageSelected: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current;

    LazyColumn(
        Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { item ->
            Column(modifier = Modifier
                .padding(12.dp, 2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    keyboardController!!.hide();
                    pageSelected(item.split(" ")[1])
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = item, color = Color.Black)
                }
                ListHorizontalDivider()
            }
        }
    }
}


@Composable
fun InstructionsDialog(onDismiss: () -> Unit) {
    val instructionsText = mutableListOf<String>("المعلم")
    instructionsText.add("إختبر حفظك لصفحات وايات القرآن الكريم من خلال هذه الميزة")
    instructionsText.add("الإرشادات:")
    instructionsText.add("بعد اختيار صفحة قم بالضغط على زر البدء")
    instructionsText.add(
        "سيتم عرض ايات الصفحة بالترتيب وبها فراغات لاكمالها حيث يمثل كل فراغ كلمة واحدة فقط من الاية"
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


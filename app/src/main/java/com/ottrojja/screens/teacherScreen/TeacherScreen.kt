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

    Column(modifier = Modifier.fillMaxHeight(0.9f)) {
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

        BrowseMenu(
            pagesList,
            { value -> pageSelected(value) })

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PageTraining(
    currentVerse: PageContent,
    checkVerse: () -> Unit,
    currentTry: Int,
    proceedVerse: () -> Unit,
    currentPage: QuranPage,
    startTeaching: () -> Unit,
    hasStarted: Boolean,
    solutionMap: HashMap<Int, List<String>>,
    inputSolutions: MutableMap<Int, TeacherAnswer>,
    onInputSolutionChanged: (String, Int) -> Unit,
    maxTries: Int,
    maxTriesReached: Boolean,
    allRight: Boolean,
    backToPageSelection: () -> Unit,
    correctVersesAnswered: Int,
    lastVerseReached: Boolean,
    isDownloading: Boolean,
    playVerse: () -> Unit,
    isPlaying: Boolean,
    onPauseClicked: () -> Unit,
    onDispose: () -> Unit,
    showInstructions: Boolean,
    infoClicked: () -> Unit,
    hideInstructions: () -> Unit,
) {
    val hiddenIndecies = solutionMap.keys;
    val focusManager: FocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current;
    var clearFocusTrigger by remember { mutableStateOf(false) }

    if (isDownloading) {
        LoadingDialog()
    }

    if (showInstructions) {
        InstructionsDialog(onDismiss = { hideInstructions() })
    }

    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    Row(
        modifier = Modifier
            .padding(6.dp, 4.dp)
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElevatedButton(
                onClick = { infoClicked() },
                elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                modifier = Modifier
                    .padding(4.dp, 0.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElevatedButton(
                onClick = { backToPageSelection() },
                elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                modifier = Modifier
                    .padding(4.dp, 0.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    if (hasStarted) {
        Row(
            modifier = Modifier
                .padding(0.dp, 0.dp, 0.dp, 4.dp)
                .fillMaxWidth()
        ) {
            // Animate the progress value
            val animatedProgress by animateFloatAsState(
                targetValue = currentPage.pageContent.indexOf(currentVerse) / (currentPage.pageContent.size - 1).toFloat(),
                animationSpec = tween(durationMillis = 500) // Customize the duration as needed
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(),
                strokeCap = StrokeCap.Square,
                gapSize = 0.dp
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp, 2.dp, 12.dp, 8.dp)
    ) {
        if (!hasStarted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "الصفحة ${currentPage.pageNum}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Button(onClick = { startTeaching() }) {
                    Text(
                        text = "البدء",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, 300.dp)
                    .border(
                        BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 4.dp)
                ) {
                    currentVerse.verseTextPlain.split(" ").forEachIndexed { index, it ->
                        if (hiddenIndecies.contains(index)) {
                            val textMeasurer = rememberTextMeasurer()
                            val textLayoutResult: TextLayoutResult =
                                textMeasurer.measure(
                                    text = AnnotatedString("$it  "), //extra spacing for input field sizing
                                    style = LocalTextStyle.current
                                )
                            val textSize = textLayoutResult.size
                            val density = LocalDensity.current

                            SolutionInputTextField(
                                value = inputSolutions.get(index)!!,
                                onValueChange = { value ->
                                    onInputSolutionChanged(
                                        value,
                                        index
                                    )
                                },
                                desiredWidth = with(density) { textSize.width.toDp() },
                                clearFocusTrigger = clearFocusTrigger
                            )
                        } else {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(2.dp, 2.dp),
                            )
                        }
                    }
                    Text(
                        text = Helpers.convertToIndianNumbers(
                            currentVerse.verseNum
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "المحاولة $maxTries/$currentTry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { keyboardController!!.hide(); checkVerse() },
                    enabled = !maxTriesReached && !allRight
                ) {
                    Text(
                        text = "تحقق",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (maxTriesReached || allRight) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "النص الصحيح",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, 300.dp)
                        .border(
                            BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 6.dp)
                    ) {
                        Text(
                            text = "${currentVerse.verseText} ${
                                Helpers.convertToIndianNumbers(
                                    currentVerse.verseNum
                                )
                            }",
                            style = MaterialTheme.typography.labelLarge,
                            lineHeight = 36.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp, 2.dp),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    if (isPlaying) {
                        Image(painter = painterResource(R.drawable.playing),
                            contentDescription = "pause",
                            modifier = Modifier
                                .padding(10.dp, 0.dp)
                                .clickable { onPauseClicked() }
                                .size(35.dp))
                    } else {
                        Image(painter = painterResource(R.drawable.play),
                            contentDescription = "play",
                            modifier = Modifier
                                .clickable { playVerse() }
                                .size(35.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))

                    }
                }


                if (!lastVerseReached) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Button(onClick = {
                            clearFocusTrigger = true;
                            proceedVerse();
                            focusManager.clearFocus();
                        }) {
                            Text(
                                text = "الاية التالية",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
                if (lastVerseReached) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Button(onClick = { backToPageSelection() }) {
                            Text(
                                text = "إنهاء",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${correctVersesAnswered} ايات صحيحة من اصل ${currentPage.pageContent.filter { it.type == PageContentItemType.verse }.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
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
fun SolutionInputTextField(
    value: TeacherAnswer,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    desiredWidth: Dp,
    clearFocusTrigger: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(clearFocusTrigger) {
        if (clearFocusTrigger) {
            println("clear focus")
            focusManager.clearFocus(force = true) // Clear and prevent re-focusing
        }
    }

    val textFieldModifier = modifier
        .padding(horizontal = 2.dp, vertical = 1.dp)
        .width(desiredWidth)
        .background(
            if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.background else if (value.status == AnswerStatus.RIGHT) Color(
                0xFFE2FFD6
            ) else MaterialTheme.colorScheme.errorContainer
        )
        .border(
            1.dp,
            if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.primary else if (value.status == AnswerStatus.RIGHT) Color(
                0xFF29712C
            ) else MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(6.dp)
        )
        .onFocusChanged { isFocused = it.isFocused }

    Row(
        modifier = textFieldModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value.answer,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.labelLarge.copy(
                fontSize = 24.sp,
                color = if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.primary else if (value.status == AnswerStatus.RIGHT)
                    Color(0xFF29712C) else MaterialTheme.colorScheme.error
            ),
            visualTransformation = VisualTransformation.None,
            singleLine = true,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .onFocusChanged { isFocused = it.isFocused },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next // Choose appropriate IME action (Done, Search, etc.)
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        focusDirection = FocusDirection.Next,
                    )
                }
            )
        )
    }
}


@Composable
fun InstructionsDialog(onDismiss: () -> Unit) {
    val instructionsText = mutableListOf<String>("المعلم")
    instructionsText.add("إختبر حفظك لصفحات وايات القرآن الكريم من خلال هذه الميزة")
    instructionsText.add("الإرشادات:")
    instructionsText.add("بعد اختيار صفحة قم بالضغط على زر البدء")
    instructionsText.add("سيتم عرض ايات الصفحة بالترتيب وبها فراغات لاكمالها حيث يمثل كل فراغ كلمة واحدة فقط من الاية")
    instructionsText.add("قم بملئ الفراغات بالكلمات بدون استخدام اي تشكيل او حركات للحروف")
    instructionsText.add("ثم اضغط على زر التحقق للتحقق من اجاباتك")
    instructionsText.add("سيتم اظهار الاجابات الصحيحة باللون الاخضر والخاطئة باللون الاحمر")
    instructionsText.add("امامك ثلاث محاولات للوصول للاجابات الصحيحة وبعدها يتم إظهار النص الصحيح لمقارنته بالحل ويمكنك عندها الاستماع للاية صوتيا")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxSize()
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
    }
}


/*TextField(
                                value = inputSolutions.get(index)!!,
                                onValueChange = { value -> onInputSolutionChanged(value, index) },
                                singleLine = true,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .width(with(density) { textSize.width.toDp() }),
                                textStyle = MaterialTheme.typography.labelLarge,
                                textSize = 24.sp
                            )*/
/*Text(
    text = "_",
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(4.dp, 2.dp)
)*/
/*Row(
    modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        text = "النص الصحيح",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary
    )
}*/

package com.ottrojja.screens.teacherScreen

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.onKeyEvent
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.screens.mainScreen.PillShapedTextFieldWithIcon

@Composable
fun TeacherScreen(
    modifier: Modifier, repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val teacherScreenViewModel: TeacherScreenViewModel = viewModel(
        factory = TeacherScreenViewModelFactory(repository, application)
    )


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
            allRight = teacherScreenViewModel.allRight
        )

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
    allRight: Boolean
) {
    val hiddenIndecies = solutionMap.keys;
    val focusManager: FocusManager = LocalFocusManager.current


    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp, 24.dp)
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
            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    )
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
                            onValueChange = { value -> onInputSolutionChanged(value, index) },
                            desiredWidth = with(density) { textSize.width.toDp() },
                        )

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
                    } else {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp, 2.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "المحاولة $maxTries/${currentTry}",
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
                Button(onClick = { checkVerse() }, enabled = !maxTriesReached && !allRight) {
                    Text(
                        text = "تحقق",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (maxTriesReached || allRight) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Button(onClick = { proceedVerse(); focusManager.clearFocus() }) {
                        Text(
                            text = "الاية التالية",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
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
            .fillMaxHeight(0.9f)
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
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun SolutionInputTextField(
    value: TeacherAnswer,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    desiredWidth: Dp
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current


    val textFieldModifier = modifier
        .padding(horizontal = 2.dp, vertical = 0.dp)
        .width(desiredWidth)
        .background(
            Color.White,
        )
        .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(6.dp))
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
                color = if (value.status == AnswerStatus.UNCHECKED) MaterialTheme.colorScheme.primary else if (value.status == AnswerStatus.RIGHT) Color.Green else MaterialTheme.colorScheme.error
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
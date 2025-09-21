package com.ottrojja.screens.teacherScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.room.entities.PageContent
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.classes.VerseWithAnswer
import com.ottrojja.composables.LoadingDialog

@Composable
fun PageTraining(
    currentVerse: PageContent?,
    checkVerse: () -> Unit,
    currentTry: Int,
    proceedVerse: () -> Unit,
    //startTeaching: () -> Unit,
    //hasStarted: Boolean,
    solutionMap: HashMap<Int, List<String>>,
    inputSolutions: MutableMap<Int, TeacherAnswer>,
    onInputSolutionChanged: (String, Int) -> Unit,
    maxTries: Int,
    maxTriesReached: Boolean,
    allRight: Boolean,
    showResults: () -> Unit,
    correctVersesAnswered: Int,
    lastVerseReached: Boolean,
    isDownloading: Boolean,
    playVerse: () -> Unit,
    isPlaying: Boolean,
    onPauseClicked: () -> Unit,
    onDispose: () -> Unit,
    selectedTrainingVerses: List<VerseWithAnswer>,
    getChapterName: (Int) -> String
) {
    val hiddenIndecies = solutionMap.keys;
    val focusManager: FocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current;
    var clearFocusTrigger by remember { mutableStateOf(false) }

    if (isDownloading) {
        LoadingDialog()
    }

    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    Row(
        modifier = Modifier
            .padding(0.dp, 0.dp, 0.dp, 4.dp)
            .fillMaxWidth()
    ) {
        // Animate the progress value
        val animatedProgress by animateFloatAsState(
            targetValue = if (selectedTrainingVerses.size <= 1) {
                1f
            } else {
                selectedTrainingVerses.indexOfFirst { it.verse==currentVerse }  / (selectedTrainingVerses.size - 1).toFloat()
            },
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

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(12.dp, 2.dp, 12.dp, 8.dp)
    ) {
        //show surah name on first verse
        if (currentVerse?.verseNum == 1) {
            Row(horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "سورة ${getChapterName(currentVerse.surahNum)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(2.dp, 2.dp),
                )
            }
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
            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 4.dp)
            ) {
                currentVerse?.verseTextPlain!!.split(" ").forEachIndexed { index, it ->
                    if (hiddenIndecies.contains(index)) {
                        val textMeasurer = rememberTextMeasurer()
                        val textLayoutResult: TextLayoutResult =
                            textMeasurer.measure(
                                text = AnnotatedString("$it  "
                                ), //extra spacing for input field sizing
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
                    text = Helpers.convertToIndianNumbers("${currentVerse.verseNum!!}"),
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
                        text = "${currentVerse?.verseText} ${
                            Helpers.convertToIndianNumbers("${currentVerse?.verseNum!!}")
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
                    Button(onClick = { showResults() }) {
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
                text = "${selectedTrainingVerses.filter { it.answerCorrect }.size} ايات صحيحة من اصل ${selectedTrainingVerses.size}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }
}

package com.ottrojja.screens.listeningScreen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.LoadingDialog
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.MediaSlider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.RepetitionOptionsDialog
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.screens.mainScreen.ChapterData

@Composable
fun ListeningScreen(
    repository: QuranRepository
) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val listeningViewModel: ListeningViewModel = viewModel(
        factory = ListeningViewModelFactory(repository, application)
    )

    if (listeningViewModel.isDownloading) {
        LoadingDialog()
    }

    /*val chaptersList by produceState(initialValue = emptyList<ChapterData>()) {
        value = chaptersViewModel.getChaptersList()
    }*/

    var filteredChapters by remember { mutableStateOf(emptyList<ChapterData>()) }

    LaunchedEffect(Unit) {
        listeningViewModel.initChaptersList()
    }

    LaunchedEffect(listeningViewModel.searchFilter) {
        filteredChapters = listeningViewModel.getChaptersList()
    }

    if (listeningViewModel.showSurahSelectionDialog) {
        SurahSelectionDialog(onDismiss = { listeningViewModel.showSurahSelectionDialog = false },
            chapters = filteredChapters,
            searchFilter = listeningViewModel.searchFilter,
            searchFilterChanged = { value -> listeningViewModel.searchFilter = value },
            selectSurah = { value ->
                listeningViewModel.surahSelected(value);
                listeningViewModel.searchFilter = "";
            },
            selectionPhase = listeningViewModel.selectionPhase,
            checkIfChapterDownloaded = { value ->
                listeningViewModel.checkIfChapterDownloaded(value)
            },
            downloadChapter = { value -> listeningViewModel.downloadChapter(value) }
        )
    }

    if (listeningViewModel.showVerseSelectionDialog) {
        VerseSelectionDialog(onDismiss = { listeningViewModel.showVerseSelectionDialog = false },
            versesNum = if (listeningViewModel.selectionPhase == ListeningViewModel.SelectionPhase.START) {
                listeningViewModel.startingSurah!!.verseCount
            } else {
                listeningViewModel.endSurah!!.verseCount
            },
            selectVerse = { value -> listeningViewModel.verseSelected(value) })
    }

    if (listeningViewModel.showRepetitionOptionsDialog) {
        RepetitionOptionsDialog(
            onDismissRequest = { listeningViewModel.showRepetitionOptionsDialog = false },
            onSelect = { value ->
                when (listeningViewModel.repetitionSelectionMode) {
                    ListeningViewModel.RepetitionSelectionMode.SURAH -> {
                        listeningViewModel.surahRepetitions = value;
                    }

                    ListeningViewModel.RepetitionSelectionMode.VERSE -> {
                        listeningViewModel.verseRepetitions = value;
                    }

                    ListeningViewModel.RepetitionSelectionMode.RANGE -> {
                        listeningViewModel.verseRangeRepetitions = value;
                    }
                }
                listeningViewModel.showRepetitionOptionsDialog = false;
            })
    }


    Column {
        TopBar(title = "الإستماع")
        Box(modifier = Modifier.fillMaxSize()) {
            Column {

                SecondaryTopBar {
                    OttrojjaTabs(items = QuranListeningMode.entries,
                        selectedItem = listeningViewModel.listeningMode,
                        onClickTab = { value -> listeningViewModel.switchListeningMode(value) })
                }

                if (listeningViewModel.listeningMode == QuranListeningMode.مقطع_ايات) {
                    Column {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                RangeSelectionItem(
                                    surahItem = listeningViewModel.startingSurah,
                                    selectSurahClicked = {
                                        listeningViewModel.showSurahSelectionDialog = true;
                                        listeningViewModel.selectionPhase = ListeningViewModel.SelectionPhase.START;
                                    },
                                    verseItem = listeningViewModel.startingVerse,
                                    selectVerseClicked = {
                                        listeningViewModel.showVerseSelectionDialog = true;
                                        listeningViewModel.selectionPhase = ListeningViewModel.SelectionPhase.START;
                                    },
                                    header = "من"
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                RangeSelectionItem(
                                    surahItem = listeningViewModel.endSurah,
                                    selectSurahClicked = {
                                        listeningViewModel.showSurahSelectionDialog = true;
                                        listeningViewModel.selectionPhase = ListeningViewModel.SelectionPhase.END;
                                    },
                                    verseItem = listeningViewModel.endVerse,
                                    selectVerseClicked = {
                                        listeningViewModel.showVerseSelectionDialog = true;
                                        listeningViewModel.selectionPhase = ListeningViewModel.SelectionPhase.END;
                                    },
                                    header = "إلى"
                                )
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            RangeSelectionItem(
                                surahItem = listeningViewModel.selectedSurah,
                                selectSurahClicked = {
                                    listeningViewModel.showSurahSelectionDialog = true;
                                    listeningViewModel.selectionPhase = ListeningViewModel.SelectionPhase.PLAY;
                                },
                                header = "السورة",
                                withVerseSelection = false
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                )

                if (listeningViewModel.listeningMode == QuranListeningMode.مقطع_ايات) {
                    ListeningOptionsContainer {
                        Row(
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .clickable { listeningViewModel.repetitionSelectionMode = ListeningViewModel.RepetitionSelectionMode.VERSE; listeningViewModel.showRepetitionOptionsDialog = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تكرار الاية",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .clickable { listeningViewModel.repetitionSelectionMode = ListeningViewModel.RepetitionSelectionMode.VERSE; listeningViewModel.showRepetitionOptionsDialog = true }
                                    .clip(shape = RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = listeningViewModel.verseRepetitions,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .clickable { listeningViewModel.repetitionSelectionMode = ListeningViewModel.RepetitionSelectionMode.RANGE; listeningViewModel.showRepetitionOptionsDialog = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تكرار المقطع",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .clickable { listeningViewModel.repetitionSelectionMode = ListeningViewModel.RepetitionSelectionMode.RANGE; listeningViewModel.showRepetitionOptionsDialog = true }
                                    .clip(shape = RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = listeningViewModel.verseRangeRepetitions,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                } else {
                    ListeningOptionsContainer {
                        Row(
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .clickable { listeningViewModel.toggleContChapterPlaying() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تشغيل متتال للسور",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Checkbox(
                                    checked = listeningViewModel.continuousChapterPlaying,
                                    onCheckedChange = { listeningViewModel.toggleContChapterPlaying() }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .clickable { listeningViewModel.repetitionSelectionMode = ListeningViewModel.RepetitionSelectionMode.SURAH; listeningViewModel.showRepetitionOptionsDialog = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("مرات التكرار",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .clickable { listeningViewModel.repetitionSelectionMode = ListeningViewModel.RepetitionSelectionMode.SURAH; listeningViewModel.showRepetitionOptionsDialog = true }
                                    .clip(shape = RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = listeningViewModel.surahRepetitions,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
            }

            /*LazyColumn(
                Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
                    .align(Alignment.TopCenter)
            ) {
                items(filteredChapters) { item ->
                    Column(modifier = Modifier
                        .padding(12.dp, 2.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            listeningViewModel.selectSurah(item)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = item.chapterName, color = Color.Black)
                            }
                            Column(
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (listeningViewModel.selectedSurah == item) {
                                        Image(painter = painterResource(R.drawable.playing),
                                            contentDescription = "pause",
                                            modifier = Modifier
                                                .padding(10.dp, 0.dp)
                                                .clickable { listeningViewModel.pause() }
                                                .size(35.dp)
                                        )
                                    }
                                    if (!listeningViewModel.checkIfChapterDownloaded(item.surahId
                                        )
                                    ) {
                                        Image(painter = painterResource(R.drawable.download),
                                            contentDescription = "download",
                                            modifier = Modifier
                                                .padding(10.dp, 0.dp)
                                                .clickable {
                                                    listeningViewModel.downloadChapter(item.surahId)
                                                }
                                                .size(35.dp),
                                            colorFilter = ColorFilter.tint(
                                                MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        ListHorizontalDivider()
                    }
                }
            }*/

            MediaController(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { /*without this, when the column is clicked the element behind it will be clicked*/ }
                    .padding(10.dp),
                isPlaying = listeningViewModel.isPlaying && listeningViewModel.listeningMode == listeningViewModel.currentPlayingParameters?.listeningMode,
                playbackSpeed = listeningViewModel.playbackSpeed,
                isDownloading = false,
                onFasterClicked = { listeningViewModel.increasePlaybackSpeed() },
                onPauseClicked = { listeningViewModel.pause() },
                onPlayClicked = { listeningViewModel.play() },
                onSlowerClicked = { listeningViewModel.decreasePlaybackSpeed() },
                hasNextPreviousControl = false,
            ) {
                if (listeningViewModel.currentPlayingTitle.isNotBlank() && listeningViewModel.listeningMode == listeningViewModel.currentPlayingParameters?.listeningMode) {
                    Text(
                        text = "\uFD3F \u06E9 ${listeningViewModel.currentPlayingTitle} \u06E9 \uFD3E",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (listeningViewModel.isPlaying && listeningViewModel.listeningMode == listeningViewModel.currentPlayingParameters?.listeningMode && listeningViewModel.listeningMode == QuranListeningMode.سورة_كاملة) {
                    MediaSlider(
                        sliderPosition = listeningViewModel.sliderPosition,
                        setSliderPosition = { value -> listeningViewModel.sliderChanged(value) },
                        sliderMaxDuration = listeningViewModel.maxDuration
                    )
                }
            }
        }
    }
}
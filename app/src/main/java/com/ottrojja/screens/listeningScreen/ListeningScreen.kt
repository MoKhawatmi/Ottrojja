package com.ottrojja.screens.listeningScreen

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.OttrojjaDateTime
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.MediaSlider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.RangeSelector
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.dialogs.LoadingDialog
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionItem
import com.ottrojja.composables.rangeSelectionItem.RangeSelectionSegment
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.listeningScreen.dialogs.SurahSelectionDialog
import com.ottrojja.screens.listeningScreen.dialogs.VerseSelectionDialog
import com.ottrojja.screens.quranScreen.dialogs.RepetitionOptionsDialog
import com.ottrojja.services.quran.QuranPlaybackManager
import com.ottrojja.services.quran.QuranPlayerState
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ListeningScreen(
    repository: QuranRepository
) {
    val application = LocalContext.current.applicationContext as Application
    val vm: ListeningViewModel = viewModel(
        factory = ListeningViewModelFactory(
            repository,
            QuranPlaybackManager.getInstance(application),
            application
        )
    )

    val ui by vm.ui.collectAsStateWithLifecycle()
    val player by vm.player.collectAsStateWithLifecycle()
    val sliderPosition by vm.sliderPosition.collectAsStateWithLifecycle()
    val filteredChapters by vm.filteredChapters.collectAsStateWithLifecycle()

    if (ui.isDownloading) {
        LoadingDialog()
    }

    ListeningDialogs(ui = ui, filteredChapters = filteredChapters, vm = vm)

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        TopBar(title = "الإستماع")
        SecondaryTopBar {
            OttrojjaTabs(
                items = QuranListeningMode.entries,
                selectedItem = ui.mode,
                onClickTab = { mode -> vm.switchMode(mode) }
            )
        }

        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                when (ui.mode) {
                    QuranListeningMode.مقطع_ايات -> VerseRangePane(
                        ui = ui,
                        onOpenSurah = vm::openSurahPicker,
                        onOpenVerse = vm::openVersePicker,
                        onOpenRepetition = vm::openRepetitionPicker,
                        onPlay = vm::play,
                    )

                    else -> FullSurahPane(
                        ui = ui,
                        onOpenSurah = vm::openSurahPicker,
                        onOpenRepetition = vm::openRepetitionPicker,
                        onToggleContinuous = vm::toggleContinuous,
                    )
                }
            }

            PlayerBar(
                player = player,
                isActiveMode = player.activeMode == ui.mode,
                sliderPosition = sliderPosition,
                vm = vm,
            )
        }
    }
}

@Composable
private fun ListeningDialogs(
    ui: ListeningUiState,
    filteredChapters: List<ChapterData>,
    vm: ListeningViewModel,
) {
    when (val dialog = ui.dialog) {
        is ListeningDialog.SurahPicker -> SurahSelectionDialog(
            onDismiss = vm::dismissDialog,
            chapters = filteredChapters,
            searchFilter = ui.searchFilter,
            searchFilterChanged = vm::onSearchFilterChanged,
            selectSurah = vm::onSurahSelected,
            checkIfChapterDownloaded = { id -> id in ui.downloadedChapters },
            downloadChapter = vm::downloadChapter,
            allowDownload = dialog.target == SurahTarget.FULL_SURAH,
            downloadState = ui.isDownloading,
            selectedSurah = when (dialog.target) {
                SurahTarget.RANGE_START -> ui.startSurah
                SurahTarget.RANGE_END -> ui.endSurah
                SurahTarget.FULL_SURAH -> ui.selectedSurah
            },
        )

        is ListeningDialog.VersePicker -> {
            val surah = if (dialog.end == RangeEnd.START) ui.startSurah else ui.endSurah
            if (surah != null) {
                VerseSelectionDialog(
                    onDismiss = vm::dismissDialog,
                    versesNum = surah.verseCount,
                    selectVerse = vm::onVerseSelected,
                    selectedVerse = if (dialog.end == RangeEnd.START) ui.startVerse else ui.endVerse,
                )
            }
        }

        is ListeningDialog.Repetition -> RepetitionOptionsDialog(
            onDismissRequest = vm::dismissDialog,
            onSelect = vm::onRepetitionSelected,
        )

        null -> Unit
    }
}

@Composable
private fun VerseRangePane(
    ui: ListeningUiState,
    onOpenSurah: (SurahTarget) -> Unit,
    onOpenVerse: (RangeEnd) -> Unit,
    onOpenRepetition: (RepetitionKind) -> Unit,
    onPlay: () -> Unit,
) {
    RangeSelector(
        startSegments = listOf(
            RangeSelectionSegment(
                title = "من",
                value = surahLabel(ui.startSurah),
                onClick = { onOpenSurah(SurahTarget.RANGE_START) }
            ),
            RangeSelectionSegment(
                title = "الاية",
                value = ui.startVerse.toString(),
                onClick = { onOpenVerse(RangeEnd.START) }
            )
        ),
        endSegments = listOf(
            RangeSelectionSegment(
                title = "الى",
                value = surahLabel(ui.endSurah),
                onClick = { onOpenSurah(SurahTarget.RANGE_END) }
            ),
            RangeSelectionSegment(
                title = "الاية",
                value = ui.endVerse.toString(),
                onClick = { onOpenVerse(RangeEnd.END) }
            )
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth(0.3f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Image(
                painter = painterResource(R.drawable.play),
                contentDescription = "play",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }
    }

    OptionsDivider()

    ListeningOptionsContainer {
        RepetitionRow("تكرار الاية", ui.options.verseRepetitions) { onOpenRepetition(RepetitionKind.VERSE) }
        RepetitionRow("تكرار المقطع", ui.options.rangeRepetitions) { onOpenRepetition(RepetitionKind.RANGE) }
    }
}

@Composable
private fun FullSurahPane(
    ui: ListeningUiState,
    onOpenSurah: (SurahTarget) -> Unit,
    onOpenRepetition: (RepetitionKind) -> Unit,
    onToggleContinuous: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(modifier = Modifier.weight(1f)) {
            RangeSelectionItem(
                segments = listOf(
                    RangeSelectionSegment(
                        title = "السورة",
                        value = surahLabel(ui.selectedSurah),
                        onClick = { onOpenSurah(SurahTarget.FULL_SURAH) }
                    )
                )
            )
        }
    }

    OptionsDivider()

    ListeningOptionsContainer {
        OptionRow(label = "تشغيل متتال للسور", onClick = onToggleContinuous) {
            Checkbox(
                checked = ui.options.continuousChapterPlaying,
                onCheckedChange = { onToggleContinuous() }
            )
        }
        RepetitionRow("مرات التكرار", ui.options.surahRepetitions) { onOpenRepetition(RepetitionKind.SURAH) }
    }
}

@Composable
private fun PlayerBar(
    player: QuranPlayerState,
    isActiveMode: Boolean,
    sliderPosition: Long,
    vm: ListeningViewModel,
) {
    MediaController(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* swallow clicks so the content behind isn't clicked */ }
            .padding(10.dp),
        isPlaying = player.isPlaying && isActiveMode,
        playbackSpeed = player.speed,
        isDownloading = false,
        onFasterClicked = vm::increasePlaybackSpeed,
        onPauseClicked = vm::pause,
        onPlayClicked = vm::play,
        onSlowerClicked = vm::decreasePlaybackSpeed,
        hasNextPreviousControl = false,
    ) {
        if (!isActiveMode) return@MediaController

        if (player.title.isNotBlank()) {
            OttrojjaText(
                text = "\uFD3F \u06E9 ${player.title} \u06E9 \uFD3E",
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (player.activeMode == QuranListeningMode.سورة_كاملة && player.durationMs > 0) {
            OttrojjaText(
                text = "${OttrojjaDateTime.formatDuration(sliderPosition)}/${OttrojjaDateTime.formatDuration(player.durationMs)}",
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            MediaSlider(
                sliderPosition = sliderPosition.toFloat(),
                setSliderPosition = vm::onSliderChanged,
                sliderMaxDuration = player.durationMs.toFloat()
            )
        }
    }
}

@Composable
private fun OptionsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
    )
}

@Composable
private fun OptionRow(
    label: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OttrojjaText(
                label,
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(0.5f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            trailing()
        }
    }
}

@Composable
private fun RepetitionRow(label: String, value: String, onClick: () -> Unit) {
    OptionRow(label = label, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        ) {
            OttrojjaText(
                text = value,
                color = MaterialTheme.colorScheme.onPrimary,
                style = OttrojjaTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
            )
        }
    }
}

private fun surahLabel(chapter: ChapterData?) =
    if (chapter == null) "-\n-" else "${chapter.surahId}.\n${chapter.chapterName}"

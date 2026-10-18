package com.ottrojja.screens.listeningScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.DownloadState
import com.ottrojja.classes.FileDownloader
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.classes.StorageBase
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.services.quran.ListeningOptions
import com.ottrojja.services.quran.QuranAudio
import com.ottrojja.services.quran.QuranPlaybackManager
import com.ottrojja.services.quran.QuranPlaybackRequest
import com.ottrojja.services.quran.QuranPlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SurahTarget { RANGE_START, RANGE_END, FULL_SURAH }
enum class RangeEnd { START, END }
enum class RepetitionKind { SURAH, VERSE, RANGE }

sealed interface ListeningDialog {
    data class SurahPicker(val target: SurahTarget) : ListeningDialog
    data class VersePicker(val end: RangeEnd) : ListeningDialog
    data class Repetition(val kind: RepetitionKind) : ListeningDialog
}

data class ListeningUiState(
    val mode: QuranListeningMode = QuranListeningMode.مقطع_ايات,
    val chapters: List<ChapterData> = emptyList(),
    val searchFilter: String = "",
    val selectedSurah: ChapterData? = null,
    val startSurah: ChapterData? = null,
    val startVerse: Int = 1,
    val endSurah: ChapterData? = null,
    val endVerse: Int = 1,
    val options: ListeningOptions = ListeningOptions(),
    val dialog: ListeningDialog? = null,
    val downloadedChapters: Set<Int> = emptySet(),
    val isDownloading: Boolean = false,
)

class ListeningViewModel(
    private val repository: QuranRepository,
    private val playbackManager: QuranPlaybackManager,
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _ui = MutableStateFlow(ListeningUiState())
    val ui: StateFlow<ListeningUiState> = _ui.asStateFlow()

    val player: StateFlow<QuranPlayerState> = playbackManager.state

    private val _sliderPosition = MutableStateFlow(0L)
    val sliderPosition: StateFlow<Long> = _sliderPosition.asStateFlow()

    val filteredChapters: StateFlow<List<ChapterData>> = _ui
        .map { it.chapters to it.searchFilter.trim() }
        .distinctUntilChanged()
        .map { (chapters, query) ->
            if (query.isBlank()) chapters
            else chapters.filter { chapter ->
                chapter.chapterName.contains(query)
                        || chapter.surahId.toString() == query
                        || chapter.surahId.toString() == convertToArabicNumbers(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        playbackManager.connect()

        viewModelScope.launch {
            playbackManager.positionMs.collect { _sliderPosition.value = it }
        }

        viewModelScope.launch {
            val chapters = withContext(Dispatchers.IO) { repository.getAllChapters() }
            val downloaded = withContext(Dispatchers.IO) { scanDownloadedChapters() }
            val first = chapters.firstOrNull()
            _ui.update {
                it.copy(
                    chapters = chapters,
                    downloadedChapters = downloaded,
                    selectedSurah = first,
                    startSurah = first,
                    endSurah = first,
                )
            }
            restoreFromPlayback()

            // continuous playback moves to the next surah on its own; follow it in the UI
            playbackManager.state
                .map { it.currentSurahId }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { surahId ->
                    if (playbackManager.state.value.request is QuranPlaybackRequest.FullSurah) {
                        _ui.update { s ->
                            s.copy(selectedSurah = s.chapters.find { it.surahId == surahId } ?: s.selectedSurah)
                        }
                    }
                }
        }
    }

    /** Re-opening the screen while something is loaded shows what is playing. */
    private fun restoreFromPlayback() {
        val state = playbackManager.state.value
        val request = state.request ?: return
        _ui.update { s ->
            when (request) {
                is QuranPlaybackRequest.FullSurah -> s.copy(
                    mode = request.mode,
                    selectedSurah = s.chapters.find { it.surahId == (state.currentSurahId ?: request.surah.surahId) }
                        ?: request.surah,
                    options = state.options,
                )

                is QuranPlaybackRequest.VerseRange -> s.copy(
                    mode = request.mode,
                    startSurah = request.startSurah,
                    startVerse = request.startVerse,
                    endSurah = request.endSurah,
                    endVerse = request.endVerse,
                    options = state.options,
                )
            }
        }
    }

    override fun onCleared() {
        playbackManager.releaseIfIdle()
        super.onCleared()
    }


    fun switchMode(mode: QuranListeningMode) = _ui.update { it.copy(mode = mode) }

    fun openSurahPicker(target: SurahTarget) =
        _ui.update { it.copy(dialog = ListeningDialog.SurahPicker(target), searchFilter = "") }

    fun openVersePicker(end: RangeEnd) =
        _ui.update { it.copy(dialog = ListeningDialog.VersePicker(end)) }

    fun openRepetitionPicker(kind: RepetitionKind) =
        _ui.update { it.copy(dialog = ListeningDialog.Repetition(kind)) }

    fun dismissDialog() = _ui.update { it.copy(dialog = null) }

    fun onSearchFilterChanged(value: String) = _ui.update { it.copy(searchFilter = value) }

    fun onSurahSelected(surah: ChapterData) {
        val target = (_ui.value.dialog as? ListeningDialog.SurahPicker)?.target ?: return
        _ui.update {
            when (target) {
                SurahTarget.RANGE_START -> it.copy(startSurah = surah, startVerse = 1)
                SurahTarget.RANGE_END -> it.copy(endSurah = surah, endVerse = 1)
                SurahTarget.FULL_SURAH -> it.copy(selectedSurah = surah)
            }.copy(dialog = null, searchFilter = "")
        }
        if (target == SurahTarget.FULL_SURAH) play()
    }

    fun onVerseSelected(verse: Int) {
        val end = (_ui.value.dialog as? ListeningDialog.VersePicker)?.end ?: return
        _ui.update {
            when (end) {
                RangeEnd.START -> it.copy(startVerse = verse)
                RangeEnd.END -> it.copy(endVerse = verse)
            }.copy(dialog = null)
        }
    }


    fun onRepetitionSelected(value: String) {
        val kind = (_ui.value.dialog as? ListeningDialog.Repetition)?.kind ?: return
        val options = _ui.value.options
        setOptions(
            when (kind) {
                RepetitionKind.SURAH -> options.copy(surahRepetitions = value)
                RepetitionKind.VERSE -> options.copy(verseRepetitions = value)
                RepetitionKind.RANGE -> options.copy(rangeRepetitions = value)
            }
        )
        dismissDialog()
    }

    fun toggleContinuous() {
        val options = _ui.value.options
        setOptions(options.copy(continuousChapterPlaying = !options.continuousChapterPlaying))
    }

    /** Applies immediately to what is playing, like before. */
    private fun setOptions(options: ListeningOptions) {
        _ui.update { it.copy(options = options) }
        playbackManager.updateOptions(options)
    }


    fun play() {
        val s = _ui.value
        when (s.mode) {
            QuranListeningMode.سورة_كاملة -> {
                val surah = s.selectedSurah ?: return
                playbackManager.play(
                    QuranPlaybackRequest.FullSurah(
                        surah = surah,
                        chapterNames = s.chapters.sortedBy { it.surahId }.map { it.chapterName }
                    ),
                    s.options
                )
            }

            else -> playRange(s)
        }
    }

    private fun playRange(s: ListeningUiState) {
        val start = s.startSurah ?: return
        val end = s.endSurah ?: return

        if (start.surahId > end.surahId || (start.surahId == end.surahId && s.startVerse > s.endVerse)) {
            AppToastManager.show(
                message = "موضع بداية التلاوة يجب ان لا يرد بعد موضع النهاية",
                type = AppToastType.ERROR
            )
            return
        }

        viewModelScope.launch {
            val verses = withContext(Dispatchers.IO) {
                repository.getPagesContentRange(
                    startingSurah = start.surahId,
                    startingVerse = s.startVerse,
                    endSurah = end.surahId,
                    endVerse = s.endVerse,
                )
            }
            if (verses.isEmpty()) return@launch

            val files = buildList {
                verses.forEach { verse ->
                    // basmalah at the start of each surah except Al-Fatiha and At-Tawbah
                    if (verse.verseNum == 1 && verse.surahNum != 1 && verse.surahNum != 9) {
                        add(QuranAudio.BASMALAH_FILE)
                    }
                    add(QuranAudio.verseFile(verse))
                }
            }

            playbackManager.play(
                QuranPlaybackRequest.VerseRange(start, s.startVerse, end, s.endVerse, files),
                s.options
            )
        }
    }

    fun pause() {
        playbackManager.pause()
    }

    fun increasePlaybackSpeed() {
        playbackManager.increaseSpeed()
    }

    fun decreasePlaybackSpeed() {
        playbackManager.decreaseSpeed()
    }

    fun onSliderChanged(value: Float) {
        _sliderPosition.value = value.toLong()
        playbackManager.seekTo(value.toLong())
    }


    private fun scanDownloadedChapters(): Set<Int> =
        (1..114).filter { QuranAudio.localFile(context, QuranAudio.chapterFile(it)).exists() }.toSet()

    fun downloadChapter(surahId: Int) {
        if (!Helpers.checkNetworkConnectivity(context)) {
            AppToastManager.show(message = "لا يوجد اتصال بالانترنت", type = AppToastType.ERROR)
            return
        }

        FileDownloader.download(
            context = context,
            url = QuranAudio.chapterUrl(surahId),
            relativePath = QuranAudio.chapterFile(surahId),
            storageBase = StorageBase.EXTERNAL_FILES_DIR
        ).onEach { state ->
            when (state) {
                is DownloadState.Running -> _ui.update { it.copy(isDownloading = true) }

                is DownloadState.Success -> {
                    _ui.update {
                        it.copy(isDownloading = false, downloadedChapters = it.downloadedChapters + surahId)
                    }
                    AppToastManager.show(message = "تم التحميل بنجاح!", type = AppToastType.SUCCESS)
                }

                is DownloadState.Failure -> {
                    _ui.update { it.copy(isDownloading = false) }
                    state.error.printStackTrace()
                    reportException(
                        exception = state.error,
                        file = "ListeningViewModel",
                        details = "Cancellation or Failure to download surah file; link: /chapters/$surahId.mp3"
                    )
                    val message = if (state.error.message?.contains("ENOSPC") == true) {
                        context.resources.getString(R.string.enospc)
                    } else {
                        "حدث خطأ اثناء التحميل"
                    }
                    AppToastManager.show(message = message, type = AppToastType.ERROR)
                }
            }
        }.launchIn(viewModelScope)
    }

}

class ListeningViewModelFactory(
    private val repository: QuranRepository,
    private val playbackManager: QuranPlaybackManager,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListeningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListeningViewModel(repository, playbackManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

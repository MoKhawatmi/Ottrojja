package com.ottrojja.composables.quranRangeSelector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.repositories.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranRangeSelectorViewModel(
    private val repository: QuranRepository,
    application: Application
) : AndroidViewModel(application) {

    val context = application.applicationContext;

    enum class SelectionTarget {
        START,
        END
    }

    private var initialized = false

    fun initializeRange(range: QuranRangeState) {
        if (initialized) return

        _uiState.update {
            it.copy(range = range)
        }

        initialized = true
    }

    fun reset() {
        initialized = false

        _uiState.update {
            it.copy(
                range = QuranRangeState(),
                searchQuery = "",
                showSurahDialog = false,
                showVerseDialog = false,
                selectionTarget = SelectionTarget.START
            )
        }
    }

    private val _rangeChanges = MutableSharedFlow<QuranRangeState>()
    val rangeChanges = _rangeChanges.asSharedFlow()

    data class UiState(
        val range: QuranRangeState = QuranRangeState(),
        val chapters: List<ChapterData> = emptyList(),
        val searchQuery: String = "",
        val showSurahDialog: Boolean = false,
        val showVerseDialog: Boolean = false,
        val selectionTarget: SelectionTarget = SelectionTarget.START,
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadChapters()
    }

    private suspend fun updateRange(
        newRange: QuranRangeState
    ) {
        _uiState.update {
            it.copy(range = newRange)
        }

        _rangeChanges.emit(newRange)
    }

    private fun loadChapters() {
        viewModelScope.launch {
            val chapters = withContext(Dispatchers.IO) {
                repository.getAllChapters()
            }

            _uiState.update {
                it.copy(
                    chapters = chapters,
                    isLoading = false
                )
            }
        }
    }


    fun openSurahSelection(target: SelectionTarget) {

        _uiState.update {
            it.copy(
                selectionTarget = target,
                showSurahDialog = true,
                searchQuery = ""
            )
        }
    }

    fun openVerseSelection(target: SelectionTarget) {
        _uiState.update {
            it.copy(
                selectionTarget = target,
                showVerseDialog = true
            )
        }
    }

    fun dismissSurahDialog() {
        _uiState.update {
            it.copy(showSurahDialog = false)
        }
    }

    fun dismissVerseDialog() {
        _uiState.update {
            it.copy(showVerseDialog = false)
        }
    }

    fun updateSearchQuery(value: String) {
        _uiState.update {
            it.copy(searchQuery = value)
        }
    }

    fun surahUpdated(surah: ChapterData) {
        val target = _uiState.value.selectionTarget

        viewModelScope.launch {
            val firstVerse = withContext(Dispatchers.IO) {
                repository.getPageContent(
                    surah.surahId,
                    1
                )
            } ?: return@launch

            val currentRange = _uiState.value.range

            val updatedRange = when (target) {
                SelectionTarget.START -> {
                    currentRange.copy(
                        startSurah = surah,
                        startVerse = firstVerse
                    )
                }

                SelectionTarget.END -> {
                    currentRange.copy(
                        endSurah = surah,
                        endVerse = firstVerse
                    )
                }
            }

            updateRange(updatedRange)

            dismissSurahDialog()
        }
    }

    fun verseUpdated(verseNum: Int) {
        val state = _uiState.value
        val target = state.selectionTarget

        val surah = when (target) {
            SelectionTarget.START -> state.range.startSurah
            SelectionTarget.END -> state.range.endSurah
        } ?: return

        viewModelScope.launch {
            val verse = withContext(Dispatchers.IO) {
                repository.getPageContent(
                    surah.surahId,
                    verseNum
                )
            } ?: return@launch

            val currentRange = _uiState.value.range

            val updatedRange = when (target) {
                SelectionTarget.START -> {
                    currentRange.copy(
                        startVerse = verse
                    )
                }

                SelectionTarget.END -> {
                    currentRange.copy(
                        endVerse = verse
                    )
                }
            }

            updateRange(updatedRange)

            dismissVerseDialog()
        }
    }

    fun getFilteredChapters(): List<ChapterData> {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        if (query.isBlank()) {
            return state.chapters
        }

        return state.chapters.filter { chapter ->
            chapter.chapterName.contains(
                query,
                ignoreCase = true
            ) ||
                    chapter.surahId.toString() == query ||
                    chapter.surahId.toString() ==
                    convertToArabicNumbers(query)
        }
    }
}

class QuranRangeSelectorViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranRangeSelectorViewModel::class.java)) {
            return QuranRangeSelectorViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
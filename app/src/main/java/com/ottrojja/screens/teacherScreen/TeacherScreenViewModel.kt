package com.ottrojja.screens.teacherScreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class TeacherScreenViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {

    private val pagesList: List<String> = (1..604).map { "صفحة $it" };

    private val _selectedPage = mutableStateOf(
        QuranPage(
            "",
            "",
            arrayOf(""),
            arrayOf(""),
            arrayOf(""),
            arrayOf<PageContent>()
        )
    )
    var selectedPage: QuranPage
        get() = _selectedPage.value
        set(value: QuranPage) {
            _selectedPage.value = value
        }

    private val _selectedPageVerses = mutableStateOf(
        emptyList<PageContent>()
    )
    var selectedPageVerses: List<PageContent>
        get() = _selectedPageVerses.value
        set(value: List<PageContent>) {
            _selectedPageVerses.value = value
        }


    private val _currentVerse = mutableStateOf(PageContent())
    var currentVerse: PageContent
        get() = _currentVerse.value
        set(value: PageContent) {
            _currentVerse.value = value
        }

    private var _currentVerseIndex = mutableStateOf(0)
    var currentVerseIndex: Int
        get() = _currentVerseIndex.value
        set(value) {
            _currentVerseIndex.value = value
        }

    private var _hasStarted = mutableStateOf(false)
    var hasStarted: Boolean
        get() = _hasStarted.value
        set(value) {
            _hasStarted.value = value
        }


    private val _mode = mutableStateOf(TeacherMode.PAGE_SELECTION)
    var mode: TeacherMode
        get() = _mode.value
        set(value) {
            _mode.value = value
        }


    private var _searchFilter by mutableStateOf("")
    var searchFilter: String
        get() = this._searchFilter
        set(value) {
            this._searchFilter = value
        }

    val maxTries = 3;

    private var _reachedMaxTries = mutableStateOf(false)
    var reachedMaxTries: Boolean
        get() = _reachedMaxTries.value
        set(value) {
            _reachedMaxTries.value = value
        }

    private var _currentTry = mutableStateOf(0)
    var currentTry: Int
        get() = _currentTry.value
        set(value) {
            _currentTry.value = value
        }

    fun getPagesList(): List<String> {
        return pagesList.filter { data ->
            data.indexOf(_searchFilter) != -1 || data.indexOf(
                Helpers.convertToArabicNumbers(_searchFilter)
            ) != -1
        };
    }

    fun pageSelected(pageNum: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedPage.value = repository.getPage(pageNum)
            _selectedPageVerses.value =
                _selectedPage.value.pageContent.filter { it.type == "verse" }.toList()
            println(_selectedPage.value)
            println(_selectedPageVerses.value)
            _currentVerseIndex.value = 0
            _currentVerse.value = _selectedPageVerses.value[_currentVerseIndex.value]
            _mode.value = TeacherMode.PAGE_TRAINING;
        }
    }

    fun startTeaching() {
        _hasStarted.value = true;
        generateCutVerse();
    }

    private var _allRight = mutableStateOf(false)
    var allRight: Boolean
        get() = _allRight.value
        set(value) {
            _allRight.value = value
        }


    fun checkVerse() {
        if (!_reachedMaxTries.value) {
            _currentTry.value++;
        } else {
            return;
        }
        println(_inputSolutions.toMap())

        var allRight = true;

        // compare inputsolution with solutionmap
        solutionMap.keys.forEach { key ->
            val keyInput = _inputSolutions.get(key)?.answer?.trim()
            if (keyInput != null && keyInput.length > 0) {
                if (solutionMap.get(key)?.contains(keyInput) == true) {
                    keyInput.let { TeacherAnswer(it, AnswerStatus.RIGHT) }
                        .let { _inputSolutions.set(key, it) }
                    println("solution $key right")
                } else {
                    keyInput.let { TeacherAnswer(it, AnswerStatus.WRONG) }
                        .let { _inputSolutions.set(key, it) }
                    println("solution $key wrong")
                    allRight = false;
                }
            } else {
                println("solution $key empty")
                allRight = false;
            }
        }

        // need to get all solutions right to move on
        if (allRight) {
            _allRight.value = true;
        } else if (!allRight && _reachedMaxTries.value) {

        }

        if (_currentTry.value == maxTries) {
            _reachedMaxTries.value = true;
        }
    }

    fun proceedVerse() {
        solutionMap.clear()
        _reachedMaxTries.value = false;
        _currentTry.value = 0;
        _allRight.value = false;
        if (_currentVerseIndex.value + 1 <= _selectedPageVerses.value.size - 1) {
            println("here")
            _currentVerseIndex.value++;
            _currentVerse.value = _selectedPageVerses.value[_currentVerseIndex.value]
            generateCutVerse()
        }
    }

    val solutionMap = hashMapOf<Int, List<String>>()

    fun generateCutVerse() {
        val verseWordsPlain = _currentVerse.value.verseTextPlain.split("\\s+".toRegex())
        val verseWordsNum = verseWordsPlain.size;

        val indices = (0 until verseWordsNum).toList().shuffled(Random)
        val hiddenIndices = if (verseWordsNum == 1) indices else indices.take(verseWordsNum / 2)
        println(verseWordsNum)
        println(hiddenIndices)
        hiddenIndices.forEach { index ->
            solutionMap.put(
                index,
                listOf(
                    verseWordsPlain.get(index),
                    verseWordsPlain.get(index).replace(Regex("[أإآ]"), "ا")
                )
            )
        }
        _inputSolutions.clear();
        _inputSolutions.putAll(solutionMap.keys.associateWith {
            TeacherAnswer(
                "",
                AnswerStatus.UNCHECKED
            )
        }.toMap(HashMap()))

        println(solutionMap)
    }

    // i really don't understand how this works
    private var _inputSolutions = mutableStateMapOf<Int, TeacherAnswer>()
    var inputSolutions: SnapshotStateMap<Int, TeacherAnswer>
        get() = _inputSolutions
        set(value: SnapshotStateMap<Int, TeacherAnswer>) {
            _inputSolutions = value
        }


    enum class TeacherMode {
        PAGE_SELECTION, PAGE_TRAINING
    }
}

class TeacherScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherScreenViewModel::class.java)) {
            return TeacherScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
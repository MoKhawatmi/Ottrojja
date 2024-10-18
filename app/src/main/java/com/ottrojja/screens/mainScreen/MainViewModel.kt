package com.ottrojja.screens.mainScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: QuranRepository) : ViewModel() {
    private val pagesList: List<String> = (1..604).map { "صفحة $it" };
    lateinit private var partsList: List<PartData>;
    lateinit private var chaptersList: List<ChapterData>;

    init {
        viewModelScope.launch(Dispatchers.IO) {
            partsList = repository.getAllParts()
            chaptersList = repository.getAllChapters()
        }
    }

    private var _searchFilter by mutableStateOf("")
    var searchFilter: String
        get() = this._searchFilter
        set(value) {
            this._searchFilter = value
        }

    private var _selectedSection by mutableStateOf(0)
    var selectedSection: Int
        get() = this._selectedSection
        set(value) {
            this._selectedSection = value
        }

    private var _showImageList by mutableStateOf(true)
    var showImageList: Boolean
        get() = this._showImageList
        set(value) {
            this._showImageList = value
        }

    private val _quranSearchResults = mutableStateOf(mutableListOf<SearchResult>())
    var quranSearchResults: MutableList<SearchResult>
        get() = _quranSearchResults.value
        set(value) {
            _quranSearchResults.value = value
        }

    fun searchInQuran(text: String) {
        val searchText = text.trim()
        if (searchText.length == 0) {
            _quranSearchResults.value = emptyList<SearchResult>().toMutableList()
            return;
        }
        _quranSearchResults.value.clear()
        var tempResults = mutableListOf<SearchResult>()
        println("searching for $text")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllPages().forEach { page ->
                page.pageContent.forEach { verse ->
                    if (verse.type == "verse" && (verse.verseText.contains(searchText) || verse.verseTextPlain.contains(
                            searchText
                        ))
                    ) {
                        tempResults.add(
                            SearchResult(
                                page.pageNum,
                                verse.surahNum,
                                verse.verseNum,
                                verse.verseText,
                                chaptersList.find { "${it.surahId}" == verse.surahNum }!!.chapterName
                            )
                        );
                    }
                }
            }
            _quranSearchResults.value = tempResults;
        }
    }

    fun getPagesList(): List<String> {
        return pagesList.filter { data ->
            data.indexOf(_searchFilter) != -1 || data.indexOf(
                convertToArabicNumbers(_searchFilter)
            ) != -1
        };
    }

    fun getPartsList(): List<PartData> {
        return partsList.filter { data ->
            data.partName.indexOf(_searchFilter) != -1 || data.partId.indexOf(
                _searchFilter
            ) != -1 || data.partId.indexOf(convertToArabicNumbers(_searchFilter)) != -1
        };
    }

    fun getChaptersList(): List<ChapterData> {
        return chaptersList.filter { data -> data.chapterName.indexOf(_searchFilter) != -1 };
    }

    fun findPartStart(inputValue: String): String {
        return partsList.find { data -> data.partId == inputValue }!!.partStartPage
    }
}

class MainViewModelFactory(
    private val repository: QuranRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
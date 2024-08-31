package com.ottrojja.screens.mainScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.convertToIndianNumbers
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranStore
import com.ottrojja.classes.SearchResult

class MainViewModel : ViewModel() {
    val quranData: List<QuranPage> = QuranStore.getQuranData();
    private val pagesList: List<String> = getPages();
    private val partsList: List<PartData> = getParts();
    private val chaptersList: List<ChapterData> = getChapters();

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
        get() = this._quranSearchResults.value
        set(value) {
            this._quranSearchResults.value = value
        }

    fun searchInQuran(text: String) {
        val searchText = text.trim()
        if (searchText.length == 0) {
            _quranSearchResults.value.clear()
            return;
        }
        _quranSearchResults.value.clear()
        var tempResults = mutableListOf<SearchResult>()
        quranData.forEach { page ->
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
                            verse.verseText
                        )
                    );
                }
            }
        }
        _quranSearchResults.value = tempResults;
    }


    private var sectionsArray = arrayOf("الصفحات", "الاجزاء", "السور")

    fun getPagesList(): List<String> {
        return pagesList.filter { data -> data.indexOf(_searchFilter) != -1 || data.indexOf(convertToArabicNumbers(_searchFilter)) != -1 };
    }

    fun getPartsList(): List<PartData> {
        return partsList.filter { data -> data.partName.indexOf(_searchFilter) != -1 || data.partId.indexOf(_searchFilter) != -1 || data.partId.indexOf(convertToArabicNumbers(_searchFilter)) != -1 };
    }

    fun getChaptersList(): List<ChapterData> {
        return chaptersList.filter { data -> data.chapterName.indexOf(_searchFilter) != -1 };
    }

    fun findPartStart(inputValue: String): String {
        return partsList.find { data -> data.partId == inputValue }!!.partStartPage
    }
}


private fun getPages(): List<String> {
    val list = mutableListOf<String>()
    for (i in 1..604) {
        list.add("صفحة $i")
    }
    return list
}

private fun getParts(): List<PartData> {
    return QuranStore.getPartsData()
}

private fun getChapters(): List<ChapterData> {
    return QuranStore.getChaptersData();
}
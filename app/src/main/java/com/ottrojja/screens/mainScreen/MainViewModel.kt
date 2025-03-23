package com.ottrojja.screens.mainScreen

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.PageContentItemType
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

    private val pagesList: List<String> = (1..604).map { "صفحة $it" };
    lateinit private var partsList: List<PartData>;
    lateinit private var chaptersList: List<ChapterData>;

    init {
        viewModelScope.launch(Dispatchers.IO) {
            partsList = repository.getAllParts()
            chaptersList = repository.getAllChapters()
        }
    }

    private var _mostRecentPage by mutableStateOf("")
    var mostRecentPage: String
        get() = _mostRecentPage
        set(value) {
            _mostRecentPage = value
        }

    private var _searchFilter by mutableStateOf("")
    var searchFilter: String
        get() = _searchFilter
        set(value) {
            _searchFilter = value
        }

    private var _selectedSection by mutableStateOf(BrowsingOption.الصفحات)
    var selectedSection: BrowsingOption
        get() = _selectedSection
        set(value) {
            _selectedSection = value
        }

    private var _showImageList by mutableStateOf(true)
    var showImageList: Boolean
        get() = this._showImageList
        set(value) {
            this._showImageList = value
        }


    private val _quranSearchResults = mutableStateListOf<SearchResult>()
    val quranSearchResults: MutableList<SearchResult>
        get() = _quranSearchResults


    val handler = Handler(Looper.getMainLooper())
    private var searchJob: Job? = null
    fun searchInQuran(text: String) {
        handler.removeCallbacksAndMessages(null)
        val searchText = text.trim()
        // Cancel previous job if still running
        searchJob?.cancel()
        _quranSearchResults.clear()
        if (searchText.length == 0) {
            return;
        }
        println("searching for $text")
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<SearchResult>()
            repository.getAllPages().forEach { page ->
                page.pageContent.forEach { verse ->
                    if (verse.type == PageContentItemType.verse && (verse.verseText.contains(
                            searchText
                        )
                                || verse.verseTextPlain.contains(searchText))
                    ) {
                        results.add(
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
            withContext(Dispatchers.Main) {
                _quranSearchResults.addAll(results)
            }
        }
        handler.postDelayed(
            { saveLatestSearchResult() },
            500
        )
    }

    fun getPagesList(): List<String> {
        return pagesList.filter { page ->
            page.contains(_searchFilter) || page.contains(convertToArabicNumbers(_searchFilter))
        };
    }

    fun getPartsList(): List<PartData> {
        return partsList.filter { part ->
            part.partName.contains(_searchFilter) || part.partId.contains(_searchFilter
            ) || part.partId.contains(
                convertToArabicNumbers(_searchFilter)
            )
        };
    }

    fun getChaptersList(): List<ChapterData> {
        return chaptersList.filter { chapter ->
            chapter.chapterName.contains(_searchFilter)
                    || chapter.surahId.toString() == convertToArabicNumbers(_searchFilter)
                    || chapter.surahId.toString() == _searchFilter
        };
    }

    fun findPartStart(inputValue: String): String {
        return partsList.find { data -> data.partId == inputValue }!!.partStartPage
    }

    fun shareVerse(context: Context, verseItem: SearchResult) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "آية قرآنية")
            putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
            putExtra(
                Intent.EXTRA_TEXT,
                "{${verseItem.verseText}} \n سورة ${verseItem.surahName} - ${verseItem.verseNum}"
            )
            type = "text/plain"
        }

        val shareIntent =
            Intent.createChooser(sendIntent, "مشاركة الآية")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, shareIntent, null)
    }

    fun saveLatestSearchResult() {
        println("saveing ${_searchFilter}")
        sharedPreferences.edit().putString("latestSearch", _searchFilter).apply()
    }

    fun invokeLatestSearchOperation() {
        val latestSearch = sharedPreferences.getString("latestSearch", "")
        if (latestSearch != null && latestSearch.length > 0) {
            _searchFilter = latestSearch
            searchInQuran(latestSearch)
        }
    }

    fun invokeMostRecentPage() {
        _mostRecentPage = sharedPreferences.getString("mostRecentPage", "")!!
        println("most recent page: ${_mostRecentPage}")
    }

    fun clickBrowsingOption(option: BrowsingOption) {
        _selectedSection = option;
        searchFilter = "";
        if (_selectedSection == BrowsingOption.البحث) {
            quranSearchResults.clear()
            invokeLatestSearchOperation()
        }

    }
}

enum class BrowsingOption {
    الصفحات, السور, الاجزاء, البحث
}

class MainViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
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
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.SearchResult
import com.ottrojja.room.relations.PartWithQuarters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

    private val pagesList: List<String> = (1..604).map { "صفحة $it" };
    lateinit private var chaptersList: List<ChapterData>;
    private val partsWithQuartersList = mutableStateListOf<ExpandableItem<PartWithQuarters>>()

    private var _advice by mutableStateOf("")
    var advice: String
        get() = _advice
        set(value) {
            _advice = value
        }


    init {
        viewModelScope.launch(Dispatchers.IO) {
            chaptersList = repository.getAllChapters()
            val partsWithQuarters = repository.getAllPartsWithQuarters().map {
                ExpandableItem(data = it)
            };
            withContext(Dispatchers.Main) {
                partsWithQuartersList.addAll(partsWithQuarters)
            }
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
            repository.searchPagesContent(searchText).forEach { pageContent ->
                results.add(
                    SearchResult(
                        pageContent.pageNum,
                        pageContent.surahNum,
                        pageContent.verseNum!!,
                        pageContent.verseText!!,
                        chaptersList.find { it.surahId == pageContent.surahNum }!!.chapterName
                    )
                );
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

    fun getPartsList(): List<ExpandableItem<PartWithQuarters>> {
        return partsWithQuartersList.filter {
            (it.data.part.partName.contains(_searchFilter)
                    || it.data.part.partId.contains(_searchFilter)
                    || it.data.part.partId.contains(convertToArabicNumbers(_searchFilter)))
                    || (it.data.quarters.any { it.hizb.equals(_searchFilter) }
                    || it.data.quarters.any {
                it.hizb.equals(convertToArabicNumbers(_searchFilter)
                )
            })
        };
    }

    fun getChaptersList(): List<ChapterData> {
        return chaptersList.filter { chapter ->
            chapter.chapterName.contains(_searchFilter)
                    || chapter.surahId.toString() == convertToArabicNumbers(_searchFilter)
                    || chapter.surahId.toString() == _searchFilter
        };
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

    fun updateExpandedPartItem(item: ExpandableItem<PartWithQuarters>) {
        val findItemIndex = partsWithQuartersList.indexOfFirst { it.data.part.partId == item.data.part.partId }
        partsWithQuartersList.set(findItemIndex, item.copy(expanded = !item.expanded))
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
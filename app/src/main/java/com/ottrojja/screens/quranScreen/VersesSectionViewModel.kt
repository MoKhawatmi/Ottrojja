package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.room.entities.PageContent
import com.ottrojja.classes.QuranRepository
import com.ottrojja.screens.listeningScreen.ListeningViewModel.SelectionPhase
import com.ottrojja.screens.mainScreen.ChapterData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VersesSectionViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application),
    LifecycleObserver {
    val context = application.applicationContext;
    private val itemsList = MutableStateFlow(mutableListOf<SectionVerse>())
    val items: StateFlow<List<SectionVerse>> get() = itemsList

    fun setItems(pageContentItems: List<PageContent>) {
        val tempList = mutableListOf<SectionVerse>()
        pageContentItems.forEach { item -> tempList.add(SectionVerse(item, false)) }
        this.itemsList.value = tempList;
    }

    fun updateExpanded(item: SectionVerse) {
        val tempList = itemsList.value.toMutableList();
        val indexOfItem =
            tempList.indexOfFirst { verse -> verse.pageContent.verseNum == item.pageContent.verseNum && verse.pageContent.surahNum == item.pageContent.surahNum };
        val newItem: SectionVerse = SectionVerse(item.pageContent, !item.expanded);
        tempList.set(indexOfItem, newItem);
        itemsList.value = tempList;
    }

    private val chaptersList = CompletableDeferred<List<ChapterData>>()

    suspend fun initChaptersList() {
        println("Fetching chapters list")
        viewModelScope.launch(Dispatchers.IO) {
            val chapters = repository.getAllChapters()
            chaptersList.complete(chapters)
            if (_selectedSurah.value == null) {
                _selectedSurah.value = chaptersList.await().get(0);
            }
        }
    }

    private var _searchFilter = mutableStateOf("")
    var searchFilter: String
        get() = _searchFilter.value
        set(value) {
            _searchFilter.value = value
        }

    suspend fun getChaptersList(): List<ChapterData> {
        return chaptersList.await().filter { chapter ->
            chapter.chapterName.contains(_searchFilter.value)
                    || chapter.surahId.toString() == convertToArabicNumbers(_searchFilter.value)
                    || chapter.surahId.toString() == _searchFilter.value
        };
    }

    private var _showSurahSelectionDialog = mutableStateOf(false)
    var showSurahSelectionDialog: Boolean
        get() = _showSurahSelectionDialog.value
        set(value) {
            _showSurahSelectionDialog.value = value
        }

    private var _showVerseSelectionDialog = mutableStateOf(false)
    var showVerseSelectionDialog: Boolean
        get() = _showVerseSelectionDialog.value
        set(value) {
            _showVerseSelectionDialog.value = value
        }

    private var _selectionPhase = mutableStateOf(SelectionPhase.START)
    var selectionPhase: SelectionPhase
        get() = _selectionPhase.value
        set(value) {
            _selectionPhase.value = value
        }

    private var _selectedSurah = mutableStateOf<ChapterData?>(null)
    var selectedSurah: ChapterData?
        get() = _selectedSurah.value
        set(value) {
            _selectedSurah.value = value
        }

    private var _startingVerse = mutableStateOf(1)
    var startingVerse: Int
        get() = _startingVerse.value
        set(value) {
            _startingVerse.value = value
        }

    private var _endVerse = mutableStateOf(1)
    var endVerse: Int
        get() = _endVerse.value
        set(value) {
            _endVerse.value = value
        }

    /*fun surahSelected(surah: ChapterData) {
        when (_selectionPhase.value) {
            SelectionPhase.START -> {
                _startingVerse.value = 1;
            }

            SelectionPhase.END -> {
                _endVerse.value = 1;
            }

        }
    }*/

    fun verseSelected(verse: Int) {
        if (_selectionPhase.value == SelectionPhase.START) {
            _startingVerse.value = verse;
        } else {
            _endVerse.value = verse;
        }
    }


    fun shareVerse(verseItem: PageContent) {
        viewModelScope.launch(Dispatchers.IO) {
            val chapterName = repository.getChapter(verseItem.surahNum.toInt()).chapterName
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "آية قرآنية")
                putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "{${verseItem.verseText}} \n سورة ${chapterName} - ${verseItem.verseNum}\n${context.resources.getString(R.string.share_app)}"
                )
                type = "text/plain"
            }

            val shareIntent =
                Intent.createChooser(sendIntent, "مشاركة الآية")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, shareIntent, null)
        }
    }
}

class VersesSectionViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VersesSectionViewModel::class.java)) {
            return VersesSectionViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
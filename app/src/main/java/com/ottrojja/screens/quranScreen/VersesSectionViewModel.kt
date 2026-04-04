package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.Intent
import android.widget.Toast
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
import kotlinx.coroutines.withContext

class VersesSectionViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application),
    LifecycleObserver {
    val context = application.applicationContext;
    private val itemsList = MutableStateFlow<List<SectionVerse>>(emptyList())
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
        if (indexOfItem == -1) return
        val newItem = SectionVerse(item.pageContent, !item.expanded);
        tempList.set(indexOfItem, newItem);
        itemsList.value = tempList;
    }

    private var _searchFilter = mutableStateOf("")
    var searchFilter: String
        get() = _searchFilter.value
        set(value) {
            _searchFilter.value = value
        }

    private var _showVerseSelectionDialog = mutableStateOf(false)
    var showVerseSelectionDialog: Boolean
        get() = _showVerseSelectionDialog.value
        set(value) {
            _showVerseSelectionDialog.value = value
        }

    private var _showVerseShareDialog = mutableStateOf(false)
    var showVerseShareDialog: Boolean
        get() = _showVerseShareDialog.value
        set(value) {
            _showVerseShareDialog.value = value
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

    private var _startingVerse = mutableStateOf<PageContent?>(null)
    var startingVerse: PageContent?
        get() = _startingVerse.value
        set(value) {
            _startingVerse.value = value
        }

    private var _endVerse = mutableStateOf<PageContent?>(null)
    var endVerse: PageContent?
        get() = _endVerse.value
        set(value) {
            _endVerse.value = value
        }

    fun verseSelected(verse: PageContent) {
        if (_selectionPhase.value == SelectionPhase.START) {
            _startingVerse.value = verse;
        } else {
            _endVerse.value = verse;
        }
    }

    private var _selectionVersesList = mutableStateOf(emptyList<PageContent>())
    var selectionVersesList: List<PageContent>
        get() = _selectionVersesList.value
        set(value) {
            _selectionVersesList.value = value
        }

    val filteredVerses: List<PageContent>
        get() = if (searchFilter.isBlank()) {
            selectionVersesList
        } else {
            selectionVersesList.filter {
                it.verseNum.toString().contains(searchFilter, ignoreCase = true)
            }
        }


    private var _shareChapterName = mutableStateOf("")
    var shareChapterName: String
        get() = _shareChapterName.value
        set(value) {
            _shareChapterName.value = value
        }

    private var isFetching = false
    fun fetchChapterVerses(verseItem: PageContent) {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch(Dispatchers.IO) {
            val verses = repository.getChapterVerses(verseItem.surahNum)
            val chapterName = repository.getChapter(verseItem.surahNum).chapterName
            withContext(Dispatchers.Main) {
                _selectionVersesList.value = verses;
                _startingVerse.value = verseItem;
                _endVerse.value = verseItem;
                _shareChapterName.value = chapterName;
                // show share bottomsheet after all fetching and setting states is done
                _showVerseShareDialog.value = true;
                isFetching = false
            }
        }
    }

    fun shareVerses() {
        val startVerseNum = _startingVerse.value?.verseNum
        val endVerseNum = _endVerse.value?.verseNum

        if (startVerseNum == null || endVerseNum == null) return

        if (startVerseNum > endVerseNum) {
            Toast.makeText(
                context,
                "موضع اية النهاية لا يجب ان يكون قبل اية البداية",
                Toast.LENGTH_SHORT
            ).show()
            return;
        }
        val startVerseIndex = _selectionVersesList.value.indexOf(_startingVerse.value)
        val endVerseIndex = _selectionVersesList.value.indexOf(_endVerse.value) + 1
        var versesRange = "$startVerseNum";
        if (startVerseNum != endVerseNum) {
            versesRange += " - $endVerseNum"
        }

        val shareVersesText = selectionVersesList.subList(startVerseIndex, endVerseIndex).joinToString(separator = "") { "${it.verseText} {${it.verseNum}} " }
        viewModelScope.launch(Dispatchers.IO) {
            val chapterName = _shareChapterName.value
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "آيات قرآنية")
                putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "{${shareVersesText}} \n سورة ${chapterName}: $versesRange\n${context.resources.getString(R.string.share_app)}"
                )
                type = "text/plain"
            }

            val shareIntent =
                Intent.createChooser(sendIntent, "مشاركة")
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
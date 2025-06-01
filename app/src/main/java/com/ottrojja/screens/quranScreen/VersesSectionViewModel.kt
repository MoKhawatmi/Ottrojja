package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.room.entities.PageContent
import com.ottrojja.classes.QuranRepository
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
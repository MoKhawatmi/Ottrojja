package com.ottrojja.screens.quranScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import com.ottrojja.classes.PageContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VersesSectionViewModel(application: Application) : AndroidViewModel(application),
    LifecycleObserver {
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

}

package com.ottrojja.screens.BookmarksScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Bookmark
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.BookmarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BookmarksViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private var _bookmarks = mutableStateListOf<Bookmark>()
    val bookmarks: MutableList<Bookmark>
        get() = _bookmarks

    fun getBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bookmarks.clear()
                val storedBookmarks = repository.getBookmarks();
                println(storedBookmarks)
                storedBookmarks.forEach {
                    _bookmarks.add(
                        Bookmark(
                            pageNum = it.pageNum,
                            timeStamp = it.timeStamp,
                            expanded = false
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
            }
        }

    }


    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteBookmark(BookmarkEntity(bookmark.pageNum, bookmark.timeStamp))
                val indexOfItem = _bookmarks.indexOfFirst { it.pageNum == bookmark.pageNum };
                _bookmarks.removeAt(index = indexOfItem)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, " تم تحديث المرجعيات بنجاح", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateExpanded(item: Bookmark) {
        val indexOfItem = _bookmarks.indexOfFirst { bookmark -> bookmark.pageNum == item.pageNum };
        _bookmarks.set(indexOfItem, item.copy(expanded = !item.expanded));
    }
}

class BookmarksViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
            return BookmarksViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

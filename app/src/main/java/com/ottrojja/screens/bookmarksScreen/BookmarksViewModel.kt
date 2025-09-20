package com.ottrojja.screens.bookmarksScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Bookmark
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.BookmarkEntity
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
        viewModelScope.launch {
            try {
                val storedBookmarks = withContext(Dispatchers.IO) {
                    repository.getBookmarks()
                }
                println(storedBookmarks)
                _bookmarks.clear()
                _bookmarks.addAll(storedBookmarks.map {
                    Bookmark(pageNum = it.pageNum,
                        timeStamp = it.timeStamp,
                        expanded = false
                    )
                })
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "BookmarksViewModel")
                Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteBookmark(BookmarkEntity(bookmark.pageNum, bookmark.timeStamp))

                withContext(Dispatchers.Main) {
                    _bookmarks.remove(bookmark)
                    Toast.makeText(context, " تم تحديث المرجعيات بنجاح", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "BookmarksViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
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

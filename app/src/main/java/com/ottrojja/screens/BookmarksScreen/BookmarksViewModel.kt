package com.ottrojja.screens.BookmarksScreen

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.ottrojja.screens.quranScreen.SectionVerse

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

    private val _bookmarks = mutableStateOf(mutableListOf<Bookmark>())
    var bookmarks: MutableList<Bookmark>
        get() = this._bookmarks.value
        set(value) {
            this._bookmarks.value = value
        }

    fun getBookmarks() {
        val bookmarks = sharedPreferences.getString("bookmarks", "");
        val bookmarksList = mutableListOf<Bookmark>();
        for (item in bookmarks?.split(",")!!) {
            if(item.trim().length!=0){
                bookmarksList.add(Bookmark(item, false))
            }
        }
        this._bookmarks.value = bookmarksList
    }


    fun removeBookmark(pageNum: String) {
        val bookmarks = sharedPreferences.getString("bookmarks", "");
        val bookmarksList = bookmarks?.split(",")?.toMutableList();
        if (bookmarksList?.size == 0) {
            return;
        } else {
            bookmarksList?.remove(pageNum)
        }
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("bookmarks", bookmarksList?.joinToString(","))
        editor.apply()

        Toast.makeText(context, " تم تحديث المرجعيات بنجاح", Toast.LENGTH_LONG).show()

        getBookmarks();
    }

    fun updateExpanded(item: Bookmark) {
        println("update $item")
        val tempList = _bookmarks.value.toMutableList();
        val indexOfItem =
            tempList.indexOfFirst { bookmark -> bookmark.pageNum == item.pageNum };
        println("index $indexOfItem")
        val newItem: Bookmark = Bookmark(item.pageNum, !item.expanded);
        tempList.set(indexOfItem, newItem);
        _bookmarks.value = tempList;
        println(_bookmarks.value)
    }


}
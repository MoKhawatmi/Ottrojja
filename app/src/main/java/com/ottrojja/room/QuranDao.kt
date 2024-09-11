package com.ottrojja.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ottrojja.classes.QuranPage

@Dao
interface QuranDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQuranPage(quranPage: QuranPage)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQuranPages(quranPages: List<QuranPage>)

}
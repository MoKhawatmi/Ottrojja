package com.ottrojja.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ottrojja.classes.Converters
import com.ottrojja.classes.QuranPage
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.quranScreen.E3rabData
import com.ottrojja.screens.quranScreen.TafseerData

@TypeConverters(Converters::class)
@Database(entities = [QuranPage::class, ChapterData::class, PartData::class, TafseerData::class , E3rabData::class], version = 1)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}
package com.ottrojja.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ottrojja.classes.Converters
import com.ottrojja.classes.QuranPage

@TypeConverters(Converters::class)
@Database(entities = [QuranPage::class], version = 1)
abstract class QuranDatabase: RoomDatabase() {
    abstract fun quranDao(): QuranDao
}
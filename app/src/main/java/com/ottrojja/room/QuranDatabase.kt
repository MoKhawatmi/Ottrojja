package com.ottrojja.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ottrojja.classes.CauseOfRevelation
import com.ottrojja.classes.Converters
import com.ottrojja.classes.QuranPage
import com.ottrojja.screens.azkarScreen.Azkar
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.quranScreen.E3rabData
import com.ottrojja.screens.quranScreen.TafseerData

@TypeConverters(Converters::class)
@Database(
    entities = [QuranPage::class, ChapterData::class, PartData::class, TafseerData::class, E3rabData::class, Azkar::class, CauseOfRevelation::class],
    version = 3
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}

// Migration from version 2 to version 3
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the new column 'firstWords' to the 'PartData' table
        database.execSQL("ALTER TABLE PartData ADD COLUMN firstWords TEXT NOT NULL DEFAULT ''".trimIndent())
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `CauseOfRevelation` (
                `id` TEXT PRIMARY KEY NOT NULL,
                `sura` INTEGER NOT NULL,
                `verses` TEXT NOT NULL,
                `text` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new column to the "YourTable" table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Azkar` (
                `azkarTitle` TEXT PRIMARY KEY NOT NULL,
                `ytLink` TEXT NOT NULL,
                `firebaseAddress` TEXT NOT NULL,
                `azkarText` TEXT NOT NULL,
                `image` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

package com.ottrojja.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ottrojja.room.entities.CauseOfRevelation
import com.ottrojja.classes.Converters
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.room.entities.BookmarkEntity
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.dao.KhitmahDao
import com.ottrojja.room.dao.QuranDao
import com.ottrojja.room.dao.ReminderDao
import com.ottrojja.room.dao.TasabeehDao
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList
import com.ottrojja.room.entities.Azkar
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.room.entities.E3rabData
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.Quarter
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.VerseMeanings

@TypeConverters(Converters::class)
@Database(
    entities = [QuranPage::class, PageContent::class, ChapterData::class, PartData::class,
        TafseerData::class, E3rabData::class, Azkar::class,
        CauseOfRevelation::class, BookmarkEntity::class, Khitmah::class,
        KhitmahSession::class, CustomTasbeeh::class, TasabeehList::class, VerseMeanings::class, Quarter::class,
        Reminder::class],
    version = 14
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun khitmahDao(): KhitmahDao
    abstract fun tasabeehDao(): TasabeehDao
    abstract fun reminderDao(): ReminderDao
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Verse tap regions. Rows are filled by SyncLocalData.syncPagesContent()
        db.execSQL("ALTER TABLE PageContent ADD COLUMN polygons TEXT NOT NULL DEFAULT '[]'")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS Azkar")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS Azkar (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                azkarTitle TEXT NOT NULL,
                ytLink TEXT NOT NULL,
                zikrAddress TEXT NOT NULL,
                azkarText TEXT NOT NULL,
                image TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS `KhitmahMark`")
        db.execSQL("DROP TABLE IF EXISTS `Khitmah`")

        // Recreate Khitmah with the new schema
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Khitmah` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `targetDate` TEXT,
                `reminderId` INTEGER,
                `pagesRead` INTEGER NOT NULL DEFAULT 0,
                `versesCovered` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`reminderId`)
                    REFERENCES `Reminder`(`id`)
                    ON UPDATE NO ACTION
                    ON DELETE SET NULL
            )
            """.trimIndent()
        )

        // Recreate Khitmah indices
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_Khitmah_reminderId`
            ON `Khitmah` (`reminderId`)
            """.trimIndent()
        )

        // Create KhitmahSession
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `KhitmahSession` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `khitmahId` INTEGER NOT NULL,
                `startPage` INTEGER NOT NULL,
                `startVerseId` INTEGER NOT NULL,
                `startSurahId` INTEGER NOT NULL,
                `endPage` INTEGER NOT NULL,
                `endVerseId` INTEGER NOT NULL,
                `endSurahId` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `readingDate` TEXT NOT NULL,
                `reflection` TEXT,
                FOREIGN KEY(`khitmahId`)
                    REFERENCES `Khitmah`(`id`)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Create KhitmahSession indices
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_KhitmahSession_khitmahId`
            ON `KhitmahSession` (`khitmahId`)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_KhitmahSession_readingDate`
            ON `KhitmahSession` (`readingDate`)
            """.trimIndent()
        )

        db.execSQL("""
            ALTER TABLE PartData
            ADD COLUMN partLastPage TEXT NOT NULL DEFAULT '1'
        """.trimIndent()
        )

        db.execSQL("ALTER TABLE CustomTasbeeh ADD COLUMN currentCount INTEGER NOT NULL DEFAULT 0")

    }
}


val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE PageContent
            ADD COLUMN mutashabihat TEXT NOT NULL DEFAULT '[]'
        """.trimIndent()
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN nameMeaning TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN namingReason TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN otherNames TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN generalPurpose TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN reasonOfRevelation TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN virtues TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ChapterData ADD COLUMN surahEvents TEXT NOT NULL DEFAULT ''")
    }
}


val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS Reminder")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS Reminder (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                customMessage TEXT,
                hour INTEGER NOT NULL,
                minute INTEGER NOT NULL,
                repeatType TEXT NOT NULL,
                isEnabled INTEGER NOT NULL,
                nextTrigger INTEGER NOT NULL,
                isMain INTEGER NOT NULL
            )
        """.trimIndent()
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE CustomTasbeeh ADD COLUMN position INTEGER NOT NULL DEFAULT 0")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS Quarter (
                id TEXT NOT NULL PRIMARY KEY,
                sura TEXT NOT NULL,
                aya TEXT NOT NULL,
                pageNum TEXT NOT NULL,
                part TEXT NOT NULL,
                hizb TEXT NOT NULL
            )
        """.trimIndent()
        )
    }
}


// Migration from version 6 to version 7
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // to separate pageContent from the QuranPage table at this stage of the project's life we drop the table entirely and create it anew

        // Drop the existing QuranPage table if it exists
        database.execSQL("DROP TABLE IF EXISTS QuranPage")

        // Recreate the QuranPage table with the updated schema
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS QuranPage (
                pageNum TEXT NOT NULL PRIMARY KEY,
                ytLink TEXT NOT NULL,
                benefits TEXT NOT NULL,
                appliance TEXT NOT NULL,
                guidance TEXT NOT NULL
            )
        """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `PageContent` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `pageNum` TEXT NOT NULL,
            `type` TEXT NOT NULL,
            `surahName` TEXT,
            `surahNum` INTEGER NOT NULL,
            `surahTotal` INTEGER,
            `surahType` TEXT,
            `verseNum` INTEGER,
            `verseText` TEXT,
            `verseTextPlain` TEXT,
            FOREIGN KEY (`pageNum`) REFERENCES `QuranPage`(`pageNum`)
            )
            """.trimIndent()
        )
    }
}

// Migration from version 5 to version 6
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `VerseMeanings` (
                `id` TEXT PRIMARY KEY NOT NULL,
                `sura` INTEGER NOT NULL,
                `aya` TEXT NOT NULL,
                `text` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

// Migration from version 4 to version 5
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `TasabeehList` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `CustomTasbeeh` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `listId` INTEGER NOT NULL,
            `count` INTEGER NOT NULL,
            `text` TEXT NOT NULL,
            FOREIGN KEY (`listId`) REFERENCES `TasabeehList`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}

// Migration from version 3 to version 4
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `BookmarkEntity` (
                `pageNum` TEXT PRIMARY KEY NOT NULL,
                `timeStamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Khitmah` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `latestPage` TEXT NOT NULL,
                `isComplete` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `KhitmahMark` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `khitmahId` INTEGER NOT NULL,
            `timeStamp` INTEGER NOT NULL,
            `pageNum` TEXT NOT NULL,
            FOREIGN KEY (`khitmahId`) REFERENCES `Khitmah`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

    }
}


val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE PartData ADD COLUMN firstWords TEXT NOT NULL DEFAULT ''".trimIndent()
        )
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

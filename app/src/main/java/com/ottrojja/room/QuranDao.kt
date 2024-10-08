package com.ottrojja.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ottrojja.classes.QuranPage
import com.ottrojja.screens.azkarScreen.Azkar
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.quranScreen.E3rabData
import com.ottrojja.screens.quranScreen.TafseerData
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM QuranPage")
    fun getAllPages(): List<QuranPage>

    @Query("SELECT * FROM QuranPage WHERE pageNum=:pageNum")
    fun getPage(pageNum: String): QuranPage

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuranPage(quranPage: QuranPage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuranPages(quranPages: List<QuranPage>)

    @Query("SELECT count(*) FROM QuranPage")
    fun getPagesCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChapters(chapters: List<ChapterData>)

    @Query("SELECT * FROM ChapterData")
    fun getAllChapters(): List<ChapterData>

    @Query("SELECT * FROM ChapterData WHERE surahId=:surahNum")
    fun getChapter(surahNum: Int): ChapterData

    @Query("SELECT count(*) FROM ChapterData")
    fun getChaptersCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertParts(parts: List<PartData>)

    @Query("SELECT * FROM PartData")
    fun getAllParts(): List<PartData>

    @Query("SELECT count(*) FROM PartData")
    fun getPartsCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertE3rabData(e3rabData: List<E3rabData>)

    @Query("SELECT * FROM E3rabData")
    fun getAllE3rabData(): List<E3rabData>

    @Query("SELECT * FROM E3rabData WHERE sura=:surahNum AND aya=:verseNum")
    fun getVerseE3rabData(surahNum: String, verseNum: String): E3rabData

    @Query("SELECT count(*) FROM E3rabData")
    fun getE3rabsCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTafseerData(chapters: List<TafseerData>)

    @Query("SELECT * FROM TafseerData")
    fun getAllTafseerData(): List<TafseerData>

    @Query("SELECT * FROM TafseerData WHERE sura=:surahNum AND aya=:verseNum AND tafseer=:tafseer")
    fun getVerseTafseerData(surahNum: String, verseNum: String, tafseer: String): TafseerData

    @Query("SELECT count(*) FROM TafseerData")
    fun getTafseersCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllAzkar(azkar: List<Azkar>)


    @Query("SELECT * FROM Azkar")
    fun getAllAzkar(): List<Azkar>

    @Query("SELECT * FROM Azkar WHERE azkarTitle=:title")
    fun getAzkarByTitle(title: String): Azkar

    @Query("SELECT count(*) FROM Azkar")
    fun getAzkarCount(): Int



}
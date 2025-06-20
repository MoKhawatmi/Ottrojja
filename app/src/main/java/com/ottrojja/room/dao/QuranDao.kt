package com.ottrojja.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ottrojja.room.entities.CauseOfRevelation
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.room.entities.BookmarkEntity
import com.ottrojja.room.entities.Azkar
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.room.entities.E3rabData
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.VerseMeanings
import com.ottrojja.room.relations.QuranPageWithContent

@Dao
interface QuranDao {

    /****************************Pages*********************************/

    @Query("SELECT * FROM QuranPage")
    fun getAllPages(): List<QuranPage>

    @Query("SELECT * FROM QuranPage WHERE pageNum=:pageNum")
    fun getPage(pageNum: String): QuranPageWithContent

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuranPage(quranPage: QuranPage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuranPages(quranPages: List<QuranPage>)

    @Query("SELECT count(*) FROM QuranPage")
    fun getPagesCount(): Int

    /****************************Page Content*********************************/

    @Query("SELECT * FROM PageContent")
    fun getAllPagesContent(): List<PageContent>

    @Query(
        "SELECT * FROM PageContent WHERE verseText LIKE '%' || :query || '%' OR verseTextPlain LIKE '%' || :query || '%'"
    )
    fun searchPagesContent(query: String): List<PageContent>

    @Query("""
    SELECT * FROM PageContent
    WHERE (surahNum > :startingSurah OR (surahNum = :startingSurah AND verseNum >= :startingVerse))
      AND (surahNum < :endSurah OR (surahNum = :endSurah AND verseNum <= :endVerse)) AND type = "verse"
""")
    fun getPagesContentRange(startingSurah: Int, startingVerse: Int, endSurah: Int, endVerse: Int): List<PageContent>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPageContent(pageContent: PageContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPagesContent(pagesContent: List<PageContent>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChapters(chapters: List<ChapterData>)

    @Query("SELECT * FROM ChapterData")
    fun getAllChapters(): List<ChapterData>

    @Query("SELECT * FROM ChapterData WHERE surahId=:surahNum")
    fun getChapter(surahNum: Int): ChapterData

    @Query("SELECT count(*) FROM ChapterData")
    fun getChaptersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParts(parts: List<PartData>)

    @Query("SELECT * FROM PartData")
    fun getAllParts(): List<PartData>

    @Query("SELECT count(*) FROM PartData")
    fun getPartsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertE3rabData(e3rabData: List<E3rabData>)

    @Query("SELECT * FROM E3rabData")
    fun getAllE3rabData(): List<E3rabData>

    @Query("SELECT * FROM E3rabData WHERE sura=:surahNum AND aya=:verseNum")
    fun getVerseE3rabData(surahNum: String, verseNum: String): E3rabData

    @Query("SELECT count(*) FROM E3rabData")
    fun getE3rabsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTafseerData(tafseers: List<TafseerData>)

    @Query("SELECT * FROM TafseerData")
    fun getAllTafseerData(): List<TafseerData>

    @Query("SELECT * FROM TafseerData WHERE sura=:surahNum AND aya=:verseNum AND tafseer=:tafseer")
    fun getVerseTafseerData(surahNum: String, verseNum: String, tafseer: String): TafseerData

    @Query("SELECT count(*) FROM TafseerData")
    fun getTafseersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAzkar(azkar: List<Azkar>)


    @Query("SELECT * FROM Azkar")
    fun getAllAzkar(): List<Azkar>

    @Query("SELECT * FROM Azkar WHERE azkarTitle=:title")
    fun getAzkarByTitle(title: String): Azkar

    @Query("SELECT count(*) FROM Azkar")
    fun getAzkarCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCausesOfRevelationData(causesOfRevelation: List<CauseOfRevelation>)

    @Query("SELECT * FROM CauseOfRevelation")
    fun getAllCausesOfRevelationData(): List<CauseOfRevelation>

    @Query(
        "SELECT * FROM CauseOfRevelation WHERE sura=:surahNum AND (verses LIKE '%' || :verseNum || ',%' OR verses LIKE '%,' || :verseNum || '%' OR verses = :verseNum)"
    )
    fun getCauseOfRevelationData(surahNum: String, verseNum: String): List<CauseOfRevelation>

    @Query("SELECT count(*) FROM CauseOfRevelation")
    fun getCausesOfRevelationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM BookmarkEntity")
    fun getBookmarks(): List<BookmarkEntity>

    @Query("SELECT EXISTS (SELECT 1 FROM BookmarkEntity WHERE pageNum=:pageNum)")
    fun isBookmarked(pageNum: String): Boolean

    @Delete
    fun deleteBookmark(bookmark: BookmarkEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVerseMeanings(verseMeanings: List<VerseMeanings>)

    @Query("SELECT * FROM VerseMeanings")
    fun getAllVerseMeanings(): List<VerseMeanings>

    @Query("SELECT * FROM VerseMeanings WHERE sura=:surahNum AND aya=:verseNum")
    fun getSingleVerseMeanings(surahNum: String, verseNum: String): VerseMeanings?

    @Query("SELECT count(*) FROM VerseMeanings")
    fun getVerseMeaningsCount(): Int


}
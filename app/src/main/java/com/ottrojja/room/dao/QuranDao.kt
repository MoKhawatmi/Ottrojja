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
import com.ottrojja.room.entities.Quarter
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.VerseMeanings
import com.ottrojja.room.relations.PartWithQuarters
import com.ottrojja.room.relations.QuranPageWithContent

@Dao
interface QuranDao {

    /****************************Pages*********************************/

    @Query("SELECT * FROM QuranPage")
    suspend fun getAllPages(): List<QuranPage>

    @Query("SELECT * FROM QuranPage WHERE pageNum=:pageNum")
    suspend fun getPage(pageNum: String): QuranPageWithContent

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranPage(quranPage: QuranPage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranPages(quranPages: List<QuranPage>)

    @Query("SELECT count(*) FROM QuranPage")
    suspend fun getPagesCount(): Int

    /****************************Page Content*********************************/

    @Query("SELECT * FROM PageContent")
    suspend fun getAllPagesContent(): List<PageContent>

    @Query(
        "SELECT * FROM PageContent WHERE verseText LIKE '%' || :query || '%' OR verseTextPlain LIKE '%' || :query || '%'"
    )
    suspend fun searchPagesContent(query: String): List<PageContent>

    @Query("""
    SELECT * FROM PageContent
    WHERE (surahNum > :startingSurah OR (surahNum = :startingSurah AND verseNum >= :startingVerse))
      AND (surahNum < :endSurah OR (surahNum = :endSurah AND verseNum <= :endVerse)) AND type = 'verse'
"""
    )
    suspend fun getPagesContentRange(startingSurah: Int, startingVerse: Int, endSurah: Int, endVerse: Int): List<PageContent>


    @Query("SELECT * FROM PageContent WHERE pageNum = :pageNum")
    suspend fun fetchPageVerses(pageNum: String): List<PageContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageContent(pageContent: PageContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPagesContent(pagesContent: List<PageContent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterData>)

    @Query("SELECT * FROM ChapterData")
    suspend fun getAllChapters(): List<ChapterData>

    @Query("SELECT * FROM ChapterData WHERE surahId=:surahNum")
    suspend fun getChapter(surahNum: Int): ChapterData

    @Query("SELECT count(*) FROM ChapterData")
    suspend fun getChaptersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParts(parts: List<PartData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuarters(quarters: List<Quarter>)

    @Query("SELECT * FROM PartData")
    suspend fun getAllParts(): List<PartData>

    @Query("SELECT * FROM PartData")
    suspend fun getAllPartsWithQuarters(): List<PartWithQuarters>

    @Query("SELECT count(*) FROM PartData")
    suspend fun getPartsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertE3rabData(e3rabData: List<E3rabData>)

    @Query("SELECT * FROM E3rabData")
    suspend fun getAllE3rabData(): List<E3rabData>

    @Query("SELECT * FROM E3rabData WHERE sura=:surahNum AND aya=:verseNum")
    suspend fun getVerseE3rabData(surahNum: String, verseNum: String): E3rabData

    @Query("SELECT count(*) FROM E3rabData")
    suspend fun getE3rabsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafseerData(tafseers: List<TafseerData>)

    @Query("SELECT * FROM TafseerData")
    suspend fun getAllTafseerData(): List<TafseerData>

    @Query("SELECT * FROM TafseerData WHERE sura=:surahNum AND aya=:verseNum AND tafseer=:tafseer")
    suspend fun getVerseTafseerData(surahNum: String,
                                    verseNum: String,
                                    tafseer: String): TafseerData

    @Query("SELECT count(*) FROM TafseerData")
    suspend fun getTafseersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAzkar(azkar: List<Azkar>)


    @Query("SELECT * FROM Azkar")
    suspend fun getAllAzkar(): List<Azkar>

    @Query("SELECT * FROM Azkar WHERE azkarTitle=:title")
    suspend fun getAzkarByTitle(title: String): Azkar

    @Query("SELECT count(*) FROM Azkar")
    suspend fun getAzkarCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCausesOfRevelationData(causesOfRevelation: List<CauseOfRevelation>)

    @Query("SELECT * FROM CauseOfRevelation")
    suspend fun getAllCausesOfRevelationData(): List<CauseOfRevelation>

    @Query(
        "SELECT * FROM CauseOfRevelation WHERE sura=:surahNum AND (verses LIKE '%' || :verseNum || ',%' OR verses LIKE '%,' || :verseNum || '%' OR verses = :verseNum)"
    )
    suspend fun getCauseOfRevelationData(surahNum: String,
                                         verseNum: String): List<CauseOfRevelation>

    @Query("SELECT count(*) FROM CauseOfRevelation")
    suspend fun getCausesOfRevelationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM BookmarkEntity")
    suspend fun getBookmarks(): List<BookmarkEntity>

    @Query("SELECT EXISTS (SELECT 1 FROM BookmarkEntity WHERE pageNum=:pageNum)")
    suspend fun isBookmarked(pageNum: String): Boolean

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerseMeanings(verseMeanings: List<VerseMeanings>)

    @Query("SELECT * FROM VerseMeanings")
    suspend fun getAllVerseMeanings(): List<VerseMeanings>

    @Query("SELECT * FROM VerseMeanings WHERE sura=:surahNum AND aya=:verseNum")
    suspend fun getSingleVerseMeanings(surahNum: String, verseNum: String): VerseMeanings?

    @Query("SELECT count(*) FROM VerseMeanings")
    suspend fun getVerseMeaningsCount(): Int


}
package com.ottrojja.classes

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ottrojja.room.entities.BookmarkEntity
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.dao.KhitmahDao
import com.ottrojja.room.entities.KhitmahMark
import com.ottrojja.room.relations.KhitmahWithMarks
import com.ottrojja.room.dao.QuranDao
import com.ottrojja.room.dao.TasabeehDao
import com.ottrojja.room.entities.CauseOfRevelation
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList
import com.ottrojja.room.relations.ListWithTasabeeh
import com.ottrojja.room.entities.Azkar
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.room.entities.E3rabData
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.Quarter
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.VerseMeanings
import com.ottrojja.room.relations.PartWithQuarters
import com.ottrojja.room.relations.QuranPageWithContent
import kotlinx.coroutines.flow.Flow

class QuranRepository(private val quranDao: QuranDao,
                      private val khitmahDao: KhitmahDao,
                      private val tasabeehDao: TasabeehDao) {


    /****************************Pages*********************************/

    suspend fun getAllPages(): List<QuranPage> {
        return quranDao.getAllPages()
    }

    suspend fun getPage(pageNum: String): QuranPageWithContent {
        return quranDao.getPage(pageNum)
    }

    suspend fun insertPage(quranPage: QuranPage) {
        quranDao.insertQuranPage(quranPage)
    }

    suspend fun insertAllPages(quranPages: List<QuranPage>) {
        quranDao.insertQuranPages(quranPages)
    }

    suspend fun getPagesCount(): Int {
        return quranDao.getPagesCount()
    }

    /****************************Page Content*********************************/


    suspend fun getAllPagesContent(): List<PageContent> {
        return quranDao.getAllPagesContent()
    }

    suspend fun searchPagesContent(query: String): List<PageContent> {
        return quranDao.searchPagesContent(query)
    }

    suspend fun getPagesContentRange(startingSurah: Int,
                                     startingVerse: Int,
                                     endSurah: Int,
                                     endVerse: Int): List<PageContent> {
        return quranDao.getPagesContentRange(startingSurah, startingVerse, endSurah, endVerse);
    }


    suspend fun fetchPageVerses(pageNum: String): List<PageContent> {
        return quranDao.fetchPageVerses(pageNum)
    }


    suspend fun insertPageContent(pageContent: PageContent) {
        quranDao.insertPageContent(pageContent)
    }

    suspend fun insertPagesContent(pagesContent: List<PageContent>) {
        quranDao.insertPagesContent(pagesContent)
    }

    /****************************Chapters*********************************/


    suspend fun insertAllChapters(chapters: List<ChapterData>) {
        quranDao.insertChapters(chapters)
    }

    suspend fun getAllChapters(): List<ChapterData> {
        return quranDao.getAllChapters()
    }

    suspend fun getChapter(surahNum: Int): ChapterData {
        return quranDao.getChapter(surahNum)
    }

    suspend fun getChaptersCount(): Int {
        return quranDao.getChaptersCount()
    }


    suspend fun insertAllParts(parts: List<PartData>) {
        quranDao.insertParts(parts)
    }

    suspend fun insertQuarters(quarters: List<Quarter>) {
        quranDao.insertQuarters(quarters)
    }

    suspend fun getAllParts(): List<PartData> {
        return quranDao.getAllParts()
    }

    suspend fun getAllPartsWithQuarters(): List<PartWithQuarters> {
        return quranDao.getAllPartsWithQuarters();
    }

    suspend fun getPartsCount(): Int {
        return quranDao.getPartsCount()
    }


    suspend fun insertAllE3rabData(e3rabData: List<E3rabData>) {
        quranDao.insertE3rabData(e3rabData)
    }

    suspend fun getAllE3rabData(): List<E3rabData> {
        return quranDao.getAllE3rabData()
    }

    suspend fun getE3rabsCount(): Int {
        return quranDao.getE3rabsCount()
    }

    suspend fun getVerseE3rabData(surahNum: String, verseNum: String): E3rabData {
        return quranDao.getVerseE3rabData(surahNum, verseNum)
    }

    suspend fun insertAllTafseerData(tafseerData: List<TafseerData>) {
        quranDao.insertTafseerData(tafseerData)
    }

    suspend fun getAllTafseerData(): List<TafseerData> {
        return quranDao.getAllTafseerData()
    }

    suspend fun getVerseTafseerData(
        surahNum: String,
        verseNum: String,
        tafseer: String
    ): TafseerData {
        return quranDao.getVerseTafseerData(surahNum, verseNum, tafseer)
    }

    suspend fun getTafseersCount(): Int {
        return quranDao.getTafseersCount()
    }

    suspend fun insertAllAzkar(azkar: List<Azkar>) {
        return quranDao.insertAllAzkar(azkar)
    }

    suspend fun getAllAzkar(): List<Azkar> {
        return quranDao.getAllAzkar()
    }

    suspend fun getAzkarByTitle(title: String): Azkar {
        return quranDao.getAzkarByTitle(title)
    }

    suspend fun getAzkarCount(): Int {
        return quranDao.getAzkarCount()
    }

    suspend fun insertAllCausesOfRevelation(CausesOfRevelation: List<CauseOfRevelation>) {
        return quranDao.insertCausesOfRevelationData(CausesOfRevelation)
    }

    suspend fun getAllCausesOfRevelation(): List<CauseOfRevelation> {
        return quranDao.getAllCausesOfRevelationData()
    }

    suspend fun getCauseOfRevelation(surahNum: String, verseNum: String): List<CauseOfRevelation> {
        return quranDao.getCauseOfRevelationData(surahNum, verseNum)
    }

    suspend fun getCauseOfRevelationCount(): Int {
        return quranDao.getCausesOfRevelationCount()
    }

    /*******************BOOKMARKS MANAGEMENT*************************/


    suspend fun insertBookmark(bookmark: BookmarkEntity) {
        quranDao.insertBookmark(bookmark)
    }

    suspend fun getBookmarks(): List<BookmarkEntity> {
        return quranDao.getBookmarks()
    }

    suspend fun isBookmarked(pageNum: String): Boolean {
        return quranDao.isBookmarked(pageNum)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) {
        quranDao.deleteBookmark(bookmark)
    }

    /*******************KHITMAH MANAGEMENT*************************/

    suspend fun insertKhitmah(khitmah: Khitmah): Long {
        return khitmahDao.insertKhitmah(khitmah)
    }

    suspend fun insertKhitmahMark(khitmahMark: KhitmahMark) {
        khitmahDao.insertKhitmahMark(khitmahMark)
    }

    suspend fun getAllKhitmah(): Flow<List<Khitmah>> {
        return khitmahDao.getAllKhitmah()
    }

    suspend fun getKhitmah(id: Int): KhitmahWithMarks {
        return khitmahDao.getKhitmah(id)
    }

    suspend fun deleteKhitmah(khitmah: Khitmah) {
        khitmahDao.deleteKhitmah(khitmah)
    }

    suspend fun deleteKhitmahMark(khitmahMark: KhitmahMark) {
        khitmahDao.deleteKhitmahMark(khitmahMark)
    }

    suspend fun updateKhitmah(khitmah: Khitmah) {
        khitmahDao.updateKhitmah(khitmah)
    }

    suspend fun deleteKhitmahMarkById(id: Int) {
        khitmahDao.deleteKhitmahMarkById(id)
    }


    /*******************TASABEEH MANAGEMENT*************************/

    suspend fun insertTasabeehList(tasabeehList: TasabeehList): Long {
        return tasabeehDao.insertTasabeehList(tasabeehList)
    }

    suspend fun insertCustomTasbeeh(customTasbeeh: CustomTasbeeh) {
        tasabeehDao.insertCustomTasbeeh(customTasbeeh)
    }

    suspend fun getTasabeehLists(): Flow<List<TasabeehList>> {
        return tasabeehDao.getTasabeehLists()
    }

    suspend fun getTasabeehList(id: Int): Flow<ListWithTasabeeh?> {
        return tasabeehDao.getTasabeehList(id)
    }

    suspend fun deleteTasabeehList(tasabeehList: TasabeehList) {
        tasabeehDao.deleteTasabeehList(tasabeehList)
    }

    suspend fun deleteCustomTasabeeh(customTasbeeh: CustomTasbeeh) {
        tasabeehDao.deleteCustomTasabeeh(customTasbeeh)
    }

    suspend fun updateTasabeehList(tasabeehList: TasabeehList) {
        tasabeehDao.updateTasabeehList(tasabeehList)
    }

    suspend fun massUpdateCustomTasabeeh(items: List<CustomTasbeeh>) {
        tasabeehDao.massUpdateCustomTasabeeh(items)
    }

    suspend fun getMaxPosition(listId: Int): Int {
        return tasabeehDao.getMaxPosition(listId)
    }


    /*******************VERSE MEANINGS MANAGEMENT*************************/


    suspend fun insertVerseMeanings(verseMeanings: List<VerseMeanings>) {
        quranDao.insertVerseMeanings(verseMeanings)
    }

    suspend fun getAllVerseMeanings(): List<VerseMeanings> {
        return quranDao.getAllVerseMeanings()
    }

    suspend fun getSingleVerseMeanings(surahNum: String, verseNum: String): VerseMeanings? {
        return quranDao.getSingleVerseMeanings(surahNum = surahNum, verseNum = verseNum)
    }

    suspend fun getVerseMeaningsCount(): Int {
        return quranDao.getVerseMeaningsCount()
    }


}

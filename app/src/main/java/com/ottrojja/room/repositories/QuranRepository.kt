package com.ottrojja.room.repositories

import androidx.room.Transaction
import com.ottrojja.classes.QuranRangeException
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.dao.KhitmahDao
import com.ottrojja.room.dao.QuranDao
import com.ottrojja.room.dao.TasabeehDao
import com.ottrojja.room.entities.Azkar
import com.ottrojja.room.entities.BookmarkEntity
import com.ottrojja.room.entities.CauseOfRevelation
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.E3rabData
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.room.entities.Quarter
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.TasabeehList
import com.ottrojja.room.entities.VerseMeanings
import com.ottrojja.room.relations.KhitmahWithSession
import com.ottrojja.room.relations.ListWithTasabeeh
import com.ottrojja.room.relations.PartWithQuarters
import com.ottrojja.room.relations.QuranPageWithContent
import com.ottrojja.screens.Khitmah.classes.KhitmahListItem
import com.ottrojja.screens.Khitmah.classes.SurahVerseRange
import com.ottrojja.screens.mainScreen.PartData
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

    suspend fun getRandomPageWithBenefits(): QuranPage {
        return quranDao.getRandomPageWithBenefits()
    }

    /****************************Page Content*********************************/


    suspend fun getAllPagesContent(): List<PageContent> {
        return quranDao.getAllPagesContent()
    }

    suspend fun searchPagesContent(query: String): List<PageContent> {
        return quranDao.searchPagesContent(query)
    }

    suspend fun getPageContent(surahNum: Int, verseNum: Int): PageContent {
        return quranDao.getPageContent(surahNum, verseNum)
    }

    suspend fun getPageContentById(verseId: Int): PageContent {
        return quranDao.getPageContentById(verseId)
    }


    suspend fun getPagesContentRange(
        startingSurah: Int,
        startingVerse: Int,
        endSurah: Int,
        endVerse: Int): List<PageContent> {
        return quranDao.getPagesContentRange(startingSurah, startingVerse, endSurah, endVerse);
    }

    suspend fun getChapterVerses(surahNum: Int): List<PageContent> {
        return quranDao.getChapterVerses(surahNum)
    }

    suspend fun getSpecificVerses(mutashabihat: List<String>): List<PageContent> {
        return quranDao.getSpecificVerses(mutashabihat = mutashabihat)
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

    suspend fun getQuranRangeFromPages(
        pages: Set<Int>
    ): QuranRangeState {
        require(pages.isNotEmpty()) {
            "Cannot create Quran range from empty pages"
        }

        val firstPageNumber = pages.minOrNull()!!
        val lastPageNumber = pages.maxOrNull()!!

        val firstPage = fetchPageVerses("$firstPageNumber")
            .filter { it.type == PageContentItemType.verse }

        if (firstPage.isEmpty()) {
            throw QuranRangeException(
                "No verses found for page $firstPageNumber"
            )
        }

        val lastPage = fetchPageVerses("$lastPageNumber")
            .filter { it.type == PageContentItemType.verse }

        if (lastPage.isEmpty()) {
            throw QuranRangeException(
                "No verses found for page $lastPageNumber"
            )
        }

        val startVerse = firstPage.first()
        val endVerse = lastPage.last()

        val startSurah = getChapter(startVerse.surahNum)
            ?: throw QuranRangeException(
                "Surah ${startVerse.surahNum} not found"
            )

        val endSurah = getChapter(endVerse.surahNum)
            ?: throw QuranRangeException(
                "Surah ${endVerse.surahNum} not found"
            )

        return QuranRangeState(
            startSurah = startSurah,
            startVerse = startVerse,
            endSurah = endSurah,
            endVerse = endVerse
        )
    }

    suspend fun getQuranRangeFromDetails(
        startSurahId: Int,
        startVerseId: Int,
        endSurahId: Int,
        endVerseId: Int
    ): QuranRangeState {
        val startSurah = getChapter(startSurahId)
            ?: throw QuranRangeException("Surah $startSurahId not found")

        val endSurah = getChapter(endSurahId)
            ?: throw QuranRangeException("Surah $endSurahId not found")

        val startVerse = getPageContentById(startVerseId)
            ?: throw QuranRangeException("Verse $startVerseId not found")

        val endVerse = getPageContentById(endVerseId)
            ?: throw QuranRangeException("Verse $endVerseId not found")

        return QuranRangeState(
            startSurah = startSurah,
            startVerse = startVerse,
            endSurah = endSurah,
            endVerse = endVerse
        )
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

    suspend fun getSurahVerseRanges(): List<SurahVerseRange> {
        return quranDao.getSurahVerseRanges()
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

    suspend fun getAzkarById(id: Long): Azkar? {
        return quranDao.getAzkarById(id)
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

    /******************* KHITMAH MANAGEMENT *************************/

    suspend fun insertKhitmah(khitmah: Khitmah): Long {
        return khitmahDao.insertKhitmah(khitmah)
    }

    suspend fun updateKhitmah(khitmah: Khitmah) {
        khitmahDao.updateKhitmah(khitmah)
    }

    suspend fun deleteKhitmah(khitmah: Khitmah) {
        khitmahDao.deleteKhitmah(khitmah)
    }

    fun getAllKhitmah(): Flow<List<Khitmah>> {
        return khitmahDao.getAllKhitmah()
    }

    fun getKhitmah(id: Long): Flow<KhitmahWithSession?> {
        return khitmahDao.getKhitmah(id)
    }

    fun getKhitmahList(): Flow<List<KhitmahListItem>> {
        return khitmahDao.getKhitmahList()
    }

    /******************* KHITMAH SESSION MANAGEMENT *************************/

    suspend fun insertSession(session: KhitmahSession): Long {
        return khitmahDao.insertSessionAndUpdateProgress(session)
    }

    suspend fun updateSession(session: KhitmahSession) {
        khitmahDao.updateSessionAndProgress(session)
    }

    suspend fun deleteSession(session: KhitmahSession) {
        khitmahDao.deleteSessionAndUpdateProgress(session)
    }

    suspend fun deleteSessionById(id: Long) {
        khitmahDao.deleteSessionById(id)
    }

    fun getSessions(khitmahId: Long): Flow<List<KhitmahSession>> {
        return khitmahDao.getSessions(khitmahId)
    }

    suspend fun getLatestSession(khitmahId: Long): KhitmahSession? {
        return khitmahDao.getLatestSession(khitmahId)
    }

    fun getSessionCount(khitmahId: Long): Flow<Int> {
        return khitmahDao.getSessionCount(khitmahId)
    }


    /*******************TASABEEH MANAGEMENT*************************/

    suspend fun insertTasabeehList(tasabeehList: TasabeehList): Long {
        return tasabeehDao.insertTasabeehList(tasabeehList)
    }

    suspend fun insertCustomTasbeeh(customTasbeeh: CustomTasbeeh) {
        tasabeehDao.insertCustomTasbeeh(customTasbeeh)
    }

    suspend fun insertMultCustomTasbeeh(customTasbeehList: List<CustomTasbeeh>) {
        tasabeehDao.insertMultCustomTasbeeh(customTasbeehList)
    }

    suspend fun getTasabeehLists(): Flow<List<TasabeehList>> {
        return tasabeehDao.getTasabeehLists()
    }

    suspend fun getTasabeehList(id: Int): ListWithTasabeeh? {
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

    suspend fun insertListWithTasabeeh(
        title: String,
        tasabeeh: List<CustomTasbeeh>
    ) {
        tasabeehDao.insertListWithTasabeeh(title, tasabeeh)
    }


    suspend fun updateTasbeehCurrentCount(id: Int, currentCount: Int) {
        return tasabeehDao.updateTasbeehCurrentCount(id, currentCount)
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
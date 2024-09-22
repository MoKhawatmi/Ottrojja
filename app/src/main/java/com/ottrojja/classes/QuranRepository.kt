package com.ottrojja.classes

import com.ottrojja.room.QuranDao
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.quranScreen.E3rabData
import com.ottrojja.screens.quranScreen.TafseerData

class QuranRepository(private val quranDao: QuranDao) {
    // Exposing the Flow directly from the DAO
    //   val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun getAllPages(): List<QuranPage> {
        return quranDao.getAllPages()
    }

    suspend fun getPage(pageNum: String): QuranPage {
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

    suspend fun getAllParts(): List<PartData> {
        return quranDao.getAllParts()
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
}

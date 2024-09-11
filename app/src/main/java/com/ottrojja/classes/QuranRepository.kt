package com.ottrojja.classes

import com.ottrojja.room.QuranDao

class QuranRepository (private val quranDao: QuranDao) {
    // Exposing the Flow directly from the DAO
 //   val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun insert(quranPage: QuranPage) {
        quranDao.insertQuranPage(quranPage)
    }

    suspend fun insertAll(quranPages: List<QuranPage>) {
        quranDao.insertQuranPages(quranPages)
    }

}

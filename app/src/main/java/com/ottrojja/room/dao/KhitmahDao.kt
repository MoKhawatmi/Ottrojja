package com.ottrojja.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ottrojja.constants.QuranConstants
import com.ottrojja.constants.QuranConstants.SURAH_HEADER_IDS
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.relations.KhitmahWithSession
import com.ottrojja.screens.Khitmah.classes.KhitmahListItem
import kotlinx.coroutines.flow.Flow


@Dao
interface KhitmahDao {

    // ─────────────────────────────────────
    // Khitmah
    // ─────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhitmah(khitmah: Khitmah): Long

    @Update
    suspend fun updateKhitmah(khitmah: Khitmah)

    @Delete
    suspend fun deleteKhitmah(khitmah: Khitmah)

    @Query("SELECT * FROM Khitmah ORDER BY id DESC")
    fun getAllKhitmah(): Flow<List<Khitmah>>

    @Transaction
    @Query("SELECT * FROM khitmah WHERE id = :id")
    fun getKhitmah(id: Long): Flow<KhitmahWithSession?>

    @Query("""
    SELECT
        k.*,

        (
            SELECT s.endPage
            FROM KhitmahSession s
            WHERE s.khitmahId = k.id
            ORDER BY s.timestamp DESC, s.id DESC
            LIMIT 1
        ) AS latestEndPage,

        (
            SELECT s.endVerseId
            FROM KhitmahSession s
            WHERE s.khitmahId = k.id
            ORDER BY s.timestamp DESC, s.id DESC
            LIMIT 1
        ) AS latestEndVerseId,

        (
            SELECT s.timestamp
            FROM KhitmahSession s
            WHERE s.khitmahId = k.id
            ORDER BY s.timestamp DESC, s.id DESC
            LIMIT 1
        ) AS latestReadingTimestamp,

        (
            SELECT COUNT(*)
            FROM KhitmahSession s
            WHERE s.khitmahId = k.id
        ) AS sessionCount

    FROM Khitmah k
    ORDER BY k.id DESC
"""
    )
    fun getKhitmahList(): Flow<List<KhitmahListItem>>

    @Query("""
    UPDATE Khitmah SET pagesRead = :pagesRead, versesCovered = :versesCovered WHERE id = :khitmahId
"""
    )
    suspend fun updateProgress(
        khitmahId: Long,
        pagesRead: Int,
        versesCovered: Int
    )

    // ─────────────────────────────────────
    // KhitmahSession
    // ─────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: KhitmahSession): Long

    @Update
    suspend fun updateSession(session: KhitmahSession)

    @Delete
    suspend fun deleteSession(session: KhitmahSession)

    @Query("DELETE FROM KhitmahSession WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("""
        SELECT * FROM KhitmahSession
        WHERE khitmahId = :khitmahId
        ORDER BY readingDate ASC
    """
    )
    fun getSessions(khitmahId: Int): Flow<List<KhitmahSession>>

    @Query("""
        SELECT * FROM KhitmahSession
        WHERE khitmahId = :khitmahId
        ORDER BY readingDate ASC
    """
    )
    fun getSessionsForKhitmah(khitmahId: Long): List<KhitmahSession>


    @Query("""
    SELECT * FROM KhitmahSession
    WHERE khitmahId = :khitmahId
    ORDER BY readingDate DESC, timestamp DESC
    LIMIT 1
"""
    )
    suspend fun getLatestSession(khitmahId: Int): KhitmahSession?

    @Query("""
    SELECT COUNT(*) FROM KhitmahSession
    WHERE khitmahId = :khitmahId
"""
    )
    fun getSessionCount(khitmahId: Int): Flow<Int>

// ─────────────────────────────────────
// Transactions
// ─────────────────────────────────────

    @Transaction
    suspend fun insertSessionAndUpdateProgress(
        session: KhitmahSession
    ): Long {
        val id = insertSession(session)

        recalculateProgressInternal(session.khitmahId)

        return id
    }

    @Transaction
    suspend fun updateSessionAndProgress(
        session: KhitmahSession
    ) {
        updateSession(session)

        recalculateProgressInternal(session.khitmahId)
    }

    @Transaction
    suspend fun deleteSessionAndUpdateProgress(
        session: KhitmahSession
    ) {
        deleteSession(session)

        recalculateProgressInternal(session.khitmahId)
    }

    // ─────────────────────────────────────
// Helpers
// ─────────────────────────────────────
    private fun calculateCoveredVerseCount(
        sessions: List<KhitmahSession>
    ): Int {
        return sessions
            .asSequence()
            .flatMap { (it.startVerseId..it.endVerseId).asSequence() }
            .distinct()
            .count { it !in SURAH_HEADER_IDS }
    }


    private suspend fun recalculateProgressInternal(
        khitmahId: Long
    ) {
        val sessions = getSessionsForKhitmah(khitmahId)

        val pagesRead = sessions
            .flatMap { it.startPage..it.endPage }
            .toSet()
            .size

        val versesCovered = calculateCoveredVerseCount(sessions)

        /*val versesCovered = sessions
            .flatMap { it.startVerseId..it.endVerseId }
            .toSet()
            .size*/

        updateProgress(
            khitmahId = khitmahId,
            pagesRead = pagesRead,
            versesCovered = versesCovered
        )
    }

}

package com.ottrojja.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.relations.KhitmahWithSession
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

    @Query("SELECT * FROM Khitmah WHERE id = :id")
    suspend fun getKhitmah(id: Int): KhitmahWithSession?


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
    """)
    fun getSessions(khitmahId: Int): Flow<List<KhitmahSession>>

    @Query("""
    SELECT * FROM KhitmahSession
    WHERE khitmahId = :khitmahId
    ORDER BY readingDate DESC, timestamp DESC
    LIMIT 1
""")
    suspend fun getLatestSession(khitmahId: Int): KhitmahSession?

    @Query("""
    SELECT COUNT(*) FROM KhitmahSession
    WHERE khitmahId = :khitmahId
""")
    fun getSessionCount(khitmahId: Int): Flow<Int>
}

/*@Dao
interface KhitmahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKhitmah(khitmah: Khitmah): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKhitmahMark(khitmahMark: KhitmahMark)

    @Query("SELECT * FROM Khitmah")
    fun getAllKhitmah(): Flow<List<Khitmah>>

    @Query("SELECT * FROM Khitmah WHERE id=:id")
    fun getKhitmah(id: Int): KhitmahWithMarks

    @Delete
    fun deleteKhitmah(khitmah: Khitmah)

    @Delete
    fun deleteKhitmahMark(khitmahMark: KhitmahMark)

    @Update
    fun updateKhitmah(khitmah: Khitmah)

    @Query("DELETE FROM KhitmahMark WHERE id = :id")
    fun deleteKhitmahMarkById(id: Int)
}*/
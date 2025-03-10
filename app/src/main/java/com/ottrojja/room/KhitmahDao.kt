package com.ottrojja.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
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


}
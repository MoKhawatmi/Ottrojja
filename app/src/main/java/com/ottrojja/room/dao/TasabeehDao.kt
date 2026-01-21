package com.ottrojja.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList
import com.ottrojja.room.relations.ListWithTasabeeh
import kotlinx.coroutines.flow.Flow

@Dao
interface TasabeehDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasabeehList(tasabeehList: TasabeehList): Long

    @Upsert
    fun insertCustomTasbeeh(customTasbeeh: CustomTasbeeh)

    @Insert
    fun insertMultCustomTasbeeh(customTasbeehList: List<CustomTasbeeh>)

    @Query("SELECT * FROM TasabeehList")
    fun getTasabeehLists(): Flow<List<TasabeehList>>

    @Query("SELECT * FROM TasabeehList WHERE id=:id")
    fun getTasabeehList(id: Int): Flow<ListWithTasabeeh?>

    @Delete
    fun deleteTasabeehList(tasabeehList: TasabeehList)

    @Delete
    suspend fun deleteCustomTasabeeh(customTasbeeh: CustomTasbeeh)

    @Update
    fun updateTasabeehList(tasabeehList: TasabeehList)

    @Update
    suspend fun massUpdateCustomTasabeeh(items: List<CustomTasbeeh>)

    @Query("SELECT COALESCE(MAX(position), 0) FROM CustomTasbeeh WHERE listId = :listId")
    suspend fun getMaxPosition(listId: Int): Int

}
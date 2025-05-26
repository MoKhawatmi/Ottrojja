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

    @Query("SELECT * FROM TasabeehList")
    fun getTasabeehLists(): Flow<List<TasabeehList>>

    @Query("SELECT * FROM TasabeehList WHERE id=:id")
    fun getTasabeehList(id: Int): Flow<ListWithTasabeeh?>

    @Delete
    fun deleteTasabeehList(tasabeehList: TasabeehList)

    @Delete
    fun deleteCustomTasabeeh(customTasbeeh: CustomTasbeeh)

    @Update
    fun updateTasabeehList(tasabeehList: TasabeehList)
}
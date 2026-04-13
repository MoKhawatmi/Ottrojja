package com.ottrojja.classes.DataStore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreRepository {
    private val datastore get() = DataStoreHolder.dataStore

    private val FLOATING_AZKAR = booleanPreferencesKey("floating_azkar")
    private val USER_TASABEEH_COUNT = intPreferencesKey("tasbeeh_count")

    /*******************FLOATING AZKAR*********************/
    val floatingAzkarFlow: Flow<Boolean> =
        datastore.data.map { it[FLOATING_AZKAR] ?: false }

    suspend fun setFloatingAzkar(enabled: Boolean) {
        datastore.edit { it[FLOATING_AZKAR] = enabled }
    }

    /*******************TASBEEH CONTROL*********************/
    val userTasabeehCountFlow: Flow<Int> = datastore.data.map {
        it[USER_TASABEEH_COUNT] ?: 0
    }

    suspend fun incrementUserTasabeehCount() {
        datastore.edit {
            it[USER_TASABEEH_COUNT] = (it[USER_TASABEEH_COUNT] ?: 0) + 1
        }
    }

    suspend fun resetUserTasabeehCount() {
        datastore.edit {
            it[USER_TASABEEH_COUNT] = 0
        }
    }


}
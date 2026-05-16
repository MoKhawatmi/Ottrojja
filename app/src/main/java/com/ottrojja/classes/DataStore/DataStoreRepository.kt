package com.ottrojja.classes.DataStore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ottrojja.composables.adviceDisplay.Advice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

object DataStoreRepository {
    private val datastore get() = DataStoreHolder.dataStore

    val json = Json {
        ignoreUnknownKeys = true
    }

    private val FLOATING_AZKAR = booleanPreferencesKey("floating_azkar")
    private val USER_TASABEEH_COUNT = intPreferencesKey("tasbeeh_count")
    private val MAIN_SCREEN_ADVICE = stringPreferencesKey("main_screen_advice")

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

    /*******************ADVICE*********************/

    val defaultAdvice = Advice(
        id = 0,
        createdAt = "",
        text = "{كُنْ في الدُّنيا كأنَّك غريبٌ أو كعابرِ سبيلٍ}",
        details = null
    )

    val mainScreenAdviceFlow: Flow<Advice> =
        datastore.data.map { prefs ->
            runCatching {
                prefs[MAIN_SCREEN_ADVICE]
                    ?.takeIf { it.isNotBlank() }
                    ?.let { json.decodeFromString<Advice>(it) }
            }.getOrNull() ?: defaultAdvice
        }

    suspend fun setMainScreenAdvice(value: Advice) {
        datastore.edit { it[MAIN_SCREEN_ADVICE] = json.encodeToString(value) }
    }


}
package com.ottrojja.classes.DataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

object DataStoreHolder {
    lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        if (!::dataStore.isInitialized) {
            dataStore = PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("ottrojja")
                }
            )
        }
    }

}
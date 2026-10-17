package com.ottrojja.screens.prayerTimesScreen.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Prayer times settings, in their own DataStore file ("prayer_times").
 *
 * Takes the DataStore as a constructor parameter so tests can pass one backed by a
 * temporary file. In the app, use the single instance from PrayerTimesModule.
 *
 * Two separate flows: calculation settings and notification modes. Changing a
 * notification mode must not trigger a recalculation, and vice versa.
 */
class PrayerTimesDataStore(private val dataStore: DataStore<Preferences>) {

    private val safeData: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    /*******************CALCULATION SETTINGS*********************/
    val settingsFlow: Flow<PrayerSettings> = safeData
        .map { prefs ->
            PrayerSettings(
                location = prefs.readLocation(),
                method = CalculationMethodOption.fromStorageKey(prefs[METHOD]),
                madhab = MadhabOption.fromStorageKey(prefs[MADHAB]),
            )
        }
        .distinctUntilChanged()

    /** Saves all location fields in one transaction so readers never see half a location. */
    suspend fun setLocation(location: PrayerLocation) {
        dataStore.edit {
            it[LATITUDE] = location.latitude
            it[LONGITUDE] = location.longitude
            it[LOCATION_NAME] = location.displayName
            it[TIME_ZONE_ID] = location.timeZoneId
            it[LOCATION_SOURCE] = location.source.storageKey
            val country = location.countryCode
            if (country != null) it[COUNTRY_CODE] = country else it.remove(COUNTRY_CODE)
        }
    }

    /** null switches back to the automatic, country-based method. */
    suspend fun setMethod(method: CalculationMethodOption?) {
        dataStore.edit {
            if (method != null) it[METHOD] = method.storageKey else it.remove(METHOD)
        }
    }

    suspend fun setMadhab(madhab: MadhabOption) {
        dataStore.edit { it[MADHAB] = madhab.storageKey }
    }

    /** Saves method and madhab together, used by the settings modal's save button. */
    suspend fun setCalculation(method: CalculationMethodOption?, madhab: MadhabOption) {
        dataStore.edit {
            if (method != null) it[METHOD] = method.storageKey else it.remove(METHOD)
            it[MADHAB] = madhab.storageKey
        }
    }

    /*******************NOTIFICATION MODES*********************/
    val notificationModesFlow: Flow<Map<PrayerType, PrayerNotificationMode>> = safeData
        .map { prefs ->
            notifiablePrayers.associateWith { type ->
                PrayerNotificationMode.fromStorageKey(prefs[notificationKey(type)])
            }
        }
        .distinctUntilChanged()

    suspend fun setNotificationMode(type: PrayerType, mode: PrayerNotificationMode) {
        require(type.isNotifiable) { "$type is not notifiable" }
        dataStore.edit { it[notificationKey(type)] = mode.storageKey }
    }

    private fun Preferences.readLocation(): PrayerLocation? {
        val latitude = this[LATITUDE] ?: return null
        val longitude = this[LONGITUDE] ?: return null
        val timeZoneId = this[TIME_ZONE_ID] ?: return null
        return PrayerLocation(
            latitude = latitude,
            longitude = longitude,
            displayName = this[LOCATION_NAME].orEmpty(),
            countryCode = this[COUNTRY_CODE],
            timeZoneId = timeZoneId,
            source = LocationSource.fromStorageKey(this[LOCATION_SOURCE]),
        )
    }

    private companion object {
        val LATITUDE = doublePreferencesKey("location_latitude")
        val LONGITUDE = doublePreferencesKey("location_longitude")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val COUNTRY_CODE = stringPreferencesKey("location_country_code")
        val TIME_ZONE_ID = stringPreferencesKey("location_time_zone_id")
        val LOCATION_SOURCE = stringPreferencesKey("location_source")
        val METHOD = stringPreferencesKey("calculation_method")
        val MADHAB = stringPreferencesKey("madhab")

        val notifiablePrayers = PrayerType.entries.filter { it.isNotifiable }

        // Keyed by the enum name: never rename a PrayerType entry once released.
        fun notificationKey(type: PrayerType) =
            stringPreferencesKey("notification_mode_${type.name.lowercase()}")
    }
}

package com.ottrojja.screens.prayerTimesScreen.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PrayerTimesDataStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val makkah = PrayerLocation(
        latitude = 21.427009, longitude = 39.828685,
        displayName = "مكة المكرمة", countryCode = "SA", timeZoneId = "Asia/Riyadh",
        source = LocationSource.GPS,
    )

    private fun TestScope.newRawStore() = PreferenceDataStoreFactory.create(
        scope = backgroundScope,
        produceFile = { File(tmp.root, "prayer_times_test.preferences_pb") },
    )

    @Test
    fun emptyStore_hasDefaults() = runTest {
        val store = PrayerTimesDataStore(newRawStore())
        val settings = store.settingsFlow.first()

        assertNull(settings.location)
        assertNull(settings.method)
        assertEquals(MadhabOption.JUMHUR, settings.madhab)
        store.notificationModesFlow.first().values.forEach {
            assertEquals(PrayerNotificationMode.DEFAULT, it)
        }
    }

    @Test
    fun location_roundTrips() = runTest {
        val store = PrayerTimesDataStore(newRawStore())
        store.setLocation(makkah)
        assertEquals(makkah, store.settingsFlow.first().location)

        val noCountry = makkah.copy(countryCode = null, source = LocationSource.MANUAL)
        store.setLocation(noCountry)
        assertEquals(noCountry, store.settingsFlow.first().location)
    }

    @Test
    fun method_andMadhab_roundTrip_andNullMeansAutomatic() = runTest {
        val store = PrayerTimesDataStore(newRawStore())

        store.setCalculation(CalculationMethodOption.JORDAN, MadhabOption.HANAFI)
        store.settingsFlow.first().let {
            assertEquals(CalculationMethodOption.JORDAN, it.method)
            assertEquals(MadhabOption.HANAFI, it.madhab)
        }

        store.setMethod(null)
        assertNull(store.settingsFlow.first().method)
    }

    @Test
    fun unknownStoredMethod_fallsBackToAutomatic() = runTest {
        val raw = newRawStore()
        raw.edit { it[stringPreferencesKey("calculation_method")] = "removed_method" }

        assertNull(PrayerTimesDataStore(raw).settingsFlow.first().method)
    }

    @Test
    fun notificationMode_roundTrips() = runTest {
        val store = PrayerTimesDataStore(newRawStore())
        store.setNotificationMode(PrayerType.MAGHRIB, PrayerNotificationMode.ADHAN)

        val modes = store.notificationModesFlow.first()
        assertEquals(PrayerNotificationMode.ADHAN, modes[PrayerType.MAGHRIB])
        assertEquals(PrayerNotificationMode.DEFAULT, modes[PrayerType.FAJR])
        assertEquals(setOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA), modes.keys)
    }
}

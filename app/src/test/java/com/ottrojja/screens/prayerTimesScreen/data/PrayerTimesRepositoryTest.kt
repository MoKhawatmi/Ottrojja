package com.ottrojja.screens.prayerTimesScreen.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ottrojja.screens.prayerTimesScreen.calculator.AdhanPrayerTimesCalculator
import com.ottrojja.screens.prayerTimesScreen.model.HijriDate
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

class PrayerTimesRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val amman = PrayerLocation(31.9539, 35.9106, "عمّان", "JO", "Asia/Amman")
    // About 1 km east of amman.
    private val ammanNearby = amman.copy(longitude = 35.9212, displayName = "عمّان")
    // About 20 km north-west of amman.
    private val salt = PrayerLocation(32.0392, 35.7272, "السلط", "JO", "Asia/Amman")

    private fun TestScope.newRepository() = PrayerTimesRepository(
        dataStore = PrayerTimesDataStore(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tmp.root, "repo_test.preferences_pb") },
            )
        ),
        calculator = AdhanPrayerTimesCalculator { HijriDate(1, 1, 1448) },
    )

    @Test
    fun gps_firstFix_isSaved() = runTest {
        val repo = newRepository()
        assertTrue(repo.updateGpsLocation(amman))
        assertEquals(LocationSource.GPS, repo.settingsFlow.first().location?.source)
    }

    @Test
    fun gps_smallMove_isIgnored_largeMove_isSaved() = runTest {
        val repo = newRepository()
        repo.updateGpsLocation(amman)

        assertFalse(repo.updateGpsLocation(ammanNearby))
        assertEquals(amman.longitude, repo.settingsFlow.first().location!!.longitude, 0.0)

        assertTrue(repo.updateGpsLocation(salt))
        assertEquals("السلط", repo.settingsFlow.first().location!!.displayName)
    }

    @Test
    fun gps_replacesManualCity_evenWhenClose() = runTest {
        val repo = newRepository()
        repo.setManualLocation(amman)

        assertTrue(repo.updateGpsLocation(ammanNearby))
        assertEquals(LocationSource.GPS, repo.settingsFlow.first().location?.source)
    }

    @Test
    fun nextOccurrence_isStrictlyAfterNow_andRollsOverToTomorrow() = runTest {
        val repo = newRepository()
        val settings = PrayerSettings(amman)
        val zone = ZoneId.of(amman.timeZoneId)
        val date = LocalDate.of(2026, 3, 20)
        val lateEvening = date.atTime(23, 0).atZone(zone).toInstant().toEpochMilli()

        val (day, fajr) = repo.nextOccurrence(PrayerType.FAJR, lateEvening, settings)!!
        assertEquals(date.plusDays(1), day.date)
        assertTrue(fajr.epochMillis > lateEvening)

        // Asking again exactly at that Fajr must give the day after, never the same alarm twice.
        val (nextDay, _) = repo.nextOccurrence(PrayerType.FAJR, fajr.epochMillis, settings)!!
        assertEquals(date.plusDays(2), nextDay.date)
    }

    @Test
    fun distance_isReasonable() {
        val km = PrayerTimesRepository.distanceKm(amman, salt)
        assertTrue("was $km", km in 19.0..21.0)
    }
}

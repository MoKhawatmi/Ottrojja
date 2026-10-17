package com.ottrojja.screens.prayerTimesScreen.data

import com.ottrojja.screens.prayerTimesScreen.calculator.DefaultMethodResolver
import com.ottrojja.screens.prayerTimesScreen.calculator.PrayerTimesCalculator
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.DayPrayerTimes
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
import com.ottrojja.screens.prayerTimesScreen.model.NextPrayer
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerTime
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The single entry point for prayer times. The ViewModel and, later, the alarm
 * scheduler depend on this, never on the DataStore or the calculator directly.
 *
 * Calculation takes well under a millisecond, so the day/next-prayer functions are
 * plain synchronous calls.
 */
class PrayerTimesRepository(
    private val dataStore: PrayerTimesDataStore,
    private val calculator: PrayerTimesCalculator,
) {

    val settingsFlow: Flow<PrayerSettings> = dataStore.settingsFlow

    val notificationModesFlow: Flow<Map<PrayerType, PrayerNotificationMode>> =
        dataStore.notificationModesFlow

    fun dayTimes(date: LocalDate, settings: PrayerSettings): DayPrayerTimes? =
        calculator.calculate(date, settings)

    fun nextPrayer(nowMillis: Long, settings: PrayerSettings): NextPrayer? =
        calculator.nextPrayer(nowMillis, settings)

    /**
     * The next time [type] occurs strictly after [nowMillis], with the day it belongs to.
     * Used by the alarm scheduler; null when no location is set or nothing can be calculated
     * in the next few days (polar regions).
     */
    fun nextOccurrence(type: PrayerType, nowMillis: Long, settings: PrayerSettings): Pair<DayPrayerTimes, PrayerTime>? {
        val zone = settings.location?.timeZoneId?.let { ZoneId.of(it) } ?: return null
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in -1L..3L) {
            val day = calculator.calculate(today.plusDays(offset), settings) ?: continue
            val time = day.timeOf(type) ?: continue
            if (time.epochMillis > nowMillis) return day to time
        }
        return null
    }

    /** The method in use, so the UI can show "Automatic (Umm al-Qura)". */
    fun effectiveMethod(settings: PrayerSettings): CalculationMethodOption =
        DefaultMethodResolver.effectiveMethod(settings)

    /** A city the user picked. Always saved. */
    suspend fun setManualLocation(location: PrayerLocation) {
        dataStore.setLocation(location.copy(source = LocationSource.MANUAL))
    }

    /**
     * A fresh GPS fix. Only saved when there is no location yet, the user was on a
     * manual city, or they moved more than [GPS_UPDATE_THRESHOLD_KM]. This keeps GPS
     * jitter from triggering recalculation and alarm rescheduling.
     *
     * @return true if the location was saved.
     */
    suspend fun updateGpsLocation(location: PrayerLocation): Boolean {
        val current = dataStore.settingsFlow.first().location
        val shouldSave = current == null ||
            current.source != LocationSource.GPS ||
            distanceKm(current, location) > GPS_UPDATE_THRESHOLD_KM
        if (shouldSave) dataStore.setLocation(location.copy(source = LocationSource.GPS))
        return shouldSave
    }

    suspend fun saveCalculationSettings(method: CalculationMethodOption?, madhab: MadhabOption) =
        dataStore.setCalculation(method, madhab)

    suspend fun setNotificationMode(type: PrayerType, mode: PrayerNotificationMode) =
        dataStore.setNotificationMode(type, mode)

    companion object {
        const val GPS_UPDATE_THRESHOLD_KM = 5.0
        private const val EARTH_RADIUS_KM = 6371.0

        /** Haversine distance. */
        internal fun distanceKm(a: PrayerLocation, b: PrayerLocation): Double {
            val lat1 = Math.toRadians(a.latitude)
            val lat2 = Math.toRadians(b.latitude)
            val dLat = lat2 - lat1
            val dLon = Math.toRadians(b.longitude - a.longitude)
            val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
            return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
        }
    }
}

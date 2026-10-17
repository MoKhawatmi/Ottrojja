package com.ottrojja.screens.prayerTimesScreen.calculator

import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultMethodResolverTest {

    @Test
    fun knownCountries_mapToTheirMethod() {
        assertEquals(CalculationMethodOption.UMM_AL_QURA, DefaultMethodResolver.defaultFor("SA"))
        assertEquals(CalculationMethodOption.EGYPTIAN, DefaultMethodResolver.defaultFor("eg"))
        assertEquals(CalculationMethodOption.TURKEY, DefaultMethodResolver.defaultFor("TR"))
    }

    @Test
    fun unknownOrMissingCountry_fallsBackToMuslimWorldLeague() {
        assertEquals(CalculationMethodOption.MUSLIM_WORLD_LEAGUE, DefaultMethodResolver.defaultFor("DE"))
        assertEquals(CalculationMethodOption.MUSLIM_WORLD_LEAGUE, DefaultMethodResolver.defaultFor(null))
    }

    @Test
    fun explicitChoice_winsOverCountryDefault() {
        val location = PrayerLocation(21.4, 39.8, "Makkah", "SA", "Asia/Riyadh")
        val settings = PrayerSettings(location, method = CalculationMethodOption.KARACHI)
        assertEquals(CalculationMethodOption.KARACHI, DefaultMethodResolver.effectiveMethod(settings))
    }

    @Test
    fun storageKeys_roundTrip_andAreUnique() {
        CalculationMethodOption.entries.forEach {
            assertEquals(it, CalculationMethodOption.fromStorageKey(it.storageKey))
        }
        assertEquals(
            CalculationMethodOption.entries.size,
            CalculationMethodOption.entries.map { it.storageKey }.toSet().size,
        )
    }
}

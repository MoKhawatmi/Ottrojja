package com.ottrojja.screens.prayerTimesScreen.calculator

import com.ottrojja.screens.prayerTimesScreen.hijri.HijriDateProvider
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.DayPrayerTimes
import com.ottrojja.screens.prayerTimesScreen.model.HijriDate
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AdhanPrayerTimesCalculatorTest {

    private val notRamadan = HijriDateProvider { HijriDate(day = 1, month = 3, year = 1437) }
    private val ramadan = HijriDateProvider { HijriDate(day = 1, month = 9, year = 1437) }

    private val calculator = AdhanPrayerTimesCalculator(notRamadan)

    private val raleigh = PrayerLocation(
        latitude = 35.7750, longitude = -78.6336,
        displayName = "Raleigh", countryCode = "US", timeZoneId = "America/New_York",
    )
    private val makkah = PrayerLocation(
        latitude = 21.427009, longitude = 39.828685,
        displayName = "Makkah", countryCode = "SA", timeZoneId = "Asia/Riyadh",
    )

    // Reference values below come from Adhan's own test suite.

    @Test
    fun raleigh_northAmerica_hanafi_matchesReference() {
        val settings = PrayerSettings(raleigh, CalculationMethodOption.NORTH_AMERICA, MadhabOption.HANAFI)
        val day = calculator.calculate(LocalDate.of(2015, 7, 12), settings)!!

        day.assertTime(PrayerType.FAJR, 4, 42)
        day.assertTime(PrayerType.SUNRISE, 6, 8)
        day.assertTime(PrayerType.DUHA, 6, 23)
        day.assertTime(PrayerType.DHUHR, 13, 21)
        day.assertTime(PrayerType.ASR, 18, 22)
        day.assertTime(PrayerType.MAGHRIB, 20, 32)
        day.assertTime(PrayerType.ISHA, 21, 57)
    }

    @Test
    fun makkah_ummAlQura_matchesReference() {
        val settings = PrayerSettings(makkah, CalculationMethodOption.UMM_AL_QURA)
        val day = calculator.calculate(LocalDate.of(2016, 1, 5), settings)!!

        day.assertTime(PrayerType.FAJR, 5, 38)
        day.assertTime(PrayerType.SUNRISE, 7, 0)
        day.assertTime(PrayerType.DHUHR, 12, 26)
        day.assertTime(PrayerType.ASR, 15, 31)
        day.assertTime(PrayerType.MAGHRIB, 17, 52)
        day.assertTime(PrayerType.ISHA, 19, 22)
    }

    @Test
    fun ummAlQura_inRamadan_addsThirtyMinutesToIshaOnly() {
        val settings = PrayerSettings(makkah, CalculationMethodOption.UMM_AL_QURA)
        val date = LocalDate.of(2016, 1, 5)
        val normal = calculator.calculate(date, settings)!!
        val inRamadan = AdhanPrayerTimesCalculator(ramadan).calculate(date, settings)!!

        inRamadan.assertTime(PrayerType.ISHA, 19, 52)
        PrayerType.entries.filter { it != PrayerType.ISHA }.forEach { type ->
            assertEquals(type.name, normal.timeOf(type)!!.epochMillis, inRamadan.timeOf(type)!!.epochMillis)
        }
    }

    @Test
    fun hanafi_changesOnlyAsr_andMakesItLater() {
        val date = LocalDate.of(2015, 7, 12)
        val jumhur = calculator.calculate(date, PrayerSettings(raleigh, CalculationMethodOption.NORTH_AMERICA, MadhabOption.JUMHUR))!!
        val hanafi = calculator.calculate(date, PrayerSettings(raleigh, CalculationMethodOption.NORTH_AMERICA, MadhabOption.HANAFI))!!

        assertTrue(hanafi.timeOf(PrayerType.ASR)!!.epochMillis > jumhur.timeOf(PrayerType.ASR)!!.epochMillis)
        PrayerType.entries.filter { it != PrayerType.ASR }.forEach { type ->
            assertEquals(type.name, jumhur.timeOf(type)!!.epochMillis, hanafi.timeOf(type)!!.epochMillis)
        }
    }

    @Test
    fun times_areInDisplayOrder() {
        val day = calculator.calculate(LocalDate.of(2015, 7, 12), PrayerSettings(raleigh))!!
        assertEquals(PrayerType.entries.toList(), day.times.map { it.type })
        assertEquals(day.times.sortedBy { it.epochMillis }, day.times)
    }

    @Test
    fun friday_isFlagged_andDhuhrIsJumuah() {
        val settings = PrayerSettings(raleigh, CalculationMethodOption.NORTH_AMERICA)
        val friday = LocalDate.of(2015, 7, 17)
        assertTrue(calculator.calculate(friday, settings)!!.isFriday)
        assertFalse(calculator.calculate(friday.minusDays(1), settings)!!.isFriday)

        val next = calculator.nextPrayer(friday.millisAt(10, 0, raleigh), settings)!!
        assertEquals(PrayerType.DHUHR, next.type)
        assertTrue(next.isJumuah)
    }

    @Test
    fun nextPrayer_skipsSunriseAndDuha() {
        val settings = PrayerSettings(raleigh, CalculationMethodOption.NORTH_AMERICA)
        val date = LocalDate.of(2015, 7, 12)
        // 05:00 is after Fajr and before Sunrise: the next prayer is Dhuhr.
        val next = calculator.nextPrayer(date.millisAt(5, 0, raleigh), settings)!!
        assertEquals(PrayerType.DHUHR, next.type)
        assertFalse(next.isJumuah) // a Sunday
    }

    @Test
    fun nextPrayer_afterIsha_isTomorrowsFajr() {
        val settings = PrayerSettings(raleigh, CalculationMethodOption.NORTH_AMERICA)
        val date = LocalDate.of(2015, 7, 12)
        val next = calculator.nextPrayer(date.millisAt(22, 30, raleigh), settings)!!

        assertEquals(PrayerType.FAJR, next.type)
        assertEquals(date.plusDays(1), next.date)
    }

    @Test
    fun noLocation_returnsNull() {
        val settings = PrayerSettings(location = null)
        assertNull(calculator.calculate(LocalDate.of(2015, 7, 12), settings))
        assertNull(calculator.nextPrayer(0L, settings))
    }

    @Test
    fun polarSummer_doesNotCrash() {
        val tromso = PrayerLocation(69.6496, 18.9560, "Tromsø", "NO", "Europe/Oslo")
        val settings = PrayerSettings(tromso)
        val midsummer = LocalDate.of(2026, 6, 21)

        // The result may be null (sun never sets) but must never throw.
        calculator.calculate(midsummer, settings)
        calculator.nextPrayer(midsummer.millisAt(12, 0, tromso), settings)
    }

    @Test
    fun everyMethod_producesOrderedTimes() {
        val amman = PrayerLocation(31.9539, 35.9106, "Amman", "JO", "Asia/Amman")
        val date = LocalDate.of(2026, 3, 20)
        CalculationMethodOption.entries.forEach { method ->
            val day = calculator.calculate(date, PrayerSettings(amman, method))
            assertNotNull(method.name, day)
            assertEquals(method.name, day!!.times.sortedBy { it.epochMillis }, day.times)
        }
    }

    @Test
    fun portugal_ishaIs77MinutesAfterMaghrib() {
        val lisbon = PrayerLocation(38.7223, -9.1393, "Lisbon", "PT", "Europe/Lisbon")
        val day = calculator.calculate(LocalDate.of(2026, 3, 20), PrayerSettings(lisbon, CalculationMethodOption.PORTUGAL))!!
        val gap = day.timeOf(PrayerType.ISHA)!!.epochMillis - day.timeOf(PrayerType.MAGHRIB)!!.epochMillis
        assertEquals(77L * 60_000, gap)
    }

    @Test
    fun automaticMethod_usesCountryDefault() {
        val auto = calculator.calculate(LocalDate.of(2016, 1, 5), PrayerSettings(makkah, method = null))
        val explicit = calculator.calculate(LocalDate.of(2016, 1, 5), PrayerSettings(makkah, CalculationMethodOption.UMM_AL_QURA))
        assertNotNull(auto)
        assertEquals(explicit!!.times, auto!!.times)
    }

    private fun DayPrayerTimes.assertTime(type: PrayerType, hour: Int, minute: Int) {
        assertEquals(type.name, LocalTime.of(hour, minute), timeOf(type)!!.localTime(zoneId))
    }

    private fun LocalDate.millisAt(hour: Int, minute: Int, location: PrayerLocation): Long =
        LocalDateTime.of(this, LocalTime.of(hour, minute))
            .atZone(ZoneId.of(location.timeZoneId))
            .toInstant()
            .toEpochMilli()
}

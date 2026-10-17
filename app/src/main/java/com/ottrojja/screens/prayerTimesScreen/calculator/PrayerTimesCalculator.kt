package com.ottrojja.screens.prayerTimesScreen.calculator

import com.ottrojja.screens.prayerTimesScreen.model.DayPrayerTimes
import com.ottrojja.screens.prayerTimesScreen.model.NextPrayer
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import java.time.LocalDate

interface PrayerTimesCalculator {

    /**
     * Times for [date] (a calendar day at the settings' location).
     *
     * Returns null when there is no location yet, or when the times cannot be
     * calculated for that day (polar day or polar night, where the sun never rises or sets).
     */
    fun calculate(date: LocalDate, settings: PrayerSettings): DayPrayerTimes?

    /**
     * The first obligatory prayer strictly after [nowMillis]. After Isha this is the
     * next day's Fajr. Returns null when there is no location or nothing can be
     * calculated in the next few days.
     */
    fun nextPrayer(nowMillis: Long, settings: PrayerSettings): NextPrayer?
}

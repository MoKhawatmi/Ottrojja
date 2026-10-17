package com.ottrojja.screens.prayerTimesScreen.hijri

import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import com.ottrojja.screens.prayerTimesScreen.model.HijriDate
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Converts a Gregorian date to a Hijri date.
 *
 * An interface so the calculator stays testable on the plain JVM, where android.icu
 * is not available.
 */
fun interface HijriDateProvider {
    fun hijriDateFor(date: LocalDate): HijriDate
}

/**
 * Umm al-Qura based conversion using android.icu (available from API 24, no desugaring
 * involved). Desugared java.time.chrono.HijrahDate is deliberately not used.
 */
class IcuHijriDateProvider : HijriDateProvider {

    override fun hijriDateFor(date: LocalDate): HijriDate {
        val calendar = IslamicCalendar(TimeZone.getTimeZone("UTC"), ULocale.ROOT).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            // Noon UTC of the Gregorian day, so the result never slips to a neighbouring day.
            timeInMillis = date.atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        }
        return HijriDate(
            day = calendar.get(IslamicCalendar.DAY_OF_MONTH),
            month = calendar.get(IslamicCalendar.MONTH) + 1, // ICU months are 0-based
            year = calendar.get(IslamicCalendar.YEAR),
        )
    }
}

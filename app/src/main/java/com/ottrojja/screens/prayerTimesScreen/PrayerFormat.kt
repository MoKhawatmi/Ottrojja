package com.ottrojja.screens.prayerTimesScreen

import android.content.Context
import android.text.format.DateFormat
import com.ottrojja.R
import com.ottrojja.screens.prayerTimesScreen.model.HijriDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Date and time formatting shared by the screen and the notifications.
 * Digits stay Western (0-9); java.time keeps them unless a DecimalStyle is set.
 */
object PrayerFormat {

    private val arabic = Locale("ar")
    private val time24 = DateTimeFormatter.ofPattern("HH:mm", arabic)
    private val time12 = DateTimeFormatter.ofPattern("h:mm a", arabic)
    private val gregorian = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabic)

    /** "13:05" or "1:05 م", following the device's 24-hour setting. */
    fun time(context: Context, epochMillis: Long, zoneId: ZoneId): String {
        val formatter = if (DateFormat.is24HourFormat(context)) time24 else time12
        return formatter.format(Instant.ofEpochMilli(epochMillis).atZone(zoneId))
    }

    /** "الجمعة، 16 أكتوبر 2026" */
    fun gregorianDate(date: LocalDate): String = gregorian.format(date)

    /** "4 جمادى الأولى 1448 هـ" */
    fun hijriDate(context: Context, hijri: HijriDate): String {
        val months = context.resources.getStringArray(R.array.hijri_months)
        return context.getString(R.string.hijri_date_format, hijri.day, months[hijri.month - 1], hijri.year)
    }

    /**
     * "02:14:33"
     *
     * Rounds to the nearest second instead of truncating: the clock ticks land a few ms
     * before or after each full second, and truncating turned that jitter into skipped
     * or repeated seconds.
     */
    fun countdown(remainingMillis: Long): String {
        val totalSeconds = (remainingMillis.coerceAtLeast(0) + 500) / 1000
        return String.format(
            Locale.US, "%02d:%02d:%02d",
            totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60,
        )
    }
}
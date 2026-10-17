package com.ottrojja.screens.prayerTimesScreen.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * A single time in the list.
 *
 * Stored as epoch millis on purpose: this value is what crosses into AlarmManager,
 * WorkManager Data and Room later, and java.time types must not cross those boundaries
 * when core library desugaring is enabled.
 */
data class PrayerTime(
    val type: PrayerType,
    val epochMillis: Long,
) {
    fun localTime(zoneId: ZoneId): LocalTime =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()
}

/**
 * Hijri date as plain numbers. [month] is 1-based (1 = Muharram, 9 = Ramadan).
 * Month names come from the R.array.hijri_months string array.
 */
data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
) {
    val isRamadan: Boolean get() = month == RAMADAN

    companion object {
        const val RAMADAN = 9
    }
}

/**
 * All times for one calendar day at one location, ordered as [PrayerType] is declared.
 *
 * Note: the Hijri date here is the one for the daytime of [date]. The Hijri day
 * technically begins at Maghrib; the UI can decide whether to reflect that.
 */
data class DayPrayerTimes(
    val date: LocalDate,
    val timeZoneId: String,
    val hijriDate: HijriDate,
    val times: List<PrayerTime>,
) {
    val zoneId: ZoneId get() = ZoneId.of(timeZoneId)

    val isFriday: Boolean get() = date.dayOfWeek == DayOfWeek.FRIDAY

    fun timeOf(type: PrayerType): PrayerTime? = times.firstOrNull { it.type == type }
}

/**
 * The upcoming obligatory prayer, used by the header countdown and later by the alarms.
 *
 * [date] is the local calendar day the prayer belongs to. After Isha it is tomorrow,
 * which the UI can show as "tomorrow" if it wants to.
 */
data class NextPrayer(
    val type: PrayerType,
    val epochMillis: Long,
    val date: LocalDate,
    val isJumuah: Boolean,
)

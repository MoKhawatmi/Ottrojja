package com.ottrojja.screens.prayerTimesScreen.calculator

import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.ottrojja.screens.prayerTimesScreen.hijri.HijriDateProvider
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.DayPrayerTimes
import com.ottrojja.screens.prayerTimesScreen.model.HijriDate
import com.ottrojja.screens.prayerTimesScreen.model.NextPrayer
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerTime
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import com.ottrojja.screens.prayerTimesScreen.model.PrayerZones
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * [PrayerTimesCalculator] backed by Adhan (com.batoulapps.adhan2).
 *
 * - High-latitude handling is left to Adhan's automatic rule (highLatitudeRule = null),
 *   so users never see that option. Methods with an authority-specific rule set their own.
 * - Duha is derived from sunrise with one fixed convention ([DUHA_MINUTES_AFTER_SUNRISE]).
 * - Umm al-Qura gets its official +30 min Isha during Ramadan. This is part of the method
 *   itself, not a user adjustment.
 * - Diyanet-style methods take Asr, Maghrib and Isha from the previous day's calculation,
 *   and Diyanet's calendars abroad apply Diyanet's summer rule above 44.5°
 *   (see AdhanMappers.kt for why and how this was verified).
 *
 * Pure apart from the injected [HijriDateProvider], so it runs in plain JVM unit tests.
 */
class AdhanPrayerTimesCalculator(
    private val hijriDateProvider: HijriDateProvider,
) : PrayerTimesCalculator {

    /** Adhan's six times for one date, as epoch millis. */
    private data class AdhanDay(
        val fajr: Long,
        val sunrise: Long,
        val dhuhr: Long,
        val asr: Long,
        val maghrib: Long,
        val isha: Long,
    )

    override fun calculate(date: LocalDate, settings: PrayerSettings): DayPrayerTimes? {
        val location = settings.location ?: return null
        val method = DefaultMethodResolver.effectiveMethod(settings)
        val coordinates = Coordinates(location.latitude, location.longitude)
        val hijriDate = hijriDateProvider.hijriDateFor(date)

        val today = adhanDay(coordinates, date, method, settings) ?: return null
        var day = today

        if (method.eveningFromPreviousDay) {
            // If yesterday can't be calculated (polar edge), today's own values are kept.
            adhanDay(coordinates, date.minusDays(1), method, settings)?.let { previous ->
                day = day.copy(
                    asr = previous.asr + DAY_MILLIS,
                    maghrib = previous.maghrib + DAY_MILLIS,
                    isha = previous.isha + DAY_MILLIS,
                )
            }
        }

        if (method.usesDiyanetHighLatitudeRule && abs(location.latitude) >= DIYANET_HIGH_LATITUDE_MIN) {
            day = applyDiyanetHighLatitudeRule(day, today, coordinates, date, method, settings)
        }

        val times = listOf(
            PrayerTime(PrayerType.FAJR, day.fajr),
            PrayerTime(PrayerType.SUNRISE, day.sunrise),
            PrayerTime(PrayerType.DUHA, day.sunrise + DUHA_OFFSET_MILLIS),
            PrayerTime(PrayerType.DHUHR, day.dhuhr),
            PrayerTime(PrayerType.ASR, day.asr),
            PrayerTime(PrayerType.MAGHRIB, day.maghrib),
            PrayerTime(PrayerType.ISHA, day.isha),
        )

        return DayPrayerTimes(
            date = date,
            timeZoneId = location.timeZoneId,
            hijriDate = hijriDate,
            times = times,
        )
    }

    override fun nextPrayer(nowMillis: Long, settings: PrayerSettings): NextPrayer? {
        val location = settings.location ?: return null
        val today = Instant.ofEpochMilli(nowMillis)
            .atZone(PrayerZones.of(location.timeZoneId))
            .toLocalDate()

        // Starts at yesterday: at high latitudes Isha can fall after local midnight.
        // Looks a few days ahead so polar days without times are skipped, not fatal.
        for (offset in -1L..LOOKAHEAD_DAYS) {
            val day = calculate(today.plusDays(offset), settings) ?: continue
            val next = day.times.firstOrNull { it.type.isObligatory && it.epochMillis > nowMillis }
                ?: continue
            return NextPrayer(
                type = next.type,
                epochMillis = next.epochMillis,
                date = day.date,
                isJumuah = next.type == PrayerType.DHUHR && day.isFriday,
            )
        }
        return null
    }

    /** Adhan's times for [date], or null when they can't be calculated (polar day/night). */
    private fun adhanDay(
        coordinates: Coordinates,
        date: LocalDate,
        method: CalculationMethodOption,
        settings: PrayerSettings,
    ): AdhanDay? {
        val adhanTimes = try {
            PrayerTimes(
                coordinates = coordinates,
                dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth),
                calculationParameters = buildParameters(method, settings, hijriDateProvider.hijriDateFor(date)),
            )
        } catch (e: IllegalStateException) {
            // Adhan throws when the sun does not rise or set that day (polar regions).
            return null
        } catch (e: IllegalArgumentException) {
            // Invalid coordinates.
            return null
        }
        return AdhanDay(
            fajr = adhanTimes.fajr.toEpochMilliseconds(),
            sunrise = adhanTimes.sunrise.toEpochMilliseconds(),
            dhuhr = adhanTimes.dhuhr.toEpochMilliseconds(),
            asr = adhanTimes.asr.toEpochMilliseconds(),
            maghrib = adhanTimes.maghrib.toEpochMilliseconds(),
            isha = adhanTimes.isha.toEpochMilliseconds(),
        )
    }

    /**
     * Diyanet's summer rule abroad: Fajr no earlier than sunrise - 5/24 of the night,
     * Isha no later than sunset + 3/16 of the night (night = sunset to next sunrise).
     * [today] is the unshifted calculation; its sunrise/maghrib minus the method's
     * adjustments give the astronomical sunrise and sunset.
     */
    private fun applyDiyanetHighLatitudeRule(
        day: AdhanDay,
        today: AdhanDay,
        coordinates: Coordinates,
        date: LocalDate,
        method: CalculationMethodOption,
        settings: PrayerSettings,
    ): AdhanDay {
        val yesterday = adhanDay(coordinates, date.minusDays(1), method, settings) ?: return day
        val tomorrow = adhanDay(coordinates, date.plusDays(1), method, settings) ?: return day
        val adjustments = method.toParameters().methodAdjustments

        val sunrise = today.sunrise - adjustments.sunrise * MINUTE_MILLIS
        val previousSunset = yesterday.maghrib - adjustments.maghrib * MINUTE_MILLIS
        val fajrBound = sunrise - (DIYANET_FAJR_NIGHT_FRACTION * (sunrise - previousSunset)).toLong() +
            adjustments.fajr * MINUTE_MILLIS

        val sunset = today.maghrib - adjustments.maghrib * MINUTE_MILLIS
        val nextSunrise = tomorrow.sunrise - adjustments.sunrise * MINUTE_MILLIS
        val ishaBound = sunset + (DIYANET_ISHA_NIGHT_FRACTION * (nextSunrise - sunset)).toLong() +
            adjustments.isha * MINUTE_MILLIS

        return day.copy(
            fajr = if (day.fajr < fajrBound) roundToMinute(fajrBound) else day.fajr,
            isha = if (day.isha > ishaBound) roundToMinute(ishaBound) else day.isha,
        )
    }

    private fun buildParameters(
        method: CalculationMethodOption,
        settings: PrayerSettings,
        hijriDate: HijriDate,
    ): CalculationParameters {
        val base = method.toParameters().copy(madhab = settings.madhab.toAdhan())

        return if (method == CalculationMethodOption.UMM_AL_QURA && hijriDate.isRamadan) {
            base.copy(
                methodAdjustments = base.methodAdjustments.copy(
                    isha = base.methodAdjustments.isha + UMM_AL_QURA_RAMADAN_ISHA_MINUTES
                )
            )
        } else {
            base
        }
    }

    companion object {
        /** Common convention: Duha begins roughly a quarter hour after sunrise. */
        const val DUHA_MINUTES_AFTER_SUNRISE = 15L
        private val DUHA_OFFSET_MILLIS = TimeUnit.MINUTES.toMillis(DUHA_MINUTES_AFTER_SUNRISE)

        private const val UMM_AL_QURA_RAMADAN_ISHA_MINUTES = 30
        private const val LOOKAHEAD_DAYS = 3L

        private const val MINUTE_MILLIS = 60_000L
        private const val DAY_MILLIS = 86_400_000L

        /** Nearest minute, half up (same as Adhan's NEAREST rounding). */
        private fun roundToMinute(epochMillis: Long): Long =
            Math.floorDiv(epochMillis + MINUTE_MILLIS / 2, MINUTE_MILLIS) * MINUTE_MILLIS
    }
}

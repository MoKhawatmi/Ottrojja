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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * [PrayerTimesCalculator] backed by Adhan (com.batoulapps.adhan2).
 *
 * - High-latitude handling is left to Adhan's automatic rule (highLatitudeRule = null),
 *   so users never see that option.
 * - Duha is derived from sunrise with one fixed convention ([DUHA_MINUTES_AFTER_SUNRISE]).
 * - Umm al-Qura gets its official +30 min Isha during Ramadan. This is part of the method
 *   itself, not a user adjustment.
 *
 * Pure apart from the injected [HijriDateProvider], so it runs in plain JVM unit tests.
 */
class AdhanPrayerTimesCalculator(
    private val hijriDateProvider: HijriDateProvider,
) : PrayerTimesCalculator {

    override fun calculate(date: LocalDate, settings: PrayerSettings): DayPrayerTimes? {
        val location = settings.location ?: return null
        val hijriDate = hijriDateProvider.hijriDateFor(date)

        val adhanTimes = try {
            PrayerTimes(
                coordinates = Coordinates(location.latitude, location.longitude),
                dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth),
                calculationParameters = buildParameters(settings, hijriDate),
            )
        } catch (e: IllegalStateException) {
            // Adhan throws when the sun does not rise or set that day (polar regions).
            return null
        } catch (e: IllegalArgumentException) {
            // Invalid coordinates.
            return null
        }

        val sunrise = adhanTimes.sunrise.toEpochMilliseconds()
        val times = listOf(
            PrayerTime(PrayerType.FAJR, adhanTimes.fajr.toEpochMilliseconds()),
            PrayerTime(PrayerType.SUNRISE, sunrise),
            PrayerTime(PrayerType.DUHA, sunrise + DUHA_OFFSET_MILLIS),
            PrayerTime(PrayerType.DHUHR, adhanTimes.dhuhr.toEpochMilliseconds()),
            PrayerTime(PrayerType.ASR, adhanTimes.asr.toEpochMilliseconds()),
            PrayerTime(PrayerType.MAGHRIB, adhanTimes.maghrib.toEpochMilliseconds()),
            PrayerTime(PrayerType.ISHA, adhanTimes.isha.toEpochMilliseconds()),
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
            .atZone(ZoneId.of(location.timeZoneId))
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

    private fun buildParameters(settings: PrayerSettings, hijriDate: HijriDate): CalculationParameters {
        val method = DefaultMethodResolver.effectiveMethod(settings)
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
    }
}

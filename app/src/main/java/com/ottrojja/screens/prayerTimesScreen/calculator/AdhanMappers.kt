package com.ottrojja.screens.prayerTimesScreen.calculator

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerAdjustments
import com.batoulapps.adhan2.model.Rounding
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption

/**
 * The only place app enums are translated into Adhan types.
 *
 * Values are tuned to reproduce each authority's published timetable, not just its sun
 * angles. Authorities add precautionary minutes (temkin / ihtiyat) and their own rounding
 * on top of the angles, and those go into methodAdjustments / rounding here.
 *
 * Main reference: salah-kit/SalahTimes METHODS.md (Oct 2026), which compares each method
 * against a full year of official timetables. "Fitted" below means the minutes are
 * measured averages from those timetables, not values the authority documents.
 */
internal fun CalculationMethodOption.toParameters(): CalculationParameters = when (this) {
    // Adhan presets used as shipped (each matches its official table within a minute)
    CalculationMethodOption.MUSLIM_WORLD_LEAGUE -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
    CalculationMethodOption.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters
    CalculationMethodOption.QATAR -> CalculationMethod.QATAR.parameters
    CalculationMethodOption.KUWAIT -> CalculationMethod.KUWAIT.parameters
    // Diyanet's domestic definition, temkin included (sunrise -7, Dhuhr +5, Asr +4, Maghrib +7).
    CalculationMethodOption.TURKEY -> CalculationMethod.TURKEY.parameters
    // MUIS: 20°/18°, Dhuhr +1, rounded up.
    CalculationMethodOption.SINGAPORE -> CalculationMethod.SINGAPORE.parameters
    CalculationMethodOption.MOON_SIGHTING_COMMITTEE -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
    CalculationMethodOption.NORTH_AMERICA -> CalculationMethod.NORTH_AMERICA.parameters

    // Presets with Adhan's generic Dhuhr +1 removed: the official Cairo (Survey Authority)
    // and Karachi (Banuri Town) tables match the bare angles with no added minutes.
    CalculationMethodOption.EGYPTIAN ->
        CalculationMethod.EGYPTIAN.parameters.copy(methodAdjustments = PrayerAdjustments())
    CalculationMethodOption.KARACHI ->
        CalculationMethod.KARACHI.parameters.copy(methodAdjustments = PrayerAdjustments())

    // UAE Awqaf. Adhan's preset has Asr +3 (fitted to 2018 tables); the 2026 Dubai and
    // Abu Dhabi tables put Asr 2 minutes earlier, so Asr is +1.
    CalculationMethodOption.DUBAI -> CalculationMethod.DUBAI.parameters.copy(
        methodAdjustments = PrayerAdjustments(sunrise = -3, dhuhr = 3, asr = 1, maghrib = 3),
    )

    // Older "Gulf region" convention, kept for users who chose it. Not an official table.
    CalculationMethodOption.GULF -> custom(fajrAngle = 19.5, ishaInterval = 90)

    // Jordan Awqaf: Maghrib ~6.6 min after sunset and sunrise ~6.5 min before it,
    // in every season (fitted from 249 days of the ministry's timetable).
    CalculationMethodOption.JORDAN -> custom(
        fajrAngle = 18.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(sunrise = -7, maghrib = 7),
    )

    // Morocco (Rabat official times, 13 Sep, 27 Sep, 12 Oct and 19 Oct 2026): 19°/17°,
    // sunrise -3, Dhuhr +5, Maghrib +4. All prayers within a minute on all four days.
    // Morocco is on plain GMT since 20 Sep 2026; see PrayerZones for the time zone fix.
    CalculationMethodOption.MOROCCO -> custom(
        fajrAngle = 19.0, ishaAngle = 17.0,
        adjustments = PrayerAdjustments(sunrise = -3, dhuhr = 5, maghrib = 4),
    )

    // Algeria (Ministry, Algiers 2026): 18°/17°, Maghrib ~3.5 min after sunset, times
    // rounded up. Rounding up turns the +3 into an average of +3.5.
    CalculationMethodOption.ALGERIA -> custom(
        fajrAngle = 18.0, ishaAngle = 17.0,
        adjustments = PrayerAdjustments(maghrib = 3),
        rounding = Rounding.UP,
    )

    // Tunisia (Ministry): 18°/18°, Dhuhr ~7 min after transit, Maghrib ~2 min after
    // sunset (fitted; checked against the ministry's Tunis times for 19 Oct 2026).
    // Note: its inland tables also correct for town elevation, which Adhan cannot model.
    CalculationMethodOption.TUNISIA -> custom(
        fajrAngle = 18.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(dhuhr = 7, maghrib = 2),
    )

    // Malaysia JAKIM: Subuh moved from 20° to 18° (National Fatwa Council, Nov 2019);
    // Zohor is transit + ~1 min.
    CalculationMethodOption.JAKIM -> custom(
        fajrAngle = 18.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(dhuhr = 1),
    )

    // Indonesia Kemenag: 20°/18°, round up, then ihtiyat Fajr +2, sunrise -3, Dhuhr +3,
    // Asr +2, Maghrib +2, Isha +2. Kemenag puts Maghrib at the sun 1° below the horizon
    // (Adhan uses 50'), which Adhan can't express; Maghrib may land ~1 min early.
    CalculationMethodOption.KEMENAG -> custom(
        fajrAngle = 20.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(fajr = 2, sunrise = -3, dhuhr = 3, asr = 2, maghrib = 2, isha = 2),
        rounding = Rounding.UP,
    )

    CalculationMethodOption.RUSSIA -> custom(fajrAngle = 16.0, ishaAngle = 15.0)
    CalculationMethodOption.FRANCE -> custom(fajrAngle = 12.0, ishaAngle = 12.0)
    // Maghrib is sunset + 3 min and Isha is 77 min after that Maghrib. Adhan measures
    // ishaInterval from sunset, so Isha gets the same +3 to land at sunset + 80.
    CalculationMethodOption.PORTUGAL -> custom(
        fajrAngle = 18.0, ishaInterval = 77,
        adjustments = PrayerAdjustments(maghrib = 3, isha = 3),
    )
}

private fun custom(
    fajrAngle: Double,
    ishaAngle: Double = 0.0,
    ishaInterval: Int = 0,
    adjustments: PrayerAdjustments = PrayerAdjustments(),
    rounding: Rounding = Rounding.NEAREST,
) = CalculationParameters(
    fajrAngle = fajrAngle,
    ishaAngle = ishaAngle,
    ishaInterval = ishaInterval,
    method = CalculationMethod.OTHER,
    methodAdjustments = adjustments,
    rounding = rounding,
)

internal fun MadhabOption.toAdhan(): Madhab = when (this) {
    MadhabOption.JUMHUR -> Madhab.SHAFI
    MadhabOption.HANAFI -> Madhab.HANAFI
}

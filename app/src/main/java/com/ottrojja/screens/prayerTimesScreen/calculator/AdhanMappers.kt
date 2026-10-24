package com.ottrojja.screens.prayerTimesScreen.calculator

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.HighLatitudeRule
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
 * "Verified" below means: compared against a full year of the authority's own timetable
 * (Oct 2026 validation, 29 timetables), with the % of days within 1 minute for every prayer.
 * "Fitted" means the minutes are measured from timetables, not documented by the authority.
 */
internal fun CalculationMethodOption.toParameters(): CalculationParameters = when (this) {
    // Adhan presets used as shipped
    CalculationMethodOption.MUSLIM_WORLD_LEAGUE -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
    CalculationMethodOption.QATAR -> CalculationMethod.QATAR.parameters
    CalculationMethodOption.KUWAIT -> CalculationMethod.KUWAIT.parameters
    // Diyanet's domestic definition, temkin included (sunrise -7, Dhuhr +5, Asr +4, Maghrib +7).
    // Evening prayers come from the previous day (see eveningFromPreviousDay).
    // Verified: Istanbul, Ankara 2027, every prayer 100%.
    CalculationMethodOption.TURKEY -> CalculationMethod.TURKEY.parameters
    // MUIS: 20°/18°, Dhuhr +1, rounded up. Verified: Singapore 2026, 99-100%.
    CalculationMethodOption.SINGAPORE -> CalculationMethod.SINGAPORE.parameters
    CalculationMethodOption.MOON_SIGHTING_COMMITTEE -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
    CalculationMethodOption.NORTH_AMERICA -> CalculationMethod.NORTH_AMERICA.parameters

    // Umm al-Qura: the official calendar rounds prayer times up and sunrise down (the end of
    // Fajr gets the safe side). Adhan has one rounding mode, so: round up, then sunrise -1.
    // Verified: Makkah, Madinah, Riyadh 1448 AH, every prayer 100% (was 97-99%).
    CalculationMethodOption.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters.copy(
        methodAdjustments = PrayerAdjustments(sunrise = -1),
        rounding = Rounding.UP,
    )

    // Presets with Adhan's generic Dhuhr +1 removed: the official Cairo (Survey Authority)
    // and Karachi (Banuri Town) tables match the bare angles with no added minutes.
    // Verified: Cairo, Alexandria, Karachi 2026, every prayer 100%.
    CalculationMethodOption.EGYPTIAN ->
        CalculationMethod.EGYPTIAN.parameters.copy(methodAdjustments = PrayerAdjustments())
    CalculationMethodOption.KARACHI ->
        CalculationMethod.KARACHI.parameters.copy(methodAdjustments = PrayerAdjustments())

    // UAE Awqaf. Adhan's preset has Asr +3 (fitted to 2018 tables); the 2026 tables put Asr
    // 2 minutes earlier, so Asr is +1. Verified: Abu Dhabi 2026, every prayer 100%.
    CalculationMethodOption.DUBAI -> CalculationMethod.DUBAI.parameters.copy(
        methodAdjustments = PrayerAdjustments(sunrise = -3, dhuhr = 3, asr = 1, maghrib = 3),
    )

    // Diyanet's calendars for Europe and the Americas: same temkin as Turkey, but Isha at 16°
    // (International Prayer Times Congress, Istanbul 2021). Above 44.5° Diyanet's own summer
    // rule applies (see usesDiyanetHighLatitudeRule); below it, middle of the night never binds.
    // Verified: New York, Texas City 2027, every prayer 100%; Berlin, Köln, Paris, Amsterdam,
    // Londonderry, Oslo: sunrise/Dhuhr/Asr/Maghrib 92-100%, Fajr/Isha 62-79% (Diyanet blends
    // its summer rule in over a few weeks; outside those weeks within 1-2 minutes).
    // Japan's Diyanet calendar still uses 17°: TURKEY matches it.
    CalculationMethodOption.DIYANET_ABROAD -> CalculationMethod.TURKEY.parameters.copy(
        ishaAngle = 16.0,
        highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
    )

    // Islamic Community in Bosnia and Herzegovina (vaktija.ba): 18°/16°, sunrise -6,
    // Dhuhr +1, Maghrib +6, Shafi'i Asr (fitted). Evening prayers from the previous day.
    // Verified: Sarajevo 2026, 92-100% (was 0% for sunrise, Maghrib and Isha).
    CalculationMethodOption.BOSNIA -> custom(
        fajrAngle = 18.0, ishaAngle = 16.0,
        adjustments = PrayerAdjustments(sunrise = -6, dhuhr = 1, maghrib = 6),
    )

    // Spiritual Administration of Muslims of Kazakhstan (muftyat.kz): 15°/15°, sunrise -3,
    // Dhuhr/Asr/Maghrib +3, Hanafi Asr. Verified: Almaty 2026, every prayer 100%.
    CalculationMethodOption.KAZAKHSTAN -> custom(
        fajrAngle = 15.0, ishaAngle = 15.0,
        adjustments = PrayerAdjustments(sunrise = -3, dhuhr = 3, asr = 3, maghrib = 3),
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
    // sunset (fitted). Verified against a secondary 2026 Tunis table: 93-100%.
    // Note: its inland tables also correct for town elevation, which Adhan cannot model.
    CalculationMethodOption.TUNISIA -> custom(
        fajrAngle = 18.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(dhuhr = 7, maghrib = 2),
    )

    // Malaysia JAKIM: Subuh 18° (National Fatwa Council, Nov 2019), Isyak 18°, Zohor +1.
    // e-Solat rounds prayer times up; its sunrise is computed for a different station than
    // the zone's prayers, which nets out at -2. Verified: zone WLY01 2026 at its reference
    // station, 93-100% (was 32% for sunrise).
    CalculationMethodOption.JAKIM -> custom(
        fajrAngle = 18.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(sunrise = -2, dhuhr = 1),
        rounding = Rounding.UP,
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

/**
 * Diyanet-style timetables (and Bosnia's) take the sun's position once per day instead of at
 * each prayer's own moment, so by the evening their solar data is most of a day old. Taking
 * Asr, Maghrib and Isha from the previous day's calculation reproduces them: Istanbul and
 * Ankara Asr/Maghrib/Isha go from 88-97% to 100% within a minute. Morning prayers are not
 * affected. Other authorities do NOT do this (Makkah and Cairo get worse with it).
 */
internal val CalculationMethodOption.eveningFromPreviousDay: Boolean
    get() = this == CalculationMethodOption.TURKEY ||
        this == CalculationMethodOption.DIYANET_ABROAD ||
        this == CalculationMethodOption.BOSNIA

/**
 * Diyanet's summer rule for its calendars abroad, from [DIYANET_HIGH_LATITUDE_MIN] degrees:
 * Fajr is never earlier than sunrise - 5/24 of the night, Isha never later than
 * sunset + 3/16 of the night (night = sunset to next sunrise). Fitted from Paris, Köln,
 * Amsterdam, Berlin, Londonderry and Oslo; without it summer Fajr/Isha were up to 2.5 h off.
 */
internal val CalculationMethodOption.usesDiyanetHighLatitudeRule: Boolean
    get() = this == CalculationMethodOption.DIYANET_ABROAD

internal const val DIYANET_HIGH_LATITUDE_MIN = 44.5
internal const val DIYANET_FAJR_NIGHT_FRACTION = 5.0 / 24.0
internal const val DIYANET_ISHA_NIGHT_FRACTION = 3.0 / 16.0

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

package com.ottrojja.screens.prayerTimesScreen.calculator

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerAdjustments
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption

/**
 * The only place app enums are translated into Adhan types.
 *
 * Methods Adhan ships as presets use them directly. The rest are built from their
 * sun angles (values as published by the Aladhan API). National ministries sometimes
 * add small precautionary offsets of their own, so expect a minute or two of difference
 * against some official tables.
 */
internal fun CalculationMethodOption.toParameters(): CalculationParameters = when (this) {
    // Adhan presets
    CalculationMethodOption.MUSLIM_WORLD_LEAGUE -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
    CalculationMethodOption.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters
    CalculationMethodOption.EGYPTIAN -> CalculationMethod.EGYPTIAN.parameters
    CalculationMethodOption.KARACHI -> CalculationMethod.KARACHI.parameters
    CalculationMethodOption.DUBAI -> CalculationMethod.DUBAI.parameters
    CalculationMethodOption.QATAR -> CalculationMethod.QATAR.parameters
    CalculationMethodOption.KUWAIT -> CalculationMethod.KUWAIT.parameters
    CalculationMethodOption.TURKEY -> CalculationMethod.TURKEY.parameters
    CalculationMethodOption.SINGAPORE -> CalculationMethod.SINGAPORE.parameters
    CalculationMethodOption.MOON_SIGHTING_COMMITTEE -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
    CalculationMethodOption.NORTH_AMERICA -> CalculationMethod.NORTH_AMERICA.parameters

    // Custom angle sets
    CalculationMethodOption.GULF -> custom(fajrAngle = 19.5, ishaInterval = 90)
    CalculationMethodOption.JORDAN -> custom(
        fajrAngle = 18.0, ishaAngle = 18.0,
        adjustments = PrayerAdjustments(maghrib = 5),
    )
    CalculationMethodOption.MOROCCO -> custom(fajrAngle = 19.0, ishaAngle = 17.0)
    CalculationMethodOption.ALGERIA -> custom(fajrAngle = 18.0, ishaAngle = 17.0)
    CalculationMethodOption.TUNISIA -> custom(fajrAngle = 18.0, ishaAngle = 18.0)
    CalculationMethodOption.JAKIM -> custom(fajrAngle = 20.0, ishaAngle = 18.0)
    CalculationMethodOption.KEMENAG -> custom(fajrAngle = 20.0, ishaAngle = 18.0)
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
) = CalculationParameters(
    fajrAngle = fajrAngle,
    ishaAngle = ishaAngle,
    ishaInterval = ishaInterval,
    method = CalculationMethod.OTHER,
    methodAdjustments = adjustments,
)

internal fun MadhabOption.toAdhan(): Madhab = when (this) {
    MadhabOption.JUMHUR -> Madhab.SHAFI
    MadhabOption.HANAFI -> Madhab.HANAFI
}

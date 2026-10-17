package com.ottrojja.screens.prayerTimesScreen.calculator

import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.ALGERIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.DUBAI
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.EGYPTIAN
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.GULF
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.JAKIM
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.JORDAN
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KARACHI
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KEMENAG
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KUWAIT
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.MOON_SIGHTING_COMMITTEE
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.MOROCCO
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.MUSLIM_WORLD_LEAGUE
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.PORTUGAL
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.QATAR
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.RUSSIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.SINGAPORE
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.TUNISIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.TURKEY
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.UMM_AL_QURA
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings

/**
 * Picks a sensible method from the country, so most users never open the setting.
 * The user's explicit choice in [PrayerSettings.method] always wins.
 *
 * These defaults are a starting point. Review them against what people in your
 * audience's countries actually expect before release. France deliberately stays on
 * the fallback: the UOIF 12° method is contested and many French mosques use 18°.
 */
object DefaultMethodResolver {

    private val byCountry: Map<String, CalculationMethodOption> = buildMap {
        put("SA", UMM_AL_QURA)
        put("YE", UMM_AL_QURA)
        put("AE", DUBAI)
        put("QA", QATAR)
        put("KW", KUWAIT)
        put("BH", GULF)
        put("OM", GULF)
        put("JO", JORDAN)
        listOf("EG", "SD", "LY", "SY", "IQ", "LB").forEach { put(it, EGYPTIAN) }
        put("MA", MOROCCO)
        put("DZ", ALGERIA)
        put("TN", TUNISIA)
        put("TR", TURKEY)
        listOf("PK", "IN", "BD", "AF").forEach { put(it, KARACHI) }
        put("MY", JAKIM)
        put("ID", KEMENAG)
        listOf("SG", "BN").forEach { put(it, SINGAPORE) }
        put("RU", RUSSIA)
        put("PT", PORTUGAL)
        listOf("US", "CA", "GB").forEach { put(it, MOON_SIGHTING_COMMITTEE) }
    }

    val fallback: CalculationMethodOption = MUSLIM_WORLD_LEAGUE

    fun defaultFor(countryCode: String?): CalculationMethodOption =
        countryCode?.uppercase()?.let(byCountry::get) ?: fallback

    /** The method actually used for a calculation: explicit choice, else country default. */
    fun effectiveMethod(settings: PrayerSettings): CalculationMethodOption =
        settings.method ?: defaultFor(settings.location?.countryCode)
}

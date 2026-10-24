package com.ottrojja.screens.prayerTimesScreen.calculator

import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.ALGERIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.BOSNIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.DUBAI
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.EGYPTIAN
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.GULF
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.JAKIM
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.JORDAN
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KAZAKHSTAN
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KARACHI
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KEMENAG
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.KUWAIT
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.MOON_SIGHTING_COMMITTEE
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.MOROCCO
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.MUSLIM_WORLD_LEAGUE
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.NORTH_AMERICA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.PORTUGAL
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.QATAR
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.RUSSIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.SINGAPORE
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.TUNISIA
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.TURKEY
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption.UMM_AL_QURA
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
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
        // Islamic Community in Bosnia and Herzegovina (vaktija.ba), verified against Sarajevo 2026.
        put("BA", BOSNIA)
        // Spiritual Administration of Muslims of Kazakhstan (muftyat.kz), verified against Almaty 2026.
        put("KZ", KAZAKHSTAN)
        listOf("PK", "IN", "BD", "AF").forEach { put(it, KARACHI) }
        put("MY", JAKIM)
        put("ID", KEMENAG)
        listOf("SG", "BN").forEach { put(it, SINGAPORE) }
        put("RU", RUSSIA)
        put("PT", PORTUGAL)
        // FCNA (2017) suggests 15° for Fajr and Isha in the USA, i.e. the ISNA setting.
        put("US", NORTH_AMERICA)
        // Canada (FCNA suggests 13°, no option for it yet) and the UK (no national
        // authority, mosques differ) stay on the Moonsighting Committee method.
        listOf("CA", "GB").forEach { put(it, MOON_SIGHTING_COMMITTEE) }
    }

    /**
     * Countries whose official timetables use the Hanafi (later) Asr: Banuri Town (PK),
     * Islamic Foundation Bangladesh, Afghanistan's ministry, Kazakhstan's muftiate (muftyat.kz).
     * India is predominantly Hanafi too.
     */
    private val hanafiCountries = setOf("PK", "IN", "BD", "AF", "KZ")

    val fallback: CalculationMethodOption = MUSLIM_WORLD_LEAGUE

    fun defaultFor(countryCode: String?): CalculationMethodOption =
        countryCode?.uppercase()?.let(byCountry::get) ?: fallback

    /** Madhab used until the user saves one explicitly. */
    fun defaultMadhabFor(countryCode: String?): MadhabOption =
        if (countryCode?.uppercase() in hanafiCountries) MadhabOption.HANAFI else MadhabOption.JUMHUR

    /** The method actually used for a calculation: explicit choice, else country default. */
    fun effectiveMethod(settings: PrayerSettings): CalculationMethodOption =
        settings.method ?: defaultFor(settings.location?.countryCode)
}

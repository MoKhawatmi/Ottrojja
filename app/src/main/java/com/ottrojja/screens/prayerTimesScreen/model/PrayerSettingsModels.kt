package com.ottrojja.screens.prayerTimesScreen.model

import androidx.annotation.StringRes
import com.ottrojja.R

/**
 * A location prayer times are calculated for, from GPS or picked manually.
 *
 * [timeZoneId] is an IANA id ("Europe/Berlin"). It decides which calendar day is "today"
 * for this location, which matters when the user picks a city in another time zone.
 * [countryCode] is ISO 3166-1 alpha-2 ("DE") and drives the default calculation method.
 */
data class PrayerLocation(
    val latitude: Double,
    val longitude: Double,
    val displayName: String,
    val countryCode: String?,
    val timeZoneId: String,
    val source: LocationSource = LocationSource.MANUAL,
)

/** Where the stored location came from. GPS locations are refreshed automatically. */
enum class LocationSource(val storageKey: String) {
    GPS("gps"),
    MANUAL("manual");

    companion object {
        fun fromStorageKey(key: String?): LocationSource =
            entries.firstOrNull { it.storageKey == key } ?: MANUAL
    }
}

/**
 * App-owned calculation methods. Mapped to Adhan's parameters in exactly one place
 * (AdhanMappers.kt) so DataStore and the UI never depend on the library.
 *
 * [storageKey] is what gets persisted. Never rename an existing key.
 * Display order in the picker follows declaration order.
 */
enum class CalculationMethodOption(val storageKey: String, @StringRes val nameRes: Int) {
    MUSLIM_WORLD_LEAGUE("mwl", R.string.method_muslim_world_league),
    UMM_AL_QURA("umm_al_qura", R.string.method_umm_al_qura),
    EGYPTIAN("egyptian", R.string.method_egyptian),
    KARACHI("karachi", R.string.method_karachi),
    GULF("gulf", R.string.method_gulf),
    DUBAI("dubai", R.string.method_dubai),
    QATAR("qatar", R.string.method_qatar),
    KUWAIT("kuwait", R.string.method_kuwait),
    JORDAN("jordan", R.string.method_jordan),
    TURKEY("turkey", R.string.method_turkey),
    MOROCCO("morocco", R.string.method_morocco),
    ALGERIA("algeria", R.string.method_algeria),
    TUNISIA("tunisia", R.string.method_tunisia),
    JAKIM("jakim", R.string.method_jakim),
    KEMENAG("kemenag", R.string.method_kemenag),
    SINGAPORE("singapore", R.string.method_singapore),
    RUSSIA("russia", R.string.method_russia),
    FRANCE("france_uoif", R.string.method_france),
    PORTUGAL("portugal", R.string.method_portugal),
    MOON_SIGHTING_COMMITTEE("moonsighting", R.string.method_moonsighting_committee),
    NORTH_AMERICA("isna", R.string.method_north_america);

    companion object {
        fun fromStorageKey(key: String?): CalculationMethodOption? =
            entries.firstOrNull { it.storageKey == key }
    }
}

/**
 * Madhab only changes Asr. Shafi'i, Maliki and Hanbali share the earlier Asr,
 * which is why the first option is labelled "Jumhur" (the majority) in the UI.
 */
enum class MadhabOption(val storageKey: String, @StringRes val nameRes: Int) {
    JUMHUR("jumhur", R.string.madhab_jumhur),
    HANAFI("hanafi", R.string.madhab_hanafi);

    companion object {
        fun fromStorageKey(key: String?): MadhabOption =
            entries.firstOrNull { it.storageKey == key } ?: JUMHUR
    }
}

/**
 * How a prayer's time is announced. Stored now so the alarm step needs no migration;
 * the UI for it arrives with the alarm step.
 */
enum class PrayerNotificationMode(val storageKey: String) {
    OFF("off"),
    NOTIFICATION("notification"),
    ADHAN("adhan");

    companion object {
        val DEFAULT = OFF

        fun fromStorageKey(key: String?): PrayerNotificationMode =
            entries.firstOrNull { it.storageKey == key } ?: DEFAULT
    }
}

/**
 * Everything the calculator needs besides the date.
 *
 * [method] == null means "automatic": resolved from the location's country.
 * Notification preferences are added later with the alarm step.
 */
data class PrayerSettings(
    val location: PrayerLocation?,
    val method: CalculationMethodOption? = null,
    val madhab: MadhabOption = MadhabOption.JUMHUR,
)

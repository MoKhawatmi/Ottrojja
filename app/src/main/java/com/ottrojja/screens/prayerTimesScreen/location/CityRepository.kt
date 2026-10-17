package com.ottrojja.screens.prayerTimesScreen.location

import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.Reader
import java.text.Normalizer
import java.time.ZoneId
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A city from the bundled GeoNames dataset (cities with 15,000+ inhabitants).
 * [arabicName] is empty when GeoNames has no Arabic name; [displayName] then uses [name].
 */
data class City(
    val id: Int,
    val name: String,
    val arabicName: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val population: Long,
    /** Every known spelling, normalized with [normalizeForSearch]; only used for search. */
    internal val searchTerms: List<String>,
) {
    val displayName: String get() = arabicName.ifBlank { name }

    /** The Latin name, shown under the Arabic one so users can tell similar cities apart. */
    val secondaryName: String? get() = name.takeIf { arabicName.isNotBlank() && it != arabicName }

    /** "برلين، ألمانيا" */
    val label: String
        get() {
            val country = countryNameArabic(countryCode)
            return if (country.isBlank()) displayName else "$displayName، $country"
        }

    companion object {
        private val arabicLocale = Locale("ar")

        /** Names used instead of the platform's locale data. */
        private val countryNameOverrides = mapOf("PS" to "فلسطين")

        fun countryNameArabic(countryCode: String): String = when {
            countryCode.isBlank() -> ""
            else -> countryNameOverrides[countryCode.uppercase(Locale.ROOT)]
                ?: Locale("", countryCode).getDisplayCountry(arabicLocale)
        }
    }
}

/**
 * Everything about turning "a place" into a saved [PrayerLocation]:
 * - offline city search and nearest-city lookup over assets/prayer_cities.dat
 *   (built by tools/build_cities_asset.py from GeoNames),
 * - [locationFor] a picked city, or [locationFor] GPS coordinates.
 *
 * The file is parsed once, on first use, off the main thread. About 26k cities are kept in
 * memory afterwards; a linear scan takes a few milliseconds, so no database is needed.
 *
 * @param reverseGeocode online fallback name lookup (the Qibla screen's resolveCity), only
 *   used for GPS fixes with no bundled city nearby.
 */
class CityRepository(
    private val openGzippedAsset: () -> InputStream,
    private val reverseGeocode: suspend (latitude: Double, longitude: Double) -> String?,
    private val deviceTimeZoneId: () -> String = { ZoneId.systemDefault().id },
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    private val mutex = Mutex()

    @Volatile
    private var cities: List<City>? = null

    private suspend fun all(): List<City> = cities ?: mutex.withLock {
        cities ?: withContext(Dispatchers.IO) {
            GZIPInputStream(openGzippedAsset()).bufferedReader(Charsets.UTF_8).use(::parseCities)
        }.sortedByDescending { it.population }.also { cities = it }
    }

    /*******************SEARCH*********************/

    /**
     * Cities matching [query], best first: exact name, then names starting with the query
     * (also after a leading "ال"), then names containing it; ties go to the larger city.
     * With a blank query: the largest cities in [countryHint], or worldwide.
     */
    suspend fun search(query: String, countryHint: String? = null, limit: Int = DEFAULT_LIMIT): List<City> {
        val all = all()
        return withContext(dispatcher) {
            val normalized = normalizeForSearch(query)
            if (normalized.isEmpty()) {
                val inCountry = countryHint?.let { cc -> all.filter { it.countryCode.equals(cc, ignoreCase = true) } }
                return@withContext (inCountry?.takeIf { it.isNotEmpty() } ?: all).take(limit)
            }
            // `all` is sorted by population and sortedBy is stable, so equal ranks keep that order.
            all.asSequence()
                .mapNotNull { city -> matchRank(city, normalized)?.let { city to it } }
                .sortedBy { it.second }
                .take(limit)
                .map { it.first }
                .toList()
        }
    }

    suspend fun nearest(latitude: Double, longitude: Double): NearestCity? {
        val all = all()
        return withContext(dispatcher) {
            all.minByOrNull { distanceKm(latitude, longitude, it.latitude, it.longitude) }
                ?.let { NearestCity(it, distanceKm(latitude, longitude, it.latitude, it.longitude)) }
        }
    }

    data class NearestCity(val city: City, val distanceKm: Double)

    /*******************TO PRAYER LOCATION*********************/

    fun locationFor(city: City): PrayerLocation = PrayerLocation(
        latitude = city.latitude,
        longitude = city.longitude,
        displayName = city.label,
        countryCode = city.countryCode,
        timeZoneId = validZoneOr(city.timeZoneId, deviceTimeZoneId()),
        source = LocationSource.MANUAL,
    )

    /**
     * A GPS fix. The nearest bundled city supplies the Arabic name (within [NEAR_CITY_KM])
     * and the country and time zone (within [SAME_REGION_KM]), all offline. Further away,
     * the online geocoder names the place and the device time zone is used.
     */
    suspend fun locationFor(latitude: Double, longitude: Double): PrayerLocation {
        val nearest = nearest(latitude, longitude)
        val near = nearest?.takeIf { it.distanceKm <= NEAR_CITY_KM }
        val sameRegion = nearest?.takeIf { it.distanceKm <= SAME_REGION_KM }

        val displayName = near?.city?.label
            ?: reverseGeocode(latitude, longitude)
            ?: sameRegion?.city?.label
            ?: String.format(Locale.US, "%.3f, %.3f", latitude, longitude)

        return PrayerLocation(
            latitude = latitude,
            longitude = longitude,
            displayName = displayName,
            countryCode = sameRegion?.city?.countryCode,
            timeZoneId = sameRegion?.let { validZoneOr(it.city.timeZoneId, deviceTimeZoneId()) }
                ?: deviceTimeZoneId(),
            source = LocationSource.GPS,
        )
    }

    companion object {
        const val DEFAULT_LIMIT = 50
        const val NEAR_CITY_KM = 30.0
        const val SAME_REGION_KM = 100.0
        private const val EARTH_RADIUS_KM = 6371.0

        private const val RANK_EXACT = 0
        private const val RANK_PREFIX = 1
        private const val RANK_CONTAINS = 2

        private val arabicDiacritics = Regex("[\\u064B-\\u0652\\u0670\\u0640]")
        private val combiningMarks = Regex("\\p{Mn}+")
        private val whitespace = Regex("\\s+")

        /**
         * Loose matching form: Arabic diacritics and tatweel removed, أ إ آ ٱ → ا, ة → ه,
         * ى → ي, ؤ → و, ئ → ي; Latin lowercased without accents ("Fès" → "fes").
         */
        internal fun normalizeForSearch(text: String): String {
            val unified = buildString(text.length) {
                for (ch in arabicDiacritics.replace(text, "")) {
                    append(
                        when (ch) {
                            'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                            'ة' -> 'ه'
                            'ى' -> 'ي'
                            'ؤ' -> 'و'
                            'ئ' -> 'ي'
                            else -> ch
                        }
                    )
                }
            }
            val withoutAccents = combiningMarks.replace(Normalizer.normalize(unified, Normalizer.Form.NFD), "")
            return whitespace.replace(withoutAccents.lowercase(Locale.ROOT), " ").trim()
        }

        private fun matchRank(city: City, query: String): Int? {
            var best: Int? = null
            for (term in city.searchTerms) {
                val bare = if (term.startsWith("ال")) term.substring(2) else term
                val rank = when {
                    term == query || bare == query -> RANK_EXACT
                    term.startsWith(query) || bare.startsWith(query) -> RANK_PREFIX
                    term.contains(query) -> RANK_CONTAINS
                    else -> continue
                }
                if (rank == RANK_EXACT) return rank
                if (best == null || rank < best) best = rank
            }
            return best
        }

        internal fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val p1 = Math.toRadians(lat1)
            val p2 = Math.toRadians(lat2)
            val h = sin((p2 - p1) / 2).pow(2) +
                cos(p1) * cos(p2) * sin(Math.toRadians(lon2 - lon1) / 2).pow(2)
            return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
        }

        /** GeoNames zone ids can be newer than the tz data on old devices. */
        internal fun validZoneOr(zoneId: String, fallback: String): String =
            runCatching { ZoneId.of(zoneId).id }.getOrDefault(fallback)

        /**
         * Parses the asset: id, name, arabic_name, extra_terms (| separated), country,
         * lat, lon, time_zone, population. Malformed lines are skipped.
         */
        internal fun parseCities(reader: Reader): List<City> {
            val result = ArrayList<City>(27_000)
            reader.forEachLine { line ->
                val c = line.split('\t')
                if (c.size < 9) return@forEachLine
                val id = c[0].toIntOrNull() ?: return@forEachLine
                val latitude = c[5].toDoubleOrNull() ?: return@forEachLine
                val longitude = c[6].toDoubleOrNull() ?: return@forEachLine
                val terms = buildList {
                    add(c[1])
                    if (c[2].isNotBlank()) add(c[2])
                    if (c[3].isNotBlank()) addAll(c[3].split('|'))
                }.map(::normalizeForSearch).filter { it.isNotEmpty() }.distinct()

                result += City(
                    id = id,
                    name = c[1],
                    arabicName = c[2],
                    countryCode = c[4],
                    latitude = latitude,
                    longitude = longitude,
                    timeZoneId = c[7],
                    population = c[8].toLongOrNull() ?: 0L,
                    searchTerms = terms,
                )
            }
            return result
        }
    }
}

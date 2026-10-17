package com.ottrojja.screens.prayerTimesScreen.location

import com.ottrojja.screens.prayerTimesScreen.location.CityRepository.Companion.normalizeForSearch
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.util.zip.GZIPOutputStream

class CityRepositoryTest {

    // Same format as assets/prayer_cities.dat.
    private val tsv = listOf(
        row(108410, "Riyadh", "الرياض", "Ar Riyad|رياض", "SA", 24.68773, 46.72185, "Asia/Riyadh", 4205961),
        row(104515, "Mecca", "مكة", "Makkah|مكة المكرمة", "SA", 21.42664, 39.82563, "Asia/Riyadh", 1578722),
        row(360630, "Cairo", "القاهرة", "Al Qahirah|قاهره", "EG", 30.06263, 31.24967, "Africa/Cairo", 9606916),
        row(250441, "Amman", "عمان", "عَمّان", "JO", 31.95522, 35.94503, "Asia/Amman", 1275857),
        row(2950159, "Berlin", "برلين", "", "DE", 52.52437, 13.41053, "Europe/Berlin", 3426354),
        row(5110302, "New Berlin", "", "", "US", 42.97640, -88.10842, "America/Chicago", 39834),
        row(281184, "Jerusalem", "القدس", "", "PS", 31.76904, 35.21633, "Asia/Jerusalem", 971800),
        row(999, "Nowhere", "", "", "XX", 0.0, 0.0, "Not/AZone", 20000),
        "malformed line without enough columns",
    ).joinToString("\n")

    private fun row(vararg v: Any) = v.joinToString("\t")

    private var geocoderCalls = 0

    private fun repository(geocoderResult: String? = "Geocoded place") = CityRepository(
        openGzippedAsset = {
            val bytes = ByteArrayOutputStream()
            GZIPOutputStream(bytes).bufferedWriter(Charsets.UTF_8).use { it.write(tsv) }
            ByteArrayInputStream(bytes.toByteArray())
        },
        reverseGeocode = { _, _ -> geocoderCalls++; geocoderResult },
        deviceTimeZoneId = { "Europe/Berlin" },
    )

    /*******************NORMALIZATION*********************/

    @Test
    fun normalize_unifiesArabicVariants_andStripsMarks() {
        assertEquals(normalizeForSearch("القاهرة"), normalizeForSearch("القاهره"))
        assertEquals(normalizeForSearch("دبي"), normalizeForSearch("دبى"))
        assertEquals(normalizeForSearch("إربد"), normalizeForSearch("اربد"))
        assertEquals("عمان", normalizeForSearch("عَمّان"))
        assertEquals("مكه", normalizeForSearch("مكـــة"))
        assertEquals("fes", normalizeForSearch("Fès"))
        assertEquals("sao paulo", normalizeForSearch("  São   Paulo "))
    }

    /*******************SEARCH*********************/

    @Test
    fun parse_skipsMalformedLines() {
        assertEquals(8, CityRepository.parseCities(StringReader(tsv)).size)
    }

    @Test
    fun search_matchesWithoutArticleAndLetterVariants() = runTest {
        val repo = repository()
        assertEquals("Riyadh", repo.search("رياض").first().name)
        assertEquals("Cairo", repo.search("القاهره").first().name)
        assertEquals("Mecca", repo.search("مكه").first().name)
        assertEquals("Mecca", repo.search("makkah").first().name)
    }

    @Test
    fun search_ranksExactBeforeContains() = runTest {
        assertEquals(listOf("Berlin", "New Berlin"), repository().search("berlin").map { it.name })
    }

    @Test
    fun blankQuery_returnsLargestCitiesInHintCountry_orWorldwide() = runTest {
        val repo = repository()
        assertEquals(listOf("Riyadh", "Mecca"), repo.search("", countryHint = "SA").map { it.name })
        assertEquals("Cairo", repo.search("").first().name)
        assertEquals("Cairo", repo.search("", countryHint = "ZZ").first().name)
    }

    @Test
    fun nearest_findsClosestCity() = runTest {
        val nearest = repository().nearest(32.0, 35.9)!!
        assertEquals("Amman", nearest.city.name)
        assertTrue("was ${nearest.distanceKm}", nearest.distanceKm < 10)
    }

    @Test
    fun label_usesArabicNameAndCountry_orLatinFallback() {
        val cities = CityRepository.parseCities(StringReader(tsv)).associateBy { it.name }
        assertEquals("برلين، ألمانيا", cities.getValue("Berlin").label)
        assertEquals("القدس، فلسطين", cities.getValue("Jerusalem").label)
        assertEquals("Berlin", cities.getValue("Berlin").secondaryName)
        assertEquals("New Berlin", cities.getValue("New Berlin").displayName)
        assertNull(cities.getValue("New Berlin").secondaryName)
    }

    /*******************TO PRAYER LOCATION*********************/

    @Test
    fun gpsNearCity_usesCityData_withoutGeocoder() = runTest {
        val location = repository().locationFor(31.97, 35.90)

        assertEquals("عمان، الأردن", location.displayName)
        assertEquals("JO", location.countryCode)
        assertEquals("Asia/Amman", location.timeZoneId)
        assertEquals(LocationSource.GPS, location.source)
        assertEquals(31.97, location.latitude, 0.0)
        assertEquals(0, geocoderCalls)
    }

    @Test
    fun gpsFarFromAnyCity_usesGeocoder_deviceZone_noCountry() = runTest {
        // Middle of the Mediterranean, > 100 km from every test city.
        val location = repository().locationFor(35.0, 20.0)

        assertEquals("Geocoded place", location.displayName)
        assertNull(location.countryCode)
        assertEquals("Europe/Berlin", location.timeZoneId)
        assertEquals(1, geocoderCalls)
    }

    @Test
    fun gpsFarFromAnyCity_withoutGeocoder_fallsBackToCoordinates() = runTest {
        assertEquals("35.000, 20.000", repository(geocoderResult = null).locationFor(35.0, 20.0).displayName)
    }

    @Test
    fun manualCity_withUnknownZone_fallsBackToDeviceZone() = runTest {
        val repo = repository()
        val location = repo.locationFor(repo.search("nowhere").single())

        assertEquals("Europe/Berlin", location.timeZoneId)
        assertEquals(LocationSource.MANUAL, location.source)
    }
}

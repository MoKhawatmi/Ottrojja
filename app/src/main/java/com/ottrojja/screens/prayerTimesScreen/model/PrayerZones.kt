package com.ottrojja.screens.prayerTimesScreen.model

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The one way the prayer feature turns a stored IANA id into a [ZoneId].
 *
 * java.time reads the time zone rules installed on the device, and many phones never get
 * tzdata updates. This class patches known rule changes those phones may be missing.
 *
 * Morocco: abolished GMT+1 and returned permanently to GMT at 02:00 local on
 * 20 September 2026 (IANA tzdata 2026c, Africa/Casablanca and Africa/El_Aaiun). With
 * older tzdata, java.time still says UTC+1, so every Moroccan time shows an hour late.
 * If the device's rules disagree with the new law, plain UTC is used instead.
 *
 * Only correct for dates from 20 Sep 2026 on, which is all the prayer screen and the
 * alarms ever compute.
 */
object PrayerZones {

    private val moroccoZoneIds = setOf("Africa/Casablanca", "Africa/El_Aaiun")

    /** Any instant after the switch, to test what the device's rules say. */
    private val moroccoProbe: Instant = Instant.parse("2026-10-01T12:00:00Z")

    /** Like ZoneId.of, but never throws: unknown ids fall back to the device zone. */
    fun of(id: String): ZoneId {
        val zone = runCatching { ZoneId.of(id) }.getOrElse { return ZoneId.systemDefault() }
        if (zone.id in moroccoZoneIds && zone.rules.getOffset(moroccoProbe) != ZoneOffset.UTC) {
            return ZoneOffset.UTC
        }
        return zone
    }
}

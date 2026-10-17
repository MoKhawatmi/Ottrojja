package com.ottrojja.screens.prayerTimesScreen.model

import androidx.annotation.StringRes
import com.ottrojja.R

/**
 * Every time shown in the prayer times list, in display order.
 *
 * Sunrise and Duha are informational: they are not obligatory prayers, so they never
 * count as the "next prayer" and are not notifiable.
 */
enum class PrayerType(
    @StringRes val nameRes: Int,
    val isObligatory: Boolean,
    val isNotifiable: Boolean,
) {
    FAJR(R.string.prayer_fajr, isObligatory = true, isNotifiable = true),
    SUNRISE(R.string.prayer_sunrise, isObligatory = false, isNotifiable = false),
    DUHA(R.string.prayer_duha, isObligatory = false, isNotifiable = false),
    DHUHR(R.string.prayer_dhuhr, isObligatory = true, isNotifiable = true),
    ASR(R.string.prayer_asr, isObligatory = true, isNotifiable = true),
    MAGHRIB(R.string.prayer_maghrib, isObligatory = true, isNotifiable = true),
    ISHA(R.string.prayer_isha, isObligatory = true, isNotifiable = true);

    /** On Fridays Dhuhr is shown as Jumu'ah. */
    @StringRes
    fun displayNameRes(isFriday: Boolean): Int =
        if (this == DHUHR && isFriday) R.string.prayer_jumuah else nameRes
}

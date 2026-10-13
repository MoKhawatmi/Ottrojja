package com.ottrojja.classes

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale


object OttrojjaDateTime {

    private val arabicLocale = Locale("ar")

    private const val DATE_PATTERN = "yyyy-MM-dd"
    private const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
    private const val TIME_PATTERN = "HH:mm"

    private val decimalStyle =
        DecimalStyle.of(arabicLocale)

    private val dateFormatter =
        DateTimeFormatter.ofPattern(
            DATE_PATTERN,
            arabicLocale
        ).withDecimalStyle(decimalStyle)

    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern(
            DATE_TIME_PATTERN,
            arabicLocale
        ).withDecimalStyle(decimalStyle)

    private val timeFormatter =
        DateTimeFormatter.ofPattern(
            TIME_PATTERN,
            arabicLocale
        ).withDecimalStyle(decimalStyle)

    private val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    fun formatDate(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .format(dateFormatter)

    fun formatDateTime(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .format(dateTimeFormatter)

    fun formatTime(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .format(timeFormatter)

    fun formatLocalDate(date: LocalDate): String =
        date.format(dateFormatter)

    fun formatLocalDateTime(
        dateTime: java.time.LocalDateTime
    ): String =
        dateTime.format(dateTimeFormatter)

    fun formatMilitaryTime(
        hour: Int,
        minute: Int
    ): String =
        LocalTime.of(hour, minute)
            .format(timeFormatter)

    fun formatDuration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0) / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = ((totalSeconds / 60) % 60).toInt()
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format(
                arabicLocale,
                "%d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        } else {
            String.format(
                arabicLocale,
                "%02d:%02d",
                totalSeconds / 60,
                seconds
            )
        }
    }
}
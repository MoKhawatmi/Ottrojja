package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.screens.Khitmah.classes.DayReadingInfo
import com.ottrojja.ui.theme.OttrojjaTheme
import io.github.boguszpawlowski.composecalendar.StaticCalendar
import io.github.boguszpawlowski.composecalendar.header.MonthState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun KhitmahCalendar(
    sessions: List<KhitmahSession>,
    modifier: Modifier = Modifier,
    sessionCount: Int,
    onMonthChanged: (YearMonth) -> Unit,
) {


    val readingDates = remember(sessions) {
        sessions
            .mapNotNull { session ->
                runCatching {
                    LocalDate.parse(session.readingDate)
                }.getOrNull()
            }
            .toSet()
    }

    val dayReadingInfo = remember(sessions) {
        sessions
            .mapNotNull { session ->
                runCatching {
                    LocalDate.parse(session.readingDate) to session
                }.getOrNull()
            }
            .groupBy { it.first }
            .mapValues { (_, entries) ->
                val daySessions = entries
                    .map { it.second }
                    .sortedBy { it.startPage }

                DayReadingInfo(
                    sessionCount = daySessions.size,
                    startPage = daySessions.first().startPage,
                    endPage = daySessions.last().endPage
                )
            }
    }

    Box(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
    ) {
        StaticCalendar(
            firstDayOfWeek = DayOfWeek.MONDAY,
            dayContent = { dayState ->

                KhitmahCalendarDay(
                    date = dayState.date,
                    isCurrentMonth = dayState.isFromCurrentMonth,
                    hasReading = dayState.date in readingDates,
                    onClick = {},
                    readingInfo = dayReadingInfo[dayState.date]
                )
            },
            monthHeader = { monthState ->
                LaunchedEffect(monthState.currentMonth) {
                    onMonthChanged(monthState.currentMonth)
                }

                KhitmahMonthHeader(
                    monthState = monthState,
                    sessionCount = sessionCount
                )
            }
        )
    }
}

@Composable
private fun KhitmahMonthHeader(
    monthState: MonthState,
    sessionCount: Int,
    modifier: Modifier = Modifier,
) {
    val arabicLocale = Locale("ar")

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                enabled = monthState.currentMonth > monthState.minMonth,
                onClick = {
                    monthState.currentMonth =
                        monthState.currentMonth.minusMonths(1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Previous month",
                )
            }

            OttrojjaText(
                text = monthState.currentMonth.month
                    .getDisplayName(
                        TextStyle.FULL,
                        arabicLocale
                    )
                    .lowercase()
                    .replaceFirstChar {
                        it.titlecase()
                    },
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            OttrojjaText(
                text = monthState.currentMonth.year.toString(),
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                enabled = monthState.currentMonth < monthState.maxMonth,
                onClick = {
                    monthState.currentMonth =
                        monthState.currentMonth.plusMonths(1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Next month",
                )
            }
        }

        OttrojjaText(
            text = "$sessionCount جلسات",
            style = OttrojjaTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

    }
}
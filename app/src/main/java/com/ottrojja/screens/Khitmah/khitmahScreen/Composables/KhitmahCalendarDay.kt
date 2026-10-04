package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.screens.Khitmah.classes.DayReadingInfo
import com.ottrojja.ui.theme.OttrojjaTheme
import java.time.LocalDate

@Composable
fun KhitmahCalendarDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    hasReading: Boolean,
    readingInfo: DayReadingInfo?,
    onClick: () -> Unit,
    isCreationDate: Boolean,
) {
    val isToday = date == LocalDate.now()

    var showPopup by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .onGloballyPositioned { anchorBounds = it }
            .clickable(
                enabled = isCurrentMonth,
                onClick = {
                    onClick()
                    showPopup = true
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showPopup && anchorBounds != null) {
            Popup(
                popupPositionProvider = AnchorCenterAbovePositionProvider(anchorBounds!!),
                onDismissRequest = { showPopup = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    KhitmahDayPopupContent(readingInfo = readingInfo)
                }
            }
        }

        if (isToday) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        if (hasReading) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }

        // Creation date indicator
        if (isCreationDate) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .rotate(45f)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(1.dp)
                    )
                    .align(Alignment.BottomCenter)
            )
        }


        OttrojjaText(
            text = Helpers.convertToIndianNumbers(date.dayOfMonth.toString()),
            color = if (hasReading) {
                MaterialTheme.colorScheme.onPrimary
            } else if (isCurrentMonth) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.35f
                )
            },
            style = OttrojjaTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun KhitmahDayPopupContent(
    readingInfo: DayReadingInfo?
) {
    if (readingInfo == null) {
        OttrojjaText(
            text = "لم يتم تسجيل قراءة",
            style = OttrojjaTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val totalPagesRead = readingInfo.sessions.sumOf {
        it.endPage - it.startPage + 1
    }

    val maxVisibleRanges = 4
    val visibleSessions = readingInfo.sessions.take(maxVisibleRanges)
    val remainingSessions = readingInfo.sessions.size - visibleSessions.size

    Column(
        modifier = Modifier
            .widthIn(min = 180.dp, max = 260.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PopupStatistic(
                value = readingInfo.sessionCount.toString(),
                label = "جلسات"
            )

            PopupStatistic(
                value = totalPagesRead.toString(),
                label = "صفحات"
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Reading ranges
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OttrojjaText(
                text = "الصفحات المقروءة",
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            visibleSessions.forEach { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OttrojjaText(
                        text = "${session.startPage} – ${session.endPage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OttrojjaText(
                        text = "${session.endPage - session.startPage + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (remainingSessions > 0) {
                OttrojjaText(
                    text = "+ $remainingSessions أخرى",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun RowScope.PopupStatistic(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        OttrojjaText(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OttrojjaText(
            text = label,
            style = OttrojjaTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

class AnchorCenterAbovePositionProvider(
    private val anchorCoordinates: LayoutCoordinates
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchor = anchorCoordinates.boundsInWindow()
        val x = (anchor.left + anchor.width / 2 - popupContentSize.width / 2)
            .toInt()
            .coerceIn(0, windowSize.width - popupContentSize.width)
        val y = (anchor.top - popupContentSize.height - 8).toInt().coerceAtLeast(0)
        return IntOffset(x, y)
    }
}
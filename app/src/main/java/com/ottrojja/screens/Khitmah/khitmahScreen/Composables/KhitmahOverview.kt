package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers.arabicDateFormatter
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.screens.Khitmah.classes.KhitmahStatus
import com.ottrojja.ui.theme.OttrojjaTheme
import com.ottrojja.ui.theme.complete_container
import com.ottrojja.ui.theme.complete_content
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun KhitmahOverview(
    modifier: Modifier = Modifier,
    khitmah: Khitmah,
    sessions: List<KhitmahSession>,
    totalPagesRead: Int,
    continueKhitmah: () -> Unit
) {

    val totalPages = 604;

    val progress = (totalPagesRead.toFloat() / totalPages)
        .coerceIn(0f, 1f)

    val percentage = (progress * 100).roundToInt()

    /*
     * Reading days
     *
     * Distinct dates on which at least one session was recorded.
     */
    val readingDays = sessions
        .mapNotNull { session ->
            session.readingDate
        }
        .distinct()
        .size

    /*
     * Total duration
     *
     * We use the first and last reading dates rather than the
     * number of sessions.
     *
     * Example:
     * Start: June 1
     * Finish: July 30
     * => 60 days
     */
    val firstReadingDate = sessions
        .mapNotNull { it.readingDate.let(LocalDate::parse) }
        .minOrNull()

    val lastReadingDate = sessions
        .mapNotNull { it.readingDate.let(LocalDate::parse) }
        .maxOrNull()

    val durationDays = if (
        firstReadingDate != null &&
        lastReadingDate != null
    ) {
        ChronoUnit.DAYS.between(
            firstReadingDate,
            lastReadingDate
        ).toInt() + 1
    } else {
        0
    }

    /*
     * Average pages per reading day.
     *
     * This is intentionally based on actual reading days,
     * not calendar duration.
     *
     * Example:
     * 604 pages / 37 reading days = 16.3 pages/day
     */
    val averagePagesPerDay = if (readingDays > 0) {
        totalPagesRead.toFloat() / readingDays
    } else {
        0f
    }

    /*
     * Longest session.
     *
     * Each session already represents a range, so we can
     * calculate its page count from start/end page.
     */
    val longestSession = sessions
        .maxOfOrNull { session ->
            (session.endPage - session.startPage + 1)
                .coerceAtLeast(0)
        }
        ?: 0

    val targetDate = if (khitmah.targetDate == null || khitmah.targetDate.isBlank()) {
        null
    } else {
        LocalDate.parse(khitmah.targetDate)
    }


    val firstReadingDateFormatted =
        firstReadingDate?.format(arabicDateFormatter)

    val lastReadingDateFormatted =
        lastReadingDate?.format(arabicDateFormatter)


    OttrojjaDetailsContainer(modifier = modifier.padding(8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------------------------------------------------------
            // Header
            // ---------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OttrojjaText(
                    text = "نظرة عامة",
                    style = OttrojjaTheme.typography.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                KhitmahStatusBadge(status = khitmah.status)
            }
            // ---------------------------------------------------------
            // Progress card
            // ---------------------------------------------------------

            KhitmahOverviewProgress(
                totalPagesRead = totalPagesRead,
                totalPages = totalPages,
                progress = progress,
                percentage = percentage
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OttrojjaButton(
                    text = "متابعة",
                    onClick = { continueKhitmah() },
                    iconAfter = Icons.Filled.PlayArrow
                )
            }

            // ---------------------------------------------------------
            // Statistics
            // ---------------------------------------------------------

            OttrojjaText(
                text = "إحصائيات الختمة",
                style = OttrojjaTheme.typography.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {

                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CalendarMonth,
                    value = durationDays.toString(),
                    unit = "يوماً",
                    label = "مدة الختمة"
                )

                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EventAvailable,
                    value = readingDays.toString(),
                    unit = "",
                    label = "أيام القراءة"
                )


                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    value = "$totalPagesRead",
                    unit = "صفحة",
                    label = "عدد الصفحات"
                )

                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    value = averagePagesPerDay
                        .roundToInt()
                        .toString(),
                    unit = "ص/يوم",
                    label = "متوسط القراءة"
                )

                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EventNote,
                    value = "${sessions.size}",
                    unit = "جلسة",
                    label = "عدد الجلسات"
                )

                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LocalFireDepartment,
                    value = longestSession.toString(),
                    unit = "صفحة",
                    label = "أطول جلسة"
                )

                KhitmahStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Flag,
                    value = "$firstReadingDateFormatted",
                    label = "تاريخ البدء"
                )

                if (khitmah.status == KhitmahStatus.COMPLETE) {
                    KhitmahStatItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CheckCircle,
                        value = "$lastReadingDateFormatted",
                        label = "تاريخ الإكمال"
                    )
                }
            }

            // ---------------------------------------------------------
            // Target
            // ---------------------------------------------------------

            if (targetDate != null) {
                KhitmahTargetCard(
                    targetDate = targetDate,
                    remainingPages = (totalPages - totalPagesRead)
                        .coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun KhitmahStatusBadge(
    status: KhitmahStatus,
    modifier: Modifier = Modifier
) {
    val isComplete = status == KhitmahStatus.COMPLETE
    val containerColor =
        if (isComplete) {
            complete_container
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    val contentColor =
        if (isComplete) {
            complete_content
        } else {
            MaterialTheme.colorScheme.primary
        }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(imageVector = if (isComplete) {
            Icons.Outlined.CheckCircle
        } else {
            Icons.AutoMirrored.Outlined.TrendingUp
        },
            contentDescription = null, tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        OttrojjaText(
            text = if (isComplete) "مكتملة" else "جارية",
            style = OttrojjaTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold, color = contentColor
        )
    }
}

@Composable
private fun KhitmahOverviewProgress(
    totalPagesRead: Int,
    totalPages: Int,
    progress: Float,
    percentage: Int,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.size(210.dp),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp,
                trackColor = MaterialTheme
                    .colorScheme
                    .surfaceVariant,
                color = if (percentage >= 100) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OttrojjaText(
                    text = "$percentage%",
                    style = OttrojjaTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OttrojjaText(
                    text = "مكتمل",
                    style = OttrojjaTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OttrojjaText(
            text = "$totalPagesRead صفحة من $totalPages",
            style = OttrojjaTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

    }
}

@Composable
private fun KhitmahStatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    unit: String? = null,
    label: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {

            OttrojjaText(
                text = value,
                style = OttrojjaTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            unit?.let {
                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                OttrojjaText(
                    text = it,
                    style = OttrojjaTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        OttrojjaText(
            text = label,
            style = OttrojjaTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun KhitmahTargetCard(
    targetDate: LocalDate,
    remainingPages: Int,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    val remainingDays = ChronoUnit.DAYS
        .between(today, targetDate)
        .toInt() + 1

    val recommendedPagesPerDay = if (remainingDays > 0) {
        ceil(
            remainingPages.toDouble() /
                    remainingDays
        ).toInt()
    } else {
        remainingPages
    }

    val targetDateFormatted =
        targetDate.format(arabicDateFormatter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                OttrojjaText(
                    text = "الموعد المستهدف",
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                OttrojjaText(
                    text = "$targetDateFormatted",
                    style = OttrojjaTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column {

                OttrojjaText(
                    text = "المعدل المقترح",
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                OttrojjaText(
                    text = "$recommendedPagesPerDay صفحة يومياً",
                    style = OttrojjaTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

}
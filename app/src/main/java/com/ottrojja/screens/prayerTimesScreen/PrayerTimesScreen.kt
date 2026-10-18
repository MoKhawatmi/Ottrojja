package com.ottrojja.screens.prayerTimesScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.screens.prayerTimesScreen.location.PrayerLocationPickerHost
import com.ottrojja.screens.prayerTimesScreen.model.DayPrayerTimes
import com.ottrojja.screens.prayerTimesScreen.model.PrayerLocation
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerTime
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerAdhanDebugPanel
import com.ottrojja.screens.prayerTimesScreen.settings.PrayerTimesSettingsModal
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/** Pages around today: a year back, a year ahead. */
private const val PAGE_COUNT = 731
private const val TODAY_PAGE = 365

@Composable
fun PrayerTimesScreen(viewModel: PrayerTimesViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val alertPermissions = rememberPrayerAlertPermissions(viewModel)

    // Always composed: the location permission flow must outlive the picker dialog.
    PrayerLocationPickerHost(viewModel)

    LaunchedEffect(settings?.location?.source) { viewModel.refreshGpsLocationIfNeeded() }

    val current = settings
    if (viewModel.showSettingsModal && current != null) {
        PrayerTimesSettingsModal(
            settings = current,
            onDismiss = { viewModel.showSettingsModal = false },
            onSave = viewModel::saveCalculationSettings,
            onLocationClick = viewModel::openLocationPicker,
            isLocating = viewModel.isLocating,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val location = current?.location
        when {
            current == null -> Unit // DataStore not read yet: a frame, not worth a spinner.
            location == null -> NoLocationContent(
                isLocating = viewModel.isLocating,
                onSetLocation = viewModel::openLocationPicker,
            )
            else -> PrayerTimesContent(viewModel, current, location, alertPermissions)
        }
    }
}

@Composable
private fun PrayerTimesContent(
    viewModel: PrayerTimesViewModel,
    settings: PrayerSettings,
    location: PrayerLocation,
    alertPermissions: PrayerAlertPermissions,
) {
    val zone = remember(location.timeZoneId) { ZoneId.of(location.timeZoneId) }
    val today = remember(zone) { LocalDate.now(zone) }
    val pagerState = rememberPagerState(initialPage = TODAY_PAGE) { PAGE_COUNT }
    val scope = rememberCoroutineScope()

    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val modes by viewModel.notificationModes.collectAsState()
    val now by viewModel.now.collectAsState()

    val shownDate = today.plusDays((pagerState.currentPage - TODAY_PAGE).toLong())
    val shownDay = remember(shownDate, settings) { viewModel.dayTimes(shownDate, settings) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrayerAdhanDebugPanel()

        LocationBar(
            name = if (viewModel.isLocating) stringResource(R.string.prayer_location_locating) else location.displayName,
            onClick = { viewModel.showSettingsModal = true },
        )

        nextPrayer?.let { state ->
            NextPrayerCard(state = state, zone = zone, isTomorrow = state.next.date != today)
        }

        DayNavigator(
            date = shownDate,
            day = shownDay,
            isToday = pagerState.currentPage == TODAY_PAGE,
            onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
            onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
            onToday = { scope.launch { pagerState.animateScrollToPage(TODAY_PAGE) } },
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            val date = today.plusDays((page - TODAY_PAGE).toLong())
            val day = remember(date, settings) { viewModel.dayTimes(date, settings) }
            DayPage(
                day = day,
                zone = zone,
                nowMillis = now,
                nextPrayerMillis = nextPrayer?.next?.epochMillis,
                modes = modes,
                alertPermissions = alertPermissions,
            )
        }
    }
}

/*******************HEADER*********************/

@Composable
private fun LocationBar(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        OttrojjaText(
            text = name,
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.prayer_times_settings),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NextPrayerCard(state: NextPrayerState, zone: ZoneId, isTomorrow: Boolean) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val onCard = MaterialTheme.colorScheme.onPrimary
    val name = stringResource(state.next.type.displayNameRes(state.next.isJumuah))
    val time = PrayerFormat.time(context, state.next.epochMillis, zone)
    val timeLabel = if (isTomorrow) "$time • ${stringResource(R.string.prayer_times_tomorrow)}" else time
    val gradient = remember(primary) {
        Brush.linearGradient(listOf(lerp(primary, Color.White, 0.08f), primary, lerp(primary, Color.Black, 0.35f)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .nextPrayerOrnaments(onCard)
            // Inner frame, like the border around a page of the mushaf.
            .padding(6.dp)
            .border(1.dp, onCard.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrnamentDiamond(color = onCard.copy(alpha = 0.6f))
            OttrojjaText(
                text = stringResource(R.string.prayer_times_next_prayer_label),
                style = OttrojjaTheme.typography.bodySmall,
                color = onCard.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            OrnamentDiamond(color = onCard.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        OttrojjaText(
            text = name,
            style = OttrojjaTheme.typography.bodyLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
            color = onCard,
            textAlign = TextAlign.Center,
        )
        OttrojjaText(
            text = timeLabel,
            style = OttrojjaTheme.typography.bodyMedium,
            color = onCard.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
        )
        OrnamentDivider(
            color = onCard.copy(alpha = 0.55f),
            modifier = Modifier.padding(vertical = 10.dp),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(onCard.copy(alpha = 0.12f))
                .border(1.dp, onCard.copy(alpha = 0.18f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = onCard.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            OttrojjaText(
                text = stringResource(R.string.prayer_times_remaining, PrayerFormat.countdown(state.remainingMillis)),
                // Tabular digits: the countdown no longer shifts sideways as the seconds change.
                style = OttrojjaTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
                color = onCard,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Faint geometric decoration behind the next prayer card: a large eight-pointed star
 * (Rub el Hizb) peeking from the trailing top corner, a smaller one at the leading bottom,
 * and a crescent with a star in the leading top corner. Mirrors itself in RTL.
 */
private fun Modifier.nextPrayerOrnaments(color: Color): Modifier = drawWithCache {
    val rtl = layoutDirection == LayoutDirection.Rtl
    val leadingX = if (rtl) size.width else 0f
    val trailingX = if (rtl) 0f else size.width
    val inward = if (rtl) -1f else 1f // x direction from the leading edge towards the center

    val bigRadius = size.height * 0.55f
    val bigStar = eightPointStar(bigRadius)
    val bigInnerStar = eightPointStar(bigRadius * 0.62f)
    val smallStar = eightPointStar(size.height * 0.26f)
    val crescentRadius = 12.dp.toPx()
    val crescent = crescentPath(crescentRadius, opening = inward)
    val tinyStar = eightPointStar(4.dp.toPx())
    val stroke = Stroke(width = 1.dp.toPx())

    onDrawBehind {
        translate(trailingX - inward * size.width * 0.08f, size.height * 0.12f) {
            rotate(degrees = 22.5f, pivot = Offset.Zero) {
                drawPath(bigStar, color.copy(alpha = 0.10f), style = stroke)
                drawPath(bigInnerStar, color.copy(alpha = 0.07f), style = stroke)
                drawCircle(color.copy(alpha = 0.06f), radius = bigRadius * 0.3f, center = Offset.Zero, style = stroke)
            }
        }
        translate(leadingX + inward * size.width * 0.1f, size.height * 0.98f) {
            drawPath(smallStar, color.copy(alpha = 0.08f), style = stroke)
        }
        val crescentCenter = Offset(leadingX + inward * 30.dp.toPx(), 30.dp.toPx())
        translate(crescentCenter.x, crescentCenter.y) {
            drawPath(crescent, color.copy(alpha = 0.28f))
        }
        translate(crescentCenter.x + inward * crescentRadius * 0.9f, crescentCenter.y - crescentRadius * 0.25f) {
            drawPath(tinyStar, color.copy(alpha = 0.32f))
        }
    }
}

/** Two overlapping squares (Rub el Hizb), centered on the origin, a point facing up. */
private fun eightPointStar(radius: Float): Path {
    // The inner corners sit where the edges of the two squares cross.
    val innerRadius = radius * (cos(PI / 4) / cos(PI / 8)).toFloat()
    return Path().apply {
        for (i in 0 until 16) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = -PI / 2 + i * PI / 8
            val x = (r * cos(angle)).toFloat()
            val y = (r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

/** A crescent centered on the origin; [opening] = 1f opens to the right, -1f to the left. */
private fun crescentPath(radius: Float, opening: Float): Path {
    val moon = Path().apply { addOval(Rect(Offset.Zero, radius)) }
    val bite = Path().apply {
        addOval(Rect(Offset(opening * radius * 0.42f, -radius * 0.12f), radius * 0.86f))
    }
    return Path().apply { op(moon, bite, PathOperation.Difference) }
}

@Composable
private fun OrnamentDiamond(color: Color, diamondSize: Dp = 6.dp) {
    Canvas(modifier = Modifier.size(diamondSize)) {
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(size.width / 2, size.height)
            lineTo(0f, size.height / 2)
            close()
        }
        drawPath(path, color)
    }
}

/** ─── ◇ ◆ ◇ ─── with the lines fading out towards the ends. */
@Composable
private fun OrnamentDivider(color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .width(170.dp)
            .height(10.dp),
    ) {
        val cy = size.height / 2
        val cx = size.width / 2
        val big = size.height / 2
        val small = big * 0.55f
        val gap = big * 2.6f // center to the small diamonds
        val lineStart = gap + small + 4.dp.toPx()
        val strokeWidth = 1.dp.toPx()

        fun diamond(x: Float, half: Float) = Path().apply {
            moveTo(x, cy - half)
            lineTo(x + half, cy)
            lineTo(x, cy + half)
            lineTo(x - half, cy)
            close()
        }

        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, color), startX = 0f, endX = cx - lineStart),
            start = Offset(0f, cy), end = Offset(cx - lineStart, cy), strokeWidth = strokeWidth,
        )
        drawLine(
            brush = Brush.horizontalGradient(listOf(color, Color.Transparent), startX = cx + lineStart, endX = size.width),
            start = Offset(cx + lineStart, cy), end = Offset(size.width, cy), strokeWidth = strokeWidth,
        )
        drawPath(diamond(cx, big), color)
        drawPath(diamond(cx - gap, small), color, style = Stroke(strokeWidth))
        drawPath(diamond(cx + gap, small), color, style = Stroke(strokeWidth))
    }
}

@Composable
private fun DayNavigator(
    date: LocalDate,
    day: DayPrayerTimes?,
    isToday: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    val context = LocalContext.current
    // Chevrons point "backwards" and "forwards" in reading direction.
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val previousIcon = if (rtl) Icons.Default.ChevronRight else Icons.Default.ChevronLeft
    val nextIcon = if (rtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(previousIcon, stringResource(R.string.prayer_times_previous_day), tint = MaterialTheme.colorScheme.primary)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OttrojjaText(
                text = PrayerFormat.gregorianDate(date),
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            day?.let {
                OttrojjaText(
                    text = PrayerFormat.hijriDate(context, it.hijriDate),
                    style = OttrojjaTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
            }
            AnimatedVisibility(visible = !isToday) {
                OttrojjaText(
                    text = stringResource(R.string.prayer_times_back_to_today),
                    style = OttrojjaTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToday)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        IconButton(onClick = onNext) {
            Icon(nextIcon, stringResource(R.string.prayer_times_next_day), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/*******************DAY LIST*********************/

@Composable
private fun DayPage(
    day: DayPrayerTimes?,
    zone: ZoneId,
    nowMillis: Long,
    nextPrayerMillis: Long?,
    modes: Map<PrayerType, PrayerNotificationMode>,
    alertPermissions: PrayerAlertPermissions,
) {
    if (day == null) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            OttrojjaText(
                text = stringResource(R.string.prayer_times_unavailable),
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        day.times.forEach { time ->
            PrayerRow(
                time = time,
                isFriday = day.isFriday,
                zone = zone,
                isNext = time.epochMillis == nextPrayerMillis,
                isPast = time.epochMillis <= nowMillis,
                mode = modes[time.type] ?: PrayerNotificationMode.OFF,
                alertPermissions = alertPermissions,
            )
        }
        if (day.isFriday) {
            OttrojjaText(
                text = stringResource(R.string.prayer_times_jumuah_hint),
                style = OttrojjaTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun PrayerRow(
    time: PrayerTime,
    isFriday: Boolean,
    zone: ZoneId,
    isNext: Boolean,
    isPast: Boolean,
    mode: PrayerNotificationMode,
    alertPermissions: PrayerAlertPermissions,
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val emphasis = if (time.type.isObligatory) FontWeight.SemiBold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isNext) primary.copy(alpha = 0.12f) else primary.copy(alpha = 0.04f))
            .alpha(if (isPast && !isNext) 0.5f else 1f)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OttrojjaText(
            text = stringResource(time.type.displayNameRes(isFriday)),
            style = OttrojjaTheme.typography.bodyMedium.copy(fontWeight = emphasis),
            color = primary,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f),
        )
        OttrojjaText(
            text = PrayerFormat.time(context, time.epochMillis, zone),
            style = OttrojjaTheme.typography.bodyMedium.copy(fontWeight = emphasis),
            color = primary,
            textAlign = TextAlign.End,
        )
        if (time.type.isNotifiable) {
            BellButton(
                mode = mode,
                enabled = alertPermissions.notificationsAllowed,
                onClick = { alertPermissions.bellClicked(time.type) },
            )
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun BellButton(mode: PrayerNotificationMode, enabled: Boolean, onClick: () -> Unit) {
    // Without notification permission every bell shows as off, as the alerts can't fire.
    val effective = if (enabled) mode else PrayerNotificationMode.OFF
    val (icon: ImageVector, descriptionRes: Int) = when (effective) {
        PrayerNotificationMode.OFF -> Icons.Default.NotificationsOff to R.string.prayer_mode_off
        PrayerNotificationMode.NOTIFICATION -> Icons.Default.NotificationsActive to R.string.prayer_mode_notification
        PrayerNotificationMode.ADHAN -> Icons.Default.Campaign to R.string.prayer_mode_adhan
    }
    val tint = when {
        !enabled -> MaterialTheme.colorScheme.outline
        effective == PrayerNotificationMode.OFF -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.primary
    }
    // Stays clickable when disabled: the tap is what asks for the permission.
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = stringResource(descriptionRes), tint = tint)
    }
}

/*******************EMPTY STATE*********************/

@Composable
private fun NoLocationContent(isLocating: Boolean, onSetLocation: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OttrojjaText(
            text = stringResource(
                if (isLocating) R.string.prayer_location_locating else R.string.prayer_times_no_location
            ),
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!isLocating) {
            OttrojjaButton(
                onClick = onSetLocation,
                text = stringResource(R.string.prayer_times_set_location),
            )
        }
    }
}

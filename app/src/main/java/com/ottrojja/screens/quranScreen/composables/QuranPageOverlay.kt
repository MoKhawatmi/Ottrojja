package com.ottrojja.screens.quranScreen.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/* ───────────────────────────── Geometry ───────────────────────────── */

/** Size of the page images (p_1 … p_604) that the polygons were measured on. */
const val PAGE_SRC_WIDTH = 740
const val PAGE_SRC_HEIGHT = 1150
const val PAGE_ASPECT_RATIO = PAGE_SRC_WIDTH.toFloat() / PAGE_SRC_HEIGHT

/** Approximate distance between two text lines on a page, in source pixels. */
private const val SRC_LINE_PITCH = 68f

/**
 * A verse prepared for hit-testing and drawing. Everything is in source-pixel space
 * (740×1150), so it never depends on the screen size.
 */
@Immutable
class VerseRegion(
    val verse: PageContent,
    val polygons: List<IntArray>,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun contains(x: Float, y: Float): Boolean {
        if (x < left || x > right || y < top || y > bottom) return false
        return polygons.any { it.containsPoint(x, y) }
    }
}

/** Keeps only verse rows that have polygons. Cheap: call inside remember(pageContent). */
fun buildVerseRegions(pageContent: List<PageContent>): List<VerseRegion> =
    pageContent.mapNotNull { item ->
        if (item.type != PageContentItemType.verse) return@mapNotNull null
        val polys = item.polygons
            .filter { it.size >= 6 && it.size % 2 == 0 }
            .map { it.toIntArray() }
        if (polys.isEmpty()) return@mapNotNull null

        var l = Int.MAX_VALUE; var t = Int.MAX_VALUE
        var r = Int.MIN_VALUE; var b = Int.MIN_VALUE
        polys.forEach { p ->
            var i = 0
            while (i < p.size) {
                l = min(l, p[i]); r = max(r, p[i])
                t = min(t, p[i + 1]); b = max(b, p[i + 1])
                i += 2
            }
        }
        VerseRegion(item, polys, l, t, r, b)
    }

/** Even-odd ray casting on a flat [x1, y1, x2, y2, …] polygon. */
private fun IntArray.containsPoint(x: Float, y: Float): Boolean {
    var inside = false
    var j = size - 2
    var i = 0
    while (i < size) {
        val xi = this[i].toFloat(); val yi = this[i + 1].toFloat()
        val xj = this[j].toFloat(); val yj = this[j + 1].toFloat()
        if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
        j = i
        i += 2
    }
    return inside
}

/* ───────────────────────────── Interaction layer ───────────────────────────── */

/**
 * What a long press produced: the verse plus where it sits on screen, in ROOT coordinates
 * (same space as every other composable in the activity window — no separate popup window).
 */
@Immutable
class VerseAnchor(
    val verse: PageContent,
    /** Top / bottom edge to anchor the card to (already limited to ~2 lines around the finger). */
    val top: Float,
    val bottom: Float,
    /** Finger x position. */
    val pressX: Float,
)

/**
 * Place this over the page image, inside a box that has exactly the image's aspect ratio
 * (see SinglePage). Long-pressing a verse reports a [VerseAnchor]; the card itself is drawn by
 * [VerseMenuHost] higher up. Plain taps are NOT consumed, so the pager's own tap handling
 * (toggling the controller) keeps working.
 */
@Composable
fun VerseInteractionLayer(
    pageContent: List<PageContent>,
    nightReadingMode: Boolean,
    highlightedVerseId: Int?,
    onVerseLongPress: (VerseAnchor) -> Unit,
    modifier: Modifier = Modifier,
) {
    val regions = remember(pageContent) { buildVerseRegions(pageContent) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val highlight = if (nightReadingMode) Color(0xFFFFD54F).copy(alpha = 0.30f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)

    QuranPageOverlay(
        regions = regions,
        highlighted = regions.firstOrNull { it.verse.id == highlightedVerseId },
        highlightColor = highlight,
        onVerseLongPress = { region, position ->
            val coords = coordinates?.takeIf { it.isAttached } ?: return@QuranPageOverlay
            val sy = coords.size.height / PAGE_SRC_HEIGHT.toFloat()
            val linePx = SRC_LINE_PITCH * sy
            // Anchor to the verse edge, but never more than ~2 lines away from the finger
            // (long verses can cover most of the page).
            val topLocal = max(region.top * sy, position.y - 2 * linePx)
            val bottomLocal = min(region.bottom * sy, position.y + 2 * linePx)
            val top = coords.localToRoot(Offset(position.x, topLocal))
            val bottom = coords.localToRoot(Offset(position.x, bottomLocal))
            onVerseLongPress(VerseAnchor(region.verse, top.y, bottom.y, top.x))
        },
        modifier = modifier.onGloballyPositioned { coordinates = it }
    )
}

/**
 * Draws the highlighted verse and detects long presses. Coordinates are mapped between the
 * box size and the 740×1150 source grid, so it works on any screen size or orientation.
 */
@Composable
fun QuranPageOverlay(
    regions: List<VerseRegion>,
    highlighted: VerseRegion?,
    highlightColor: Color,
    onVerseLongPress: (VerseRegion, Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val currentOnLongPress by rememberUpdatedState(onVerseLongPress)

    Spacer(
        modifier
            .pointerInput(regions) {
                awaitEachGesture {
                    // Don't consume the down: the pager / clickable above must still see taps.
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                    if (size.width == 0 || size.height == 0) return@awaitEachGesture

                    val sx = longPress.position.x * PAGE_SRC_WIDTH / size.width
                    val sy = longPress.position.y * PAGE_SRC_HEIGHT / size.height
                    val hit = regions.firstOrNull { it.contains(sx, sy) } ?: return@awaitEachGesture

                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnLongPress(hit, longPress.position)

                    // Swallow the rest of this gesture so lifting the finger isn't treated
                    // as a tap by the pager (which would toggle the controller).
                    longPress.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                        if (event.changes.none { it.pressed }) break
                    }
                }
            }
            .drawWithCache {
                val region = highlighted
                val sx = size.width / PAGE_SRC_WIDTH
                val sy = size.height / PAGE_SRC_HEIGHT
                val path = Path()
                region?.polygons?.forEach { p ->
                    path.moveTo(p[0] * sx, p[1] * sy)
                    var i = 2
                    while (i < p.size) {
                        path.lineTo(p[i] * sx, p[i + 1] * sy)
                        i += 2
                    }
                    path.close()
                }
                onDrawBehind {
                    if (region != null) drawPath(path, highlightColor)
                }
            }
    )
}

/* ───────────────────────────── Floating card ───────────────────────────── */

/**
 * Draws the action card for [anchor] on top of everything in its parent (put it last in a
 * full-size Box, with Modifier.matchParentSize()). Tapping anywhere outside the card or
 * pressing back dismisses it.
 *
 * The card is laid out in the same window as the page (not an Android popup window), so its
 * position can't drift when system bars / insets change, e.g. after a rotation.
 */
@Composable
fun VerseMenuHost(
    anchor: VerseAnchor?,
    onDismiss: () -> Unit,
    onShare: (PageContent) -> Unit,
    onTafseer: (PageContent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (anchor == null) return
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    var hostOrigin by remember { mutableStateOf<Offset?>(null) }

    BackHandler { currentOnDismiss() }

    val density = LocalDensity.current
    val gap = with(density) { 8.dp.roundToPx() }
    val margin = with(density) { 8.dp.roundToPx() }

    Layout(
        content = {
            key(anchor) {
                VerseActionsCard(
                    verse = anchor.verse,
                    onShare = { currentOnDismiss(); onShare(anchor.verse) },
                    onTafseer = { currentOnDismiss(); onTafseer(anchor.verse) },
                )
            }
        },
        modifier = modifier
            .onGloballyPositioned { hostOrigin = it.positionInRoot() }
            // Outside taps: consume (so the pager doesn't toggle the controller) and dismiss.
            .pointerInput(Unit) { detectTapGestures { currentOnDismiss() } }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val card = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(width, height) {
            val origin = hostOrigin ?: return@layout // first frame: wait for our position
            val w = card.width
            val h = card.height

            val maxX = max(margin, width - w - margin)
            val x = (anchor.pressX - origin.x - w / 2f).roundToInt().coerceIn(margin, maxX)

            val maxY = max(margin, height - h - margin)
            val above = (anchor.top - origin.y).roundToInt() - gap - h
            val below = (anchor.bottom - origin.y).roundToInt() + gap
            val y = when {
                above >= margin -> above
                below <= maxY -> below
                else -> above.coerceIn(margin, maxY)
            }
            card.place(x, y) // absolute, not mirrored in RTL
        }
    }
}

@Composable
private fun VerseActionsCard(
    verse: PageContent,
    onShare: () -> Unit,
    onTafseer: () -> Unit,
) {
    val appear = remember { MutableTransitionState(false) }.apply { targetState = true }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AnimatedVisibility(
            visibleState = appear,
            enter = fadeIn(tween(140)) + scaleIn(tween(180), initialScale = 0.9f)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                modifier = Modifier
                    .width(264.dp)
                    // Taps on the card's empty areas must not reach the dismiss layer behind it.
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp)
                        ) {
                            OttrojjaText(
                                text = Helpers.convertToIndianNumbers("${verse.verseNum ?: ""}"),
                                style = OttrojjaTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OttrojjaText(
                            text = verse.verseText ?: "",
                            style = OttrojjaTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VerseMenuAction(
                            text = "مشاركة",
                            icon = Icons.Default.Share,
                            background = MaterialTheme.colorScheme.tertiary,
                            content = Color.Black,
                            onClick = onShare,
                            modifier = Modifier.weight(1f)
                        )
                        VerseMenuAction(
                            text = "التفسير",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            background = MaterialTheme.colorScheme.primary,
                            content = MaterialTheme.colorScheme.onPrimary,
                            onClick = onTafseer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseMenuAction(
    text: String,
    icon: ImageVector,
    background: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        OttrojjaText(text = text, style = OttrojjaTheme.typography.bodyMedium, color = content)
    }
}

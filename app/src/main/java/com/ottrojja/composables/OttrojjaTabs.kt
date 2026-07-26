package com.ottrojja.composables

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ottrojja.interfaces.TabItem
import com.ottrojja.ui.theme.OttrojjaTheme


@Composable
fun <T> OttrojjaTabs(
    items: List<T>,
    selectedItem: T,
    onClickTab: (T) -> Unit,
    tabPrefix: String="",
    weightedLayoutThreshold: Int = 3,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color(0xFFF7F2FA),
    activeTextColor: Color = MaterialTheme.colorScheme.onPrimary,
    inactiveTextColor: Color = MaterialTheme.colorScheme.primary,
) where T : Enum<T>, T : TabItem {

    val context= LocalContext.current

    val selectedIndex = items.indexOf(selectedItem)
    val useWeightedLayout = items.size <= weightedLayoutThreshold

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val textMeasurer = rememberTextMeasurer()
    val textStyle = OttrojjaTheme.typography.tabLabel

    // Fixed tab width — only used when NOT in weighted layout
    val fixedTabWidth: Dp = remember(items) {
        val widestPx = items.maxOf { item ->
            textMeasurer.measure(
                text = AnnotatedString("$tabPrefix${item.displayName}"),
                style = textStyle,
            ).size.width
        }
        with(density) { max(90.dp, widestPx.toDp() + 32.dp) }
    }

    // Per-tab x offsets (measured via onGloballyPositioned)
    val tabOffsets = remember(items.size) {
        mutableStateListOf(*Array(items.size) { 0.dp })
    }

    var containerWidth by remember { mutableStateOf(0.dp) }
    var containerRootOffset by remember { mutableStateOf(0f) }

    // In weighted layout the pill width equals an equal share of the container.
    // We only know containerWidth after the first composition, so start at 0 and
    // let the animated value catch up on the next frame.
    val resolvedTabWidth: Dp = if (useWeightedLayout) {
        if (containerWidth > 0.dp) containerWidth / items.size else 0.dp
    } else {
        fixedTabWidth
    }

    // RTL-aware target offset for the sliding pill
    val targetOffset: Dp = remember(
        selectedIndex,
        layoutDirection,
        containerWidth,
        resolvedTabWidth,
        tabOffsets.toList(),
    ) {
        val currentOffset = tabOffsets.getOrElse(selectedIndex) { 0.dp }

        when (layoutDirection) {
            LayoutDirection.Ltr -> currentOffset
            LayoutDirection.Rtl -> containerWidth - currentOffset - resolvedTabWidth
        }
    }

    // Pill and scroll animations share a duration so they visually finish together.
    val tabTransitionDurationMillis = 200

    // Scroll state for the non-weighted (fixed-width, horizontally scrollable) layout.
    // Lifted out so we can drive it programmatically when the selected tab changes,
    // and so the pill's own animation (below) can tell when the user is manually
    // scrolling.
    val scrollState = rememberScrollState()

    // While the user is manually dragging/flinging the tab bar, the pill must
    // track the selected tab's on-screen position exactly, not chase it with an
    // animated lag — targetOffset is changing purely because the tab is physically
    // moving under a drag, not because the selection changed, so animating here
    // would make the pill visibly "slide" relative to its own tab as it scrolls.
    // Only animate when the offset changes for a real reason (a tap), i.e. when
    // no manual scroll is in progress.
    val indicatorAnimationSpec = if (scrollState.isScrollInProgress) {
        snap()
    } else {
        tween<Dp>(durationMillis = tabTransitionDurationMillis)
    }

    val indicatorWidth by animateDpAsState(
        targetValue = resolvedTabWidth,
        animationSpec = indicatorAnimationSpec,
        label = "indicatorWidth",
    )

    val indicatorOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = indicatorAnimationSpec,
        label = "indicatorOffset",
    )

    // Tracks whether we've done the very first scroll-into-view yet, so that
    // first render jumps instantly while every later selection change animates.
    // Plain composition state (not a var trapped inside a long-lived coroutine)
    // so it can't go stale across effect relaunches.
    var hasScrolledInitially by remember { mutableStateOf(false) }

    // Re-launches fresh every time selectedIndex or containerWidth changes —
    // Compose cancels the previous instance automatically, so there's no
    // persistent collector state to drift or get stuck after a few selections.
    // Always resolves to a target that leaves the selected tab fully visible;
    // "already fully visible" is just the case where target == current value.
    LaunchedEffect(selectedIndex, containerWidth) {
        if (useWeightedLayout || containerWidth <= 0.dp) return@LaunchedEffect

        val tabWidthPx = with(density) { fixedTabWidth.toPx() }
        val containerWidthPx = with(density) { containerWidth.toPx() }

        // Row mirrors child placement for Rtl automatically: item 0 ends up laid
        // out at the END of the content's local (pre-scroll) coordinate space,
        // not the start. So the tab's position in content-local coordinates uses
        // a mirrored index in Rtl — otherwise the edge-alignment below resolves
        // against the wrong physical edge and scrolling goes the wrong way.
        val contentIndex = if (layoutDirection == LayoutDirection.Rtl) {
            items.size - 1 - selectedIndex
        } else {
            selectedIndex
        }

        val tabStartPx = contentIndex * tabWidthPx
        val tabEndPx = tabStartPx + tabWidthPx

        val visibleStart = scrollState.value.toFloat()
        val visibleEnd = visibleStart + containerWidthPx

        // Deliberately NOT derived from tabOffsets/positionInRoot — those reflect
        // the tab's post-scroll on-screen position, so using them here fed back
        // into itself (scroll moves the tab -> measured offset changes ->
        // recompute a "new" target -> scroll again -> forever). All fixed-width
        // tabs sit at a predictable index * width offset in the *unscrolled*
        // content, so we can compute the target directly with no measurement
        // involved.
        val target = when {
            // Tab's leading edge is clipped -> align it to the container's start
            tabStartPx < visibleStart ->
                tabStartPx.toInt().coerceIn(0, scrollState.maxValue)
            // Tab's trailing edge is clipped -> align it to the container's end
            tabEndPx > visibleEnd ->
                (tabEndPx - containerWidthPx).toInt().coerceIn(0, scrollState.maxValue)
            // Already fully visible — target equals current position, so the
            // distance check below is a no-op and nothing moves.
            else -> scrollState.value
        }

        if (kotlin.math.abs(target - scrollState.value) >= 2) {
            if (!hasScrolledInitially) {
                scrollState.scrollTo(target)
            } else {
                scrollState.animateScrollTo(
                    value = target,
                    animationSpec = tween(durationMillis = tabTransitionDurationMillis),
                )
            }
        }

        hasScrolledInitially = true
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                containerWidth = with(density) { coords.size.width.toDp() }
                containerRootOffset = coords.positionInRoot().x
            }
            .clip(RoundedCornerShape(50))
            .background(inactiveColor)
            .height(IntrinsicSize.Min),
    ) {

        // Sliding pill indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B5DC8), // lighter
                            Color(0xFF08428E), // base
                            Color(0xFF04234A)  // darker
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!useWeightedLayout)
                        Modifier.horizontalScroll(
                            state = scrollState,
                            reverseScrolling = layoutDirection == LayoutDirection.Rtl,
                        )
                    else
                        Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .then(
                            if (useWeightedLayout)
                                Modifier.weight(1f)
                            else
                                Modifier.width(fixedTabWidth)
                        )
                        .onGloballyPositioned { coords ->
                            tabOffsets[index] = with(density) {
                                (coords.positionInRoot().x - containerRootOffset).toDp()
                            }
                        }
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onClickTab(items[index])
                        }
                        .padding(vertical = 8.dp),
                ) {
                    Column(
                        modifier = Modifier.height(26.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BasicText(
                            text = " $tabPrefix${item.displayName} ",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            softWrap = true,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 14.sp,
                                maxFontSize = 18.sp,
                                stepSize = 1.sp,
                            ),
                            style = OttrojjaTheme.typography.tabLabel.copy(
                                color = if (isSelected) activeTextColor else inactiveTextColor,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }
        }
    }
}
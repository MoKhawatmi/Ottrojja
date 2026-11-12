package com.ottrojja.screens.quranScreen.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.graphics.drawable.toBitmap
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.PageContent
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

// Mushaf palette: warm paper and gilding. The frame/ink color follows the theme's primary.
private val Paper = Color(0xFFFBF6E9)
private val Gold = Color(0xFFC9A44C)

// Page frame metrics, from the outside in.
private val FrameMargin = 6.dp   // bare paper around the frame
private val FrameBand = 14.dp    // ornamented band
private val FrameGap = 3.dp      // paper between band and inner rule
private val ContentInset = 11.dp // inner rule to content
private val KitabFontFamily = FontFamily(Font(R.font.kitab_regular))

@Composable
fun VerseImageCard(
    chapterName: String,
    verses: List<PageContent>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIcon = remember {
        context.packageManager.getApplicationIcon(context.packageName)
            .toBitmap(128, 128)
            .asImageBitmap()
    }
    val primary = MaterialTheme.colorScheme.primary



    val totalChars = verses.sumOf { it.verseText?.length ?: 0 }
    val textStyle = (if (totalChars <= 200) OttrojjaTheme.typography.bodySpecialLarge
    else OttrojjaTheme.typography.bodySpecialMedium)
        .copy(fontFamily = KitabFontFamily, lineHeight = 1.8.em)
    val body = verses.joinToString("\u2009") {
        "${it.verseText.orEmpty()}\u202F\uFD3F${Helpers.convertToIndianNumbers("${it.verseNum ?: 0}")}\uFD3E"
    }

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, 1f),
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Column(
            modifier = modifier
                .width(400.dp)
                .background(Paper)
                .mushafFrame(primary, Gold)
                .padding(FrameMargin + FrameBand + FrameGap + ContentInset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SurahHeader(chapterName)
            Spacer(Modifier.height(18.dp))
            OttrojjaText(
                text = body,
                style = textStyle,
                color = primary,
                // Full lines like a mushaf page; a short single verse sits centered instead.
                textAlign = if (totalChars <= 90) TextAlign.Center else TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            // Footer, where a mushaf carries its page-number medallion.
            // Layout direction is RTL: the first child sits on the physical right.
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterRule(Modifier.weight(1f), innerAtLeft = true)
                Spacer(Modifier.width(8.dp))
                Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                OttrojjaText(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "أترجة",
                    style = OttrojjaTheme.typography.bodySpecialMedium,
                    color = primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.width(8.dp))
                FooterRule(Modifier.weight(1f), innerAtLeft = false)
            }
        }
    }
}

/**
 * Illuminated page border: a solid band with a gold lozenge-and-bead chain,
 * gilded hairlines, an eight-pointed star in each corner, then a fine inner
 * rule with small spandrels in its corners.
 */
private fun Modifier.mushafFrame(primary: Color, accent: Color): Modifier = drawWithCache {
    val band = FrameBand.toPx()
    val outer = Rect(0f, 0f, size.width, size.height).deflate(FrameMargin.toPx())
    val inner = outer.deflate(band)
    val mid = outer.deflate(band / 2f) // center line of the band

    // Lozenge chain; the pitch stretches slightly so every side ends symmetrically.
    val pitch = 11.dp.toPx()
    val lozengeHalf = 3.dp.toPx()
    val lozenges = Path()
    val beads = mutableListOf<Offset>()
    fun chain(from: Offset, to: Offset) {
        val n = max(1, ((to - from).getDistance() / pitch).roundToInt())
        for (i in 0 until n) {
            lozenges.addDiamond(lerp(from, to, (i + 0.5f) / n), lozengeHalf)
            if (i > 0) beads += lerp(from, to, i.toFloat() / n)
        }
    }
    chain(Offset(inner.left, mid.top), Offset(inner.right, mid.top))
    chain(Offset(inner.left, mid.bottom), Offset(inner.right, mid.bottom))
    chain(Offset(mid.left, inner.top), Offset(mid.left, inner.bottom))
    chain(Offset(mid.right, inner.top), Offset(mid.right, inner.bottom))

    val cornerStars = listOf(mid.topLeft, mid.topRight, mid.bottomLeft, mid.bottomRight)
        .map { starPath(it, 4.5.dp.toPx(), 3.2.dp.toPx()) }

    val hairInset = 2.dp.toPx()
    val outerHair = outer.deflate(hairInset)
    val innerHair = inner.inflate(hairInset)

    val rule = inner.deflate(FrameGap.toPx())
    val spandrel = 16.dp.toPx()
    val spandrelCorners = listOf(
        Triple(rule.topLeft, 1f, 1f),
        Triple(rule.topRight, -1f, 1f),
        Triple(rule.bottomLeft, 1f, -1f),
        Triple(rule.bottomRight, -1f, -1f)
    )
    val spandrels = spandrelCorners.map { (c, sx, sy) -> spandrelPath(c, sx, sy, spandrel) }
    val spandrelDots = spandrelCorners.map { (c, sx, sy) ->
        Offset(c.x + sx * spandrel * 0.17f, c.y + sy * spandrel * 0.17f)
    }

    onDrawBehind {
        // band
        drawRect(primary, mid.topLeft, mid.size, style = Stroke(band))
        drawRect(accent, outerHair.topLeft, outerHair.size, style = Stroke(0.8.dp.toPx()))
        drawRect(accent, innerHair.topLeft, innerHair.size, style = Stroke(0.8.dp.toPx()))
        drawPath(lozenges, accent)
        drawPoints(
            beads, PointMode.Points, accent,
            strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round
        )
        cornerStars.forEach { drawPath(it, accent) }

        // inner rule and corner spandrels
        drawRect(primary, rule.topLeft, rule.size, style = Stroke(1.dp.toPx()))
        spandrels.forEach { drawPath(it, primary) }
        drawPoints(
            spandrelDots, PointMode.Points, accent,
            strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round
        )
    }
}

/** Gold hairline that fades toward the page edge, ending in a lozenge next to the brand. */
@Composable
private fun FooterRule(modifier: Modifier, innerAtLeft: Boolean) {
    Box(
        modifier
            .height(8.dp)
            .drawBehind {
                val cy = size.height / 2f
                val half = 3.dp.toPx()
                val innerX = if (innerAtLeft) half else size.width - half
                val outerX = if (innerAtLeft) size.width else 0f
                drawLine(
                    Brush.horizontalGradient(
                        listOf(Gold, Gold.copy(alpha = 0f)),
                        startX = innerX,
                        endX = outerX
                    ),
                    Offset(innerX, cy), Offset(outerX, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawPath(diamondPath(Offset(innerX, cy), half), Gold)
            }
    )
}

/**
 * Ornamental surah title: a cartouche with concave pointed ends, a solid band
 * in the primary color with a gold bead chain, gilded hairlines, and an
 * eight-pointed star medallion at each end.
 */
@Composable
private fun SurahHeader(
    chapterName: String,
    modifier: Modifier = Modifier,
    accent: Color = Gold
) {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val cy = h / 2f

                // --- frame geometry ---
                val outerStroke = 1.4.dp.toPx()
                val tip = 22.dp.toPx()
                val outerRect = Rect(0f, 0f, w, h).deflate(outerStroke / 2f)
                val bandInset = 5.dp.toPx()
                val innerRect = outerRect.deflate(bandInset)
                val innerTip = tip - bandInset * 0.6f
                val beadRect = outerRect.deflate(bandInset / 2f)
                val beadTip = tip - bandInset * 0.3f

                val outerPath = cartouchePath(outerRect, tip)
                val innerPath = cartouchePath(innerRect, innerTip)
                val beadPath = cartouchePath(beadRect, beadTip)

                val innerFill = Brush.verticalGradient(
                    listOf(
                        primary.copy(alpha = 0.12f),
                        primary.copy(alpha = 0.03f),
                        primary.copy(alpha = 0.12f)
                    ),
                    startY = innerRect.top,
                    endY = innerRect.bottom
                )

                val beadEffect = PathEffect.dashPathEffect(
                    floatArrayOf(0.01f, 4.dp.toPx())
                )

                // --- medallions ---
                val starOuter = 8.dp.toPx()
                val starInner = starOuter * 0.72f
                val ringRadius = starOuter + 3.dp.toPx()
                val starOffset = innerTip + ringRadius + 2.dp.toPx()
                val centers = listOf(
                    Offset(innerRect.left + starOffset, cy),
                    Offset(innerRect.right - starOffset, cy)
                )
                val stars = centers.map { starPath(it, starOuter, starInner) }

                // small diamonds pointing from each medallion toward the title
                val diamondHalf = 3.dp.toPx()
                val diamondGap = ringRadius + 5.dp.toPx()
                val diamonds = listOf(
                    diamondPath(Offset(centers[0].x + diamondGap, cy), diamondHalf),
                    diamondPath(Offset(centers[1].x - diamondGap, cy), diamondHalf)
                )

                // dots near the pointed tips
                val tipDots = listOf(
                    Offset(innerRect.left + 5.dp.toPx(), cy),
                    Offset(innerRect.right - 5.dp.toPx(), cy)
                )

                onDrawBehind {
                    // band
                    drawPath(outerPath, primary)
                    drawPath(
                        beadPath, accent,
                        style = Stroke(
                            width = 1.6.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = beadEffect
                        )
                    )
                    drawPath(outerPath, accent, style = Stroke(outerStroke))

                    // inner field
                    drawPath(innerPath, Paper)
                    drawPath(innerPath, innerFill)
                    drawPath(innerPath, accent, style = Stroke(1.dp.toPx()))

                    // medallions
                    centers.forEachIndexed { i, c ->
                        drawCircle(
                            accent.copy(alpha = 0.7f), ringRadius, c,
                            style = Stroke(0.8.dp.toPx())
                        )
                        drawPath(stars[i], accent)
                        drawPath(stars[i], primary, style = Stroke(0.9.dp.toPx()))
                        drawCircle(Paper, starInner * 0.55f, c)
                        drawCircle(primary, starInner * 0.55f, c, style = Stroke(0.8.dp.toPx()))
                        drawCircle(primary, 1.5.dp.toPx(), c)
                    }

                    diamonds.forEach { drawPath(it, accent) }
                    tipDots.forEach { drawCircle(accent, 1.6.dp.toPx(), it) }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        OttrojjaText(
            text = "سورة $chapterName",
            style = OttrojjaTheme.typography.bodySpecialLarge,
            color = primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 62.dp)
        )
    }
}

/** Rectangle whose left/right ends narrow to a point via concave (inward-curving) arcs. */
private fun cartouchePath(rect: Rect, tip: Float): Path = Path().apply {
    val cy = rect.center.y
    moveTo(rect.left + tip, rect.top)
    lineTo(rect.right - tip, rect.top)
    quadraticTo(rect.right - tip, cy, rect.right, cy)
    quadraticTo(rect.right - tip, cy, rect.right - tip, rect.bottom)
    lineTo(rect.left + tip, rect.bottom)
    quadraticTo(rect.left + tip, cy, rect.left, cy)
    quadraticTo(rect.left + tip, cy, rect.left + tip, rect.top)
    close()
}

/** Star with [points] tips (eight by default, Rub el Hizb style), one tip facing up. */
private fun starPath(center: Offset, outerR: Float, innerR: Float, points: Int = 8): Path =
    Path().apply {
        val step = PI / points
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val a = -PI / 2 + i * step
            val x = center.x + (r * cos(a)).toFloat()
            val y = center.y + (r * sin(a)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

/**
 * Corner filler: the two legs run [size] along the edges from [corner]
 * (direction given by [sx]/[sy], each +1 or -1), joined by a concave curve.
 */
private fun spandrelPath(corner: Offset, sx: Float, sy: Float, size: Float): Path =
    Path().apply {
        moveTo(corner.x, corner.y)
        lineTo(corner.x + sx * size, corner.y)
        quadraticTo(
            corner.x + sx * size * 0.3f, corner.y + sy * size * 0.3f,
            corner.x, corner.y + sy * size
        )
        close()
    }

private fun Path.addDiamond(center: Offset, half: Float) {
    moveTo(center.x, center.y - half)
    lineTo(center.x + half, center.y)
    lineTo(center.x, center.y + half)
    lineTo(center.x - half, center.y)
    close()
}

private fun diamondPath(center: Offset, half: Float): Path =
    Path().apply { addDiamond(center, half) }
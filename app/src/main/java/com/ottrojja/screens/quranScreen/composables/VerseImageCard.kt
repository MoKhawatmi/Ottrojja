package com.ottrojja.screens.quranScreen.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.PageContent
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

    val totalChars = verses.sumOf { it.verseText?.length ?: 0 }
    val textStyle = if (totalChars <= 200) OttrojjaTheme.typography.bodySpecialLarge
    else OttrojjaTheme.typography.bodySpecialMedium

    val body = verses
        .joinToString(" ")
        { "${it.verseText} ﴿${Helpers.convertToIndianNumbers("${it.verseNum ?: 0}")}﴾" }

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, 1f),
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Column(
            modifier = modifier
                .width(400.dp)
                .background(MaterialTheme.colorScheme.background)
                .border(2.dp, MaterialTheme.colorScheme.primary)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SurahHeader(chapterName)
            Spacer(Modifier.height(20.dp))
            OttrojjaText(
                text = body,
                style = textStyle,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            // Layout direction is RTL here, so Start = physical right
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                OttrojjaText(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "أترجة",
                    style = OttrojjaTheme.typography.bodySpecialMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
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
    accent: Color = Color(0xFFC9A44C) // gold
) {
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val cy = h / 2f

                // --- frame geometry ---
                val outerStroke = 1.4.dp.toPx()
                val tip = 26.dp.toPx()
                val outerRect = Rect(0f, 0f, w, h).deflate(outerStroke / 2f)
                val bandInset = 6.dp.toPx()
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
                    floatArrayOf(0.01f, 4.5.dp.toPx())
                )

                // --- medallions ---
                val starOuter = 10.dp.toPx()
                val starInner = starOuter * 0.72f
                val ringRadius = starOuter + 3.5.dp.toPx()
                val starOffset = innerTip + ringRadius + 3.dp.toPx()
                val centers = listOf(
                    Offset(innerRect.left + starOffset, cy),
                    Offset(innerRect.right - starOffset, cy)
                )
                val stars = centers.map { starPath(it, starOuter, starInner) }

                // small diamonds pointing from each medallion toward the title
                val diamondHalf = 3.5.dp.toPx()
                val diamondGap = ringRadius + 6.dp.toPx()
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
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = beadEffect
                        )
                    )
                    drawPath(outerPath, accent, style = Stroke(outerStroke))

                    // inner field
                    drawPath(innerPath, background)
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
                        drawCircle(background, starInner * 0.55f, c)
                        drawCircle(primary, starInner * 0.55f, c, style = Stroke(0.8.dp.toPx()))
                        drawCircle(primary, 1.8.dp.toPx(), c)
                    }

                    diamonds.forEach { drawPath(it, accent) }
                    tipDots.forEach { drawCircle(accent, 1.8.dp.toPx(), it) }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        OttrojjaText(
            text = "سورة $chapterName",
            style = OttrojjaTheme.typography.bodySpecialLarge,
            color = primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 78.dp)
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

/** Eight-pointed star (Rub el Hizb style), one point facing up. */
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

private fun diamondPath(center: Offset, half: Float): Path = Path().apply {
    moveTo(center.x, center.y - half)
    lineTo(center.x + half, center.y)
    lineTo(center.x, center.y + half)
    lineTo(center.x - half, center.y)
    close()
}
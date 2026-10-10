package com.ottrojja.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.*

@Composable
fun OttrojjaPrimaryTextDisplay(
    modifier: Modifier = Modifier,
    text: String,
    details: String? = null,
    loading: Boolean = false,
    onRefresh: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Helpers.generateBrandBrush(MaterialTheme.colorScheme.primary))
            .padding(8.dp, 16.dp)
            .heightIn(min = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Mandala overlay — drawn beneath the text
        Canvas(modifier = Modifier.matchParentSize()) {
            val ornamentSize = 190.dp.toPx()
            val stroke = 1.dp.toPx()
            val mandalaColor = Color.White.copy(alpha = 0.35f)

            // Top-right corner
            withTransform({
                translate(left = size.width - ornamentSize / 2, top = -ornamentSize / 2)
            }) {
                drawMandala(mandalaColor, ornamentSize, stroke)
            }

            // Bottom-left corner
            withTransform({
                translate(left = -ornamentSize / 2, top = size.height - ornamentSize / 2)
            }) {
                drawMandala(mandalaColor, ornamentSize, stroke)
            }
        }

        if (loading) {
            OttrojjaLoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = if (onRefresh != null) 44.dp else 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OttrojjaText(
                    text = text,
                    style = OttrojjaTheme.typography.bodySpecialLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (details?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.size(12.dp))
                    OttrojjaText(
                        text = details,
                        style = OttrojjaTheme.typography.bodySpecialMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Refresh button — top-left, on top of everything (only shown if onRefresh is provided)
        if (onRefresh != null) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Draws a layered, 12-fold Islamic mandala centered in a square of [diameter] x [diameter]
 * (origin at the top-left of that square).
 *
 * Layers (outer → inner):
 *  1. Triple concentric border rings
 *  2. Beaded ring of 48 dots
 *  3. Ring of 24 pointed lotus petals + 24 smaller petals between them
 *  4. Filled/outlined 12-pointed star with its inner ring
 *  5. Ring of 12 petals nestled between the star points
 *  6. Octagram (two interlaced squares)
 *  7. Ring of 8 inner petals
 *  8. Center rosette: ring, solid 8-pointed star and a dot
 */
private fun DrawScope.drawMandala(color: Color, diameter: Float, stroke: Float) {
    val c = Offset(diameter / 2, diameter / 2)
    val r = diameter / 2

    fun tone(k: Float) = color.copy(alpha = (color.alpha * k).coerceIn(0f, 1f))

    fun ring(f: Float, width: Float, k: Float) =
        drawCircle(tone(k), radius = r * f, center = c, style = Stroke(width = width * stroke))

    fun repeatRotated(count: Int, offsetDeg: Float = 0f, block: DrawScope.() -> Unit) {
        for (i in 0 until count) {
            rotate(offsetDeg + i * 360f / count, pivot = c) { block() }
        }
    }

    // 1. Border rings
    ring(0.98f, 1.3f, 1f)
    ring(0.94f, 0.6f, 0.7f)
    ring(0.85f, 1.0f, 0.9f)

    // 2. Beaded ring
    repeatRotated(48) {
        drawCircle(tone(0.9f), radius = 1.1f * stroke, center = Offset(c.x, c.y - r * 0.895f))
    }

    // 3. Lotus petals
    val bigPetal = petalPath(c, baseR = r * 0.64f, tipR = r * 0.84f, halfWidth = r * 0.075f)
    repeatRotated(24) {
        drawPath(bigPetal, tone(0.18f), style = Fill)
        drawPath(bigPetal, tone(1f), style = Stroke(width = 1f * stroke))
    }
    val smallPetal = petalPath(c, baseR = r * 0.64f, tipR = r * 0.76f, halfWidth = r * 0.04f)
    repeatRotated(24, offsetDeg = 7.5f) {
        drawPath(smallPetal, tone(0.6f), style = Stroke(width = 0.7f * stroke))
    }

    // 4. 12-pointed star
    val star12 = starPath(c, points = 12, outerR = r * 0.64f, innerR = r * 0.47f)
    drawPath(star12, tone(0.08f), style = Fill)
    drawPath(star12, tone(1f), style = Stroke(width = 1.2f * stroke))
    ring(0.47f, 0.8f, 0.7f)

    // 5. 12 petals sitting in the valleys of the star
    val midPetal = petalPath(c, baseR = r * 0.27f, tipR = r * 0.50f, halfWidth = r * 0.07f)
    repeatRotated(12, offsetDeg = 15f) {
        drawPath(midPetal, tone(0.22f), style = Fill)
        drawPath(midPetal, tone(1f), style = Stroke(width = 1f * stroke))
    }

    // 6. Octagram from two interlaced squares
    val squareA = polygonPath(c, sides = 4, radius = r * 0.34f, rotationDeg = 0f)
    val squareB = polygonPath(c, sides = 4, radius = r * 0.34f, rotationDeg = 45f)
    drawPath(squareA, tone(0.9f), style = Stroke(width = 1f * stroke))
    drawPath(squareB, tone(0.9f), style = Stroke(width = 1f * stroke))
    ring(0.27f, 0.7f, 0.6f)

    // 7. Inner petals
    val innerPetal = petalPath(c, baseR = r * 0.12f, tipR = r * 0.27f, halfWidth = r * 0.055f)
    repeatRotated(8, offsetDeg = 22.5f) {
        drawPath(innerPetal, tone(0.3f), style = Fill)
        drawPath(innerPetal, tone(1f), style = Stroke(width = 0.9f * stroke))
    }

    // 8. Center rosette
    ring(0.12f, 0.9f, 0.9f)
    drawPath(starPath(c, points = 8, outerR = r * 0.10f, innerR = r * 0.045f), tone(1f), style = Fill)
    drawCircle(tone(1f), radius = 1.6f * stroke, center = c)
}

/**
 * A pointed (ogee-like) petal pointing straight up from the center [c],
 * starting at [baseR] and ending at [tipR]. Rotate the canvas to place copies.
 */
private fun petalPath(c: Offset, baseR: Float, tipR: Float, halfWidth: Float): Path {
    val len = tipR - baseR
    return Path().apply {
        moveTo(c.x, c.y - baseR)
        cubicTo(
            c.x + halfWidth, c.y - (baseR + len * 0.30f),
            c.x + halfWidth * 0.55f, c.y - (baseR + len * 0.75f),
            c.x, c.y - tipR
        )
        cubicTo(
            c.x - halfWidth * 0.55f, c.y - (baseR + len * 0.75f),
            c.x - halfWidth, c.y - (baseR + len * 0.30f),
            c.x, c.y - baseR
        )
        close()
    }
}

/** Star polygon with [points] points, alternating between [outerR] and [innerR]. */
private fun starPath(c: Offset, points: Int, outerR: Float, innerR: Float): Path {
    val totalVerts = points * 2
    return Path().apply {
        for (i in 0 until totalVerts) {
            val angle = (i * 180.0 / points - 90.0).toRadians()
            val rad = if (i % 2 == 0) outerR else innerR
            val x = c.x + rad * cos(angle).toFloat()
            val y = c.y + rad * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

/** Regular polygon with [sides] sides, rotated by [rotationDeg]. */
private fun polygonPath(c: Offset, sides: Int, radius: Float, rotationDeg: Float): Path {
    return Path().apply {
        for (i in 0 until sides) {
            val angle = (rotationDeg + i * 360.0 / sides - 90.0).toRadians()
            val x = c.x + radius * cos(angle).toFloat()
            val y = c.y + radius * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

private fun Double.toRadians() = this * PI / 180.0
package com.ottrojja.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.*

@Composable
fun OttrojjaPrimaryTextDisplay(text: String, details: String? = null, loading: Boolean = false) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Helpers.ottrojjaBrush)
            .padding(8.dp, 16.dp)
            .heightIn(min = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Mandala overlay — drawn beneath the text
        Canvas(modifier = Modifier.matchParentSize()) {
            val ornamentSize = 320f
            val mandalaColor = Color.White.copy(alpha = 0.5f)

            // Top-right corner
            withTransform({
                translate(left = size.width - ornamentSize / 2, top = -ornamentSize / 2)
            }) {
                drawIslamicOrnament(mandalaColor, ornamentSize)
            }

            // Bottom-left corner
            withTransform({
                translate(left = -ornamentSize / 2, top = size.height - ornamentSize / 2)
            }) {
                drawIslamicOrnament(mandalaColor, ornamentSize)
            }
        }

        if (loading) {
            OttrojjaLoadingIndicator()
        } else {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = text,
                    style = OttrojjaTheme.typography.bodySpecialLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (details?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = details,
                        style = OttrojjaTheme.typography.bodySpecialMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Draws a layered Islamic geometric ornament centered at (0,0)
 * within a square of [size] x [size].
 *
 * Layers (inner → outer):
 *  1. Solid 8-pointed star (filled)
 *  2. Ring of 8 teardrop petals
 *  3. Outlined 12-pointed star
 *  4. Ring of 12 small diamonds
 *  5. Outer interlaced circle ring
 */
private fun DrawScope.drawIslamicOrnament(color: Color, size: Float) {
    val cx = size / 2
    val cy = size / 2
    val r = size / 2

    // — Layer 5: outer dashed circle band —
    drawCircle(
        color = color,
        radius = r * 0.96f,
        center = Offset(cx, cy),
        style = Stroke(width = 0.8f)
    )
    drawCircle(
        color = color,
        radius = r * 0.88f,
        center = Offset(cx, cy),
        style = Stroke(width = 0.5f)
    )

    // — Layer 4: ring of 12 small diamonds —
    val d4Count = 12
    val d4Radius = r * 0.82f
    val d4Size = r * 0.055f
    for (i in 0 until d4Count) {
        val angle = (i * 360.0 / d4Count).toRadians()
        val dx = cx + d4Radius * cos(angle).toFloat()
        val dy = cy + d4Radius * sin(angle).toFloat()
        withTransform({ translate(dx, dy); rotate((i * 360f / d4Count) + 45f, Offset.Zero) }) {
            val diamond = Path().apply {
                moveTo(0f, -d4Size)
                lineTo(d4Size * 0.6f, 0f)
                lineTo(0f, d4Size)
                lineTo(-d4Size * 0.6f, 0f)
                close()
            }
            drawPath(diamond, color = color)
        }
    }

    // — Layer 3: 12-pointed outlined star —
    val star12Path = buildStarPath(
        cx = cx, cy = cy,
        points = 12,
        outerR = r * 0.72f,
        innerR = r * 0.44f
    )
    drawPath(star12Path, color = color, style = Stroke(width = 1.2f))

    // — Layer 2: ring of 8 teardrop petals —
    val petalCount = 8
    val petalRadius = r * 0.38f
    val petalLen = r * 0.28f
    val petalWidth = r * 0.10f
    for (i in 0 until petalCount) {
        val angle = (i * 360.0 / petalCount).toRadians()
        val px = cx + petalRadius * cos(angle).toFloat()
        val py = cy + petalRadius * sin(angle).toFloat()
        withTransform({
            translate(px, py)
            rotate((i * 360f / petalCount) + 90f, Offset.Zero)
        }) {
            val petal = Path().apply {
                moveTo(0f, -petalLen * 0.5f)          // tip
                cubicTo(
                    petalWidth, -petalLen * 0.15f,
                    petalWidth, petalLen * 0.3f,
                    0f, petalLen * 0.5f
                )
                cubicTo(
                    -petalWidth, petalLen * 0.3f,
                    -petalWidth, -petalLen * 0.15f,
                    0f, -petalLen * 0.5f
                )
                close()
            }
            drawPath(petal, color = color, style = Stroke(width = 1f))
        }
    }

    // — Layer 1: solid 8-pointed star (centre) —
    val star8Path = buildStarPath(
        cx = cx, cy = cy,
        points = 8,
        outerR = r * 0.22f,
        innerR = r * 0.10f
    )
    drawPath(star8Path, color = color)
}

/**
 * Builds a star polygon path with [points] points,
 * alternating between [outerR] and [innerR].
 */
private fun buildStarPath(
    cx: Float, cy: Float,
    points: Int,
    outerR: Float,
    innerR: Float
): Path {
    val totalVerts = points * 2
    return Path().apply {
        for (i in 0 until totalVerts) {
            val angle = (i * 180.0 / points - 90.0).toRadians()
            val r = if (i % 2 == 0) outerR else innerR
            val x = cx + r * cos(angle).toFloat()
            val y = cy + r * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

private fun Double.toRadians() = this * PI / 180.0
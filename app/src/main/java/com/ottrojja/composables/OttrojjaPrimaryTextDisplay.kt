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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Corner ornament drawn behind the text of [OttrojjaPrimaryTextDisplay]. */
enum class OttrojjaOrnament {
    /** Eight-pointed star: the classic {8/3} star around the khatam (two interlaced squares). */
    Khatam,

    /** Twelve-fold star: four interlaced triangles around three interlaced squares. */
    Najma,

    /** Sunburst medallion as found on illuminated manuscript pages, with radiating finials. */
    Shamsa
}

@Composable
fun OttrojjaPrimaryTextDisplay(
    modifier: Modifier = Modifier,
    text: String,
    details: String? = null,
    loading: Boolean = false,
    ornament: OttrojjaOrnament = OttrojjaOrnament.Shamsa,
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
        // Ornament overlay — drawn beneath the text
        Canvas(modifier = Modifier.matchParentSize()) {
            val ornamentSize = 190.dp.toPx()
            val stroke = 1.dp.toPx()
            val ornamentColor = Color.White.copy(alpha = 0.35f)

            // Top-right corner
            withTransform({
                translate(left = size.width - ornamentSize / 2, top = -ornamentSize / 2)
            }) {
                drawOrnament(ornament, ornamentColor, ornamentSize, stroke)
            }

            // Bottom-left corner
            withTransform({
                translate(left = -ornamentSize / 2, top = size.height - ornamentSize / 2)
            }) {
                drawOrnament(ornament, ornamentColor, ornamentSize, stroke)
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

        // Refresh button — top-end, on top of everything (only shown if onRefresh is provided)
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
 * Draws [ornament] centered in a square of [diameter] x [diameter]
 * (origin at the top-left of that square).
 */
private fun DrawScope.drawOrnament(
    ornament: OttrojjaOrnament,
    color: Color,
    diameter: Float,
    stroke: Float
) {
    val pen = OrnamentPen(
        scope = this,
        c = Offset(diameter / 2, diameter / 2),
        r = diameter / 2,
        stroke = stroke,
        color = color
    )
    when (ornament) {
        OttrojjaOrnament.Khatam -> pen.khatam()
        OttrojjaOrnament.Najma -> pen.najma()
        OttrojjaOrnament.Shamsa -> pen.shamsa()
    }
}

// ---------------------------------------------------------------------------------------------
// The three ornaments. Radii are fractions of the ornament radius, widths are multiples of the
// base stroke, and `alpha` / `fill` are multiples of the base color's alpha.
// ---------------------------------------------------------------------------------------------

/** Beaded border, an {8/3} star, and the khatam (two interlaced squares) locked into its core. */
private fun OrnamentPen.khatam() {
    ring(0.98f, width = 1.2f)
    ring(0.90f, width = 0.6f, alpha = 0.7f)
    beads(count = 32, radius = 0.94f, size = 1.0f, rotation = 5.625f, alpha = 0.9f)

    star(points = 8, skip = 3, radius = 0.90f, width = 1.1f, fill = 0.10f)
    beads(count = 8, radius = 0.72f, size = 1.6f, rotation = 22.5f)

    // 0.373 = radius at which the {8/3} star's innermost crossings sit, so the squares lock in.
    star(points = 8, skip = 2, radius = 0.373f, width = 1.0f, fill = 0.12f)
    ring(0.19f, width = 0.7f, alpha = 0.8f)
    rosette(points = 8, outer = 0.15f, inner = 0.07f, rotation = 22.5f, width = 0f, fill = 1f)
}

/** Beaded border, four interlaced triangles ({12/4}) and three interlaced squares ({12/3}). */
private fun OrnamentPen.najma() {
    ring(0.98f, width = 1.2f)
    ring(0.90f, width = 0.6f, alpha = 0.7f)
    beads(count = 48, radius = 0.94f, size = 1.0f, rotation = 3.75f, alpha = 0.9f)

    star(points = 12, skip = 4, radius = 0.90f, width = 1.1f, fill = 0.08f)
    star(points = 12, skip = 3, radius = 0.466f, rotation = 15f, width = 1.0f, fill = 0.10f)

    ring(0.22f, width = 0.7f, alpha = 0.8f)
    rosette(points = 12, outer = 0.17f, inner = 0.10f, width = 0f, fill = 1f)
}

/** Sixteen-pointed sunburst medallion with finials, holding an {8/3} star and a small khatam. */
private fun OrnamentPen.shamsa() {
    rays(count = 16, from = 0.82f, to = 0.98f, width = 1.0f)
    beads(count = 16, radius = 0.99f, size = 1.3f)
    beads(count = 16, radius = 0.86f, size = 0.9f, rotation = 11.25f, alpha = 0.8f)

    rosette(points = 16, outer = 0.78f, inner = 0.66f, width = 1.1f, fill = 0.12f)
    ring(0.60f, width = 1.0f)
    ring(0.55f, width = 0.6f, alpha = 0.7f)

    star(points = 8, skip = 3, radius = 0.55f, width = 1.0f, fill = 0.10f)
    star(points = 8, skip = 2, radius = 0.228f, width = 0.9f, fill = 0.12f)
    rosette(points = 8, outer = 0.10f, inner = 0.045f, rotation = 22.5f, width = 0f, fill = 1f)
}

// ---------------------------------------------------------------------------------------------
// Drawing primitives
// ---------------------------------------------------------------------------------------------

private class OrnamentPen(
    private val scope: DrawScope,
    private val c: Offset,
    private val r: Float,
    private val stroke: Float,
    private val color: Color
) {
    private fun tone(k: Float) = color.copy(alpha = (color.alpha * k).coerceIn(0f, 1f))

    /** Point at [fraction] of the radius, [degrees] clockwise from straight up. */
    private fun point(fraction: Float, degrees: Float): Offset {
        val a = (degrees - 90.0) * PI / 180.0
        return Offset(c.x + r * fraction * cos(a).toFloat(), c.y + r * fraction * sin(a).toFloat())
    }

    private fun Path.polygon(vertices: List<Offset>) {
        vertices.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        close()
    }

    private fun draw(path: Path, width: Float, alpha: Float, fill: Float) {
        if (fill > 0f) scope.drawPath(path, tone(fill), style = Fill)
        if (width > 0f) scope.drawPath(path, tone(alpha), style = Stroke(width = width * stroke))
    }

    fun ring(radius: Float, width: Float, alpha: Float = 1f) {
        scope.drawCircle(
            tone(alpha), radius = r * radius, center = c, style = Stroke(width = width * stroke)
        )
    }

    /**
     * Star polygon {points/skip}: every vertex is joined to the one [skip] steps further round.
     * When the two numbers share a factor this yields interlaced polygons, e.g. {8/2} is two
     * squares and {12/4} is four triangles.
     */
    fun star(
        points: Int,
        skip: Int,
        radius: Float,
        rotation: Float = 0f,
        width: Float = 1f,
        alpha: Float = 1f,
        fill: Float = 0f
    ) {
        val strands = gcd(points, skip)
        val path = Path()
        for (s in 0 until strands) {
            path.polygon(List(points / strands) { j ->
                point(radius, rotation + ((s + j * skip) % points) * 360f / points)
            })
        }
        draw(path, width, alpha, fill)
    }

    /** Zig-zag star outline alternating between the [outer] and [inner] radius. */
    fun rosette(
        points: Int,
        outer: Float,
        inner: Float,
        rotation: Float = 0f,
        width: Float = 1f,
        alpha: Float = 1f,
        fill: Float = 0f
    ) {
        val path = Path().apply {
            polygon(List(points * 2) { i ->
                point(if (i % 2 == 0) outer else inner, rotation + i * 180f / points)
            })
        }
        draw(path, width, alpha, fill)
    }

    /** [count] dots evenly spaced on a circle; [size] is the dot radius in stroke units. */
    fun beads(count: Int, radius: Float, size: Float, rotation: Float = 0f, alpha: Float = 1f) {
        for (i in 0 until count) {
            scope.drawCircle(
                tone(alpha), radius = size * stroke, center = point(radius, rotation + i * 360f / count)
            )
        }
    }

    /** [count] radial lines running from the [from] radius out to the [to] radius. */
    fun rays(
        count: Int,
        from: Float,
        to: Float,
        width: Float,
        rotation: Float = 0f,
        alpha: Float = 1f
    ) {
        for (i in 0 until count) {
            val deg = rotation + i * 360f / count
            scope.drawLine(
                tone(alpha),
                start = point(from, deg),
                end = point(to, deg),
                strokeWidth = width * stroke,
                cap = StrokeCap.Round
            )
        }
    }

    private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
}
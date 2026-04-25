package com.ottrojja.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OttrojjaLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    val transition = rememberInfiniteTransition(label = "legacy-loader")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val radius = (size.toPx() - stroke) / 2f
        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)

        // --- 1. Fixed track (background ring)
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(
                width = stroke
            )
        )

        // --- 2. Rotating arc (foreground indicator)
        drawArc(
            color = indicatorColor,
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round
            ),
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2)
        )
    }
}
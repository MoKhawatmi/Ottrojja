package com.ottrojja.composables.floatingNavigationDock

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ottrojja.R

@Composable
fun FloatingNavOrb(
    compSize: Dp = 64.dp,
    baseColor: Color = Color(0xFF194D65),
    onClick: () -> Unit,
    expanded: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val infinite = rememberInfiniteTransition(label = "orb")

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "icon_rotation"
    )

    val scale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOut), RepeatMode.Reverse),
        label = "scale"
    )
    val lightShift by infinite.animateFloat(
        initialValue = -0.3f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOut), RepeatMode.Reverse),
        label = "lightShift"
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val glowRadius by infinite.animateFloat(
        initialValue = 1.4f, targetValue = 1.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glowRadius"
    )

    Box(
        modifier = Modifier
            .size(compSize)              // ← layout footprint stays exactly compSize
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                // allow children to draw outside this box's bounds
                clip = false
            }
            .drawWithContent {
                val orbRadius = size.minDimension / 2f

                // ── Golden ring — overflows bounds freely, no layout impact ──
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFE566).copy(alpha = 0.45f * glowPulse),
                            Color(0xFFFFD700).copy(alpha = 0.22f * glowPulse),
                            Color.Transparent
                        ),
                        radius = orbRadius * glowRadius * 0.95f
                    ),
                    radius = orbRadius * glowRadius * 0.95f
                )

                // ── Hot spot ──────────────────────────────────────────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFCC).copy(alpha = 0.6f * glowPulse),
                            Color(0xFFFFD700).copy(alpha = 0.3f * glowPulse),
                            Color.Transparent
                        ),
                        radius = orbRadius * 0.55f
                    ),
                    radius = orbRadius * 0.55f
                )

                // ── Draw the actual orb content on top ────────────────────────
                drawContent()
            }
            .shadow(
                elevation = 28.dp,
                shape = CircleShape,
                ambientColor = baseColor.copy(alpha = 0.35f),
                spotColor = baseColor.copy(alpha = 0.7f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.95f),
                        baseColor.copy(alpha = 1f),
                        Color(0xFF0B2230)
                    ),
                    center = Offset(x = 0.5f + lightShift, y = 0.3f),
                    radius = 500f
                ),
                shape = CircleShape
            )
            .border(
                width = 1.2.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.35f)
                    )
                ),
                shape = CircleShape
            )
            .drawBehind {
                val r = size.minDimension * 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF5FD3FF).copy(alpha = 0.25f), Color.Transparent),
                        radius = size.minDimension
                    ),
                    radius = r
                )
                drawCircle(color = Color.Black.copy(alpha = 0.12f), radius = r * 0.85f)
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Icon(
            painter = painterResource(R.drawable.docking_toggle),
            contentDescription = null,
            tint = Color(0xFFFFE566),
            modifier = Modifier.align(Alignment.Center).rotate(rotation)
        )
    }
}
package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.screens.Khitmah.khitmahScreen.KhitmahViewModel
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.math.roundToInt


@Composable
fun PartProgress(
    viewModel: KhitmahViewModel
) {
    val parts by viewModel.parts.collectAsStateWithLifecycle()

    val completedParts = parts.count {
        viewModel.calculatePartProgress(it) >= 1f
    }

    val overallProgress = if (parts.isNotEmpty()) {
        completedParts.toFloat() / parts.size
    } else {
        0f
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OttrojjaText(
            text = "إكتمال الاجزاء",
            style = OttrojjaTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        OttrojjaText(
            text = "$completedParts مكتمل من اصل ${parts.size} · ${(overallProgress * 100).roundToInt()}%",
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )


        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(
                items = parts,
                key = { it.partId }
            ) { part ->

                val progress = viewModel.calculatePartProgress(part)

                PartProgressItem(
                    part = part,
                    progress = progress
                )
            }
        }
    }
}


@Composable
private fun PartProgressItem(
    part: PartData,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PartProgressCircle(
            progress = progress
        )

        when {
            progress >= 1f -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(20.dp)
                )
            }

            progress > 0f -> {
                OttrojjaText(
                    text = "${(progress * 100).roundToInt()}%",
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        OttrojjaText(
            text = part.partName,
            style = OttrojjaTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )

    }
}


@Composable
private fun PartProgressCircle(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val targetProgress = progress.coerceIn(0f, 1f)

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(targetProgress) {
        animatedProgress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
    }

    val fillColor = if (targetProgress >= 1f) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Canvas(
        modifier = modifier.size(72.dp)
    ) {
        drawCircle(
            color = Color(0xFFE2E2EC)
        )

        if (animatedProgress.value > 0f) {
            drawArc(
                color = fillColor,
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = true
            )
        }
    }
}
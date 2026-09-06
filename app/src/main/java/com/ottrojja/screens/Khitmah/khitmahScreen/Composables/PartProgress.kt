package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaDetailsContainer
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

    val linearAnimatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(
            durationMillis = 600
        ),
        label = "total juz progress"
    )


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OttrojjaDetailsContainer(modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                modifier = Modifier.padding(8.dp)
            ) {

                Image(
                    painter = painterResource(R.drawable.progress_header),
                    contentDescription = "",
                    modifier = Modifier.size(84.dp)
                )

                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)) {
                    OttrojjaText(
                        text = "إكتمال الاجزاء",
                        style = OttrojjaTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    OttrojjaText(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.primary)
                            ) {
                                append(completedParts.toString())
                            }
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                append(" مكتمل من اصل ${parts.size} · ")
                            }
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.primary)
                            ) {
                                append("${(overallProgress * 100).roundToInt()}%")
                            }

                        },
                        style = OttrojjaTheme.typography.bodyMedium,
                    )

                    LinearProgressIndicator(
                        progress = { linearAnimatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (completedParts == 30) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Butt,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
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
            progress = progress,
            partNumber = part.partId.toInt()
        )

        when {
            progress >= 1f -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(24.dp)
                )
            }

            progress >= 0f -> {
                OttrojjaText(
                    text = "${(progress * 100).roundToInt()}%",
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

    }
}


@Composable
private fun PartProgressCircle(
    progress: Float,
    partNumber: Int,
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


    val textMeasurer = rememberTextMeasurer()
    val textStyle = OttrojjaTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.Bold,
        color = if (targetProgress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    )

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

        val measuredText = textMeasurer.measure(
            text = Helpers.convertToIndianNumbers(partNumber.toString()),
            style = textStyle
        )

        drawText(
            textLayoutResult = measuredText,
            topLeft = Offset(
                x = (size.width - measuredText.size.width) / 2f,
                y = (size.height - measuredText.size.height) / 2f
            ),
        )
    }
}
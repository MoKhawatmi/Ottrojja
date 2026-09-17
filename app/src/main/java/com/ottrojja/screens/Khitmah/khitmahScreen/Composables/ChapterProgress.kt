package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.screens.Khitmah.classes.SurahProgress
import com.ottrojja.ui.theme.OttrojjaTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ChapterProgress(
    surahProgress: List<SurahProgress>,
    modifier: Modifier = Modifier
) {
    val completedSurahs = surahProgress.count {
        it.progress >= 1f
    }

    val overallProgress = if (surahProgress.isNotEmpty()) {
        completedSurahs.toFloat() / surahProgress.size
    } else {
        0f
    }

    val linearAnimatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(
            durationMillis = 600
        ),
        label = "total chapter progress"
    )


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OttrojjaDetailsContainer(modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
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
                        text = "إكتمال السور",
                        style = OttrojjaTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    OttrojjaText(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.primary)
                            ) {
                                append("$completedSurahs")
                            }
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                append(" سورة مكتملة من اصل ${surahProgress.size} · ")
                            }
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.primary)
                            ) {
                                append("${String.format(Locale.US, "%.2f", (overallProgress * 100))}%")
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
                        color = if (completedSurahs == 114) {
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


        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = surahProgress,
                key = { it.chapter.surahId }
            ) { surah ->
                ChapterProgressItem(
                    surah = surah,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }

}

@Composable
private fun ChapterProgressItem(
    surah: SurahProgress,
    modifier: Modifier = Modifier
) {
    val progress = surah.progress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 600
        ),
        label = "surah_progress_${surah.chapter.surahId}"
    )

    val isComplete = progress >= 1f

    OttrojjaDetailsContainer() {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OttrojjaText(
                        text = "${surah.chapter.surahId}",
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .wrapContentSize(Alignment.Center)
                    )

                    OttrojjaText(
                        text = "${surah.chapter.chapterName}",
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    OttrojjaText(
                        text = "${(progress * 100).toInt()}%",
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (isComplete) {
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
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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

@Composable
private fun SurahHeader(chapterName: String, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                // soft fill + double frame
                drawRoundRect(primary.copy(alpha = 0.08f), cornerRadius = CornerRadius(12.dp.toPx()))
                drawRoundRect(
                    color = primary,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
                val inset = 5.dp.toPx()
                drawRoundRect(
                    color = primary.copy(alpha = 0.55f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // eight-pointed star (two overlapping squares) at both ends
                val starSize = 14.dp.toPx()
                val cy = size.height / 2
                listOf(26.dp.toPx(), size.width - 26.dp.toPx()).forEach { cx ->
                    val topLeft = Offset(cx - starSize / 2, cy - starSize / 2)
                    val squareSize = Size(starSize, starSize)
                    drawRect(primary, topLeft, squareSize, style = Stroke(1.5.dp.toPx()))
                    rotate(45f, Offset(cx, cy)) {
                        drawRect(primary, topLeft, squareSize, style = Stroke(1.5.dp.toPx()))
                    }
                    drawCircle(primary, radius = 2.dp.toPx(), center = Offset(cx, cy))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        OttrojjaText(
            text = "سورة $chapterName",
            style = OttrojjaTheme.typography.bodySpecialLarge,
            color = primary,
            modifier = Modifier.padding(horizontal = 52.dp)
        )
    }
}
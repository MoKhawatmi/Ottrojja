package com.ottrojja.screens.Khitmah.khitmahListScreen.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.constants.QuranConstants.TOTAL_QURAN_VERSES
import kotlin.math.roundToInt

@Composable
fun KhitmahItemProgress(
    totalVersesCovered: Int,
    modifier: Modifier = Modifier
) {
    val progress = (totalVersesCovered / TOTAL_QURAN_VERSES.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).roundToInt()

    Box(
        modifier = modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Butt,
            gapSize = 0.dp,
            color = if(percentage==100) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )

        OttrojjaText(
            text = "$percentage%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
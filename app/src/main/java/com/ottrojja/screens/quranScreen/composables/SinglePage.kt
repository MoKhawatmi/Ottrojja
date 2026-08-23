package com.ottrojja.screens.quranScreen.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun SinglePage(pageNum: String, nightReadingMode: Boolean) {
    val pagePath = "p_$pageNum"
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.verticalScroll(
            rememberScrollState()
        ) else Modifier,

        ) {
        Image(
            painter = painterResource(
                LocalContext.current.getResources()
                    .getIdentifier(pagePath, "drawable", context.packageName)
            ),
            contentDescription = "page",
            modifier = Modifier
                .fillMaxWidth(
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 0.75f else 1.0f
                )
                .fillMaxHeight(0.92f),
            contentScale = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) ContentScale.Crop else ContentScale.Fit,
            colorFilter = if (!nightReadingMode) null else ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp, 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(2.dp)
                    .weight(0.5f)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {}
            OttrojjaText(
                text = "صفحة ${Helpers.convertToIndianNumbers(pageNum)}",
                style = OttrojjaTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(4.dp, 0.dp)
                    .offset(y = -2.dp)
            )
            Row(
                modifier = Modifier
                    .height(2.dp)
                    .weight(0.5f)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {}
        }
    }
}
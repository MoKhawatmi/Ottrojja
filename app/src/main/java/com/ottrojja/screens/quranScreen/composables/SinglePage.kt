package com.ottrojja.screens.quranScreen.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import com.ottrojja.room.entities.PageContent
import com.ottrojja.ui.theme.OttrojjaTheme

private val NightModeFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

/**
 * @param pageContent this page's rows (with polygons). Pass it only for the page the user is
 * on; null renders a plain, non-interactive image (e.g. the neighbours the pager pre-renders).
 */
@Composable
fun SinglePage(
    pageNum: String,
    nightReadingMode: Boolean,
    pageContent: List<PageContent>? = null,
    menuAnchor: VerseAnchor? = null,
    onVerseLongPress: (VerseAnchor) -> Unit = {},
    onDismissMenu: () -> Unit = {},
    onShareVerse: (PageContent) -> Unit = {},
    onTafseerVerse: (PageContent) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier,
    ) {
        if (isLandscape) {
            // Same as before: 75% of the width, full page height, scrollable.
            PageImage(
                pageNum, nightReadingMode, pageContent,
                menuAnchor, onVerseLongPress, onDismissMenu, onShareVerse, onTafseerVerse,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(PAGE_ASPECT_RATIO)
            )
        } else {
            // Same as before: the whole page fitted (and centred) in the available area,
            // but the overlay box now matches the drawn picture exactly.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                PageImage(
                    pageNum, nightReadingMode, pageContent,
                    menuAnchor, onVerseLongPress, onDismissMenu, onShareVerse, onTafseerVerse,
                    modifier = Modifier.aspectRatio(PAGE_ASPECT_RATIO)
                )
            }
        }

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

/** The page picture and its verse overlay, in one box with the image's exact aspect ratio. */
@Composable
private fun PageImage(
    pageNum: String,
    nightReadingMode: Boolean,
    pageContent: List<PageContent>?,
    menuAnchor: VerseAnchor?,
    onVerseLongPress: (VerseAnchor) -> Unit,
    onDismissMenu: () -> Unit,
    onShareVerse: (PageContent) -> Unit,
    onTafseerVerse: (PageContent) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        Image(
            painter = painterResource(
                context.resources.getIdentifier("p_$pageNum", "drawable", context.packageName)
            ),
            contentDescription = "page",
            contentScale = ContentScale.FillBounds, // box already has the image's ratio
            colorFilter = if (nightReadingMode) NightModeFilter else null,
            modifier = Modifier.matchParentSize()
        )
        if (pageContent != null) {
            VerseInteractionLayer(
                pageContent = pageContent,
                nightReadingMode = nightReadingMode,
                menuAnchor = menuAnchor,
                onVerseLongPress = onVerseLongPress,
                onDismissMenu = onDismissMenu,
                onShareVerse = onShareVerse,
                onTafseerVerse = onTafseerVerse,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

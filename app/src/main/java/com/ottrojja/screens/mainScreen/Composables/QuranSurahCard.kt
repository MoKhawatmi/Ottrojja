package com.ottrojja.screens.mainScreen.Composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSurahCard(onDismiss: () -> Unit, surah: ChapterData) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    val openPara = "\uFD3F"
    val closePara = "\uFD3E"


    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = null,
        sheetGesturesEnabled = false,
    ) {
        // Constrain the content to 75% of screen height, pinned to bottom
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp


        Box(modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.75f)
        ) {
            Column(modifier = Modifier
                .verticalScroll(scrollState)
            )
            {
                Box{
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/arch.svg")
                            .decoderFactory(SvgDecoder.Factory())
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                    Column(modifier = Modifier
                        .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.size(60.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OttrojjaText(buildString {
                                append(openPara)
                                append(" سورة ")
                                append(surah.chapterName)
                                append(" ")
                                append(closePara)
                            },
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodySpecialLarge
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .wrapContentWidth()
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
                                .padding(vertical = 4.dp, horizontal = 6.dp)

                        ) {
                            OttrojjaText(
                                text = "${surah.chapterType} وآياتها ${Helpers.convertToIndianNumbers("${surah.verseCount}")}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = OttrojjaTheme.typography.bodyMedium,
                                fontWeight = FontWeight(700)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CardItem(title = "معنى اسمها", value = surah.nameMeaning)
                    CardItem(title = "سبب تسميتها", value = surah.namingReason)
                    CardItem(title = "اسماؤها", value = surah.otherNames)
                    CardItem(title = "مقصدها العام", value = surah.generalPurpose)
                    CardItem(title = "سبب نزولها", value = surah.reasonOfRevelation)
                    CardItem(title = "فضلها", value = surah.virtues)
                    CardItem(title = "مناسباتها", value = surah.surahEvents)
                }
            }

            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(0.5f), shape = CircleShape)
                    .align(Alignment.TopEnd)
                    .clickable {
                        coroutineScope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            onDismiss()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Modal",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

        }
    }
}

@Composable
fun CardItem(title: String, value: String) {
    Column(modifier = Modifier.padding(top = 6.dp, bottom = 24.dp, start = 6.dp, end = 6.dp)) {
        OttrojjaDetailsContainer(title = title) {
            Column(modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.size(8.dp))

                value.split("|").forEach {
                    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                        Icon(painterResource(R.drawable.card_info),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Card Info",
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .offset(y = 2.dp)
                        )
                        OttrojjaText(it.trim(),
                            color = MaterialTheme.colorScheme.primary,
                            style = OttrojjaTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
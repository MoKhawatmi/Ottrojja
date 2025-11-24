package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.room.entities.PageContent
import com.ottrojja.screens.quranScreen.RepetitionTab

@Composable
fun ListeningOptionsDialog(
    onDismissRequest: () -> Unit,
    selectedVerse: PageContent?,
    selectedEndVerse: PageContent?,
    onSelectVerseClicked: () -> Unit,
    onSelectStartPageClicked: () -> Unit,
    onSelectEndVerseClicked: () -> Unit,
    onSelectEndPageClicked: () -> Unit,
    selectedRepetition: String,
    onSelectRepetitionClicked: () -> Unit,
    continuousPlay: Boolean,
    setContPlay: (Boolean) -> Unit,
    repetitionTabs: List<RepetitionTab>,
    selectedRepetitionTab: RepetitionTab,
    onSelectRepetitionTab: (RepetitionTab) -> Unit,
    startPlayingPage: Int,
    endPlayingPage: Int,
    takeOnCurrentPageParameters: () -> Unit
) {
    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = onDismissRequest,
        useDefaultWidth = false,
    ) {
        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "خيارات الإستماع", textAlign = TextAlign.Center)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { takeOnCurrentPageParameters() }) {
                    Text(
                        text = "الحالية",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }


            Row(modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                InteractiveSelection(
                    onClick = { onSelectStartPageClicked() },
                    titleText = "من صفحة",
                    contentText = "ص${startPlayingPage}",
                    disabled = continuousPlay,
                    modifier = Modifier.weight(1f)
                )

                InteractiveSelection(
                    onClick = { onSelectVerseClicked() },
                    titleText = "من اية",
                    contentText = if (selectedVerse != null) "اية: ${selectedVerse.verseNum}" else "الاية",
                    disabled = continuousPlay,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            Row(modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                InteractiveSelection(
                    onClick = { onSelectEndPageClicked() },
                    titleText = "الى صفحة",
                    contentText = "ص${endPlayingPage}",
                    disabled = continuousPlay,
                    modifier = Modifier.weight(1f)
                )

                InteractiveSelection(
                    onClick = { onSelectEndVerseClicked() },
                    titleText = "الى اية",
                    contentText = if (selectedEndVerse != null) "اية: ${selectedEndVerse.verseNum}" else "الاية",
                    disabled = continuousPlay,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRepetitionClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مرات التكرار", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier
                        .padding(0.dp, 6.dp, 6.dp, 5.dp)
                        .fillMaxWidth(0.5f)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = selectedRepetition,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectEndVerseClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OttrojjaTabs(
                    items = repetitionTabs,
                    selectedItem = selectedRepetitionTab,
                    onClickTab = { value -> onSelectRepetitionTab(value) },
                    tabPrefix = "تكرار "
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth()
                    .clickable { setContPlay(!continuousPlay) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("تشغيل متتال للصفحات", style = MaterialTheme.typography.bodyMedium)
                    Text("(غير متوفر في الخلفية حاليا)", style = MaterialTheme.typography.bodySmall)
                }
                Checkbox(
                    checked = continuousPlay,
                    onCheckedChange = { newCheckedState -> setContPlay(newCheckedState) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { onDismissRequest() }) {
                    Text(
                        text = "إغلاق",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveSelection(onClick: () -> Unit,
                         titleText: String,
                         contentText: String,
                         disabled: Boolean,
                         modifier: Modifier) {
    Column(
        modifier = modifier
            .clickable { if(!disabled) onClick(); },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(titleText, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Row(
            modifier = Modifier
                .padding(0.dp, 6.dp, 6.dp, 5.dp)
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(4.dp))
                .background(if (!disabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = contentText,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
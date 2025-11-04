package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import kotlin.collections.forEach

@Composable
fun SelectVerseDialog(
    onDismissRequest: () -> Unit,
    onSelect: (PageContent) -> Unit,
    versesList: List<PageContent>
) {
    OttrojjaDialog(onDismissRequest = { onDismissRequest() }, contentModifier = Modifier
        .padding(8.dp)
        .fillMaxHeight(0.4f)
        .background(MaterialTheme.colorScheme.secondary)
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            versesList.forEach { option ->
                if (option.type == PageContentItemType.verse) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .padding(6.dp)) {
                        Text(
                            text = "الاية ${option.verseNum}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = " سورة " + "${option.surahName}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

    }
}
package com.ottrojja.screens.listeningScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.screens.listeningScreen.ListeningViewModel
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun VerseSelectionDialog(
    onDismiss: () -> Unit,
    versesNum: Int,
    selectVerse: (Int) -> Unit,
    selectedVerse: Int=1
) {
    val verses = List(versesNum) { it + 1 }

    var searchFilter by remember { mutableStateOf("") }

    val filteredVerses = verses.filter { verse ->
        val verseNumber = verse.toString()
        val arabicVerseNumber = convertToArabicNumbers(verseNumber)

        verseNumber.contains(searchFilter) ||
                arabicVerseNumber.contains(searchFilter)
    }

    OttrojjaItemSelectionDialog(
        title = "إختيار الآية",
        onDismiss = { onDismiss() },
        searchBar = {
            PillShapedTextFieldWithIcon(
                value = searchFilter,
                onValueChange = { searchFilter = it },
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth(0.9f),
                placeHolder = "رقم الآية"
            )
        },
        selectionItems = filteredVerses,
        selectedItem= selectedVerse,
        onSelect = { verse ->
            selectVerse(verse)
            onDismiss()
        },
        itemContent = { verse ->
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaText(
                    text = convertToArabicNumbers(verse.toString()),
                    color = MaterialTheme.colorScheme.primary,
                    style = OttrojjaTheme.typography.bodyLarge
                )
            }
        }
    )
}
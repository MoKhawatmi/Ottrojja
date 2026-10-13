package com.ottrojja.screens.quranScreen

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.VerseTextWithNumber
import com.ottrojja.composables.ottrojjaFlexibleActions.FlexibleAction
import com.ottrojja.composables.ottrojjaFlexibleActions.OttrojjaFlexibleActions
import com.ottrojja.composables.quranRangeSelector.QuranRangeSelector
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.entities.PageContent
import com.ottrojja.screens.quranScreen.dialogs.ShareVersesBottomSheet
import com.ottrojja.ui.theme.OttrojjaTheme


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersesSection(
    items: List<PageContent>,
    onSheetRequest: (PageContent, TafseerSheetMode) -> Unit,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val versesSectionViewModel: VersesSectionViewModel = viewModel(
        factory = VersesSectionViewModelFactory(repository, application)
    )

    LaunchedEffect(items) {
        versesSectionViewModel.setItems(items);
    }
    val versesList = versesSectionViewModel.items.collectAsState();

    // The share sheet itself is hosted once in QuranScreen (ShareVersesHost), so it also works
    // when sharing is started from a long-press on the page.

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(versesList.value) { item ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable { versesSectionViewModel.updateExpanded(item) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerseTextWithNumber(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        text = item.pageContent.verseText ?: "",
                        number = item.pageContent.verseNum ?: 0
                    )
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Expand Verse Options",
                        modifier = Modifier.clickable {
                            versesSectionViewModel.updateExpanded(item)
                        }
                    )
                }
                AnimatedVisibility(
                    visible = item.expanded,
                ) {
                    OttrojjaFlexibleActions(
                        actionList = listOf(
                            FlexibleAction(
                                text = "مشاركة",
                                bgColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.Black,
                                action = { versesSectionViewModel.fetchChapterVerses(item.pageContent) }
                            ),
                            FlexibleAction(
                                text = "التفسير",
                                bgColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.التفسير) }
                            ),
                            FlexibleAction(
                                text = "الإعراب",
                                bgColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.Black,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.الإعراب) }
                            ),
                            FlexibleAction(
                                text = "سبب النزول",
                                bgColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.أسباب_النزول) }
                            ),
                            FlexibleAction(
                                text = "المفردات",
                                bgColor = MaterialTheme.colorScheme.tertiary,
                                textColor = Color.Black,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.معاني_المفردات) }
                            ),
                            FlexibleAction(
                                text = "المشابهات",
                                bgColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                action = { onSheetRequest(item.pageContent, TafseerSheetMode.المشابهات) }
                            ),
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(14.dp),
                thickness = 1.dp,
                color = Color.Black.copy(alpha = 0.1f)
            )
        }
    }
}


/** Shows the verses share sheet when [VersesSectionViewModel.showVerseShareDialog] is true. */
@Composable
fun ShareVersesHost(
    versesSectionViewModel: VersesSectionViewModel,
    repository: QuranRepository
) {
    if (versesSectionViewModel.showVerseShareDialog) {
        ShareVersesBottomSheet(
            onDismissRequest = { versesSectionViewModel.showVerseShareDialog = false },
            title = "مشاركة ايات من سورة ${versesSectionViewModel.shareChapter?.chapterName}",
            surah = versesSectionViewModel.shareChapter,
            startVerse = versesSectionViewModel.startingVerse,
            endVerse = versesSectionViewModel.endVerse,
            onRangeChanged = { newRange -> versesSectionViewModel.startingVerse = newRange.startVerse; versesSectionViewModel.endVerse = newRange.endVerse },
            repository = repository,
            shareAsImage = versesSectionViewModel.shareAsImage,
            onShareAsImageChange = { versesSectionViewModel.shareAsImage = it },
            selectedVerses = versesSectionViewModel.selectedVerses(),
            maxImageChars = VersesSectionViewModel.MAX_IMAGE_CHARS,
            onShareTextClick = { versesSectionViewModel.shareVerses() },
            onShareImageClick = { versesSectionViewModel.shareVerseImage(it) },
            onValidate = { versesSectionViewModel.validateSelection() },
        )
    }
}

package com.ottrojja.screens.Khitmah.khitmahListScreen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.OttrojjaDateTime
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.CircleStatusIndicator
import com.ottrojja.composables.EmptyListMessage
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.constants.QuranConstants.TOTAL_QURAN_VERSES
import com.ottrojja.navigateToQuran
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahForm
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormMode
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormState
import com.ottrojja.screens.Khitmah.khitmahListScreen.composables.KhitmahItemProgress
import com.ottrojja.screens.quranScreen.classes.QuranReaderContext
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun KhitmahList(
    navController: NavController,
    quranRepository: QuranRepository,
    reminderRepository: ReminderRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewmodel: KhitmahListViewModel = viewModel(
        factory = KhitmahListViewModelFactory(quranRepository, reminderRepository, application)
    )

    LaunchedEffect(Unit) {
        viewmodel.fetchKhitmahList()
    }


    if (viewmodel.khitmahDialog) {
        KhitmahForm(
            initialState = KhitmahFormState(
                mode = KhitmahFormMode.CREATE
            ),
            onDismiss = {
                viewmodel.khitmahDialog = false
            },
            onSubmit = { state ->
                viewmodel.createKhitmah(state)
                viewmodel.khitmahDialog = false
            }
        )
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary)
    ) {
        TopBar(title = "الختمات", mainAction = ButtonAction(Icons.Default.Add,
            action = { viewmodel.khitmahDialog = true })
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (viewmodel.khitmahList.size <= 0) {
                item {
                    EmptyListMessage("لا يوجد ختمات حاليا")
                }
            }

            items(viewmodel.khitmahList, key = { it.khitmah.id }) { item ->
                val khitmahCompleted = item.khitmah.versesCovered == TOTAL_QURAN_VERSES;
                OttrojjaDetailsContainer(
                    modifier = Modifier
                        .clickable { navController.navigate(Screen.KhitmahScreen.invokeRoute("${item.khitmah.id}")) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .fillMaxWidth()
                        ) {
                            CircleStatusIndicator(
                                status = khitmahCompleted,
                                truthyColor = MaterialTheme.colorScheme.secondary,
                                falsyColor = MaterialTheme.colorScheme.primary,
                                iconDescription = "Khitmah Status"
                            )
                            OttrojjaText(
                                text = item.khitmah.title,
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodyLarge,
                                textAlign = TextAlign.Right,
                            )
                        }
                        /*****/
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KhitmahItemProgress(
                                totalVersesCovered = item.khitmah.versesCovered
                            )
                            if (!khitmahCompleted) {
                                OttrojjaButton(
                                    text = if (item.latestEndPage != null) "متابعة" else "البدء",
                                    onClick = {
                                        navController.navigateToQuran(
                                            pageNum = "${item.latestEndPage ?: 1}",
                                            context = QuranReaderContext.Khitmah(
                                                khitmahId = item.khitmah.id,
                                                khitmahName = item.khitmah.title
                                            )
                                        )
                                    },
                                    iconAfter = Icons.Filled.PlayArrow
                                )
                            }
                        }
                        /*****/
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssistantPhoto,
                                contentDescription = "Start Date",
                                tint = if (khitmahCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )

                            OttrojjaText(
                                text = if (khitmahCompleted) "مكتملة" else "جارية",
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right,
                            )
                        }
                        /*****/
                        item.latestEndPage?.let {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Latest Page",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                OttrojjaText(
                                    text = "اخر صفحة: ${it}",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = OttrojjaTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Right,
                                )
                            }
                        }
                        /*****/
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EventNote,
                                contentDescription = "Session Count",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            OttrojjaText(
                                text = "عدد الجلسات: ${item.sessionCount}",
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right,
                            )
                        }
                        /*****/
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Start Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            OttrojjaText(
                                text = "تاريخ الإنشاء: ${OttrojjaDateTime.formatDate(item.khitmah.createdAt)}",
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right,
                            )
                        }

                        /*****/
                        item.latestReadingTimestamp?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = "Latest Reading Date",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                OttrojjaText(
                                    text = "آخر قراءة: ${OttrojjaDateTime.formatDateTime(it)}",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = OttrojjaTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Right,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.ottrojja.screens.khitmahListScreen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.ButtonAction
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.CircleStatusIndicator
import com.ottrojja.composables.EmptyListMessage
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.forms.OttrojjaTimePickerDialog
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.khitmahListScreen.classes.KhitmahStatus
import com.ottrojja.screens.khitmahListScreen.dialogs.KhitmahDialog
import com.ottrojja.ui.theme.OttrojjaTheme
import com.ottrojja.ui.theme.complete_green
import java.util.Calendar
import java.util.Locale

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
    val formState = viewmodel.formState.collectAsState()

    LaunchedEffect(Unit) {
        viewmodel.fetchKhitmahList()
    }

    var showKhitmahDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }



    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->

                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }

                            val dateString = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            viewmodel.updateKhitmahInWork(
                                formState.value.khitmah.copy(
                                    targetDate = dateString
                                )
                            )
                            showDatePicker = false
                        }
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("الغاء")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    if (showTimePicker) {
        OttrojjaTimePickerDialog(
            onDismiss = { showTimePicker = false },
            hour = formState.value.reminderTime
                .split(":")[0]
                .toInt(),
            minute = formState.value.reminderTime
                .split(":")[1]
                .toInt(),
            label = "توقيت مذكر الختمة",
            onConfirm = { h, m ->
                viewmodel.updateReminderTime("$h:$m")
                showTimePicker = false
            }
        )
    }

    if (showKhitmahDialog) {
        KhitmahDialog(
            onDismiss = {
                showKhitmahDialog = false;
                viewmodel.resetForm()
            },
            onConfirm = {
                viewmodel.createKhitmah()
                showKhitmahDialog = false
            },
            formState = formState.value,
            onKhitmahChange = {
                viewmodel.updateKhitmahInWork(it)
            },
            invokeTargetDatePicker = {
                showDatePicker = true
            },
            invokeReminderTimePicker = {
                showTimePicker = true
            },
            toggleIncludeReminder = {
                viewmodel.updateReminderEnabled(
                    !formState.value.reminderEnabled
                )
            },
            onReminderTimeChange = {
                viewmodel.updateReminderTime(it)
            }
        )
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary)
    ) {
        TopBar(title = "الختمات", mainAction = ButtonAction(Icons.Default.Add,
            action = { showKhitmahDialog = true })
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (viewmodel.khitmahList.size <= 0) {
                item {
                    EmptyListMessage("لا يوجد ختمات حاليا")
                }
            }

            items(viewmodel.khitmahList, key = { it.id }) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            navController.navigate(Screen.KhitmahScreen.invokeRoute("${item.id}"))
                        }
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        CircleStatusIndicator(
                            status = item.status == KhitmahStatus.COMPLETE,
                            truthyColor = complete_green,
                            falsyColor = MaterialTheme.colorScheme.primary,
                            iconDescription = "Khitmah Status"
                        )
                        OttrojjaText(
                            text = item.title,
                            color = MaterialTheme.colorScheme.primary,
                            style = OttrojjaTheme.typography.bodyLarge,
                            textAlign = TextAlign.Right,
                        )
                    }


                    /*if (item.latestPage.isNotBlank()) {
                        Button(onClick = {
                            navController.navigate(Screen.QuranScreen.invokeRoute(item.latestPage))
                        }, modifier = Modifier.fillMaxWidth()) {
                            OttrojjaText(
                                "ص${Helpers.convertToIndianNumbers(item.latestPage)}",
                                style = OttrojjaTheme.typography.bodyMedium,
                            )
                        }
                    }*/
                }
                ListHorizontalDivider()
            }
        }
    }
}
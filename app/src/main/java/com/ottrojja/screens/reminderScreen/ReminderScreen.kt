package com.ottrojja.screens.reminderScreen

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Helpers.formatMilitaryTime
import com.ottrojja.classes.Helpers.truncate
import com.ottrojja.composables.CircleStatusIndicator
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaWarningBar
import com.ottrojja.composables.SwitchWithIcon
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.forms.OttrojjaTimePickerDialog
import com.ottrojja.composables.ottrojjaFlexibleActions.FlexibleAction
import com.ottrojja.composables.ottrojjaFlexibleActions.OttrojjaFlexibleActions
import com.ottrojja.composables.overlayPermissionHandler.OverlayPermissionHandler
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.reminderScreen.dialogs.ReminderFormDialog
import com.ottrojja.screens.reminderScreen.dialogs.SelectReminderTypeDialog
import com.ottrojja.ui.theme.complete_green

@Composable
fun ReminderScreen(repository: ReminderRepository) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val reminderVM: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(repository, application)
    )


    val postNotificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // nothing
            } else {
                // nothing
            }
        }

    var notificationPermissionState by remember { mutableStateOf(false) }
    val reminders by reminderVM.reminders.collectAsState(initial = emptyList())
    val overlayPermissionHandler by reminderVM.overlayPermissionHandler.collectAsState(false)
    val floatingAzkarEnabled by reminderVM.enabledFloatingAzakar.collectAsState(false)

    LaunchedEffect(Unit) {
        reminderVM.insertMainReminder()
        reminderVM.getReminders()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!notificationPermissionState) {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    fun confirmDeleteReminder(reminder: Reminder) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("حذف المذكر")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذا المذكر؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            reminderVM.deleteReminder(reminder)
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    if (reminderVM.reminderDialog) {
        ReminderFormDialog(
            onDismiss = { reminderVM.dismissReminderForm() },
            formValidationResult = reminderVM.formValidationResult,
            onSubmit = { reminderVM.handleReminderFormSubmission() },
            invokeTimePicker = { reminderVM.timePickerDialog = true },
            invokeRepetitionOptions = { reminderVM.selectRepetitionDialog = true },
            reminderInWork = reminderVM.reminderInWorks,
            onReminderChange = { updatedReminder -> reminderVM.updateReminder(updatedReminder) },
            mode = reminderVM.dialogMode,
        )
    }

    if (reminderVM.selectRepetitionDialog) {
        SelectReminderTypeDialog(
            onDismiss = { reminderVM.selectRepetitionDialog = false },
            onSelect = { item -> reminderVM.updateReminder(reminderVM.reminderInWorks.copy(repeatType = item)) }
        )
    }

    if (reminderVM.timePickerDialog) {
        OttrojjaTimePickerDialog(
            onDismiss = { reminderVM.timePickerDialog = false },
            hour = reminderVM.reminderInWorks.hour,
            minute = reminderVM.reminderInWorks.minute,
            onConfirm = { hour, minute -> reminderVM.updateReminder(reminderVM.reminderInWorks.copy(hour = hour, minute = minute)) }
        )
    }

    if (overlayPermissionHandler) {
        OverlayPermissionHandler(
            onPermissionGranted = { reminderVM.activateFloatingAzkarService() },
            onPermissionDenied = { reminderVM.toggleFloatingAzkar(false) },
            onFinished = { reminderVM.toggleOverlayPermissionHandler(false) },
            forceShowRequest = true
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary)
    ) {
        TopBar(
            title = "المذكر",
            mainAction = ButtonAction(icon = Icons.Default.Add, action = { reminderVM.invokeAddDialog() })
        )
        if (!notificationPermissionState) {
            OttrojjaWarningBar(text = "الرجاء السماح للتطبيق بصلاحيات الإشعارات ليتمكن المذكر من العمل بشكل صحيح")
        }


        LazyColumn {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {}
                        .padding(12.dp, 2.dp)
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp, 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الأذكار العائمة",
                                color = Color.Black
                            )
                        }
                        Column {
                            SwitchWithIcon(
                                checked = floatingAzkarEnabled,
                                onCheckedChange = { reminderVM.toggleFloatingAzkar(it) },
                                icon = Icons.Default.Check
                            )
                        }
                    }
                }
            }
            items(reminders) { item ->
                ReminderItem(
                    reminder = item,
                    toggleExpand = { id -> reminderVM.toggleItemExpanded(id) },
                    toggleEnabled = { id -> reminderVM.toggleReminderEnabled(id) },
                    invokeEditDialog = { reminder -> reminderVM.invokeEditDialog(reminder) },
                    deleteReminder = { reminder -> confirmDeleteReminder(reminder) },
                    getNextTriggerTime = { reminder -> reminderVM.getNextTriggerTime(reminder) }
                )
                ListHorizontalDivider()
            }
        }


        /*OttrojjaButton(text = "test", onClick = { reminderVM.sendTestNotification() })
        OttrojjaButton(text = "test2", onClick = { reminderVM.sendTestScheduledNotification() })*/
    }
}

@Composable
fun ReminderItem(reminder: ExpandableItem<Reminder>,
                 toggleExpand: (Int) -> Unit,
                 toggleEnabled: (Int) -> Unit,
                 invokeEditDialog: (Reminder) -> Unit,
                 deleteReminder: (Reminder) -> Unit,
                 getNextTriggerTime: (Reminder) -> String
) {

    val rotation by animateFloatAsState(
        targetValue = if (reminder.expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    val nextTrigger = remember(reminder.data) {
        getNextTriggerTime(reminder.data)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { toggleExpand(reminder.data.id) }
            .padding(12.dp, 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp, 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                CircleStatusIndicator(
                    status = reminder.data.isEnabled,
                    truthyColor = complete_green,
                    falsyColor = MaterialTheme.colorScheme.error,
                    iconDescription = "Reminder Enabled"
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = reminder.data.title,
                        color = Color.Black
                    )
                    if (reminder.data.customMessage?.isNotBlank() == true) {
                        Text(text =
                            if (reminder.expanded) "${reminder.data.customMessage}" else "${reminder.data.customMessage?.truncate(30)}",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "expand reminder",
                modifier = Modifier
                    .rotate(rotation)
                    .padding(horizontal = 2.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { toggleExpand(reminder.data.id) }
                    ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        AnimatedVisibility(
            visible = reminder.expanded,
        ) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp, 8.dp),
                verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start
            ) {
                ReminderInfoRow(
                    label = "التوقيت",
                    value = formatMilitaryTime(reminder.data.hour, reminder.data.minute),
                    valueColor = MaterialTheme.colorScheme.primary
                )

                ReminderInfoRow(
                    label = "الإشعار القادم",
                    value = nextTrigger,
                    valueColor = MaterialTheme.colorScheme.primary
                )

                ReminderInfoRow(
                    label = "التكرار",
                    value = reminder.data.repeatType.displayName,
                    valueColor = MaterialTheme.colorScheme.primary
                )

                ReminderInfoRow(
                    label = if (reminder.data.isEnabled) {
                        "مفعل"
                    } else {
                        "غير مفعل"
                    },
                    value = "",
                    labelColor = if (reminder.data.isEnabled) {
                        complete_green
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    trailing = {
                        SwitchWithIcon(
                            checked = reminder.data.isEnabled,
                            onCheckedChange = { toggleEnabled(reminder.data.id) },
                            icon = Icons.Default.Check
                        )
                    }
                )

                OttrojjaFlexibleActions(
                    listOfNotNull(
                        FlexibleAction(
                            text = "تعديل",
                            bgColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            action = { invokeEditDialog(reminder.data) }
                        ),
                        if (!reminder.data.isMain) {
                            FlexibleAction(
                                text = "حذف",
                                bgColor = MaterialTheme.colorScheme.error,
                                textColor = MaterialTheme.colorScheme.onError,
                                action = { deleteReminder(reminder.data) }
                            )
                        } else null
                    )
                )
            }
        }
    }
}


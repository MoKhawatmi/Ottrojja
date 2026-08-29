package com.ottrojja.screens.Khitmah.khitmahScreen

import android.app.AlertDialog
import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.binayshaw7777.kotstep.model.LineDefault
import com.binayshaw7777.kotstep.model.StepDefaults
import com.binayshaw7777.kotstep.model.StepStyle
import com.binayshaw7777.kotstep.model.iconVerticalWithLabel
import com.binayshaw7777.kotstep.ui.vertical.VerticalStepper
import com.ottrojja.classes.ButtonAction
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.TopBar
import com.ottrojja.navigateToQuran
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahForm
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormMode
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormState
import com.ottrojja.screens.Khitmah.SessionForm.SessionForm
import com.ottrojja.screens.Khitmah.classes.KhitmahStatus
import com.ottrojja.screens.Khitmah.khitmahScreen.Composables.KhitmahCalendar
import com.ottrojja.screens.Khitmah.khitmahScreen.Composables.PartProgress
import com.ottrojja.screens.Khitmah.khitmahScreen.Composables.SessionStepItem
import com.ottrojja.screens.quranScreen.classes.QuranReaderContext
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun KhitmahScreen(
    navController: NavController,
    quranRepository: QuranRepository,
    reminderRepository: ReminderRepository,
    id: Int
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewmodel: KhitmahViewModel = viewModel(
        factory = KhitmahViewModelFactory(quranRepository, reminderRepository, application)
    )

    var editKhitmahForm by remember { mutableStateOf(false) }
    var editSessionForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewmodel.fetchKhitmah(id)
    }
    val khitmah by viewmodel.khitmah.collectAsState()
    val reminder by viewmodel.reminder.collectAsState()
    val sessions by viewmodel.sessions.collectAsState()
    val selectedSession by viewmodel.selectedSession.collectAsState()
    val monthSessionCount by viewmodel.calendarMonthProgress.collectAsState()

    fun confirmDeleteKhitmah() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("حذف الختمة")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذه الختمة؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            viewmodel.deleteKhitmah()
            dialog.dismiss()
            navController.popBackStack()
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    if (editKhitmahForm && khitmah != null) {
        KhitmahForm(
            initialState = KhitmahFormState(
                khitmah = khitmah!!,
                mode = KhitmahFormMode.EDIT,
                reminderEnabled = reminder != null,
                reminderTime = reminder?.let {
                    "%02d:%02d".format(it.hour, it.minute)
                } ?: "12:00"
            ),
            onDismiss = {
                editKhitmahForm = false
            },
            onSubmit = { formState ->
                viewmodel.updateKhitmah(formState)
                editKhitmahForm = false
            }
        )
    }

    if (editSessionForm) {
        selectedSession?.let { session ->
            SessionForm(
                khitmahId = session.session.khitmahId,
                startPage = session.session.startPage,
                endPage = session.session.endPage,
                session = session.session,
                onDismiss = { editSessionForm = false },
                onFinished = {},
                repository = quranRepository
            )
        }
    }


    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {

        TopBar(title = khitmah?.title ?: "",
            mainAction = ButtonAction(icon = Icons.Filled.ArrowBack,
                action = { navController.popBackStack() }),
            secondaryActions = listOf(
                ButtonAction(icon = Icons.Default.Edit, action = { editKhitmahForm = true },
                    title = "تعديل"
                ),
                if (khitmah?.status == KhitmahStatus.COMPLETE) {
                    ButtonAction(
                        icon = Icons.Outlined.Pending,
                        action = { viewmodel.toggleKhitmahStatus(); },
                        title = "تعيين كجارية"
                    )
                } else {
                    ButtonAction(
                        icon = Icons.Default.CheckCircle,
                        action = { viewmodel.toggleKhitmahStatus(); },
                        title = "تعيين كمكتملة"
                    )
                },
                ButtonAction(icon = Icons.Default.Close, action = { confirmDeleteKhitmah(); },
                    title = "حذف"
                ),
            )
        )

        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OttrojjaText(
                    text = "ابدأ ختمتك",
                    style = OttrojjaTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                OttrojjaText(
                    text = "لم تبدأ هذه الختمة بعد.",
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal
                )
                if (khitmah != null) {
                    OttrojjaButton(
                        modifier = Modifier.padding(8.dp),
                        text = "ابدأ الان",
                        onClick = {
                            navController.navigateToQuran(
                                pageNum = "1",
                                context = QuranReaderContext.Khitmah(
                                    khitmahId = khitmah!!.id,
                                    khitmahName = khitmah!!.title
                                )
                            )
                        }
                    )
                }
            }
        } else {
            OttrojjaButton(
                modifier = Modifier.padding(8.dp),
                text = "متابعة الختمة",
                onClick = {
                    navController.navigateToQuran(
                        pageNum = sessions.last().endPage.toString(),
                        context = QuranReaderContext.Khitmah(
                            khitmahId = khitmah!!.id,
                            khitmahName = khitmah!!.title
                        )
                    )
                }
            )

            PartProgress(viewmodel)

            OttrojjaDetailsContainer(modifier = Modifier.padding(8.dp)) {
                KhitmahCalendar(
                    sessions = sessions,
                    sessionCount = monthSessionCount,
                    onMonthChanged = { month ->
                        viewmodel.calendarMonthChanged(month)
                    }
                )
            }

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 8.dp)
            ) {
                VerticalStepper(
                    style = iconVerticalWithLabel(
                        totalSteps = sessions.size,
                        icons = List(sessions.size) { Icons.Default.Check },
                        currentStep = if (khitmah?.status == KhitmahStatus.COMPLETE) sessions.size else sessions.size - 1,
                        trailingLabels = sessions.map { session ->
                            {
                                val selected = selectedSession

                                SessionStepItem(
                                    item = session,
                                    itemQuranRange = selected?.range,
                                    itemLoading = selected?.isLoading == true,
                                    itemExpanded = selected?.session?.id == session.id,
                                    onDelete = {},
                                    onClick = { session -> viewmodel.selectSession(session) },
                                    invokeEditSessionForm = { editSessionForm = true }
                                )
                            }
                        },
                        stepStyle = StepStyle(
                            colors = StepDefaults.defaultColors().copy(
                                currentContainerColor = MaterialTheme.colorScheme.primary,
                                doneLineColor = MaterialTheme.colorScheme.primary,
                                todoLineColor = MaterialTheme.colorScheme.primary,
                                currentLineColor = MaterialTheme.colorScheme.primary,
                                doneContentColor = MaterialTheme.colorScheme.onPrimary,
                                todoContentColor = MaterialTheme.colorScheme.primary,
                                currentContentColor = MaterialTheme.colorScheme.primary,
                                doneContainerColor = MaterialTheme.colorScheme.primary,
                                todoContainerColor = MaterialTheme.colorScheme.primary,
                            ),
                            showCheckMarkOnDone = false,
                            stepStroke = 10f,
                            lineStyle = LineDefault(progressStrokeCap = StrokeCap.Round)
                        ),
                    ),
                ) {
                    println("clicked")
                }
            }
        }
    }
}


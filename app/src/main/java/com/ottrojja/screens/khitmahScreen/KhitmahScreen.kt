package com.ottrojja.screens.khitmahScreen

import android.app.AlertDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.binayshaw7777.kotstep.model.LineDefault
import com.binayshaw7777.kotstep.model.StepDefaults
import com.binayshaw7777.kotstep.model.StepStyle
import com.binayshaw7777.kotstep.model.tabVerticalWithLabel
import com.binayshaw7777.kotstep.ui.vertical.VerticalStepper
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.OttrojjaTopBarTitle
import com.ottrojja.composables.TopBar
import com.ottrojja.room.entities.KhitmahMark
import com.ottrojja.ui.theme.complete_green
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KhitmahScreen(
    navController: NavController,
    repository: QuranRepository,
    id: Int
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val khitmahViewModel: KhitmahViewModel = viewModel(
        factory = KhitmahViewModelFactory(repository,
            application
        )
    )

    LaunchedEffect(Unit) {
        khitmahViewModel.fetchKhitmah(id)
    }

    var expanded by remember { mutableStateOf(false) }

    fun confirmDeleteKhitmah() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("حذف الختمة")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذه الختمة؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            khitmahViewModel.deleteKhitmah()
            dialog.dismiss()
            navController.popBackStack()
            /*val currentDestination = navController.currentBackStackEntry?.destination?.route
            currentDestination?.let {
                navController.navigate(Screen.KhitmahListScreen.route) {
                    popUpTo(it) { inclusive = true }
                }
            }

            navController.navigate(Screen.KhitmahListScreen.route){
                popUpTo("current_destination") { inclusive = true }
            }*/
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {

        TopBar(title = khitmahViewModel.khitmah?.title ?: "",
            mainAction = ButtonAction(icon = Icons.Filled.ArrowBack,
                action = { navController.popBackStack() }),
            secondaryActions = listOf(
                ButtonAction(icon = Icons.Outlined.Pending, action = { khitmahViewModel.toggleKhitmahStatus(); }, title = "تعيين كجارية"),
                ButtonAction(icon = Icons.Default.CheckCircle, action = { khitmahViewModel.toggleKhitmahStatus(); }, title = "تعيين كمكتملة"),
                ButtonAction(icon = Icons.Default.Close, action = { confirmDeleteKhitmah(); }, title = "حذف الختمة"),
            )
        )

        if (khitmahViewModel.khitmahMarks.size > 0) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Text(text = "مسار الختمة",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (khitmahViewModel.khitmah?.isComplete == true) complete_green else MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)

                ) {
                    Text(
                        text = if (khitmahViewModel.khitmah?.isComplete == true) "مكتملة" else "جارية",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Start,
                    )
                    Icon(
                        if (khitmahViewModel.khitmah?.isComplete == true) Icons.Default.CheckCircle else Icons.Outlined.Pending,
                        contentDescription = "completion status",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(32.dp)
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxHeight(0.95f)
                    .verticalScroll(rememberScrollState())
            ) {
                VerticalStepper(
                    style = tabVerticalWithLabel(
                        totalSteps = khitmahViewModel.khitmahMarks.size,
                        currentStep = if (khitmahViewModel.khitmah?.isComplete == true) khitmahViewModel.khitmahMarks.size else khitmahViewModel.khitmahMarks.size - 1,
                        trailingLabels = khitmahViewModel.khitmahMarks.map { mark ->
                            {
                                MarkItem(
                                    item = mark,
                                    onClick = { pageNum ->
                                        navController.navigate(
                                            Screen.QuranScreen.invokeRoute(pageNum)
                                        )
                                    },
                                    removeKhitmahMark = { id ->
                                        khitmahViewModel.removeKhitmahMark(id)
                                    },
                                    toggleExpanded = { id ->
                                        khitmahViewModel.toggleMarkExpanded(id
                                        )
                                    }
                                )
                            }
                        },
                        stepStyle = StepStyle(
                            colors = StepDefaults.defaultColors().copy(
                                currentContainerColor = MaterialTheme.colorScheme.primary,
                                doneLineColor = MaterialTheme.colorScheme.primary,
                                todoLineColor = MaterialTheme.colorScheme.primary,
                                currentLineColor = MaterialTheme.colorScheme.primary,
                                doneContentColor = MaterialTheme.colorScheme.primary,
                                todoContentColor = MaterialTheme.colorScheme.primary,
                                currentContentColor = MaterialTheme.colorScheme.primary,
                                doneContainerColor = MaterialTheme.colorScheme.primary,
                                todoContainerColor = MaterialTheme.colorScheme.primary
                            ),
                            showCheckMarkOnDone = false,
                            stepStroke = 10f,
                            lineStyle = LineDefault(progressStrokeCap = StrokeCap.Round)
                        ),
                    ),
                ) {
                    // Do something...
                    println("clicked")
                }

            }
        }
    }
}

@Composable
fun MarkItem(item: ExpandableItem<KhitmahMark>,
             onClick: (String) -> Unit,
             removeKhitmahMark: (Int) -> Unit,
             toggleExpanded: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clickable { onClick(item.data.pageNum) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.fillMaxWidth(0.9f)) {
                Text(
                    text = "صفحة ${item.data.pageNum}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = SimpleDateFormat("HH:mm dd.MM.yyyy",
                        Locale.US
                    ).format(Date(item.data.timeStamp)),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                )
            }

            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Khitmah Mark Options",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { toggleExpanded(item.data.id) }
            )
        }


        androidx.compose.animation.AnimatedVisibility(
            visible = item.expanded,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1.0f)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            removeKhitmahMark(item.data.id)
                        }
                        .padding(4.dp, 6.dp)
                ) {
                    Text(
                        text = "حذف",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        ListHorizontalDivider()
    }
}


package com.ottrojja.screens.CustomTasabeehListScreen

import android.app.AlertDialog
import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTopBar
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.ui.theme.timeNormal

@Composable
fun CustomTasabeehListScreen(
    navController: NavController,
    repository: QuranRepository,
    id: Int
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val customTasabeehListScreenViewModel: CustomTasabeehListScreenViewModel = viewModel(
        factory = CustomTasabeehListScreenViewModelFactory(repository, application)
    )

    LaunchedEffect(Unit) {
        customTasabeehListScreenViewModel.fetchCustomTasabeehList(id)
    }

    var expanded by remember { mutableStateOf(false) }

    fun confirmDeleteList() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("حذف القائمة")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذه القائمة؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            customTasabeehListScreenViewModel.deleteCustomTasabeehList()
            dialog.dismiss()
            navController.popBackStack()
            /*val currentDestination = navController.currentBackStackEntry?.destination?.route
            currentDestination?.let {
                navController.navigate(Screen.KhitmahListScreen.route) {
                    popUpTo(it) { inclusive = true }
                }
            }

            navController.navigate(Screen.KhitmahListScreen.route) {
                popUpTo("current_destination") { inclusive = true }
            }*/
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    if (customTasabeehListScreenViewModel.addTasbeehDialog) {
        AddCustomTasbeehDialog(
            onDismiss = { customTasabeehListScreenViewModel.addTasbeehDialog = false },
            onConfirm = { text, count ->
                customTasabeehListScreenViewModel.addCustomTasbeeh(text, count)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        OttrojjaTopBar {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${customTasabeehListScreenViewModel.customTasabeehList?.title}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 32.sp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start
                )

                Row {
                    Column {
                        OttrojjaElevatedButton(
                            onClick = { expanded = !expanded },
                            icon = Icons.Default.MoreVert
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "إضافة ذكر",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "add zikr",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = { customTasabeehListScreenViewModel.addTasbeehDialog = true; expanded = false; }
                            )
                            ListHorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "حذف القائمة",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "delete list",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = { confirmDeleteList(); expanded = false; }
                            )
                        }
                    }

                    OttrojjaElevatedButton(
                        onClick = { navController.popBackStack() },
                        icon = Icons.Filled.ArrowBack
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)) {
            items(customTasabeehListScreenViewModel.customTasabeeh) { item ->
                CustomTasbeehCounter(item)
            }
        }
    }
}

@Composable
fun CustomTasbeehCounter(item: CustomTasbeeh) {
    var tasbeehCount by remember { mutableStateOf(item.count) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(0.8F)
                    .border(
                        BorderStroke(4.dp, color = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12))
                    .padding(8.dp)
            ) {
                Text(
                    text = "${tasbeehCount}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = timeNormal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 42.sp,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ElevatedButton(
                onClick = {
                    if (tasbeehCount > 0) {
                        tasbeehCount--
                    }
                },
                elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9F)
                    .padding(24.dp, 0.dp)
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = "Custom Counter Touch",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = item.text,
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
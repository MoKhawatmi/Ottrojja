package com.ottrojja.screens.CustomTasabeehListScreen

import android.app.AlertDialog
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTopBar

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
        alertDialogBuilder.setTitle("حذف الختمة")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذه الختمة؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            //khitmahViewModel.deleteKhitmah()
            dialog.dismiss()
            val currentDestination = navController.currentBackStackEntry?.destination?.route
            currentDestination?.let {
                navController.navigate(Screen.KhitmahListScreen.route) {
                    popUpTo(it) { inclusive = true }
                }
            }

            navController.navigate(Screen.KhitmahListScreen.route) {
                popUpTo("current_destination") { inclusive = true }
            }
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
                                        contentDescription = "delete khitmah",
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
    }
}
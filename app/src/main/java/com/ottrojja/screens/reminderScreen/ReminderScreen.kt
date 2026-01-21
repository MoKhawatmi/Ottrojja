package com.ottrojja.screens.reminderScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.TopBar

@Composable
fun ReminderScreen(reminderViewModel: ReminderViewModel = viewModel()) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "المذكر")
    }


}
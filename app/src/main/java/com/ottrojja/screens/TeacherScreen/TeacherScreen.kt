package com.ottrojja.screens.TeacherScreen

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.classes.QuranRepository

@Composable
fun TeacherScreen(
    modifier: Modifier, repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val teacherScreenViewModel: TeacherScreenViewModel = viewModel(
        factory = TeacherScreenViewModelFactory(repository, application)
    )

}
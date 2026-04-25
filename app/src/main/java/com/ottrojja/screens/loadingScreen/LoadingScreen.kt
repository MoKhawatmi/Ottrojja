package com.ottrojja.screens.loadingScreen

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.OttrojjaLoadingIndicator

@Composable
fun LoadingScreen(
    navController: NavController,
    repository: QuranRepository,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val loadingScreenViewModel: LoadingScreenViewModel = viewModel(
        factory = LoadingScreenViewModelFactory(repository, application)
    )

    if (loadingScreenViewModel.loaded){
        navController.navigate(Screen.MainScreen.route)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        OttrojjaLoadingIndicator(
            size = 64.dp,
            indicatorColor = MaterialTheme.colorScheme.primary,
        )
    }
}

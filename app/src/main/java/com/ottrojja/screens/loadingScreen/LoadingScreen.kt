package com.ottrojja.screens.loadingScreen

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.OttrojjaLoadingIndicator
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.buttons.OttrojjaTextButton
import com.ottrojja.screens.loadingScreen.LoadingScreenViewModel.LoadState

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

    // Navigate once, as a side effect (not during composition), and remove the
    // loading screen from the back stack so Back never returns to it.
    LaunchedEffect(loadingScreenViewModel.state) {
        if (loadingScreenViewModel.state == LoadState.Loaded) {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.MainScreen.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (loadingScreenViewModel.state) {
            LoadState.Failed -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                OttrojjaText(
                    text = "تعذر تحميل بعض البيانات",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                OttrojjaButton(text = "إعادة المحاولة", onClick = { loadingScreenViewModel.retry() })
                OttrojjaTextButton(text = "متابعة", onClick = { loadingScreenViewModel.continueAnyway() })
            }

            else -> OttrojjaLoadingIndicator(
                size = 64.dp,
                indicatorColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

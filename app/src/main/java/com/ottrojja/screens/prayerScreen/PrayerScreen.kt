package com.ottrojja.screens.prayerScreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PrayerScreen(
    modifier: Modifier,
    prayerScreenViewModel: PrayerScreenViewModel = viewModel()
) {

    Text(text = "صلاتي")

}
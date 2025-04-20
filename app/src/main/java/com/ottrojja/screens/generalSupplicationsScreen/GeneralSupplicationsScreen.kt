package com.ottrojja.screens.generalSupplicationsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.Header

@Composable
fun GeneralSupplicationsScreen(generalSupplicationsViewModel: GeneralSupplicationsViewModel = viewModel()) {

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        Header(title = "أدعية مأثورة")
    }

}
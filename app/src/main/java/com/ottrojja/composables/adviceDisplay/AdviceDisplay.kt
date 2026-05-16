package com.ottrojja.composables.adviceDisplay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.OttrojjaPrimaryTextDisplay

@Composable
fun AdviceDisplay(viewModel: AdviceDisplayViewModel = viewModel()) {

    val loading = viewModel.loading.collectAsState()
    val advice = viewModel.advice.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAdvice()
    }
    OttrojjaPrimaryTextDisplay(
        text = advice.value?.text ?: "",
        details = advice.value?.details,
        loading = loading.value
    )
}
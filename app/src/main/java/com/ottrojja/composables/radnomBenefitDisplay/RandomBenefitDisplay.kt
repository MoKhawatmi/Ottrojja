package com.ottrojja.composables.radnomBenefitDisplay

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Screen
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.composables.OttrojjaPrimaryTextDisplay

@Composable
fun RandomBenefitDisplay(navController: NavController, repository: QuranRepository) {

    val viewModel: RandomBenefitDisplayViewModel = viewModel(
        factory = RandomBenefitDisplayModelFactory(repository)
    )

    val benefit = viewModel.benefit.collectAsState()
    val pageNum = viewModel.pageNumber.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchBenefit()
    }
    OttrojjaPrimaryTextDisplay(
        text = benefit.value,
        details = if (!pageNum.value.isNullOrBlank()) "من فوائد الصفحة ${Helpers.convertToIndianNumbers(pageNum.value!!)}" else "",
        modifier = Modifier.clickable {
            if (!pageNum.value.isNullOrBlank()) {
                navController.navigate(
                    Screen.QuranScreen.invokeRoute(pageNum.value!!)
                )
            }
        }
    )
}
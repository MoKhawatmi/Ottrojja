package com.ottrojja.screens.azkarScreen

import androidx.lifecycle.ViewModel

class AzkarViewModel : ViewModel() {
    val azkarData: List<Azkar> = AzkarStore.getAzkarData();

}
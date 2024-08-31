package com.ottrojja.screens.azkarScreen


object AzkarStore {
    private var azkarData = listOf<Azkar>()
    fun getAzkarData():List<Azkar> {
        return azkarData;
    }

    fun setAzkarData(data: List<Azkar>) {
        azkarData = data;
    }


}
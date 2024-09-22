package com.ottrojja.classes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ottrojja.screens.quranScreen.TafseerData


object QuranStore {
    private var quranData = listOf<QuranPage>()
    fun setQuranData(data: List<QuranPage>) {
        quranData = data;
    }
}
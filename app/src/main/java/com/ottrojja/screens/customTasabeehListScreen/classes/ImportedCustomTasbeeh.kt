package com.ottrojja.screens.customTasabeehListScreen.classes

import androidx.annotation.Keep

@Keep
data class ImportedCustomTasbeeh(
    val text: String,
    val count: Int,
    val currentCount: Int? = null
)
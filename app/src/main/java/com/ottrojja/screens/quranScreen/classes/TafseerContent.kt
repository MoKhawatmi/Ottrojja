package com.ottrojja.screens.quranScreen.classes

import androidx.annotation.Keep

@Keep
data class TafseerContent(val text: String, val action: (() -> Unit)? = null)

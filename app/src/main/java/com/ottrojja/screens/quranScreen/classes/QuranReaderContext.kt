package com.ottrojja.screens.quranScreen.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface QuranReaderContext : Parcelable {

    @Parcelize
    data object Normal : QuranReaderContext

    @Parcelize
    data class Khitmah(
        val khitmahId: Long,
        val khitmahName: String
    ) : QuranReaderContext
}
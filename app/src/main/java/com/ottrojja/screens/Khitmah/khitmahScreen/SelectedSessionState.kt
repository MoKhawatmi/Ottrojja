package com.ottrojja.screens.Khitmah.khitmahScreen

import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.entities.KhitmahSession

data class SelectedSessionState(
    val session: KhitmahSession,
    val range: QuranRangeState? = null,
    val isLoading: Boolean = false
)

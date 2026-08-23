package com.ottrojja.screens.Khitmah.SessionForm

import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.entities.KhitmahSession

data class SessionFormState(
    val khitmahId: Long,
    val session: KhitmahSession? = null,

    val startPage: Int? = null,
    val endPage: Int? = null,

    val quranRange: QuranRangeState? = null,

    val reflection: String = "",
) {
    val isEditMode: Boolean
        get() = session != null
}

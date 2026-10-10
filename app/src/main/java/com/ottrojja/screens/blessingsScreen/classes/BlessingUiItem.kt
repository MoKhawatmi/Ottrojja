package com.ottrojja.screens.blessingsScreen.classes

import androidx.annotation.Keep
import com.mikepenz.markdown.model.State as ParsedMarkdown

@Keep
data class BlessingUiItem(
    val blessing: Blessing,
    val markdown: ParsedMarkdown,
)
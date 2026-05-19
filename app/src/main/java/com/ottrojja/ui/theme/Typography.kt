package com.ottrojja.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class OttrojjaTypography(

    val quranTextLarge: TextStyle,
    val quranTextMedium: TextStyle,
    val quranTextSmall: TextStyle,

    val bodySpecialLarge: TextStyle,
    val bodySpecialMedium: TextStyle,

    val title: TextStyle,

    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,

    val counter: TextStyle,

    val navLabel: TextStyle,

    val tabLabel: TextStyle
)

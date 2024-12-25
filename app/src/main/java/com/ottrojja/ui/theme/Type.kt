package com.ottrojja.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.ottrojja.R

val uthmanic = FontFamily(
    Font(R.font.uthmanic, FontWeight.Normal, FontStyle.Normal),
)

val qalam = FontFamily(Font(R.font.qalam, FontWeight.Normal, FontStyle.Normal))

val timeNormal = FontFamily(
    Font(R.font.timenormal, FontWeight.Normal, FontStyle.Normal)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl
    ),
    titleLarge = TextStyle( // this is used with search and verse section
        fontFamily = uthmanic,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl,
        localeList = LocaleList(Locale("ar"))
    ),
    labelLarge = TextStyle(  // this is used with teacher
        fontFamily = uthmanic,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl,
        textAlign = TextAlign.Center,
        localeList = LocaleList(Locale("ar"))

    ),
    labelSmall = TextStyle(  // this is used with teacher
        fontFamily = uthmanic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl,
        textAlign = TextAlign.Start,
        localeList = LocaleList(Locale("ar"))
    ),
    displayLarge = TextStyle(
        fontFamily = qalam,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.Rtl
    ),
)
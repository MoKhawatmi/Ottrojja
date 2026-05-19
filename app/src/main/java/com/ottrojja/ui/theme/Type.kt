package com.ottrojja.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
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

val Typography = OttrojjaTypography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    quranTextLarge = TextStyle(  // this is used with teacher
        fontFamily = uthmanic,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        textAlign = TextAlign.Center,
        localeList = LocaleList(Locale("ar")),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    quranTextMedium = TextStyle( // this is used with search and verse section
        fontFamily = uthmanic,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        localeList = LocaleList(Locale("ar")),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    quranTextSmall = TextStyle(  // this is used with teacher
        fontFamily = uthmanic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        textAlign = TextAlign.Start,
        localeList = LocaleList(Locale("ar")),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    bodySpecialLarge = TextStyle(
        fontFamily = qalam,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    bodySpecialMedium = TextStyle(
        fontFamily = qalam,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    title = TextStyle(
        fontFamily = qalam,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    counter = TextStyle(
        fontFamily = timeNormal,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.5.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    navLabel = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),

    tabLabel = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),

)


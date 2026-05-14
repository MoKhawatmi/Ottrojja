package com.ottrojja.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ottrojja.classes.DynamicAzkarHelper

@Composable
fun ZikrDisplay() {
    val context = LocalContext.current
    OttrojjaPrimaryTextDisplay(DynamicAzkarHelper.getNextZekr(context))
}
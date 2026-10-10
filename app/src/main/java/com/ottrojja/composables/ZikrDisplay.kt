package com.ottrojja.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ottrojja.classes.DynamicAzkarHelper

@Composable
fun ZikrDisplay() {
    val context = LocalContext.current

    var zikrText by remember { mutableStateOf("") }

    fun updateZikr() {
        val text = DynamicAzkarHelper.getNextZekr(context);
        zikrText = text;
    }

    LaunchedEffect(Unit) {
        updateZikr()
    }

    OttrojjaPrimaryTextDisplay(text = zikrText, onRefresh = { updateZikr() })
}
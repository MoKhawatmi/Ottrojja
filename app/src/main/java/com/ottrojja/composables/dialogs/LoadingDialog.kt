package com.ottrojja.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ottrojja.composables.OttrojjaLoadingIndicator

@Composable
fun LoadingDialog() {
    OttrojjaDialog(
        onDismissRequest = { },
        contentModifier = Modifier
            .padding(8.dp)
            .fillMaxHeight(0.2f)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            OttrojjaLoadingIndicator(
                size = 52.dp,
                indicatorColor = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )
        }
    }
}
package com.ottrojja.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaBanner(
    text: String,
    variant: OttrojjaBannerVariant = OttrojjaBannerVariant.DANGER
) {
    val colors = MaterialTheme.colorScheme
    val (backgroundColor, textColor) = when (variant) {
        OttrojjaBannerVariant.DANGER -> colors.errorContainer to colors.error
        OttrojjaBannerVariant.WARNING -> colors.secondary to colors.onSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OttrojjaText(
            text = text,
            color = textColor,
            textAlign = TextAlign.Center,
            style = OttrojjaTheme.typography.bodyMedium
        )
    }
}

enum class OttrojjaBannerVariant {
    WARNING, DANGER
}
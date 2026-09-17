package com.ottrojja.composables.forms

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class OttrojjaFieldColors(
    val background: Color,
    val content: Color,
    val border: Color
)

object OttrojjaFieldDefaults {

    val shape: RoundedCornerShape
        @Composable
        get() = RoundedCornerShape(16.dp)

    val BorderWidth = 2.dp

    @Composable
    fun colors(
        enabled: Boolean
    ): OttrojjaFieldColors {
        return if (enabled) {
            OttrojjaFieldColors(
                background = MaterialTheme.colorScheme.background,
                content = MaterialTheme.colorScheme.primary,
                border = MaterialTheme.colorScheme.primary
            )
        } else {
            OttrojjaFieldColors(
                background = MaterialTheme.colorScheme.outline,
                content = MaterialTheme.colorScheme.onTertiary,
                border = MaterialTheme.colorScheme.outline
            )
        }
    }

}
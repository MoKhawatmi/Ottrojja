package com.ottrojja.composables.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaFormField(
    label: String = "",
    modifier: Modifier = Modifier,
    error: String? = null,
    supportingText: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
    ) {
        if(label.isNotBlank()){
            OttrojjaText(
                text = label,
                style = OttrojjaTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
            )
        }

        content()

        supportingText?.let {
            OttrojjaText(
                text = it,
                style = OttrojjaTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }


        if (error != null && error.isNotBlank()) {
            OttrojjaFieldError(
                error = error
            )
        }
    }
}
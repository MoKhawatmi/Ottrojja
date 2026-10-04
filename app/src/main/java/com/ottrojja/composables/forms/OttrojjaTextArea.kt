package com.ottrojja.composables.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlinx.coroutines.launch

@Composable
fun OttrojjaTextArea(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = Icons.Default.EditNote,
    minLines: Int = 4,
    maxLines: Int = 6,
) {
    val colors = OttrojjaFieldDefaults.colors(enabled)
    val shape = OttrojjaFieldDefaults.shape

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()


    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            onChange(
                if (maxLength != null) {
                    newValue.take(maxLength)
                } else {
                    newValue
                }
            )
        },
        enabled = enabled,
        modifier = modifier
            .height(150.dp)
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            }
            .clip(shape)
            .background(colors.background)
            .border(
                width = 2.dp,
                color = colors.border,
                shape = shape
            ),
        textStyle = OttrojjaTheme.typography.bodyMedium.copy(
            color = colors.content
        ),
        /*minLines = minLines,
        maxLines = maxLines,*/
        cursorBrush = SolidColor(colors.content),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                leadingIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = colors.content,
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 12.dp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 12.dp
                        )
                ) {
                    innerTextField()
                }
            }
        }
    )
}
package com.ottrojja.composables.dialogs

import androidx.compose.runtime.Composable

@Composable
fun AdvancedPermissionDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: (@Composable (() -> Unit))? = null
) {
    OttrojjaAlertDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm() },
        title = title,
        text = text,
        content= content
    )
}
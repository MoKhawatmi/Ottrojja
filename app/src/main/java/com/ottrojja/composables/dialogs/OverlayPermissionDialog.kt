package com.ottrojja.composables.dialogs

import androidx.compose.runtime.Composable

@Composable
fun OverlayPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    OttrojjaAlertDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm() },
        title = "السماح بالأذكار على الشاشة",
        text = "يمكننا عرض أذكار فوق التطبيقات الأخرى حتى لا تفوتك."
    )
}
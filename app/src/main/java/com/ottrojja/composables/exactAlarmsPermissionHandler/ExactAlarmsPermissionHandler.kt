package com.ottrojja.composables.exactAlarmsPermissionHandler

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.dialogs.AdvancedPermissionDialog

@Composable
fun ExactAlarmsPermissionHandler(onPermissionGranted: () -> Unit,
                                 onPermissionDenied: () -> Unit,
                                 onFinished: () -> Unit,
                                 forceShowRequest: Boolean = false,
                                 viewModel: ExactAlarmsPermissionHandlerpermissionViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showExactAlarmsPermissionDialog by remember { mutableStateOf(false) }
    var hasLaunchedSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val shouldAskExactAlarmsPerm = forceShowRequest || (!viewModel.hasPermission() && !viewModel.hasAskedBefore())

        if (shouldAskExactAlarmsPerm) {
            showExactAlarmsPermissionDialog = true
        } else {
            onFinished()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasLaunchedSettings) {
                if (viewModel.hasPermission()) {
                    println("exact alarms perm granted")
                    onPermissionGranted()
                } else {
                    println("exact alarms perm denied")
                    onPermissionDenied()
                }
                onFinished()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showExactAlarmsPermissionDialog) {
        AdvancedPermissionDialog(
            title = "السماح بالاشعارات الدقيقة",
            text = "لتمكين التطبيق من ارسال اشعارات المذكر بتوقيت دقيق",
            onConfirm = {
                hasLaunchedSettings = true
                showExactAlarmsPermissionDialog = false
                viewModel.setAsked()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.fromParts("package", context.packageName, null))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            },
            onDismiss = {
                showExactAlarmsPermissionDialog = false
                viewModel.setAsked()
                onFinished()
            }
        )
    }
}
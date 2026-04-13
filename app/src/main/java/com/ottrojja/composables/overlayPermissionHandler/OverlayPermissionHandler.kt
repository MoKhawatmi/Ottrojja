package com.ottrojja.composables.overlayPermissionHandler

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.dialogs.OverlayPermissionDialog
import kotlinx.coroutines.flow.filter

@Composable
fun OverlayPermissionHandler(onPermissionGranted: () -> Unit,
                             onPermissionDenied: () -> Unit,
                             onFinished: () -> Unit,
                             forceShowRequest: Boolean = false,
                             viewModel: OverlaypermissionViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var hasLaunchedSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val shouldAskOverlayPerm = forceShowRequest || (!viewModel.hasPermission() && !viewModel.hasAskedBefore())

        if (shouldAskOverlayPerm) {
            showOverlayPermissionDialog = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasLaunchedSettings) {
                if (Settings.canDrawOverlays(context)) {
                    println("perm granted")
                    onPermissionGranted()
                } else {
                    println("perm denied")
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

    if (showOverlayPermissionDialog) {
        OverlayPermissionDialog(
            onConfirm = {
                hasLaunchedSettings = true
                showOverlayPermissionDialog = false
                viewModel.setAsked()

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                context.startActivity(intent)
            },
            onDismiss = {
                showOverlayPermissionDialog = false
                viewModel.setAsked()
            }
        )
    }


}
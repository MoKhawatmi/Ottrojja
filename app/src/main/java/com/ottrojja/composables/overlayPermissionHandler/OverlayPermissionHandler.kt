package com.ottrojja.composables.overlayPermissionHandler

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.composables.dialogs.AdvancedPermissionDialog

@Composable
fun OverlayPermissionHandler(onPermissionGranted: () -> Unit,
                             onPermissionDenied: () -> Unit,
                             onFinished: () -> Unit,
                             forceShowRequest: Boolean = false,
                             viewModel: OverlayPermissionViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var hasLaunchedSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val shouldAskOverlayPerm = forceShowRequest || (!viewModel.hasPermission() && !viewModel.hasAskedBefore())

        if (shouldAskOverlayPerm) {
            showOverlayPermissionDialog = true
        } else {
            onFinished()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasLaunchedSettings) {
                if (Settings.canDrawOverlays(context)) {
                    println("overlay perm granted")
                    onPermissionGranted()
                } else {
                    println("overlay perm denied")
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
        AdvancedPermissionDialog(
            title = "السماح بالأذكار على الشاشة",
            text = "لتمكين التطبيق من عرض أذكار مأثورة على شاشة الهاتف.",
            onConfirm = {
                hasLaunchedSettings = true
                showOverlayPermissionDialog = false
                viewModel.setAsked()

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", context.packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            onDismiss = {
                showOverlayPermissionDialog = false
                viewModel.setAsked()
                onFinished()
            },
            content = {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.floating_zikr_example),
                        contentDescription = "Floating Azkar",
                        modifier = Modifier,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                }
            }
        )
    }
}
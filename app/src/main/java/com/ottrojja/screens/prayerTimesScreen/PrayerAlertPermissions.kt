package com.ottrojja.screens.prayerTimesScreen

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ottrojja.R
import com.ottrojja.composables.dialogs.AdvancedPermissionDialog
import com.ottrojja.screens.prayerTimesScreen.data.PrayerTimesModule
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerNotifications

/**
 * Gatekeeper for the bell buttons. Notification modes only work with notifications
 * allowed, so until then the bells look disabled and a tap asks for the permission.
 * Exact alarms are optional: without them alerts may come a few minutes late, so the
 * user is asked once, but the mode still changes.
 */
class PrayerAlertPermissions internal constructor(notificationsAllowed: Boolean) {
    var notificationsAllowed by mutableStateOf(notificationsAllowed)
        internal set

    internal var onBellClick: (PrayerType) -> Unit = {}

    fun bellClicked(type: PrayerType) = onBellClick(type)
}

private enum class AlertPermissionDialog { NOTIFICATION_RATIONALE, NOTIFICATION_SETTINGS, EXACT_ALARM }

@Composable
fun rememberPrayerAlertPermissions(viewModel: PrayerTimesViewModel): PrayerAlertPermissions {
    val context = LocalContext.current
    var dialog by rememberSaveable { mutableStateOf<AlertPermissionDialog?>(null) }
    var pendingType by rememberSaveable { mutableStateOf<PrayerType?>(null) }
    var exactAlarmAsked by rememberSaveable { mutableStateOf(false) }

    val state = remember { PrayerAlertPermissions(PrayerNotifications.canPostNotifications(context)) }

    fun applyPending() {
        pendingType?.let(viewModel::cycleNotificationMode)
        pendingType = null
    }

    fun proceed(type: PrayerType) {
        if (!exactAlarmAsked && !PrayerTimesModule.alarmScheduler.canScheduleExactAlarms()) {
            exactAlarmAsked = true
            pendingType = type
            dialog = AlertPermissionDialog.EXACT_ALARM
        } else {
            viewModel.cycleNotificationMode(type)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.notificationsAllowed = PrayerNotifications.canPostNotifications(context)
        if (granted && state.notificationsAllowed) {
            viewModel.onAlertPermissionsChanged()
            pendingType?.let { type -> pendingType = null; proceed(type) }
        } else {
            pendingType = null
            // Denied for good (or switched off in settings): only the settings screen helps now.
            dialog = AlertPermissionDialog.NOTIFICATION_SETTINGS
        }
    }

    state.onBellClick = { type ->
        when {
            state.notificationsAllowed -> proceed(type)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                pendingType = type
                dialog = AlertPermissionDialog.NOTIFICATION_RATIONALE
            }
            // Before Android 13 notifications can only be off in the system settings.
            else -> dialog = AlertPermissionDialog.NOTIFICATION_SETTINGS
        }
    }

    // Re-check when coming back from the system settings.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val allowed = PrayerNotifications.canPostNotifications(context)
                if (allowed != state.notificationsAllowed) {
                    state.notificationsAllowed = allowed
                    viewModel.onAlertPermissionsChanged()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (dialog) {
        AlertPermissionDialog.NOTIFICATION_RATIONALE -> AdvancedPermissionDialog(
            title = stringResource(R.string.prayer_notification_permission_title),
            text = stringResource(R.string.prayer_notification_permission_rationale),
            onConfirm = {
                dialog = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDismiss = { dialog = null; pendingType = null },
        )

        AlertPermissionDialog.NOTIFICATION_SETTINGS -> AdvancedPermissionDialog(
            title = stringResource(R.string.prayer_notification_permission_title),
            text = stringResource(R.string.prayer_notification_permission_settings),
            onConfirm = { dialog = null; context.openNotificationSettings() },
            onDismiss = { dialog = null },
        )

        AlertPermissionDialog.EXACT_ALARM -> AdvancedPermissionDialog(
            title = stringResource(R.string.prayer_exact_alarm_title),
            text = stringResource(R.string.prayer_exact_alarm_text),
            onConfirm = {
                dialog = null
                applyPending()
                context.openExactAlarmSettings()
            },
            onDismiss = { dialog = null; applyPending() },
        )

        null -> Unit
    }

    return state
}

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    }
    safeStart(intent)
}

private fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        safeStart(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.fromParts("package", packageName, null)))
    }
}

private fun Context.safeStart(intent: Intent) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

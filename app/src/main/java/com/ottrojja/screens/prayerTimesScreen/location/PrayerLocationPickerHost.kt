package com.ottrojja.screens.prayerTimesScreen.location

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.AdvancedPermissionDialog
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.screens.prayerTimesScreen.PrayerTimesViewModel
import com.ottrojja.ui.theme.OttrojjaTheme

private sealed interface LocationPickerItem {
    data object CurrentLocation : LocationPickerItem
    data class CityItem(val city: City) : LocationPickerItem
}

private enum class PermissionDialog { RATIONALE, OPEN_SETTINGS }

/**
 * Hosts everything around choosing a location: the city picker, the location permission
 * flow and the "location services are off" dialog.
 *
 * Keep this composed for the whole prayer times screen (not only while the picker is
 * open): the picker dialog closes itself on selection, and the permission request that
 * follows a tap on "use my current location" must outlive it.
 *
 * Open the picker with [PrayerTimesViewModel.openLocationPicker].
 */
@Composable
fun PrayerLocationPickerHost(viewModel: PrayerTimesViewModel) {
    val context = LocalContext.current
    var permissionDialog by rememberSaveable { mutableStateOf<PermissionDialog?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            viewModel.onUseCurrentLocation()
        } else if (!context.shouldShowLocationRationale()) {
            // Denied with "don't ask again" (or the system auto-denied): only Settings helps now.
            permissionDialog = PermissionDialog.OPEN_SETTINGS
        }
    }

    fun requestCurrentLocation() {
        if (DeviceLocationProvider(context).hasPermission()) {
            viewModel.onUseCurrentLocation()
        } else {
            // Explain first. If the user previously chose "don't ask again", the system
            // denies without UI and the launcher callback above shows the settings dialog.
            permissionDialog = PermissionDialog.RATIONALE
        }
    }

    if (viewModel.showLocationPicker) {
        CityPickerDialog(
            viewModel = viewModel,
            onUseCurrentLocation = ::requestCurrentLocation,
        )
    }

    when (permissionDialog) {
        PermissionDialog.RATIONALE -> AdvancedPermissionDialog(
            title = stringResource(R.string.prayer_location_permission_title),
            text = stringResource(R.string.prayer_location_permission_rationale),
            onConfirm = {
                permissionDialog = null
                permissionLauncher.launch(DeviceLocationProvider.PERMISSIONS)
            },
            onDismiss = { permissionDialog = null },
        )

        PermissionDialog.OPEN_SETTINGS -> AdvancedPermissionDialog(
            title = stringResource(R.string.prayer_location_permission_title),
            text = stringResource(R.string.prayer_location_permission_settings),
            onConfirm = {
                permissionDialog = null
                context.openAppSettings()
            },
            onDismiss = { permissionDialog = null },
        )

        null -> Unit
    }

    if (viewModel.showLocationDisabledDialog) {
        AdvancedPermissionDialog(
            title = stringResource(R.string.prayer_location_disabled_title),
            text = stringResource(R.string.prayer_location_disabled_text),
            onConfirm = {
                viewModel.showLocationDisabledDialog = false
                context.openLocationSettings()
            },
            onDismiss = { viewModel.showLocationDisabledDialog = false },
        )
    }
}

@Composable
private fun CityPickerDialog(
    viewModel: PrayerTimesViewModel,
    onUseCurrentLocation: () -> Unit,
) {
    val cities by viewModel.cityResults.collectAsState()
    // "Use my current location" is always first, which also keeps the list non-empty:
    // OttrojjaItemSelectionDialog hides the search bar when its list is empty.
    val items = listOf<LocationPickerItem>(LocationPickerItem.CurrentLocation) +
        cities.map(LocationPickerItem::CityItem)
    val currentLocation = viewModel.settings.collectAsState().value?.location

    OttrojjaItemSelectionDialog(
        title = stringResource(R.string.prayer_location_picker_title),
        onDismiss = viewModel::closeLocationPicker,
        searchBar = {
            OttrojjaTextField(
                value = viewModel.cityQuery,
                onChange = viewModel::onCityQueryChange,
                leadingIcon = Icons.Default.Search,
            )
        },
        selectionItems = items,
        onSelect = { item ->
            when (item) {
                LocationPickerItem.CurrentLocation -> onUseCurrentLocation()
                is LocationPickerItem.CityItem -> viewModel.onCitySelected(item.city)
            }
        },
        itemKey = { item ->
            when (item) {
                LocationPickerItem.CurrentLocation -> "current_location"
                is LocationPickerItem.CityItem -> item.city.id
            }
        },
        itemContent = { item ->
            when (item) {
                LocationPickerItem.CurrentLocation -> CurrentLocationRow()
                is LocationPickerItem.CityItem -> CityRow(
                    city = item.city,
                    selected = currentLocation != null &&
                        currentLocation.latitude == item.city.latitude &&
                        currentLocation.longitude == item.city.longitude,
                )
            }
        },
    )
}

@Composable
private fun CurrentLocationRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        OttrojjaText(
            text = stringResource(R.string.prayer_location_use_current),
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CityRow(city: City, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OttrojjaText(
                text = city.label,
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
            )
            city.secondaryName?.let {
                OttrojjaText(
                    text = it,
                    style = OttrojjaTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Start,
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.shouldShowLocationRationale(): Boolean {
    val activity = findActivity() ?: return false
    return DeviceLocationProvider.PERMISSIONS.any {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private fun Context.openLocationSettings() {
    try {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        openAppSettings()
    }
}

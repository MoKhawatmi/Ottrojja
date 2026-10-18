package com.ottrojja.screens.prayerTimesScreen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.buttons.OttrojjaTextButton
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.composables.forms.OttrojjaForm
import com.ottrojja.composables.forms.OttrojjaFormField
import com.ottrojja.composables.forms.OttrojjaSelect
import com.ottrojja.composables.forms.SwitchRow
import com.ottrojja.screens.prayerTimesScreen.calculator.DefaultMethodResolver
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.ui.theme.OttrojjaTheme

/**
 * Settings modal for prayer times.
 *
 * Method and madhab are edited as a draft and only saved with the save button.
 * Location is different: the picker (next step) saves it immediately, and this modal
 * just reflects the new [settings] it receives.
 *
 * @param onSave called with the draft values; the modal dismisses itself afterwards.
 * @param onLocationClick opens the location picker (PrayerTimesViewModel.openLocationPicker).
 * @param isLocating shows a "locating" label in the location row while GPS is resolving.
 */
@Composable
fun PrayerTimesSettingsModal(
    settings: PrayerSettings,
    onDismiss: () -> Unit,
    onSave: (method: CalculationMethodOption?, madhab: MadhabOption) -> Unit,
    onLocationClick: () -> Unit,
    isLocating: Boolean = false,
) {
    var draftMethod by rememberSaveable { mutableStateOf(settings.method) }
    var draftMadhab by rememberSaveable { mutableStateOf(settings.madhab) }
    var showMethodPicker by rememberSaveable { mutableStateOf(false) }

    // Follows the current location, so the label stays right after a location change.
    val automaticMethod = DefaultMethodResolver.defaultFor(settings.location?.countryCode)

    OttrojjaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.prayer_settings_title),
        useDefaultWidth = false,
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        OttrojjaForm(
            modifier = Modifier.padding(4.dp),
            body = {
                OttrojjaFormField(
                    label = stringResource(R.string.prayer_location_title),
                    supportingText = if (settings.location?.source == LocationSource.GPS) {
                        stringResource(R.string.prayer_settings_location_gps_hint)
                    } else {
                        null
                    },
                ) {
                    OttrojjaSelect(
                        value = if (isLocating) {
                            stringResource(R.string.prayer_location_locating)
                        } else {
                            settings.location?.displayName?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.prayer_settings_location_not_set)
                        },
                        onClick = onLocationClick,
                        enabled = !isLocating,
                        leadingIcon = Icons.Default.LocationOn,
                    )
                }

                OttrojjaFormField(label = stringResource(R.string.prayer_settings_method_title)) {
                    OttrojjaSelect(
                        value = methodLabel(draftMethod, automaticMethod),
                        onClick = { showMethodPicker = true },
                    )
                }

                OttrojjaFormField(
                    label = stringResource(R.string.prayer_settings_madhab_title),
                    supportingText = stringResource(R.string.prayer_settings_hanafi_hint),
                ) {
                    SwitchRow(
                        label = stringResource(R.string.prayer_settings_hanafi_switch),
                        checked = draftMadhab == MadhabOption.HANAFI,
                        onCheckedChange = { checked ->
                            draftMadhab = if (checked) MadhabOption.HANAFI else MadhabOption.JUMHUR
                        },
                    )
                }
            },
            footer = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OttrojjaButton(
                        text = stringResource(R.string.prayer_settings_cancel),
                        onClick = onDismiss,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OttrojjaButton(
                        text = stringResource(R.string.prayer_settings_save),
                        onClick = {
                            onSave(draftMethod, draftMadhab)
                            onDismiss()
                        },
                    )
                }
            },
        )
    }

    if (showMethodPicker) {
        // null = automatic, listed first.
        val options: List<CalculationMethodOption?> = listOf(null) + CalculationMethodOption.entries

        OttrojjaItemSelectionDialog(
            title = stringResource(R.string.prayer_settings_method_title),
            onDismiss = { showMethodPicker = false },
            selectionItems = options,
            onSelect = { draftMethod = it },
            selectedItem = draftMethod,
            itemKey = { it?.storageKey ?: AUTOMATIC_KEY },
            itemContent = { option ->
                MethodOptionRow(
                    label = methodLabel(option, automaticMethod),
                    selected = option == draftMethod,
                )
            },
        )
    }
}

@Composable
private fun methodLabel(
    method: CalculationMethodOption?,
    automaticMethod: CalculationMethodOption,
): String = if (method == null) {
    stringResource(R.string.prayer_settings_method_automatic, stringResource(automaticMethod.nameRes))
} else {
    stringResource(method.nameRes)
}

@Composable
private fun MethodOptionRow(label: String, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OttrojjaText(
            text = label,
            style = OttrojjaTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private const val AUTOMATIC_KEY = "automatic"

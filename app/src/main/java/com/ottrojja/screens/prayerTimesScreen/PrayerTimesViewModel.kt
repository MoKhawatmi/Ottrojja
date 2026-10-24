package com.ottrojja.screens.prayerTimesScreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.screens.prayerTimesScreen.data.PrayerTimesModule
import com.ottrojja.screens.prayerTimesScreen.location.City
import com.ottrojja.screens.prayerTimesScreen.location.DeviceLocationProvider
import com.ottrojja.screens.prayerTimesScreen.location.DeviceLocationResult
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.DayPrayerTimes
import com.ottrojja.screens.prayerTimesScreen.model.LocationSource
import com.ottrojja.screens.prayerTimesScreen.model.MadhabOption
import com.ottrojja.screens.prayerTimesScreen.model.NextPrayer
import com.ottrojja.screens.prayerTimesScreen.model.PrayerNotificationMode
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.screens.prayerTimesScreen.model.PrayerType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/** The header's next prayer with the time left, refreshed every second. */
data class NextPrayerState(
    val next: NextPrayer,
    val remainingMillis: Long,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerTimesModule.repository
    private val cityRepository = PrayerTimesModule.cityRepository
    private val deviceLocation = DeviceLocationProvider(application)

    /** null until DataStore has been read for the first time. */
    val settings: StateFlow<PrayerSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notificationModes: StateFlow<Map<PrayerType, PrayerNotificationMode>> = repository.notificationModesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /*******************TIMES*********************/

    /** Ticks on every full second while the screen is visible. */
    private val clock: Flow<Long> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(now)
            delay(1_000 - now % 1_000)
        }
    }

    val now: StateFlow<Long> = clock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), System.currentTimeMillis())

    val nextPrayer: StateFlow<NextPrayerState?> = run {
        // Only recalculated when the settings change or the cached prayer has passed.
        var cachedFor: PrayerSettings? = null
        var cached: NextPrayer? = null
        combine(settings.filterNotNull(), now) { settings, now ->
            if (settings != cachedFor || cached == null || now >= cached!!.epochMillis) {
                cached = repository.nextPrayer(now, settings)
                cachedFor = settings
            }
            cached?.let { NextPrayerState(it, it.epochMillis - now) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }

    fun dayTimes(date: LocalDate, settings: PrayerSettings): DayPrayerTimes? =
        repository.dayTimes(date, settings)

    fun effectiveMethod(settings: PrayerSettings): CalculationMethodOption =
        repository.effectiveMethod(settings)

    /*******************NOTIFICATION MODES*********************/

    /** OFF → notification → adhan → OFF. Alarms are rescheduled automatically by PrayerTimesModule. */
    fun cycleNotificationMode(type: PrayerType) {
        if (!type.isNotifiable) return
        val next = when (notificationModes.value[type] ?: PrayerNotificationMode.OFF) {
            PrayerNotificationMode.OFF -> PrayerNotificationMode.NOTIFICATION
            PrayerNotificationMode.NOTIFICATION -> PrayerNotificationMode.ADHAN
            PrayerNotificationMode.ADHAN -> PrayerNotificationMode.OFF
        }
        viewModelScope.launch { repository.setNotificationMode(type, next) }
    }

    /** Permission granted or exact alarms allowed: nothing changed in DataStore, so reschedule by hand. */
    fun onAlertPermissionsChanged() = PrayerTimesModule.rescheduleAlarms()

    /*******************SETTINGS MODAL*********************/
    var showSettingsModal by mutableStateOf(false)

    /*******************TESTING NOTICE*********************/
    /** The weekly "feature in testing" dialog. Only this screen's ViewModel decides it. */
    var showTestingNotice by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            try {
                showTestingNotice = repository.shouldShowTestingNotice(System.currentTimeMillis())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reportException(exception = e, file = "PrayerTimesViewModel")
            }
        }
    }

    fun dismissTestingNotice() {
        showTestingNotice = false
        viewModelScope.launch { repository.markTestingNoticeShown(System.currentTimeMillis()) }
    }

    fun saveCalculationSettings(method: CalculationMethodOption?, madhab: MadhabOption) {
        viewModelScope.launch { repository.saveCalculationSettings(method, madhab) }
    }

    /*******************CITY PICKER*********************/
    var showLocationPicker by mutableStateOf(false)
        private set

    var cityQuery by mutableStateOf("")
        private set

    private val cityQueryFlow = MutableStateFlow("")

    val cityResults: StateFlow<List<City>> = cityQueryFlow
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .mapLatest { query ->
            try {
                cityRepository.search(query, countryHint = settings.value?.location?.countryCode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "PrayerTimesViewModel")
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openLocationPicker() {
        onCityQueryChange("")
        showLocationPicker = true
    }

    fun closeLocationPicker() {
        showLocationPicker = false
    }

    fun onCityQueryChange(query: String) {
        cityQuery = query
        cityQueryFlow.value = query
    }

    fun onCitySelected(city: City) {
        viewModelScope.launch {
            repository.setManualLocation(cityRepository.locationFor(city))
        }
    }

    /*******************DEVICE LOCATION*********************/
    var isLocating by mutableStateOf(false)
        private set

    var showLocationDisabledDialog by mutableStateOf(false)

    private var silentRefreshDone = false

    /** Called after the user picked "use my current location" and permission is granted. */
    fun onUseCurrentLocation() = locate(silent = false)

    /**
     * Refreshes a GPS-based location once per screen visit, without any UI on failure.
     * Does nothing for manually chosen cities or without permission.
     */
    fun refreshGpsLocationIfNeeded() {
        if (silentRefreshDone || !deviceLocation.hasPermission()) return
        val location = settings.value?.location ?: return
        if (location.source != LocationSource.GPS) return
        silentRefreshDone = true
        locate(silent = true)
    }

    private fun locate(silent: Boolean) {
        if (isLocating) return
        viewModelScope.launch {
            isLocating = true
            try {
                when (val result = deviceLocation.currentLocation()) {
                    is DeviceLocationResult.Success -> repository.updateGpsLocation(
                        cityRepository.locationFor(result.latitude, result.longitude)
                    )
                    DeviceLocationResult.LocationDisabled -> if (!silent) showLocationDisabledDialog = true
                    DeviceLocationResult.Unavailable -> if (!silent) showError(R.string.prayer_location_unavailable)
                    DeviceLocationResult.PermissionMissing -> Unit // The UI requests it first.
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "PrayerTimesViewModel")
                if (!silent) showError(R.string.prayer_location_error)
            } finally {
                isLocating = false
            }
        }
    }

    private fun showError(messageRes: Int) {
        AppToastManager.show(
            message = getApplication<Application>().getString(messageRes),
            type = AppToastType.ERROR,
        )
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 150L
    }
}

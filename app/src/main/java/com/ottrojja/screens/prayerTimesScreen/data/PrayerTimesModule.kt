package com.ottrojja.screens.prayerTimesScreen.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.screens.prayerTimesScreen.calculator.AdhanPrayerTimesCalculator
import com.ottrojja.screens.prayerTimesScreen.hijri.IcuHijriDateProvider
import com.ottrojja.screens.prayerTimesScreen.location.CityRepository
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerAlarmScheduler
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerNotifications
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerRescheduleWorker
import com.ottrojja.screens.qiblaScreen.resolveCity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/**
 * Manual DI for the prayer times feature, in the same style as DataStoreHolder.
 * Call [init] once from App.onCreate(), next to DataStoreHolder.init(...).
 *
 * Besides creating the singletons, init keeps the prayer alarms in sync: any change to
 * the settings or the notification modes re-registers them, and a periodic worker does
 * the same as a safety net.
 */
object PrayerTimesModule {

    lateinit var repository: PrayerTimesRepository
        private set

    /** Cheap to create: the cities file is only read on the first search or GPS lookup. */
    lateinit var cityRepository: CityRepository
        private set

    lateinit var alarmScheduler: PrayerAlarmScheduler
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun init(context: Context) {
        if (::repository.isInitialized) return

        val appContext = context.applicationContext
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("prayer_times") }
        )
        repository = PrayerTimesRepository(
            dataStore = PrayerTimesDataStore(dataStore),
            calculator = AdhanPrayerTimesCalculator(IcuHijriDateProvider()),
        )
        cityRepository = CityRepository(
            openGzippedAsset = { appContext.assets.open(CITIES_ASSET) },
            reverseGeocode = { lat, lon -> resolveCity(appContext, lat, lon) },
        )
        alarmScheduler = PrayerAlarmScheduler(appContext, repository)

        PrayerNotifications.ensureChannels(appContext)
        keepAlarmsInSync()
        enqueueSafetyWorker(appContext)
    }

    /** Called when something outside DataStore changed, e.g. notification permission granted. */
    fun rescheduleAlarms() {
        appScope.launch { runCatchingReported { alarmScheduler.rescheduleAll() } }
    }

    private fun keepAlarmsInSync() {
        appScope.launch {
            combine(repository.settingsFlow, repository.notificationModesFlow) { settings, modes -> settings to modes }
                .distinctUntilChanged()
                .collectLatest { runCatchingReported { alarmScheduler.rescheduleAll() } }
        }
    }

    private fun enqueueSafetyWorker(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PrayerRescheduleWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PrayerRescheduleWorker>(12, TimeUnit.HOURS).build(),
        )
    }

    private suspend fun runCatchingReported(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportException(exception = e, file = "PrayerTimesModule")
        }
    }

    private const val CITIES_ASSET = "prayer_cities.dat"
}

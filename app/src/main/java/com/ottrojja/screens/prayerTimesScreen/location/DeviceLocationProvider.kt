package com.ottrojja.screens.prayerTimesScreen.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.ottrojja.classes.Helpers.reportException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

sealed interface DeviceLocationResult {
    data class Success(val latitude: Double, val longitude: Double) : DeviceLocationResult
    data object PermissionMissing : DeviceLocationResult
    data object LocationDisabled : DeviceLocationResult
    data object Unavailable : DeviceLocationResult
}

/**
 * One-shot device location for prayer times. City-level accuracy is enough, so:
 * 1. a recent last-known fix is used immediately when there is one,
 * 2. otherwise the first update from the Qibla screen's [LocationProvider] is taken,
 * 3. if that times out, any older last-known fix is better than nothing.
 */
class DeviceLocationProvider(context: Context) {

    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasPermission(): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean = LocationManagerCompat.isLocationEnabled(locationManager)

    suspend fun currentLocation(): DeviceLocationResult {
        if (!hasPermission()) return DeviceLocationResult.PermissionMissing
        if (!isLocationEnabled()) return DeviceLocationResult.LocationDisabled

        lastKnownLocation(maxAgeMillis = RECENT_FIX_MAX_AGE_MILLIS)?.let { return it.toSuccess() }

        val fresh = try {
            withTimeoutOrNull(FRESH_FIX_TIMEOUT) {
                LocationProvider.getLocationUpdates(appContext).first()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportException(exception = e, file = "DeviceLocationProvider")
            null
        }

        return (fresh ?: lastKnownLocation(maxAgeMillis = null))?.toSuccess()
            ?: DeviceLocationResult.Unavailable
    }

    /** The newest last-known fix across all providers, optionally limited by age. */
    @SuppressLint("MissingPermission") // Checked by hasPermission() in currentLocation().
    private fun lastKnownLocation(maxAgeMillis: Long?): Location? {
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }
        val now = SystemClock.elapsedRealtimeNanos()
        return providers
            .mapNotNull { provider ->
                try {
                    locationManager.getLastKnownLocation(provider)
                } catch (e: SecurityException) {
                    null
                } catch (e: IllegalArgumentException) {
                    null // Provider does not exist on this device.
                }
            }
            .filter { location ->
                maxAgeMillis == null ||
                    TimeUnit.NANOSECONDS.toMillis(now - location.elapsedRealtimeNanos) <= maxAgeMillis
            }
            .maxByOrNull { it.elapsedRealtimeNanos }
    }

    private fun Location.toSuccess() = DeviceLocationResult.Success(latitude, longitude)

    companion object {
        /** Same pair the Qibla screen requests; the user may grant approximate only. */
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        private val RECENT_FIX_MAX_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30)
        private val FRESH_FIX_TIMEOUT = 20.seconds
    }
}

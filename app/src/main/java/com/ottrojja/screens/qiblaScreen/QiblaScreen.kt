package com.ottrojja.screens.qiblaScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.math.min
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import java.io.IOException
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.OttrojjaWarningBar
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.ui.theme.complete_green
import com.ottrojja.ui.theme.md_theme_light_primary
import com.ottrojja.ui.theme.md_theme_light_secondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.sqrt
import com.ottrojja.screens.qiblaScreen.dialogs.CalibrationDialog
import com.ottrojja.screens.qiblaScreen.dialogs.DevicePositionDialog
import com.ottrojja.ui.theme.OttrojjaTheme
import com.ottrojja.ui.theme.md_theme_light_error
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds


@Composable
fun QiblaScreen(qiblaViewModel: QiblaViewModel = viewModel()) {
    val context = LocalContext.current
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var permissionsGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted = perms.values.any { it }
    }

    LaunchedEffect(Unit) {
        val anyGranted = permissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        permissionsGranted = anyGranted
        if (!anyGranted) permissionLauncher.launch(permissions.toTypedArray())
    }

    if (permissionsGranted) {
        CompassContent(
            showPositionDialog = qiblaViewModel.showPoistionDialog,
            triggerPositionDialog = { value -> qiblaViewModel.showPoistionDialog = value },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OttrojjaText(
                    "يحتاج التطبيق لأذونات الوصول للموقع لتحديد اتجاه القبلة بدقة",
                    color = MaterialTheme.colorScheme.primary,
                    style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                OttrojjaButton(
                    onClick = { permissionLauncher.launch(permissions.toTypedArray()) },
                    text = "منح الأذونات"
                )
            }
        }
    }
}

@Composable
private fun CompassContent(
    showPositionDialog: Boolean,
    triggerPositionDialog: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var angle by remember { mutableStateOf(0f) }
    var bearing by remember { mutableStateOf(0f) }
    var heading by remember { mutableStateOf(0f) }

    // FIX: magnetic declination — difference between magnetic north (what the
    // magnetometer measures) and true north (what bearingTo() returns).
    // Updated from GeomagneticField whenever a new location arrives.
    var declination by remember { mutableStateOf(0f) }

    // Spring animation — natural overshooting feel for needle and ring.
    // FIX: animate the raw accumulated values. `angle` and `heading` are already
    // continuous (shortest-path deltas are accumulated in the sensor listener), so the
    // spring always takes the short way. Wrapping the target into (-180, 180] caused a
    // 358° spin whenever the target crossed ±180 (facing south / facing away from Qibla).
    val springSpec = spring<Float>(dampingRatio = 0.6f, stiffness = 120f)
    val animatedAngle by animateFloatAsState(
        targetValue = angle,
        animationSpec = springSpec
    )
    val animatedHeading by animateFloatAsState(
        targetValue = heading,
        animationSpec = springSpec
    )

    // Track alignment and animate Kaaba saturation (0f = greyscale → 1f = full color)
    // Wrap angle into (-180, 180] before checking — after heavy rotation `angle` can be
    // e.g. 362f or -718f which is physically 2f or 2f but abs() would never pass < 2f.
    val wrappedAngle = ((angle % 360f) + 540f) % 360f - 180f
    val isAligned = abs(wrappedAngle) < 2f
    val animatedSaturation by animateFloatAsState(
        targetValue = if (isAligned) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    )

    // Haptic pulse fires once on the leading edge of alignment,
    // but only when the composable is in the foreground (lifecycle RESUMED).
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned &&
            lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasAligned = isAligned
    }

    var calibrationRequired by remember { mutableStateOf(false) }
    val locationState = remember { mutableStateOf<Location?>(null) }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var calibrationCoolDown by remember { mutableStateOf(false) }
    var positionCoolDown by remember { mutableStateOf(false) }
    var locationCity by remember { mutableStateOf("") }
    // FIX: last location that was successfully reverse-geocoded, used to throttle lookups
    var lastGeocodedLocation by remember { mutableStateOf<Location?>(null) }

    var magneticFieldValue by remember { mutableStateOf(0f) }
    var expectedField_uT by remember { mutableStateOf(50f) }
    var magneticFieldQuality by remember { mutableStateOf(MFSQuality.MEDIUM) }

    val calibrationHandler = Handler(Looper.getMainLooper())
    fun triggerCalibrationWarning() {
        if (calibrationCoolDown) return
        calibrationRequired = true
        calibrationCoolDown = true
        calibrationHandler.removeCallbacksAndMessages(null)
        calibrationHandler.postDelayed({ calibrationCoolDown = false }, 1000L * 30)
    }

    val positionHandler = Handler(Looper.getMainLooper())
    fun triggerPositionWarning() {
        if (positionCoolDown) return
        triggerPositionDialog(true)
        positionCoolDown = true
        positionHandler.removeCallbacksAndMessages(null)
        positionHandler.postDelayed({ positionCoolDown = false }, 1000L * 30)
    }

    val sensorEventListener = remember {
        val windowSize = 30
        val magnitudeWindow = FloatArray(windowSize)
        var windowIndex = 0
        var windowFilled = false

        fun computeStdDev(): Float {
            val size = if (windowFilled) windowSize else windowIndex
            if (size == 0) return 0f
            val mean = magnitudeWindow.take(size).average().toFloat()
            var sum = 0f
            for (i in 0 until size) {
                val d = magnitudeWindow[i] - mean; sum += d * d
            }
            return sqrt(sum / size)
        }

        object : SensorEventListener {
            private val accelerometerReading = FloatArray(3)
            private val magnetometerReading = FloatArray(3)
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)

            // Low-pass filter — smooths raw sensor noise
            private val alpha = 0.15f
            private fun lowPass(input: FloatArray, output: FloatArray) {
                for (i in input.indices) output[i] = output[i] + alpha * (input[i] - output[i])
            }

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0];
                        val y = event.values[1];
                        val z = event.values[2]
                        val isFlat = (abs(x) < 4f && abs(y) < 4f && z in 7f..11f)
                        if (!isFlat) triggerPositionWarning() else triggerPositionDialog(false)
                        lowPass(event.values, accelerometerReading)
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val x = event.values[0];
                        val y = event.values[1];
                        val z = event.values[2]
                        val B = sqrt(x * x + y * y + z * z)
                        magneticFieldValue = B
                        magnitudeWindow[windowIndex] = B
                        windowIndex = (windowIndex + 1) % windowSize
                        if (windowIndex == 0) windowFilled = true
                        val stddev = computeStdDev()
                        val deltaB = abs(B - expectedField_uT)
                        magneticFieldQuality = when {
                            deltaB < 3f && stddev < 1f -> MFSQuality.GOOD
                            deltaB < 10f && stddev < 5f -> MFSQuality.MEDIUM
                            else -> MFSQuality.BAD
                        }
                        if (magneticFieldQuality == MFSQuality.BAD) triggerCalibrationWarning()
                        lowPass(event.values, magnetometerReading)
                    }
                }

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    // FIX: convert magnetic azimuth to true-north azimuth so it matches
                    // the true-north bearing returned by Location.bearingTo()
                    val azimuthDegrees =
                        Math.toDegrees(orientationAngles[0].toDouble()).toFloat() + declination

                    // Shortest-path delta — prevents 359°→1° wrap-around snap
                    var headingDelta = azimuthDegrees - heading
                    headingDelta = ((headingDelta % 360f) + 540f) % 360f - 180f
                    heading += headingDelta

                    val rawAngle = bearing - heading
                    var angleDelta = rawAngle - angle
                    angleDelta = ((angleDelta % 360f) + 540f) % 360f - 180f
                    angle += angleDelta
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW,
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> triggerCalibrationWarning()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            LocationProvider.getLocationUpdates(context)
                .onCompletion { cause ->
                    if (cause is CancellationException) {
                    }
                }
                .catch { e ->
                    e.printStackTrace()
                    AppToastManager.show(
                        message = "حصل خطأ يرجى المحاولة لاحقا",
                        type = AppToastType.ERROR
                    )
                }
                .collectLatest { location ->
                    locationState.value = location
                    val userLoc = Location("service Provider").apply {
                        longitude = location.longitude
                        latitude = location.latitude
                        altitude = location.altitude
                    }
                    val destinationLoc = Location("service Provider").apply {
                        latitude = 21.422487
                        longitude = 39.826206
                    }
                    bearing = normalizeAngle(userLoc.bearingTo(destinationLoc))
                    val geomagnetic = GeomagneticField(
                        location.latitude.toFloat(), location.longitude.toFloat(),
                        location.altitude.toFloat(), System.currentTimeMillis()
                    )
                    expectedField_uT = geomagnetic.fieldStrength / 1000f
                    // FIX: store declination so the heading can be corrected to true north
                    declination = geomagnetic.declination
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "QiblaScreen")
            AppToastManager.show(
                message = "حصل خطأ يرجى المحاولة لاحقا",
                type = AppToastType.ERROR
            )
        }
    }

    // FIX: only reverse-geocode when the user has moved meaningfully (or we have no city yet).
    // Previously every location update fired a network call, multiplying timeouts on bad connections.
    LaunchedEffect(locationState.value) {
        val loc = locationState.value ?: return@LaunchedEffect
        val previous = lastGeocodedLocation
        if (previous != null && locationCity.isNotEmpty() &&
            previous.distanceTo(loc) < GEOCODE_MIN_DISTANCE_METERS
        ) return@LaunchedEffect

        val city = resolveCity(context, loc.latitude, loc.longitude)
        if (!city.isNullOrEmpty()) {
            locationCity = city
            lastGeocodedLocation = loc
        }
    }

    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(sensorEventListener) }
    }

    // FIX: decoded once and cached across recompositions (previously decoded on every sensor update)
    val imageBitmap = ImageBitmap.imageResource(R.drawable.kaaba)

    if (calibrationRequired) CalibrationDialog(onDismiss = { calibrationRequired = false })
    if (showPositionDialog) DevicePositionDialog(onDismiss = { triggerPositionDialog(false) })

    // Capture theme colors before entering Canvas scope
    // (MaterialTheme composition locals are not accessible inside Canvas)
    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
    val colorError = md_theme_light_error.toArgb()
    val colorFace = MaterialTheme.colorScheme.surface

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top header ────────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OttrojjaWarningBar(text = "الرجاء تفعيل نظام تحديد المواقع GPS ليتمكن التطبيق من تحديد إتجاه القبلة")
            }
            if (locationState.value != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OttrojjaText(
                        text = "إتجاه القبلة °${bearing.toString().split(".")[0]}",
                        color = MaterialTheme.colorScheme.primary,
                        style = OttrojjaTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── Compass Canvas ────────────────────────────────────────────────────────
        Canvas(modifier = Modifier.size(400.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = min(size.width, size.height) / 2 * 0.7f

            // Radial gradient compass face — dark centre fading outward gives depth
            val gradientBrush = ShaderBrush(
                RadialGradientShader(
                    center = Offset(centerX, centerY),
                    radius = radius,
                    colors = listOf(
                        colorFace.copy(alpha = 0.20f),
                        colorFace.copy(alpha = 0.03f)
                    )
                )
            )
            drawCircle(brush = gradientBrush, radius = radius, center = Offset(centerX, centerY))

            // ── Rotating compass ring (ticks + labels + glow) ─────────────────────
            rotate(-animatedHeading, Offset(centerX, centerY)) {

                // Glow halo — two faint rings behind the main ring when aligned
                if (isAligned) {
                    drawCircle(
                        color = complete_green.copy(alpha = 0.20f),
                        radius = radius + 10.dp.toPx(),
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 14.dp.toPx())
                    )
                    drawCircle(
                        color = complete_green.copy(alpha = 0.08f),
                        radius = radius + 20.dp.toPx(),
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 10.dp.toPx())
                    )
                }

                // Main ring — slightly thicker when aligned
                drawCircle(
                    color = if (isAligned) complete_green else md_theme_light_secondary,
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = if (isAligned) 10.dp.toPx() else 8.dp.toPx())
                )

                // Three-tier tick system
                //   Every 5°  → tiny hairline (very dim)
                //   Every 10° → minor tick    (dimmer)
                //   Every 45° → intercardinal (medium height, medium opacity)
                //   Every 90° → cardinal      (tallest, full opacity)
                for (i in 0 until 360 step 5) {
                    val isCardinal = i % 90 == 0
                    val isIntercardinal = i % 45 == 0 && !isCardinal
                    val isMedium = i % 10 == 0 && !isCardinal && !isIntercardinal

                    val tickLength = when {
                        isCardinal -> radius * 0.14f
                        isIntercardinal -> radius * 0.10f
                        isMedium -> radius * 0.06f
                        else -> radius * 0.03f
                    }
                    val strokeWidth = when {
                        isCardinal -> 3.dp.toPx()
                        isIntercardinal -> 2.dp.toPx()
                        isMedium -> 1.2f.dp.toPx()
                        else -> 0.8f.dp.toPx()
                    }
                    val tickAlpha = when {
                        isCardinal -> 1.0f
                        isIntercardinal -> 0.75f
                        isMedium -> 0.45f
                        else -> 0.22f
                    }

                    rotate(i.toFloat(), Offset(centerX, centerY)) {
                        drawLine(
                            color = md_theme_light_primary.copy(alpha = tickAlpha),
                            start = Offset(centerX, centerY - radius),
                            end = Offset(centerX, centerY - radius + tickLength),
                            strokeWidth = strokeWidth
                        )
                    }
                }

                // Direction labels
                // Cardinals (N/S/E/W) are large + bold; N gets the error-red accent.
                // Intercardinals (NE/SE/SW/NW) are smaller + dimmer so they don't compete.
                val cardinalTextRadius = radius * 0.76f
                val intercardinalTextRadius = radius * 0.72f

                drawContext.canvas.nativeCanvas.apply {
                    val cardinalPaint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = radius * 0.13f
                        isFakeBoldText = true
                        isAntiAlias = true
                        color = colorPrimary
                    }
                    val northPaint = android.graphics.Paint(cardinalPaint).apply {
                        color = colorError
                        textSize = radius * 0.15f
                    }
                    val intercardinalPaint = android.graphics.Paint().apply {
                        // ~73% opacity of primary
                        color = (colorPrimary.toLong() and 0x00FFFFFFL or 0xBB000000L).toInt()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = radius * 0.09f
                        isFakeBoldText = false
                        isAntiAlias = true
                    }

                    fun drawCardinal(text: String, angleDeg: Float, paint: android.graphics.Paint) {
                        val rad = Math.toRadians(angleDeg.toDouble())
                        val x = centerX + (cardinalTextRadius * sin(rad)).toFloat()
                        val y = centerY - (cardinalTextRadius * cos(rad)).toFloat()
                        drawText(text, x, y + paint.textSize / 3f, paint)
                    }

                    fun drawIntercardinal(text: String, angleDeg: Float) {
                        val rad = Math.toRadians(angleDeg.toDouble())
                        val x = centerX + (intercardinalTextRadius * sin(rad)).toFloat()
                        val y = centerY - (intercardinalTextRadius * cos(rad)).toFloat()
                        drawText(text, x, y + intercardinalPaint.textSize / 3f, intercardinalPaint)
                    }

                    drawCardinal("N", 0f, northPaint)
                    drawCardinal("E", 90f, cardinalPaint)
                    drawCardinal("S", 180f, cardinalPaint)
                    drawCardinal("W", 270f, cardinalPaint)

                    drawIntercardinal("NE", 45f)
                    drawIntercardinal("SE", 135f)
                    drawIntercardinal("SW", 225f)
                    drawIntercardinal("NW", 315f)
                }
            }

            // ── Qibla needle (independent rotation) ──────────────────────────────
            rotate(animatedAngle, Offset(centerX, centerY)) {
                val needleLength = radius * 0.9f
                val needleWidth = radius * 0.15f
                val tailWidth = radius * 0.07f

                Path().apply {
                    moveTo(centerX, centerY - needleLength)
                    lineTo(centerX - needleWidth, centerY)
                    lineTo(centerX - tailWidth, centerY)
                    lineTo(centerX, centerY + needleLength * 0.3f)
                    lineTo(centerX + tailWidth, centerY)
                    lineTo(centerX + needleWidth, centerY)
                    close()
                }.let { drawPath(it, md_theme_light_primary) }

                Path().apply {
                    moveTo(centerX, centerY + needleLength * 0.3f)
                    lineTo(centerX - tailWidth, centerY)
                    lineTo(centerX, centerY - needleLength)
                    lineTo(centerX + tailWidth, centerY)
                    close()
                }.let { drawPath(it, Color.White) }

                drawCircle(
                    color = Color.White,
                    radius = radius * 0.05f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = md_theme_light_secondary,
                    radius = radius * 0.03f,
                    center = Offset(centerX, centerY)
                )
            }

            // Kaaba image with animated saturation fade ────────────────────
            // Builds a proper luminance-preserving saturation ColorMatrix driven by
            // animatedSaturation (0 = greyscale, 1 = full colour).
            val s = animatedSaturation
            val satMatrix = ColorMatrix(floatArrayOf(
                0.213f + 0.787f * s, 0.715f - 0.715f * s, 0.072f - 0.072f * s, 0f, 0f,
                0.213f - 0.213f * s, 0.715f + 0.285f * s, 0.072f - 0.072f * s, 0f, 0f,
                0.213f - 0.213f * s, 0.715f - 0.715f * s, 0.072f + 0.928f * s, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            )

            // Proportional image sizing + offset
            val imgSize = (radius * 0.30f).toInt()
            val imgLeft = (centerX - imgSize / 2f).toInt()
            // sits just outside the ring with a small gap so it doesn't touch the stroke
            val imgTop = (centerY - radius - imgSize - radius * 0.35f).toInt()
            drawImage(
                imageBitmap,
                dstOffset = IntOffset(imgLeft, imgTop),
                dstSize = IntSize(imgSize, imgSize),
                colorFilter = ColorFilter.colorMatrix(satMatrix)
            )
        }

        // ── Bottom info card ──────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(12.dp)) {
            OttrojjaDetailsContainer {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (locationState.value != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OttrojjaText(
                                "Lat: ${String.format(Locale.US, "%.4f", locationState.value?.latitude)}" +
                                        "    Lng: ${String.format(Locale.US, "%.4f", locationState.value?.longitude)}",
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (locationCity.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OttrojjaText(
                                    locationCity,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = OttrojjaTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OttrojjaText("جاري المعالجة..",
                                color = MaterialTheme.colorScheme.primary,
                                style = OttrojjaTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OttrojjaText(
                            text = "قوة المجال المغناطيسي: ",
                            color = MaterialTheme.colorScheme.primary,
                            style = OttrojjaTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        OttrojjaText(
                            "${magneticFieldValue.toInt()} µT",
                            color = when (magneticFieldQuality) {
                                MFSQuality.GOOD -> complete_green
                                MFSQuality.MEDIUM -> MaterialTheme.colorScheme.secondary
                                MFSQuality.BAD -> MaterialTheme.colorScheme.error
                            },
                            style = OttrojjaTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable { calibrationRequired = true },
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}

private const val GEOCODE_MIN_DISTANCE_METERS = 2_000f
private val GEOCODE_TIMEOUT = 6.seconds

private fun normalizeAngle(angle: Float): Float = when {
    angle < 0 -> angle + 360
    angle >= 360 -> angle - 360
    else -> angle
}

private fun Address.bestName(): String? =
    locality ?: subAdminArea ?: adminArea ?: countryName

/**
 * Reverse-geocodes a coordinate to a human-readable place name.
 *
 * Returns null (never throws) when:
 *  - no geocoder backend exists on the device
 *  - the network/backend is unavailable (DEADLINE_EXCEEDED, UNAVAILABLE, …)
 *  - the lookup takes longer than [GEOCODE_TIMEOUT_MS]
 *
 * Network failures are expected and are NOT reported as exceptions.
 */

suspend fun resolveCity(context: Context, latitude: Double, longitude: Double): String? {
    if (!Geocoder.isPresent()) return null
    val geocoder = Geocoder(context, Locale.getDefault())
    return withTimeoutOrNull(GEOCODE_TIMEOUT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (cont.isActive) cont.resume(addresses.firstOrNull()?.bestName())
                    }

                    override fun onError(errorMessage: String?) {
                        if (cont.isActive) cont.resume(null)
                    }
                })
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.bestName()
                } catch (e: IOException) {
                    null // network / backend unavailable — expected, don't report
                } catch (e: IllegalArgumentException) {
                    reportException(exception = e, file = "QiblaScreen")
                    null
                }
            }
        }
    }
}

enum class MFSQuality { GOOD, BAD, MEDIUM }
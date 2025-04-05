package com.ottrojja.screens.qiblaScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.ui.theme.complete_green
import com.ottrojja.ui.theme.md_theme_light_primary
import com.ottrojja.ui.theme.md_theme_light_secondary
import kotlin.math.abs


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
            ContextCompat.checkSelfPermission(context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        permissionsGranted = anyGranted
        if (!anyGranted) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    if (permissionsGranted) {
        CompassContent()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("يحتاج التطبيق لأذونات الوصول للموقع لتحديد اتجاه القبلة بدقة",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(onClick = { permissionLauncher.launch(permissions.toTypedArray()) }) {
                    Text(
                        "منح الأذونات", style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassContent() {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE
        ) as SensorManager
    }
    var angle by remember { mutableStateOf(0f) }
    var bearing by remember { mutableStateOf(0f) }
    val animatedAngle by animateFloatAsState(
        targetValue = angle,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
    )

    var calibrationRequired by remember { mutableStateOf(false) }
    var sensorAccuracy by remember { mutableStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }

    val locationState = remember { mutableStateOf<Location?>(null) }
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    val sensorEventListener = remember {
        object : SensorEventListener {
            private val accelerometerReading = FloatArray(3)
            private val magnetometerReading = FloatArray(3)
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0,
                        accelerometerReading, 0, 3
                    )

                    Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0,
                        magnetometerReading, 0, 3
                    )
                }

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading,
                        magnetometerReading
                    )
                ) {
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthRadians = orientationAngles[0]
                    val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
                    angle = bearing - azimuthDegrees
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
                when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                        calibrationRequired = true
                    }

                    SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                        calibrationRequired = true
                    }

                    else -> {
                        calibrationRequired = false
                    }
                }
            }

        }
    }

    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            sensorEventListener,
            magnetometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        val provider = LocationManager.GPS_PROVIDER

        // Create a listener that updates the state with new location values.
        val locationListener = object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                locationState.value = newLocation
                val userLoc = Location("service Provider");
                //get longitudeM Latitude and altitude of current location with gps class and  set in userLoc
                userLoc.setLongitude(newLocation.longitude);
                userLoc.setLatitude(newLocation.latitude);
                userLoc.setAltitude(newLocation.altitude);

                val destinationLoc = Location("service Provider");
                destinationLoc.setLatitude(21.422487); //kaaba latitude setting
                destinationLoc.setLongitude(39.826206); //kaaba longitude setting
                val bearTo = userLoc.bearingTo(destinationLoc);
                bearing = normalizeAngle(bearTo)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        //Ignore the permission warning, this composable wouldn't be rendered unless the permission is given in the first place
        locationManager.requestLocationUpdates(provider, 5000L, 5f, locationListener)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
            locationManager.removeUpdates(locationListener)
        }
    }

    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.kaaba)
    val imageBitmap = bitmap.asImageBitmap()

    // Calibration dialog
    if (calibrationRequired) {
        CalibrationDialog(
            onDismiss = { calibrationRequired = false },
            onConfirm = { calibrationRequired = false }
        )
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تجريبي",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الرجاء تفعيل نظام تحديد المواقع GPS ليتمكن التطبيق من تحديد إتجاه القبلة",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (locationState.value != null) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "إتجاه القبلة °${bearing.toString().split(".").get(0)}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Canvas(modifier = Modifier
            .size(400.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = min(size.width, size.height) / 2 * 0.7f

            // Draw compass background
            drawCircle(
                color = if (abs(angle.toInt()) == 0) complete_green else md_theme_light_secondary,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 8.dp.toPx())
            )

            // Draw compass rose
            val roseSize = radius * 0.1f
            for (i in 0 until 360 step 30) {
                rotate(i.toFloat(), Offset(centerX, centerY)) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(centerX, centerY - radius + 10.dp.toPx()),
                        end = Offset(centerX, centerY - radius + roseSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }


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
                }.let { path ->
                    drawPath(path, md_theme_light_primary)
                }

                // South needle (white)
                Path().apply {
                    moveTo(centerX, centerY + needleLength * 0.3f)
                    lineTo(centerX - tailWidth, centerY)
                    lineTo(centerX, centerY - needleLength)
                    lineTo(centerX + tailWidth, centerY)
                    close()
                }.let { path ->
                    drawPath(path, Color.White)
                }

                // Needle center circle
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

            val greyscaleMatrix = ColorMatrix(
                floatArrayOf(
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            val drawImageWidth = 108;
            val drawImageHeight = 108;

            drawImage(imageBitmap,
                dstOffset = IntOffset((centerX - drawImageWidth / 2).toInt(),
                    (centerY - radius - 200).toInt()
                ),
                dstSize = IntSize(drawImageWidth, drawImageHeight
                ),
                colorFilter = if (abs(angle.toInt()) == 0) null else ColorFilter.colorMatrix(
                    greyscaleMatrix
                )
            )
        }

        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (locationState.value != null) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Lat: ${
                        String.format(Locale.US, "%.4f", locationState.value?.latitude
                        )
                    }    Lng: ${String.format(Locale.US, "%.4f", locationState.value?.longitude)}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(getCityFromLocation(context,
                        locationState.value?.latitude,
                        locationState.value?.longitude
                    ), color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("جاري المعالجة..",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// IMPROVED NORMALIZATION
private fun normalizeAngle(angle: Float): Float {
    return when {
        angle < 0 -> angle + 360
        angle >= 360 -> angle - 360
        else -> angle
    }
}

private fun getCityFromLocation(context: Context, latitude: Double?, longitude: Double?): String {
    if (latitude != null && longitude != null) {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
            addresses[0].locality ?: ""
        } catch (e: Exception) {
            Log.e("Geocoder Error", e.message.toString())
            ""
        }
    }
    return ""
}


@Composable
private fun CalibrationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    OttrojjaDialog(contentModifier = Modifier
        .padding(8.dp)
        .fillMaxHeight(0.7f)
        .clip(shape = RoundedCornerShape(12.dp)), onDismissRequest = { onDismiss() }) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxHeight(0.9f)
                .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("معايرة البوصلة",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Image(painter = painterResource(R.drawable.calibration),
                    contentDescription = "calibration",
                    modifier = Modifier.fillMaxWidth()
                )

                Text("لتحديد اتجاه القبلة بدقة يرجى:", style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
                Text("١. أمساك الجهاز بشكل مسطح", style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
                Text("٢. تحريكه في نمط بشكل رقم 8", style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
                Text("٣. تجنب التداخل المغناطيسي وأبعد الجهاز عن أي أجهزة كهربائية أو مغناطيسات",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(onClick = { onDismiss() }) {
                    Text(
                        "إغلاق",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 20.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}


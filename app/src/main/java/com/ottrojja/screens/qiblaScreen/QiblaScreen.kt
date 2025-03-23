package com.ottrojja.screens.qiblaScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R


@Composable
fun QiblaScreen(qiblaViewModel: QiblaViewModel = viewModel()) {
    // Observe the azimuth and calibration states from the ViewModel
    val azimuth by qiblaViewModel.azimuth
    val needsCalibration by qiblaViewModel.needsCalibration

    // Animate the rotation smoothly using animateFloatAsState.
    // Note: We use a negative value so the needle rotates in the expected direction.
    val animatedRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Display the compass image with the animated rotation
        Image(
            painter = painterResource(id = R.drawable.needle),
            contentDescription = "Compass Needle",
            modifier = Modifier.size(200.dp).rotate(animatedRotation)
        )

        // If sensor accuracy is low, show a calibration prompt
        if (needsCalibration) {
            CalibrationPrompt()
        }
    }

}


@Composable
fun CalibrationPrompt() {
    AlertDialog(
        onDismissRequest = { /* Optionally allow dismissing */ },
        title = { Text("Compass Calibration Needed") },
        text = { Text("Your compass accuracy is low. Please move your phone in a figure-8 motion to calibrate it.") },
        confirmButton = {
            TextButton(onClick = { /* You might dismiss the prompt after user acknowledgment */ }) {
                Text("OK")
            }
        }
    )
}

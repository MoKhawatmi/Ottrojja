package com.ottrojja.composables

import android.widget.SeekBar
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ottrojja.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSlider(
    sliderPosition: Float,
    setSliderPosition: (Float) -> Unit,
    sliderMaxDuration: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = { setSliderPosition(it) },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            steps = 0,
            thumb = {
                Image(
                    painterResource(id = R.drawable.thumb),
                    "thumb",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(0.dp)
                )
            },
            valueRange = 0f..sliderMaxDuration,
        )
    }
}

@Composable
fun MediaSeekBar(
    sliderPosition: Float,
    setSliderPosition: (Float) -> Unit,
    sliderMaxDuration: Float
) {
    val primaryContainer=MaterialTheme.colorScheme.primaryContainer;
    val primary=MaterialTheme.colorScheme.primary;
    BoxWithConstraints(modifier = Modifier.height(32.dp)) {
        val width = constraints.maxWidth.toFloat()
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newValue = (change.position.x / width).coerceIn(0f, sliderMaxDuration)
                    setSliderPosition(newValue)
                }
            }
        ) {
            // Draw inactive track
            drawRoundRect(
                color = primaryContainer,
                size = size.copy(width = width, height = 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            // Draw active track
            drawRoundRect(
                color = primary,
                size = size.copy(width = sliderPosition * width, height = 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            // Draw thumb
            drawRect(
                color = primary,
                size = size.copy(width = 24.dp.toPx(), height = 24.dp.toPx()),
                )
        }
    }
}

@Composable
fun LegacySeekBar(
    sliderPosition: Float,
    setSliderPosition: (Float) -> Unit,
    sliderMaxDuration: Float
) {
    AndroidView(
        modifier = Modifier,
        factory = { context ->
            SeekBar(context).apply {
                max = sliderMaxDuration.toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) setSliderPosition(progress.toFloat())
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
        },
        update = { seekBar ->
            seekBar.progress = sliderPosition.toInt()
        }
    )
}

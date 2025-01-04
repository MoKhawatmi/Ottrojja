package com.ottrojja.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R

@Composable
fun MediaController(
    modifier: Modifier = Modifier
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
        .fillMaxWidth()
        .padding(10.dp),
    isPlaying: Boolean,
    playbackSpeed: Float,
    isDownloading: Boolean,
    onFasterClicked: () -> Unit,
    onNextClicked: () -> Unit = {},
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onPreviousClicked: () -> Unit = {},
    onSlowerClicked: () -> Unit,
    hasNextPreviousControl: Boolean,
    content: @Composable() () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        content()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPlaying) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.15f)
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isPlaying && !isDownloading) {
                    Image(painter = painterResource(R.drawable.faster),
                        contentDescription = "faster",
                        modifier = Modifier
                            .clickable { onFasterClicked() }
                            .size(25.dp))
                    if (hasNextPreviousControl) {
                        Image(painter = painterResource(R.drawable.next),
                            contentDescription = "next",
                            modifier = Modifier
                                .clickable { onNextClicked() }
                                .size(25.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                } else if (isPlaying) {
                    Image(painter = painterResource(R.drawable.playing),
                        contentDescription = "pause",
                        modifier = Modifier
                            .padding(10.dp, 0.dp)
                            .clickable { onPauseClicked() }
                            .size(35.dp))
                } else {
                    Image(painter = painterResource(R.drawable.play),
                        contentDescription = "play",
                        modifier = Modifier
                            .clickable { onPlayClicked() }
                            .size(35.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                }
                if (isPlaying && !isDownloading) {
                    if (hasNextPreviousControl) {
                        Image(painter = painterResource(R.drawable.previous),
                            contentDescription = "prev",
                            modifier = Modifier
                                .clickable { onPreviousClicked() }
                                .size(25.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Image(painter = painterResource(R.drawable.slower),
                        contentDescription = "slower",
                        modifier = Modifier
                            .clickable { onSlowerClicked() }
                            .size(25.dp))
                }
            }
        }
    }

}
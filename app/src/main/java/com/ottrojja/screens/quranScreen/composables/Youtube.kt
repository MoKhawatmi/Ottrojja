package com.ottrojja.screens.quranScreen.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ottrojja.BuildConfig
import com.ottrojja.classes.Helpers
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTube(link: String) {
    val context = LocalContext.current
    println(link)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(10.dp)
    ) {
        if (!Helpers.checkNetworkConnectivity(context)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "تعذر عرض المقطع لعدم توفر اتصال بالشبكة",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            YoutubeScreen(videoId = link, modifier = Modifier)
        }
    }
}


@Composable
fun YoutubeScreen(
    videoId: String, modifier: Modifier
) {
    /* val context = LocalContext.current

     val iFramePlayerOptions = IFramePlayerOptions.Builder()
         .origin("https://${BuildConfig.APPLICATION_ID}")
         .build()


     AndroidView(factory = {
         var view = YouTubePlayerView(it)
         val fragment = view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
             override fun onReady(youTubePlayer: YouTubePlayer) {
                 super.onReady(youTubePlayer)
                 youTubePlayer.cueVideo(videoId, 0f)
             }

             override fun onStateChange(
                 youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState
             ) {
                 super.onStateChange(youTubePlayer, state)
                 if (state.toString() == "PLAYING") {
                     Helpers.terminateAllServices(context)
                 }
                 println(state)
             }
         })
         view
     })*/

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            YouTubePlayerView(ctx ).apply {
                enableAutomaticInitialization=false
                // Attach lifecycle
                lifecycleOwner.lifecycle.addObserver(this)

                // Build iframe options with a custom origin
                val iFramePlayerOptions = IFramePlayerOptions.Builder()
                    .origin("https://${BuildConfig.APPLICATION_ID}")
                    .controls(1)
                    .build()

                initialize(
                    object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.cueVideo(videoId, 0f)
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState
                        ) {
                            super.onStateChange(youTubePlayer, state)
                            if (state.toString() == "PLAYING") {
                                Helpers.terminateAllServices(context)
                            }
                            println(state)
                        }
                    },
                    iFramePlayerOptions
                )
            }
        },
        modifier = modifier
    )
}
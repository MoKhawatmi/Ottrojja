package com.ottrojja.services.common

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.ottrojja.classes.Helpers.reportException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * The MediaController side of one MediaSessionService, shared by the playback managers:
 * connects lazily, queues actions until connected, releases when idle and polls the position.
 *
 * Must be used from the main thread (MediaController requirement).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaSessionConnection(
    context: Context,
    private val serviceClass: Class<out MediaSessionService>,
    /** Name reported with connection errors. */
    private val owner: String,
    /** Called whenever the player state or the session extras change. */
    private val onSync: (MediaController) -> Unit,
    /** Called when the connection is lost or released, to reset the exposed state. */
    private val onReset: () -> Unit,
    /** Keeps the connection in releaseIfIdle() even with nothing loaded (e.g. while downloading). */
    private val isBusy: () -> Boolean = { false },
) {
    private val appContext = context.applicationContext
    private val mainExecutor = ContextCompat.getMainExecutor(appContext)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    var controller: MediaController? = null
        private set
    private val pendingActions = mutableListOf<(MediaController) -> Unit>()

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            controller?.let(onSync)
        }
    }

    fun connect() {
        if (controllerFuture != null) return

        val token = SessionToken(appContext, ComponentName(appContext, serviceClass))
        val future = MediaController.Builder(appContext, token)
            .setListener(object : MediaController.Listener {
                override fun onDisconnected(controller: MediaController) = onControllerLost()
                override fun onExtrasChanged(controller: MediaController, extras: Bundle) = onSync(controller)
            })
            .buildAsync()
        controllerFuture = future

        future.addListener({
            if (future.isCancelled) return@addListener
            val connected = try {
                future.get()
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = owner)
                onControllerLost()
                return@addListener
            }
            controller = connected
            connected.addListener(playerListener)
            onSync(connected)

            val actions = pendingActions.toList()
            pendingActions.clear()
            actions.forEach { it(connected) }
        }, mainExecutor)
    }

    /** Called when the screen goes away; keeps the connection while something is loaded. */
    fun releaseIfIdle() {
        val c = controller ?: return
        val idle = (c.mediaItemCount == 0
                || c.playbackState == Player.STATE_IDLE
                || c.playbackState == Player.STATE_ENDED)
                && !isBusy()
        if (!idle) return

        c.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
        onReset()
    }

    /** Runs [action] now if connected, otherwise connects and runs it once connected. */
    fun withController(action: (MediaController) -> Unit) {
        val c = controller
        if (c != null) {
            action(c)
        } else {
            pendingActions += action
            connect()
        }
    }

    /** Current position; polls every 400ms only while playing, emits once when paused. */
    fun positionMs(isPlaying: Flow<Boolean>): Flow<Long> = isPlaying
        .distinctUntilChanged()
        .flatMapLatest { playing ->
            flow {
                emit(controller?.currentPosition ?: 0L)
                while (playing) {
                    delay(400)
                    emit(controller?.currentPosition ?: 0L)
                }
            }
        }
        .flowOn(Dispatchers.Main)

    private fun onControllerLost() {
        controller = null
        controllerFuture = null
        pendingActions.clear()
        onReset()
    }
}

package com.ottrojja.composables.ToastManager.classes

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicLong

object AppToastManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val queue = Channel<AppToast>(Channel.UNLIMITED)
    private val nextId = AtomicLong(0)

    private val _currentToast = MutableStateFlow<AppToast?>(null)
    val currentToast = _currentToast.asStateFlow()

    // Completed when the currently shown toast should be dismissed early
    private var dismissSignal: CompletableDeferred<Unit>? = null

    init {
        scope.launch {
            for (toast in queue) {
                val signal = CompletableDeferred<Unit>()
                dismissSignal = signal
                _currentToast.value = toast

                // Wait for the duration OR an early dismiss, whichever comes first
                withTimeoutOrNull(toast.durationMillis.coerceAtLeast(1000L)) {
                    signal.await()
                }

                dismissSignal = null
                _currentToast.value = null
            }
        }
    }

    fun show(
        message: String,
        type: AppToastType = AppToastType.INFO,
        durationMillis: Long = 3000L
    ) {
        if (message.isBlank()) return
        queue.trySend(
            AppToast(
                id = nextId.incrementAndGet(),
                message = message,
                type = type,
                durationMillis = durationMillis
            )
        )
    }

    /** Dismiss the toast with [id] if it's the one currently shown. */
    fun dismiss(id: Long) {
        scope.launch {
            if (_currentToast.value?.id == id) dismissSignal?.complete(Unit)
        }
    }

    /** Dismiss whatever toast is currently shown. */
    fun dismissCurrent() {
        scope.launch { dismissSignal?.complete(Unit) }
    }
}
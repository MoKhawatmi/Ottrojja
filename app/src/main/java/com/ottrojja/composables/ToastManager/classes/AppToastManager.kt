package com.ottrojja.composables.ToastManager.classes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

object AppToastManager {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private val queue = Channel<AppToast>(Channel.UNLIMITED)
    private val nextId = AtomicLong(0)

    private val _currentToast = MutableStateFlow<AppToast?>(null)
    val currentToast = _currentToast.asStateFlow()

    init {
        scope.launch {
            for (toast in queue) {
                _currentToast.value = toast

                delay(toast.durationMillis.coerceAtLeast(1000L))

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
}
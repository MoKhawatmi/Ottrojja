package com.ottrojja.classes

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.ottrojja.classes.DataStore.DataStoreRepository
import com.ottrojja.services.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object OverlayBootstrap {
    fun init(context: Context, scope: CoroutineScope) {
        scope.launch {
            checkAndStartOverlay(context)
        }
    }

    private suspend fun checkAndStartOverlay(context: Context) {
        val enabled = DataStoreRepository.floatingAzkarFlow.first()

        val canDraw = Settings.canDrawOverlays(context)

        if (enabled && canDraw) {
            startOverlay(context)
        }
    }

    private fun startOverlay(context: Context) {
        val intent = Intent(context, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

}
package com.ottrojja.screens.quranScreen.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout


fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Renders [content] in a throwaway ComposeView behind the app UI and returns it as a Bitmap. */
suspend fun renderCardBitmap(
    activity: Activity,
    parent: CompositionContext,
    content: @Composable () -> Unit
): Bitmap {
    val decor = activity.window.decorView as ViewGroup
    val result = CompletableDeferred<Bitmap>()

    val view = ComposeView(activity).apply {
        setParentCompositionContext(parent) // inherits your theme / CompositionLocals
        setContent {
            val layer = rememberGraphicsLayer()
            Box(
                Modifier.drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                }
            ) { content() }

            LaunchedEffect(Unit) {
                withFrameNanos { }   // let it compose, measure and draw fully
                withFrameNanos { }
                result.complete(layer.toImageBitmap().asAndroidBitmap())
            }
        }
    }

    // Index 0 = drawn underneath the real UI, so the user never sees it
    decor.addView(view, 0, FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    )
    try {
        return withTimeout(3000) { result.await() }
    } finally {
        decor.removeView(view)
    }
}
package com.ottrojja.classes.Toast

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.ToastManager.classes.AppToast
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun AppToastHost(
    modifier: Modifier = Modifier,
    bottomOffset: Dp = 16.dp
) {
    val toast by AppToastManager.currentToast.collectAsState()

    var lastToast by remember { mutableStateOf<AppToast?>(null) }
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(toast) {
        if (toast != null) lastToast = toast
        visibleState.targetState = toast != null
    }

    val windowActive = visibleState.currentState || visibleState.targetState
    val shown = lastToast

    if (windowActive && shown != null) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val view = LocalView.current
            SideEffect {
                (view.parent as? DialogWindowProvider)?.window?.apply {
                    // Window is only as tall as the toast, so the rest of the screen stays interactive
                    setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )
                    setGravity(Gravity.BOTTOM)
                    clearFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    // Touches outside the window pass through to the app behind
                    addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    )
                }
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(180)) + slideInVertically { it / 3 },
                exit = fadeOut(tween(140)) + slideOutVertically { it / 3 },
                modifier = modifier
            ) {
                Box(
                    Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = bottomOffset)
                ) {
                    ToastCard(
                        toast = shown,
                        onDismiss = { AppToastManager.dismiss(shown.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToastCard(
    toast: AppToast,
    onDismiss: () -> Unit
) {
    val (background, foreground, symbol) = when (toast.type) {
        AppToastType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "✓"
        )

        AppToastType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "!"
        )

        AppToastType.WARNING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "⚠"
        )

        AppToastType.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "i"
        )
    }

    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // no ripple
                onClick = onDismiss
            ),
        shape = shape,
        color = background,
        contentColor = foreground,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OttrojjaText(
                text = symbol,
                style = OttrojjaTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = foreground
            )

            Spacer(Modifier.width(12.dp))

            OttrojjaText(
                text = toast.message,
                modifier = Modifier.weight(1f),
                style = OttrojjaTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = foreground
            )
        }
    }
}
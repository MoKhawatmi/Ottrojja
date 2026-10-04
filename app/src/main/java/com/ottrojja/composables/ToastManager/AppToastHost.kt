package com.ottrojja.classes.Toast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomOffset
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = toast,
            transitionSpec = {
                (
                        fadeIn(tween(180)) +
                                slideInVertically { it / 3 }
                        ) togetherWith (
                        fadeOut(tween(140)) +
                                slideOutVertically { it / 3 }
                        )
            },
            label = "app_toast"
        ) { item ->
            if (item != null) {
                ToastCard(item)
            }
        }
    }
}

@Composable
private fun ToastCard(toast: AppToast) {
    val (background, foreground, symbol) = when (toast.type) {
        AppToastType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = background,
        contentColor = foreground,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 14.dp
            ),
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
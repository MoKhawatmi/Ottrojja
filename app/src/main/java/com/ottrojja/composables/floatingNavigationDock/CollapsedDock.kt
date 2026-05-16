package com.ottrojja.composables.floatingNavigationDock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.classes.Screen
import kotlin.math.exp

@Composable
fun CollapsedDock(
    currentRoute: String?,
    onClick: (String) -> Unit,
    expanded: Boolean,
    toggleExpanded: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )
    val orbRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "orbRotation"
    )



    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                optionText = "المصحف",
                isCurrent = Screen.MainScreen.route == currentRoute,
                onClick = {
                    onClick(Screen.MainScreen.route);
                },
                iconId = R.drawable.chapters_open,
                alternateIcon = R.drawable.chapters_closed,
                modifier = Modifier.weight(1f)

            )

            DockItem(
                optionText = "الاستماع",
                isCurrent = Screen.ListeningScreen.route == currentRoute,
                onClick = {
                    onClick(Screen.ListeningScreen.route);
                },
                iconId = R.drawable.chapters_listen,
                alternateIcon = null,
                modifier = Modifier.weight(1f)

            )
        }


        Box(
            modifier = Modifier.weight(0.6f),
            contentAlignment = Alignment.Center
        ) {
            ToggleButton(onClick = { toggleExpanded() }, expanded = expanded)
        }

        Row(modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                optionText = "الاذكار",
                isCurrent = Screen.AzkarMain.route == currentRoute,
                onClick = {
                    onClick(Screen.AzkarMain.route);
                },
                iconId = R.drawable.azkar,
                alternateIcon = null,
                modifier = Modifier.weight(1f)

            )

            DockItem(
                optionText = "المسبحة",
                isCurrent = Screen.TasbeehScreen.route == currentRoute,
                onClick = {
                    onClick(Screen.TasbeehScreen.route);
                },
                iconId = R.drawable.pin,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
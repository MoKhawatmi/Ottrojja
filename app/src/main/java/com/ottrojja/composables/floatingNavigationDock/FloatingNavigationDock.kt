package com.ottrojja.composables.floatingNavigationDock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun FloatingNavigationDock(navController: NavController,
                           expanded: Boolean,
                           onToggle: () -> Unit
) {

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    var dockBounds by remember {
        mutableStateOf<Rect?>(null)
    }

    val outsideClickModifier =
        if (expanded) {
            Modifier.pointerInput(Unit) {

                detectTapGestures { tapOffset ->

                    val clickedInside =
                        dockBounds?.contains(tapOffset) == true

                    if (!clickedInside) {
                        onToggle()
                    }
                }
            }
        } else {
            Modifier
        }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .fillMaxSize()
            .then(outsideClickModifier)
    ) {

        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .onGloballyPositioned { coordinates ->
                    dockBounds = coordinates.boundsInRoot()
                }
        ) {

            Column(
                modifier = Modifier
                    .animateContentSize()
            ) {
                // COLLAPSED BAR (ALWAYS VISIBLE)
                CollapsedDock(
                    currentRoute = currentRoute,
                    onClick = { route -> navController.navigate(route); if(expanded)onToggle() },
                    expanded = expanded,
                    toggleExpanded = { onToggle() }
                )

                // EXPANDED AREA
                AnimatedVisibility(visible = expanded) {
                    ExpandedDockContent(
                        navController = navController,
                        currentRoute = currentRoute,
                        onItemClick = {
                            navController.navigate(it)
                            onToggle()
                        }
                    )
                }

            }
        }
    }
}





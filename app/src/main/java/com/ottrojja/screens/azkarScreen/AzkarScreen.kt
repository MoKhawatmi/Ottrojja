package com.ottrojja.screens.azkarScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.TopBar

@Composable
fun AzkarScreen(
    navController: NavController,
    repository: QuranRepository
) {
    val context = LocalContext.current;
    val azkarViewModel: AzkarViewModel = viewModel(
        factory = AzkarModelFactory(repository)
    )

    LaunchedEffect(Unit) {
        azkarViewModel.fetchAzakr()
    }

    Column {
        TopBar(title = "الأذكار",
            mainAction = ButtonAction(Icons.Default.ArrowBack, action = { navController.popBackStack() })
        )
        Column(modifier = Modifier.padding(8.dp)) {
            LazyColumn {
                items(azkarViewModel.azkarData) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .background(Helpers.ottrojjaBrush, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12))
                            .clickable {
                                navController.navigate(
                                    Screen.ZikrScreen.invokeRoute(item.azkarTitle)
                                )
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.azkarTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Image(
                            painter = painterResource(
                                id = if (item.image.length > 0) context.getResources()
                                    .getIdentifier(item.image, "drawable", context.getPackageName()
                                    ) else R.drawable.azkar_filler
                            ),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}
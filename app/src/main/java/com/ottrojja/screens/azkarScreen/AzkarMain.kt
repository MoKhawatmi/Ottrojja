package com.ottrojja.screens.azkarScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Screen
import com.ottrojja.composables.Header

@Composable
fun AzkarMain(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(title = "الأذكار")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 3 columns
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(top = 12.dp, end = 6.dp, start = 6.dp, bottom = 6.dp)
        ) {
            item {
                AzkarSectionItem(imageId = R.drawable.supplication_symbol,
                    title = "الأذكار",
                    onClick = { navController.navigate(Screen.AzkarScreen.route) })
            }
            item {
                AzkarSectionItem(imageId = R.drawable.jwam3,
                    title = "جوامع الدعاء",
                    onClick = { navController.navigate(Screen.Jwam3Screen.route) })
            }
            item {
                AzkarSectionItem(imageId = R.drawable.meditation,
                    title = "أدعية مأثورة",
                    onClick = { navController.navigate(Screen.MainScreen.route) })
            }
        }
    }
}

@Composable
fun AzkarSectionItem(imageId: Int, title: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Helpers.ottrojjaBrush, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 16.dp)
    ) {

        Image(
            painter = painterResource(id = imageId),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(top = 38.dp)
        )


    }
}
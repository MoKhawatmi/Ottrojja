package com.ottrojja.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ottrojja.R

@Composable
fun Header(modifier: Modifier=Modifier){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(0.dp)
            .fillMaxHeight(0.1f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(1f)
                .fillMaxWidth(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
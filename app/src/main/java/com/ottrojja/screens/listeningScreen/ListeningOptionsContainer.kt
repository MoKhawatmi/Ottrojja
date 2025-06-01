package com.ottrojja.screens.listeningScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun ListeningOptionsContainer(content:@Composable()()->Unit){
    Column(modifier = Modifier
        .padding(12.dp)
        .fillMaxWidth()
        .shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            clip = false
        )
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())
        .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        content()
    }

}
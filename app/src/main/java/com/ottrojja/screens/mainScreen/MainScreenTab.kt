package com.ottrojja.screens.mainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.MainScreenTab(title: String,
                  imageId: Int,
                  startColor: Color,
                  endColor: Color,
                  onClick: () -> Unit) {

    Column(
        modifier = Modifier
            .padding(12.dp, 18.dp)
            .clip(RoundedCornerShape(16))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        startColor,
                        endColor
                    )
                )
            )
            .weight(1f)
            .clickable {
                onClick()
            }
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = "",
            modifier = Modifier
                .size(60.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White
        );
    }
}
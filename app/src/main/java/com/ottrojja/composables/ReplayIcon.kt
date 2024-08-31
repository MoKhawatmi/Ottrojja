package com.ottrojja.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReplayIcon(number: String, onClickUpdateRep: ()->Unit) {
    val primaryColor=MaterialTheme.colorScheme.primary;
    Box(
        modifier = Modifier
            .padding(0.dp, 5.dp, 8.dp, 5.dp).clickable { onClickUpdateRep() }
    ) {
        Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center).size(32.dp),
        )
        if (number > "0") {
            Text(
                text = number,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                modifier = Modifier
                    .offset(x = -2.dp, y=2.dp)
                    .drawBehind {
                        drawCircle(
                            color = primaryColor,
                        )
                    }
                    .align(Alignment.BottomStart).padding(6.dp)
            )
        }
    }
}
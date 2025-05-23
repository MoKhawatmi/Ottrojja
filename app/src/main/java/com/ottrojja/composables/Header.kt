package com.ottrojja.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.R
import com.ottrojja.classes.ButtonAction

@Composable
fun Header(modifier: Modifier = Modifier,
           isMain: Boolean = false,
           title: String = "",
           buttonAction: ButtonAction? = null) {
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
        if (!isMain) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (buttonAction != null) Arrangement.SpaceBetween else Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp, 0.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                if (buttonAction != null) {
                    OttrojjaElevatedButton(icon = buttonAction.icon,
                        onClick = { buttonAction.action() })
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp, 0.dp)
            ) {
                Text(
                    text = "مَثَلُ الْمُؤْمِنِ الَّذِي يَقْرَأُ الْقُرْآنَ كَمَثَلِ الْأُتْرُجَّةِ، رِيحُهَا طَيِّبٌ وَطَعْمُهَا طَيِّبٌ",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }


    }
}
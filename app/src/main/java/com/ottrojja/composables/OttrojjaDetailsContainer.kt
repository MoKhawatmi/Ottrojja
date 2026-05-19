package com.ottrojja.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaDetailsContainer(
    title: String?=null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        // Main card
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp, 24.dp, 8.dp, 16.dp) // extra top padding for pill space
        ) {
            content()
        }

        // Floating pill title
        if(title!=null){
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .padding(start = 20.dp)
                    .defaultMinSize(minWidth = 100.dp)
                    .align(Alignment.TopStart)
                    .offset(y = (-20).dp) // pulls it up over the border
            ) {
                OttrojjaText(
                    text = title,
                    style = OttrojjaTheme.typography.bodySpecialMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
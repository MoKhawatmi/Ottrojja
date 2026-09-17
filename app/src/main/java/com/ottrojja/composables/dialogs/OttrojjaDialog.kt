package com.ottrojja.composables.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText

@Composable
fun OttrojjaDialog(
    @SuppressLint("ModifierParameter") contentModifier: Modifier = Modifier
        .padding(8.dp)
        .wrapContentHeight()
        .background(MaterialTheme.colorScheme.background)
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(12.dp)),
    onDismissRequest: () -> Unit,
    useDefaultWidth: Boolean = true,
    title: String? = null,
    content: @Composable() () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = useDefaultWidth)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Box(modifier = contentModifier) {
                Image(
                    painterResource(R.drawable.splash_background),
                    contentDescription = "background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Column() {

                    if (title != null && title.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OttrojjaText(
                                text = title,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    content()
                }

            }
        }
    }
}
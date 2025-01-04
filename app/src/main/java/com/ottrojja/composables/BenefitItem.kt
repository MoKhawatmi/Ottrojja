package com.ottrojja.composables

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.ottrojja.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BenefitItem(
    benefitContent: String,
    shareSubject: String = "",
    shareTitle: String = "",
    shareContent: String = ""
) {
    val context = LocalContext.current;
    Row(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                        putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            shareContent
                        )
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, shareTitle)
                    startActivity(context, shareIntent, null)
                },
            )
            .padding(5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            painterResource(id = R.drawable.lightbulb),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(0.dp, 6.dp, 4.dp, 6.dp)
                .fillMaxWidth(0.08f)
        )
        Text(
            text = benefitContent,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(0.dp, 6.dp)
                .fillMaxWidth(0.92f)
        )
    }
}

@Composable
fun BenefitSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
package com.ottrojja.screens.teacherScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.VerseWithAnswer
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment


@Composable
fun FinalResults(selectedTrainingVerses: List<VerseWithAnswer>,
                 backToSelection: () -> Unit) {
    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(all = 10.dp),

            ) {
            items(selectedTrainingVerses) { item ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(if (item.answerCorrect) Color(0xFFE2FFD6
                                ) else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (item.answerCorrect) Color(0xFF29712C
                                    ) else MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${
                                    Helpers.convertToIndianNumbers("${item.verse.verseNum!!}")
                                } ${item.verse.verseText}",
                                color = if (item.answerCorrect) Color(0xFF29712C
                                ) else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(0.9f),
                            )
                        }

                        Icon(
                            imageVector = if (item.answerCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = "Answer Icon",
                            tint = if (item.answerCorrect) Color(0xFF29712C
                            ) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )

                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(14.dp),
                    thickness = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f)
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedTrainingVerses.filter { it.answerCorrect }.size} ايات صحيحة من اصل ${selectedTrainingVerses.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Button(onClick = { backToSelection() }) {
                    Text(
                        text = "إنهاء",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }


    }
}
package com.ottrojja.screens.Khitmah.khitmahScreen.Composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaDetailsContainer
import com.ottrojja.composables.OttrojjaLoadingIndicator
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.ottrojjaFlexibleActions.FlexibleAction
import com.ottrojja.composables.ottrojjaFlexibleActions.OttrojjaFlexibleActions
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun SessionStepItem(
    item: KhitmahSession,
    itemQuranRange: QuranRangeState?,
    onClick: (KhitmahSession) -> Unit,
    onDelete: () -> Unit,
    itemExpanded: Boolean,
    itemLoading: Boolean,
    invokeEditSessionForm: () -> Unit
) {
    OttrojjaDetailsContainer(modifier = Modifier.padding(bottom = if (itemExpanded) 8.dp else 0.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(item) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    OttrojjaText(
                        text = "صفحات ${item.startPage} - ${item.endPage}",
                        color = MaterialTheme.colorScheme.primary,
                        style = OttrojjaTheme.typography.bodyLarge,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "reading date",
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )

                        OttrojjaText(
                            text = Helpers.formatDateTime(item.timestamp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            style = OttrojjaTheme.typography.bodySmall,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                        )
                    }
                }

                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Khitmah Session Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onClick(item) }
                )
            }


            AnimatedVisibility(
                visible = itemExpanded,
            ) {
                Column(modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                ) {
                    if (itemLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OttrojjaLoadingIndicator()
                        }
                    } else {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Start),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = "progress",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                OttrojjaText(
                                    text = "القراءة: ${itemQuranRange?.startSurah?.chapterName}: ${itemQuranRange?.startVerse?.verseNum} - ${itemQuranRange?.endSurah?.chapterName}: ${itemQuranRange?.endVerse?.verseNum}",
                                    style = OttrojjaTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Start),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Numbers,
                                    contentDescription = "pages",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )

                                OttrojjaText(
                                    text = "الصفحات: ${(item.endPage - item.startPage) + 1} صفحة",
                                    style = OttrojjaTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (item.reflection != null) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Start),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ListAlt,
                                        contentDescription = "reflections",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    OttrojjaText(
                                        text = "ملاحظات: ${item.reflection}",
                                        style = OttrojjaTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            OttrojjaFlexibleActions(
                                modifier = Modifier.background(Color.Transparent),
                                actionList = listOf(
                                    FlexibleAction(
                                        text = "تعديل",
                                        bgColor = MaterialTheme.colorScheme.primary,
                                        textColor = MaterialTheme.colorScheme.onPrimary,
                                        action = { invokeEditSessionForm() },
                                        icon = Icons.Default.Edit
                                    ),
                                    FlexibleAction(
                                        text = "حذف",
                                        bgColor = MaterialTheme.colorScheme.error,
                                        textColor = MaterialTheme.colorScheme.onError,
                                        action = { onDelete() },
                                        icon = Icons.Default.Close
                                    )
                                ),
                                bgColor = Color.Transparent
                            )
                        }
                    }
                }
            }
        }
    }
}
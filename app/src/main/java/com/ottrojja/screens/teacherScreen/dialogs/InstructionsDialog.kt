package com.ottrojja.screens.teacherScreen.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun InstructionsDialog(onDismiss: () -> Unit) {
    val instructionsText = mutableListOf<String>("المعلم")
    instructionsText.add("إختبر حفظك للقرآن الكريم من خلال هذه الميزة")
    instructionsText.add("الإرشادات:")
    instructionsText.add("بعد اختيار ايات التدريب قم بالضغط على زر البدء")
    instructionsText.add(
        "سيتم عرض الايات بالترتيب وبها فراغات لاكمالها حيث يمثل كل فراغ كلمة واحدة فقط من الاية"
    )
    instructionsText.add("قم بملئ الفراغات بالكلمات بدون استخدام اي تشكيل او حركات للحروف")
    instructionsText.add("ثم اضغط على زر التحقق للتحقق من اجاباتك")
    instructionsText.add("سيتم اظهار الاجابات الصحيحة باللون الاخضر والخاطئة باللون الاحمر")
    instructionsText.add(
        "امامك ثلاث محاولات للوصول للاجابات الصحيحة وبعدها يتم إظهار النص الصحيح لمقارنته بالحل ويمكنك عندها الاستماع للاية صوتيا"
    )
    instructionsText.add(
        "بعد الانتهاء من الاجابة على جميع الايات سيتم عرض قائمة بالايات وإن كانت الاجابة عليها صحيحة ام لا"
    )


    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
        allowVerticalScroll = false
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
            ) {
                itemsIndexed(instructionsText) { index, item ->

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.lightbulb),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(0.dp, 2.dp, 4.dp, 2.dp)
                                .fillMaxWidth(0.08f)
                        )
                        OttrojjaText(
                            text = item,
                            style = OttrojjaTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
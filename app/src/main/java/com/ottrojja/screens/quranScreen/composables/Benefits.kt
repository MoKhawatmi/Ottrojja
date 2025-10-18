package com.ottrojja.screens.quranScreen.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.BenefitItem
import com.ottrojja.composables.BenefitSectionTitle

@Composable
fun BenefitSectionSeparator() {
    Image(painter = painterResource(R.drawable.benefit_seperator),
        contentDescription = "",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun Benefits(
    benefits: Array<String>,
    appliance: Array<String>,
    guidance: Array<String>,
    pageNum: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LazyColumn() {
            if (benefits.size != 0) {
                item {
                    BenefitSectionTitle("فوائد الصفحة");
                    BenefitSectionSeparator()
                }
            }
            items(benefits) { benefit ->
                BenefitItem(
                    benefitContent = benefit,
                    shareSubject = "فائدة قرآنية",
                    shareTitle = "مشاركة الفائدة",
                    shareContent = "من الفوائد القرآنية للصفحة $pageNum \n $benefit\n${
                        stringResource(
                            R.string.share_app
                        )
                    }"
                )
            }

            if (guidance.size != 0) {
                item {
                    BenefitSectionTitle("توجيهات الصفحة");
                    BenefitSectionSeparator()
                }
            }
            items(guidance) { guidanceItem ->
                BenefitItem(
                    benefitContent = guidanceItem,
                    shareSubject = "توجيه قرآني",
                    shareTitle = "مشاركة التوجيه",
                    shareContent = "من التوجيهات القرآنية للصفحة $pageNum \n $guidanceItem\n${
                        stringResource(
                            R.string.share_app
                        )
                    }"
                )
            }

            if (appliance.size != 0) {
                item {
                    BenefitSectionTitle("الجانب التطبيقي");
                    BenefitSectionSeparator()
                }
            }
            items(appliance) { applianceItem ->
                BenefitItem(
                    benefitContent = applianceItem,
                    shareSubject = "تطبيق قرآني",
                    shareTitle = "مشاركة التطبيق",
                    shareContent = "من التطبيقات القرآنية للصفحة $pageNum \n $applianceItem\n${
                        stringResource(
                            R.string.share_app
                        )
                    }"
                )
            }
        }
    }
}

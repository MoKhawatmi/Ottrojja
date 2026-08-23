package com.ottrojja.screens.Khitmah.SessionForm

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaLoadingIndicator
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaFormFieldContainer
import com.ottrojja.composables.forms.OttrojjaTextArea
import com.ottrojja.composables.quranRangeSelector.QuranRangeSelector
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun SessionForm(
    khitmahId: Long,
    startPage: Int? = null,
    endPage: Int? = null,
    session: KhitmahSession? = null,
    onDismiss: () -> Unit,
    onFinished: () -> Unit,
    repository: QuranRepository
) {
    val context = LocalContext.current

    val viewModel: SessionFormViewModel = viewModel(
        factory = SessionFormViewModelFactory(
            quranRepository = repository,
            application = context.applicationContext as Application
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(session?.id) {
        viewModel.initialize(
            khitmahId = khitmahId,
            startPage = startPage,
            endPage = endPage,
            session = session
        )
    }

    fun dismissForm(){
        viewModel.reset()
        onDismiss()
    }

    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .clip(RoundedCornerShape(12.dp))
            .padding(12.dp),

        onDismissRequest = {dismissForm()},
        useDefaultWidth = false
    ) {
        val state = uiState

        if (state == null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
                OttrojjaLoadingIndicator(indicatorColor = MaterialTheme.colorScheme.primary)
            }

            return@OttrojjaDialog
        }

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OttrojjaText(
                    text = if (state.isEditMode) {
                        "تعديل جلسة القراءة"
                    } else {
                        "حفظ جلسة القراءة"
                    },
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                state.quranRange?.let { range ->
                    QuranRangeSelector(
                        range = range,
                        onRangeChange = viewModel::updateRange,
                        repository = repository
                    )
                }

                OttrojjaFormFieldContainer(
                    backgroundColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            OttrojjaText(
                                text = "الصفحات المسجلة",
                                style = OttrojjaTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            OttrojjaText(
                                text = "${state.quranRange?.startVerse?.pageNum} - ${state.quranRange?.endVerse?.pageNum}",
                                style = OttrojjaTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OttrojjaTextArea(
                    value = state.reflection,
                    onChange = viewModel::updateReflection,
                    label = "تأملات أو ملاحظات",
                    maxLength = 250
                )
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OttrojjaButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    text = if (state.isEditMode) "حفظ التعديلات" else "حفظ الجلسة",
                    onClick = {
                        viewModel.save(onSuccess = { dismissForm(); onFinished(); })
                    }
                )
                OttrojjaButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    text = "الغاء",
                    onClick = { dismissForm() }
                )
            }
        }
    }
}

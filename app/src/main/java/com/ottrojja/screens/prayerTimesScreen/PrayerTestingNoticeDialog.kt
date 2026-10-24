package com.ottrojja.screens.prayerTimesScreen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.screens.prayerTimesScreen.model.CalculationMethodOption
import com.ottrojja.screens.prayerTimesScreen.model.PrayerSettings
import com.ottrojja.ui.theme.OttrojjaTheme
import java.util.Locale

/** Where users report wrong prayer times (same address as the settings contact dialog). */
internal const val PRAYER_TIMES_FEEDBACK_EMAIL = "ottrojjaapp@gmail.com"

/**
 * Weekly notice that prayer times are still being tested. Shown only by PrayerTimesScreen;
 * PrayerTimesViewModel decides when (once per week, see PrayerTimesRepository).
 *
 * "مراسلتنا" opens the mail app with the details we need already filled in
 * (country, city, coordinates, method, madhab), so the user only adds the wrong times.
 */
@Composable
fun PrayerTestingNoticeDialog(
    settings: PrayerSettings?,
    effectiveMethod: CalculationMethodOption?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val location = settings?.location
    val methodLabel = when {
        effectiveMethod == null -> ""
        settings?.method == null -> stringResource(
            R.string.prayer_settings_method_automatic,
            stringResource(effectiveMethod.nameRes),
        )
        else -> stringResource(effectiveMethod.nameRes)
    }
    val emailSubject = stringResource(R.string.prayer_testing_notice_email_subject)
    val emailBody = stringResource(
        R.string.prayer_testing_notice_email_body,
        location?.displayName.orEmpty(),
        location?.countryCode.orEmpty(),
        location?.let { String.format(Locale.US, "%.4f, %.4f", it.latitude, it.longitude) }.orEmpty(),
        methodLabel,
        settings?.let { stringResource(it.madhab.nameRes) }.orEmpty(),
    )

    OttrojjaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.prayer_testing_notice_title),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            OttrojjaText(
                text = stringResource(R.string.prayer_testing_notice_body),
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OttrojjaText(
                text = stringResource(R.string.prayer_testing_notice_report),
                style = OttrojjaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Selectable, so the address can be copied when no mail app is installed.
            SelectionContainer {
                OttrojjaText(
                    text = PRAYER_TIMES_FEEDBACK_EMAIL,
                    modifier = Modifier.fillMaxWidth(),
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OttrojjaButton(
                    text = stringResource(R.string.prayer_testing_notice_send),
                    onClick = { context.openFeedbackEmail(emailSubject, emailBody) },
                )
                Spacer(modifier = Modifier.width(10.dp))
                OttrojjaButton(
                    text = stringResource(R.string.prayer_testing_notice_ok),
                    onClick = onDismiss,
                )
            }
        }
    }
}

private fun Context.openFeedbackEmail(subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(PRAYER_TIMES_FEEDBACK_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No mail app: the address in the dialog is selectable, so it can still be copied.
    }
}

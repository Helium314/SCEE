package de.westnordost.streetcomplete.ui.common.quest

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.no_qr_code_handler
import de.westnordost.streetcomplete.ui.common.ToastPopup
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.plus

@Composable
fun MultiValueQuestQrScanForm(
    on: (QuestAction<String>) -> Unit,
    addAnotherValueText: StringResource,
    scanAnotherValueText: StringResource,
    noQrCodeHandlerResId: StringResource = Res.string.no_qr_code_handler,
    modifier: Modifier = Modifier,
    isOk: (String) -> Boolean = { true },
    simpleSuggestions: Collection<String>? = null,
    prioritySuggestions: (String) -> Collection<String>? = { null },
    onQrCodeParsed: ((String, (String?) -> Unit) -> Unit)? = null,
    minLengthForSuggestions: Int = 1,
    otherAnswers: @Composable (() -> List<AnswerItem>) = { emptyList() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    hint: String? = null
) {
    var values by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showNoQrCodeHandlerToast by remember { mutableStateOf(false) }

    val qrLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == RESULT_OK) {
                val content = result.data?.getStringExtra("SCAN_RESULT")
                if (!content.isNullOrBlank()) {
                    if (onQrCodeParsed != null) {
                        onQrCodeParsed(content) {
                            if (it != null) values = values + it
                        }
                    } else {
                         values = values + content
                    }
                }
            }
    }

    MultiValueQuestForm(
        on = on,
        values = values,
        onValuesChange = { values = it },
        addAnotherValueText = addAnotherValueText,
        modifier = modifier,
        isOk = isOk,
        simpleSuggestions = simpleSuggestions,
        prioritySuggestions = prioritySuggestions,
        minLengthForSuggestions = minLengthForSuggestions,
        otherAnswers = otherAnswers,
        keyboardOptions = keyboardOptions,
        hint = hint
    ) {
        TextButton(
            onClick = {
                try {
                    qrLauncher.launch(Intent("com.google.zxing.client.android.SCAN"))
                } catch (_: ActivityNotFoundException) {
                    showNoQrCodeHandlerToast = true
                }
            }
        ) {
            Text(stringResource(scanAnotherValueText))
        }

        if (showNoQrCodeHandlerToast) {
            ToastPopup(
                onDismissRequest = { showNoQrCodeHandlerToast = false },
                text = stringResource(noQrCodeHandlerResId),
            )
        }
    }
}

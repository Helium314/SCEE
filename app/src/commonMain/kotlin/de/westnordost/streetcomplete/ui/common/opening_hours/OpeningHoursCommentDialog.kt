package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.text.replace

/** Dialog to input an opening hours comment */
@Composable fun OpeningHoursCommentDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (comment: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var comment by remember { mutableStateOf(TextFieldValue()) }
    // - 2 because the comment is put into "…"
    val isTooLong by remember { derivedStateOf { comment.text.length > (MAX_OSM_TAG_VALUE_LENGTH - 2) } }
    val prefs: Preferences = koinInject()
    val lastPicked = remember { prefs.getLastPicked<String>("OpeningHoursComment") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    if (comment.text.isNotEmpty()) {
                        prefs.addLastPicked("OpeningHoursComment", comment.text.trim())
                        onConfirm(comment.text.trim())
                        onDismissRequest()
                    }
                },
                enabled = comment.text.isNotEmpty() && !isTooLong
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        },
        title = { Text(stringResource(Res.string.quest_openingHours_comment_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_openingHours_comment_description))
                }
                AutoCompleteTextField(
                    value = comment,
                    onValueChange = { comment = if (comment.text.contains("\"")) TextFieldValue(it.text.replace("\"", "")) else it },
                    isError = isTooLong,
                    suggestions = lastPicked,
                    startExpanded = true,
                    startExpandedWithoutFocus = true
                )
            }
        },
    )
}

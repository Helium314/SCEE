package de.westnordost.streetcomplete.screens.main

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

/** Shows a dialog that asks the user whether he wants to replace the current download with a
 *  download at a different location */
@Composable
fun ConfirmReplaceDownloadDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    onEnqueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = { Text(stringResource(Res.string.confirmation_cancel_prev_download_title)) },
        buttons = { FlowRow {
            TextButton(onClick = {
                onDismissRequest()
                onConfirmed()
            } ) {
                Text(stringResource(Res.string.confirmation_cancel_prev_download_confirmed))
            }
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.confirmation_cancel_prev_download_cancel))
            }
            TextButton(onClick = {
                onDismissRequest()
                onEnqueue()
            } ) {
                Text(stringResource(Res.string.enqueue_download))
            }
        }},
        modifier = modifier,
    )
}

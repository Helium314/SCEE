package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import kotlinx.io.files.FileSystem
import org.jetbrains.compose.resources.stringResource

/** Form in which you can leave a note, with images */
@Composable
fun NoteForm(
    text: String,
    onTextChange: (String) -> Unit,
    addImagesEnabled: Boolean,
    onDeleteImage: (imagePath: String) -> Unit,
    onTakePhoto: () -> Unit,
    fileSystem: FileSystem,
    imagePaths: List<String>,
    modifier: Modifier = Modifier,
    isGpxAttached: Boolean = false,
    isShowingGpxButton: Boolean = false,
    showClose: Boolean = false,
    close: Boolean = false,
    onCloseCheck: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            singleLine = false,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        if (showClose) {
            Row(
                Modifier.clickable { onCloseCheck(!close) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(close, onCloseCheck)
                Text(stringResource(Res.string.close_note_checkbox))
            }
        }
        if (isGpxAttached) {
            Text(
                text = stringResource(Res.string.quest_leave_new_note_track_recording),
                modifier = Modifier.alpha(ContentAlpha.medium)
            )
        }
        if (addImagesEnabled) {
            NoteImagesRow(
                fileSystem = fileSystem,
                imagePaths = imagePaths,
                onDeleteImage = onDeleteImage,
                onTakePhoto = onTakePhoto,
                // because otherwise it would overlap with the OK button
                modifier = Modifier.padding(
                    end = 72.dp,
                    start = if (isShowingGpxButton) 40.dp else 0.dp
                ),
            )
        }
    }
}

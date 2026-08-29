package de.westnordost.streetcomplete.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.getRawBlockList
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun NoteSettingsScreen(
    onClickBack: () -> Unit,
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var error by remember { mutableStateOf<StringResource?>(null) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.pref_screen_notes)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
        ) {
            var showHideNotesDialog by remember { mutableStateOf(false) }
            SwitchPreference(
                name = stringResource(Res.string.pref_show_gpx_button_title),
                description = stringResource(Res.string.pref_show_gpx_button_summary),
                pref = Prefs.GPX_BUTTON,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_swap_gpx_note_button),
                pref = Prefs.SWAP_GPX_NOTE_BUTTONS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_hide_keyboard_title),
                description = stringResource(Res.string.pref_hide_keyboard_summary),
                pref = Prefs.HIDE_KEYBOARD_FOR_NOTE,
                default = true,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_really_all_notes_title),
                description = stringResource(Res.string.pref_really_all_notes_summary),
                pref = Prefs.REALLY_ALL_NOTES,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_create_custom_quest_title),
                description = stringResource(Res.string.pref_create_custom_quest_summary),
                pref = Prefs.CREATE_EXTERNAL_QUESTS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_save_photos_title),
                description = stringResource(Res.string.pref_save_photos_summary),
                pref = Prefs.SAVE_PHOTOS,
                default = false,
            )
            Preference(
                name = stringResource(Res.string.pref_hide_notes_title),
                onClick = { showHideNotesDialog = true },
            )
            suspend fun saveGpxNotes(file: PlatformFile) {
                val os = file.sink().buffered().asOutputStream()
                try {
                    // read gpx and extract images
                    val files = mutableListOf(gpxNotesFile)
                    val gpxText = gpxNotesFile.readString()
                    val picturesDir = FileKit.filesDir
                    // get all files in pictures dir and check whether they occur in gpxText
                    if (picturesDir.isDirectory()) {
                        picturesDir.list().forEach {
                            if (!it.isDirectory() && it.extension == "jpg" && gpxText.contains(it.name))
                                files.add(it)
                        }
                    }
                    gpxNotesDir.list().forEach {
                        if (it.name.startsWith("track_") && it.extension == "gpx" && gpxText.contains(it.name))
                            files.add(it)
                    }

                    // write files to zip
                    val zipStream = ZipOutputStream(os)
                    files.forEach {
                        val fileStream = it.source().buffered().asInputStream().buffered()
                        zipStream.putNextEntry(ZipEntry(it.name))
                        fileStream.copyTo(zipStream, 1024)
                        fileStream.close()
                        zipStream.closeEntry()
                    }
                    zipStream.close()
                    files.forEach { it.delete() }
                } catch (_: Exception) {
                    error = Res.string.pref_save_file_error
                }
                os.close()
            }
            Preference(
                name = stringResource(Res.string.pref_save_gpx),
                onClick = {
                    if (gpxNotesFile.exists()) {
                        scope.launch {
                            val file = FileKit.openFileSaver("notes", defaultExtension = "zip")
                            if (file != null) saveGpxNotes(file)
                        }
                    } else {
                        error = Res.string.pref_files_not_found
                    }
                },
            )
            suspend fun saveFullSizePhotos(file: PlatformFile) {
                val os = file.sink().buffered().asOutputStream()
                try {
                    val files = mutableListOf<PlatformFile>()
                    val picturesDir = fullSizePhotosDir
                    // get all files in pictures dir
                    if (picturesDir.isDirectory()) {
                        picturesDir.list().forEach {
                            if (!it.isDirectory()) files.add(it)
                        }
                    }
                    else { // we checked for this, but better be sure
                        error = Res.string.pref_files_not_found
                        return
                    }

                    // write files to zip
                    val zipStream = ZipOutputStream(os)
                    files.forEach {
                        val fileStream = it.source().buffered().asInputStream().buffered()
                        zipStream.putNextEntry(ZipEntry(it.name))
                        fileStream.copyTo(zipStream, 1024)
                        fileStream.close()
                        zipStream.closeEntry()
                    }
                    zipStream.close()
                    files.forEach { it.delete() }
                } catch (e: Exception) {
                    error = Res.string.pref_save_file_error
                }
                os.close()
            }
            Preference(
                name = stringResource(Res.string.pref_get_photos_title),
                onClick = {
                    if (fullSizePhotosDir.exists() && fullSizePhotosDir.isDirectory() && fullSizePhotosDir.list().isNotEmpty()) {
                        scope.launch {
                            val file = FileKit.openFileSaver("full_photos", defaultExtension = "zip")
                            if (file != null) saveFullSizePhotos(file)
                        }
                    } else {
                        error = Res.string.pref_files_not_found
                    }
                },
            )
            if (showHideNotesDialog) {
                val prefs: Preferences = koinInject()
                val blockList = getRawBlockList(prefs)
                TextInputDialog(
                    onDismissRequest = { showHideNotesDialog = false },
                    onConfirmed = {
                        val content = it.split(",").map { it.trim().lowercase() }
                        prefs.putString(Prefs.HIDE_NOTES_BY_USERS, Json.encodeToString(content))
                        OsmQuestController.reloadQuestTypes()
                    },
                    singleLine = false,
                    title = { Text(stringResource(Res.string.pref_hide_notes_message)) },
                    textInputLabel = { Text(stringResource(Res.string.pref_hide_notes_hint)) },
                    text = blockList.joinToString(", ")
                )
            }
        }
    }
    if (error != null)
        ToastPopup({ error = null }, stringResource(error!!))
}

val gpxNotesDir = PlatformFile(FileKit.filesDir, "gpx_notes")

val gpxNotesFile = PlatformFile(gpxNotesDir, "notex.gpx")

val fullSizePhotosDir = PlatformFile(FileKit.filesDir, "full_photos")

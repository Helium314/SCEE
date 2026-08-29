package de.westnordost.streetcomplete.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.presets.EditTypePresetsTable
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeTable
import de.westnordost.streetcomplete.quests.amenity_cover.AddAmenityCover
import de.westnordost.streetcomplete.quests.custom.CustomQuest
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import de.westnordost.streetcomplete.ui.common.dialogs.WheelPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import de.westnordost.streetcomplete.util.getCustomOverlayIndices
import de.westnordost.streetcomplete.util.getFakeCustomOverlays
import de.westnordost.streetcomplete.util.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.util.logs.Log
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.source
import io.ktor.utils.io.core.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun DataManagementScreen(
    onClickBack: () -> Unit,
) {
    val prefs: Preferences = koinInject()
    val db: Database = koinInject()
    val editTypePresetsController: EditTypePresetsController = koinInject()
    val urlConfigController: UrlConfigController = koinInject()
    val visibleEditTypeController: VisibleEditTypeController = koinInject()
    val osmoseDao: OsmoseDao = koinInject()
    val externalSourceQuestController: ExternalSourceQuestController = koinInject()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var showDeleteAfterDialog by remember { mutableStateOf(false) }
    var showGpsIntervalDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showRasterUrlDialog by remember { mutableStateOf(false) }
    var exportPresets by remember { mutableStateOf<PlatformFile?>(null) }
    var exportOverlays by remember { mutableStateOf<PlatformFile?>(null) }
    var importPresets by remember { mutableStateOf<PlatformFile?>(null) }
    var importOverlays by remember { mutableStateOf<PlatformFile?>(null) }
    var currentError by remember { mutableStateOf<StringResource?>(null) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.pref_screen_data_management)) },
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
            SwitchPreference(
                name = stringResource(Res.string.pref_auto_download_title),
                description = stringResource(Res.string.pref_auto_download_summary),
                pref = Prefs.AUTO_DOWNLOAD,
                default = true,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_manual_download_cache_title),
                description = stringResource(Res.string.pref_manual_download_cache_summary),
                pref = Prefs.MANUAL_DOWNLOAD_OVERRIDE_CACHE,
                default = true,
            )
            Preference(
                name = stringResource(Res.string.pref_tile_source_title),
                onClick = { showRasterUrlDialog = true },
            )
            Preference(
                name = stringResource(Res.string.pref_delete_old_data_after),
                onClick = { showDeleteAfterDialog = true },
                description = stringResource(Res.string.pref_delete_old_data_after_summary, prefs.getInt(Prefs.DATA_RETAIN_TIME, 14))
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_update_local_statistics),
                description = stringResource(Res.string.pref_update_local_statistics_summary),
                pref = Prefs.UPDATE_LOCAL_STATISTICS,
                default = true,
            )
            Preference(
                name = stringResource(Res.string.pref_location_interval_title),
                onClick = { showGpsIntervalDialog = true },
                description = stringResource(Res.string.pref_interval_summary, prefs.getInt(Prefs.LOCATION_INTERVAL, 0))
            )
            Preference(
                name = stringResource(Res.string.pref_export),
                onClick = { showExportDialog = true },
            )
            Preference(
                name = stringResource(Res.string.pref_import),
                onClick = { showImportDialog = true },
            )
        }
        if (showDeleteAfterDialog) {
            val selectable = remember { (5..30).toList() }
            WheelPickerDialog(
                onDismissRequest = { showDeleteAfterDialog = false },
                selectableValues = selectable,
                onSelected = { prefs.putInt(Prefs.DATA_RETAIN_TIME, it) },
                itemContent = { Text(it.toString()) },
                selectedInitialValue = prefs.getInt(Prefs.DATA_RETAIN_TIME, 14),
                title = { Text(stringResource(Res.string.pref_delete_old_data_after)) },
                text = { Text(stringResource(Res.string.pref_delete_old_data_after_message)) }
            )
        }
        if (showGpsIntervalDialog) {
            val selectable = remember { (0..15).toList() + listOf(20, 25, 30, 45, 60, 90, 120) }
            WheelPickerDialog(
                onDismissRequest = { showGpsIntervalDialog = false },
                selectableValues = selectable,
                onSelected = { prefs.putInt(Prefs.LOCATION_INTERVAL, it) },
                itemContent = { Text(it.toString()) },
                selectedInitialValue = prefs.getInt(Prefs.LOCATION_INTERVAL, 0),
                title = { Text(stringResource(Res.string.pref_location_interval_title)) },
                text = { Text(stringResource(Res.string.pref_interval_message)) }
            )
        }
        if (showExportDialog) {
            fun saveFile(name: String, onSave: (PlatformFile) -> Unit) = scope.launch {
                val file = FileKit.openFileSaver(name, defaultExtension = "txt")
                if (file != null) onSave(file)
                showExportDialog = false
            }
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                buttonRow = { TextButton({ showExportDialog = false }) { Text(stringResource(Res.string.cancel)) } },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button({ saveFile("settings") { exportSettings(it) } }, Modifier.fillMaxWidth())
                            { Text(stringResource(Res.string.import_export_settings)) }
                        Button({ saveFile("hidden_quests") { exportHidden(it, db) } }, Modifier.fillMaxWidth())
                            { Text(stringResource(Res.string.import_export_hidden_quests)) }
                        Button({ saveFile("presets") { exportPresets = it } }, Modifier.fillMaxWidth())
                            { Text(stringResource(Res.string.import_export_presets)) }
                        Button({ saveFile("overlays") { exportOverlays = it } }, Modifier.fillMaxWidth())
                            { Text(stringResource(Res.string.import_export_custom_overlays)) }
                    }
                }
            )
        }
        if (showImportDialog) {
            fun loadFile(onLoad: (PlatformFile) -> Unit) = scope.launch {
                val file = FileKit.openFilePicker()
                if (file != null) onLoad(file)
                showImportDialog = false
            }
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                buttonRow = { TextButton({ showImportDialog = false }) { Text(stringResource(Res.string.cancel)) } },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button({ loadFile { if (!importSettings(it, osmoseDao, externalSourceQuestController)) currentError = Res.string.import_error } }, Modifier.fillMaxWidth())
                        { Text(stringResource(Res.string.import_export_settings)) }
                        Button({ loadFile { importHidden(it, db, visibleEditTypeController) { currentError = it } } }, Modifier.fillMaxWidth())
                        { Text(stringResource(Res.string.import_export_hidden_quests)) }
                        Button({ loadFile { importPresets = it } }, Modifier.fillMaxWidth())
                        { Text(stringResource(Res.string.import_export_presets)) }
                        Button({ loadFile { importOverlays = it } }, Modifier.fillMaxWidth())
                        { Text(stringResource(Res.string.import_export_custom_overlays)) }
                    }
                }
            )
        }
        if (showRasterUrlDialog) {
            var maxZoom by remember { mutableStateOf(TextFieldValue(prefs.getInt(Prefs.RASTER_TILE_MAXZOOM, ApplicationConstants.RASTER_DEFAULT_MAXZOOM).toString())) }
            var hideLabels by remember { mutableStateOf(prefs.getBoolean(Prefs.NO_SATELLITE_LABEL, false)) }
            var url  by remember { mutableStateOf(TextFieldValue(prefs.getString(Prefs.RASTER_TILE_URL, ApplicationConstants.RASTER_DEFAULT_URL))) }
            AlertDialog(
                onDismissRequest = { showRasterUrlDialog = false },
                title = { Text(stringResource(Res.string.pref_tile_source_title)) },
                buttonRow = {
                    TextButton({
                        prefs.remove(Prefs.RASTER_TILE_URL)
                        prefs.remove(Prefs.RASTER_TILE_MAXZOOM)
                        prefs.remove(Prefs.NO_SATELLITE_LABEL)
                        showRasterUrlDialog = false
                    }) { Text(stringResource(Res.string.action_reset)) }
                    TextButton({ showRasterUrlDialog = false }) { Text(stringResource(Res.string.cancel)) }
                    TextButton({
                        prefs.putString(Prefs.RASTER_TILE_URL, url.text)
                        prefs.putInt(Prefs.RASTER_TILE_MAXZOOM, maxZoom.text.toInt())
                        prefs.putBoolean(Prefs.NO_SATELLITE_LABEL, hideLabels)

                        // trigger the listener in MapFragment (if it exists)
                        val map = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
                        prefs.putString(Prefs.THEME_BACKGROUND, if (map == "MAP") "AERIAL" else "MAP")
                        prefs.putString(Prefs.THEME_BACKGROUND, map)
                        showRasterUrlDialog = false
                    },
                        enabled = maxZoom.text.toIntOrNull() != null && (url.text.contains("{x}") && url.text.contains("{y}")
                            && (url.text.contains("{z}") || url.text.contains("{zoom}")))
                            || url.text.contains("{bbox-epsg-3857}")
                            || (url.text.contains("{bbox}") && url.text.contains("{proj}"))
                    ) { Text(stringResource(Res.string.ok)) }
                },
                text = {
                    Column {
                        Text(stringResource(Res.string.pref_tile_source_message))
                        TextField2(value = url, onValueChange = { url = it })
                        Spacer(Modifier.size(6.dp))
                        TextField2(
                            value = maxZoom,
                            onValueChange = { maxZoom = it },
                            label = { Text(stringResource(Res.string.pref_tile_maxzoom)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { hideLabels = !hideLabels }) {
                            Text(stringResource(Res.string.pref_tile_source_hide_labels))
                            Switch(hideLabels, { hideLabels = it })
                        }
                    }
                }
            )
        }
        if (exportPresets != null) {
            val allPresets = mutableListOf<EditTypePreset>()
            allPresets.add(EditTypePreset(0, stringResource(Res.string.quest_presets_default_name)))
            allPresets.addAll(editTypePresetsController.getAll())
            var selectedPresets by remember { mutableStateOf(setOf<EditTypePreset>()) }
            AlertDialog(
                onDismissRequest = { exportPresets = null },
                title = { Text(stringResource(Res.string.import_export_presets_select)) },
                text = {
                    CheckboxGroup(
                        options = allPresets,
                        selectedOptions = selectedPresets,
                        onSelectionsChange = { selectedPresets = it },
                        itemContent = { Text(it.name) }
                    )
                },
                buttonRow = {
                    TextButton({ exportPresets = null }) { Text(stringResource(Res.string.cancel)) }
                    TextButton({
                        exportPresets(selectedPresets.map { it.id }, exportPresets!!, db, urlConfigController)
                        exportPresets = null
                    }, enabled = selectedPresets.isNotEmpty()) { Text(stringResource(Res.string.ok)) }
                }
            )
        }
        if (exportOverlays != null) {
            val allOverlays = getFakeCustomOverlays(prefs, false)
            var selectedOverlays by remember { mutableStateOf(setOf<Overlay>()) }
            AlertDialog(
                onDismissRequest = { exportOverlays = null },
                title = { Text(stringResource(Res.string.import_export_presets_select)) },
                text = {
                    CheckboxGroup(
                        options = allOverlays,
                        selectedOptions = selectedOverlays,
                        onSelectionsChange = { selectedOverlays = it },
                        itemContent = { Text(it.changesetComment) }
                    )
                },
                buttonRow = {
                    TextButton({ exportOverlays = null }) { Text(stringResource(Res.string.cancel)) }
                    TextButton({
                        exportCustomOverlays(selectedOverlays.map { it.name }, exportOverlays!!)
                        exportOverlays = null
                    }, enabled = selectedOverlays.isNotEmpty()) { Text(stringResource(Res.string.ok)) }
                }
            )
        }
        if (importPresets != null) {
            val lines = remember { importLinesAndCheck(importPresets!!, BACKUP_PRESETS, db) { currentError = it } }
            if (lines.isEmpty())
                importPresets = null
            AlertDialog(
                onDismissRequest = { importPresets = null },
                text = { stringResource(Res.string.import_presets_overlays_message) },
                title = { Text(stringResource(Res.string.pref_import)) },
                buttonRow = {
                    TextButton({ importPresets = null }) { Text(stringResource(Res.string.cancel)) }
                    TextButton({
                        importPresets(lines, true, db, visibleEditTypeController)
                        importPresets = null
                    }) { Text(stringResource(Res.string.import_presets_overlays_replace)) }
                    TextButton({
                        importPresets(lines, false, db, visibleEditTypeController)
                        importPresets = null
                    }) { Text(stringResource(Res.string.import_presets_overlays_add)) }
                }
            )
        }
        if (importOverlays != null) {
            AlertDialog(
                onDismissRequest = { importOverlays = null },
                text = { stringResource(Res.string.import_presets_overlays_message) },
                title = { Text(stringResource(Res.string.pref_import)) },
                buttonRow = {
                    TextButton({ importOverlays = null }) { Text(stringResource(Res.string.cancel)) }
                    TextButton({
                        if (!importCustomOverlays(importOverlays!!, true))
                            currentError = Res.string.import_error
                        importOverlays = null
                    }) { Text(stringResource(Res.string.import_presets_overlays_replace)) }
                    TextButton({
                        if (!importCustomOverlays(importOverlays!!, false))
                            currentError = Res.string.import_error
                        importOverlays = null
                    }) { Text(stringResource(Res.string.import_presets_overlays_add)) }
                }
            )
        }
        if (currentError != null)
            ToastPopup({ currentError = null }, stringResource(currentError!!))
    }
}

private const val BACKUP_HIDDEN_OSM_QUESTS = "quests"
private const val BACKUP_HIDDEN_NOTES = "notes"
private const val BACKUP_HIDDEN_OTHER_QUESTS = "other_source_quests"
private const val BACKUP_PRESETS = "presets"
private const val BACKUP_PRESETS_ORDERS = "orders"
private const val BACKUP_PRESETS_VISIBILITIES = "visibilities"
private const val BACKUP_PRESETS_QUEST_SETTINGS = "quest_settings"

private const val TAG = "DataManagementSettings"

const val LAST_KNOWN_DB_VERSION = 20L

val renamedQuests = mapOf(
    "ExternalQuest" to CustomQuest::class.simpleName!!,
    "AddPicnicTableCover" to AddAmenityCover::class.simpleName!!,
)
fun String.renameUpdatedQuests() =
    renamedQuests.entries.fold(this) { acc, (old, new) -> acc.replace(old, new) }

private fun exportHidden(file: PlatformFile, db: Database) {
    val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()

    val hiddenOsmQuests = db.query(OsmQuestsHiddenTable.NAME) { c ->
        c.getLong(OsmQuestsHiddenTable.Columns.ELEMENT_ID).toString() + "," +
            c.getString(OsmQuestsHiddenTable.Columns.ELEMENT_TYPE) + "," +
            c.getString(OsmQuestsHiddenTable.Columns.QUEST_TYPE) + "," +
            c.getLong(OsmQuestsHiddenTable.Columns.TIMESTAMP)
    }
    val hiddenNotes = db.query(NoteQuestsHiddenTable.NAME) { c->
        c.getLong(NoteQuestsHiddenTable.Columns.NOTE_ID).toString() + "," +
            c.getLong(NoteQuestsHiddenTable.Columns.TIMESTAMP)
    }
    val hiddenExternalSourceQuests = db.query(ExternalSourceQuestTables.NAME_HIDDEN) { c ->
        c.getString(ExternalSourceQuestTables.Columns.SOURCE) + "," +
            c.getString(ExternalSourceQuestTables.Columns.ID) + "," +
            c.getLong(ExternalSourceQuestTables.Columns.TIMESTAMP)
    }

    runBlocking { withContext(Dispatchers.IO) { file.sink().buffered().use {
        it.writeText(version.toString())
        it.writeText("\n\n$BACKUP_HIDDEN_OSM_QUESTS\n")
        it.writeText(hiddenOsmQuests.joinToString("\n"))
        it.writeText("\n\n$BACKUP_HIDDEN_NOTES\n")
        it.writeText(hiddenNotes.joinToString("\n"))
        it.writeText("\n\n$BACKUP_HIDDEN_OTHER_QUESTS\n")
        it.writeText(hiddenExternalSourceQuests.joinToString("\n") + "\n")
    } } }
}

private fun exportPresets(ids: Collection<Long>, file: PlatformFile, db: Database, urlConfigController: UrlConfigController) {
    val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()

    val presetString = ids.joinToString(",")
    val presets = db.query(EditTypePresetsTable.NAME, where = "${EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID} IN ($presetString)") { c ->
        c.getLong(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID).toString() + "," +
            c.getString(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_NAME)
    }.map { "$it,${urlConfigController.create(it.substringBefore(',').toLong())}" }
    val orders = db.query(QuestTypeOrderTable.NAME, where = "${QuestTypeOrderTable.Columns.EDIT_TYPE_PRESET_ID} IN ($presetString)") { c->
        c.getLong(QuestTypeOrderTable.Columns.EDIT_TYPE_PRESET_ID).toString() + "," +
            c.getString(QuestTypeOrderTable.Columns.BEFORE) + "," +
            c.getString(QuestTypeOrderTable.Columns.AFTER)
    }
    val visibilities = db.query(VisibleEditTypeTable.NAME, where = "${VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID} IN ($presetString)") { c ->
        c.getLong(VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID).toString() + "," +
            c.getString(VisibleEditTypeTable.Columns.EDIT_TYPE) + "," +
            c.getLong(VisibleEditTypeTable.Columns.VISIBILITY).toString()
    }
    val perPresetQuestSetting = "\\d+_qs_.+".toRegex()
    val questSettings = Prefs.sharedPreferences.all.filterKeys { it.matches(perPresetQuestSetting) && it.substringBefore('_').toLongOrNull() in ids }

    runBlocking { withContext(Dispatchers.IO) { file.sink().buffered().use {
        it.writeText(version.toString())
        it.writeText("\n\n$BACKUP_PRESETS\n")
        it.writeText(presets.joinToString("\n"))
        it.writeText("\n\n$BACKUP_PRESETS_ORDERS\n")
        it.writeText(orders.joinToString("\n"))
        it.writeText("\n\n$BACKUP_PRESETS_VISIBILITIES\n")
        it.writeText(visibilities.joinToString("\n"))
        it.writeText("\n\n$BACKUP_PRESETS_QUEST_SETTINGS\n")
        it.writeText("\n" + settingsToString(questSettings))
    } } }
}

// this will ignore settings with value null, but should be fine
@Suppress("UNCHECKED_CAST") // it is checked... but whatever (except string set, because not allowed to check for that)
private fun settingsToString(settings: Map<String, Any?>): String {
    val booleans = settings.filterValues { it is Boolean } as Map<String, Boolean>
    val ints = settings.filterValues { it is Int } as Map<String, Int>
    val longs = settings.filterValues { it is Long } as Map<String, Long>
    val floats = settings.filterValues { it is Float } as Map<String, Float>
    val strings = settings.filterValues { it is String } as Map<String, String>
    val stringSets = settings.filterValues { it is Set<*> } as Map<String, Set<String>>
    // now write
    val sb = StringBuilder()
    sb.appendLine("boolean settings")
    sb.appendLine( Json.encodeToString(booleans))
    sb.appendLine()
    sb.appendLine("int settings")
    sb.appendLine( Json.encodeToString(ints))
    sb.appendLine()
    sb.appendLine("long settings")
    sb.appendLine( Json.encodeToString(longs))
    sb.appendLine()
    sb.appendLine("float settings")
    sb.appendLine( Json.encodeToString(floats))
    sb.appendLine()
    sb.appendLine("string settings")
    sb.appendLine( Json.encodeToString(strings))
    sb.appendLine()
    sb.appendLine("string set settings")
    sb.appendLine( Json.encodeToString(stringSets))
    return sb.toString()
}

private fun exportCustomOverlays(indices: Collection<String>, file: PlatformFile) {
    val prefs = Prefs.sharedPreferences
    val filterRegex = "custom_overlay_(?:${indices.joinToString("|")})_.*".toRegex()
    val settings = prefs.all.filterKeys { filterRegex.matches(it) }.toMutableMap()
    settings[Prefs.CUSTOM_OVERLAY_INDICES] = indices.joinToString(",")
    if (prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0).toString() in indices)
        settings[Prefs.CUSTOM_OVERLAY_SELECTED_INDEX] = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
    runBlocking { withContext(Dispatchers.IO) { file.sink().buffered().use {
        it.writeString("overlays\n")
        it.writeText("\n" + settingsToString(settings))
    } } }
}

private fun exportSettings(file: PlatformFile) {
    val perPresetQuestSetting = "\\d+_qs_.+".toRegex()
    val settings = Prefs.sharedPreferences.all.filterKeys {
        !it.contains("TangramPinsSpriteSheet") // this is huge and gets generated if missing anyway
            && !it.contains("TangramIconsSpriteSheet") // this is huge and gets generated if missing anyway
            && it != Preferences.OAUTH2_ACCESS_TOKEN // login
            && !it.contains("osm.") // login data
            && !it.matches(perPresetQuestSetting) // per-preset quest settings should be stored with presets, because preset id is never guaranteed to match
            && !it.startsWith("custom_overlay") // custom overlays are exported separately
    }
    runBlocking { withContext(Dispatchers.IO) { file.sink().buffered().use { it.writeText(settingsToString(settings)) } } }
}

private fun importCustomOverlays(file: PlatformFile, replaceExisting: Boolean): Boolean {
    if (!file.exists()) return false
    val lines = runBlocking { withContext(Dispatchers.IO) {
        file.source().buffered().use { it.readString().lines() }
    } }
    if (lines.first() != "overlays") return false
    val prefs = Prefs.sharedPreferences
    return if (replaceExisting) {
        // first remove old overlays
        // this is necessary because otherwise overlay may remain, but hidden due to not in indices pref
        prefs.edit { prefs.all.keys.forEach { if (it.startsWith("custom_overlay")) remove(it) } }

        val result = readToSettings(lines.subList(1, lines.size))
        // update in case of old data
        if (prefs.contains("custom_overlay_filter") || prefs.contains("custom_overlay_color_key")) {
            val indices = if (prefs.contains(Prefs.CUSTOM_OVERLAY_INDICES)) getCustomOverlayIndices(prefs) else emptyList()
            val newIndex = indices.maxOrNull() ?: 0
            prefs.edit {
                if (prefs.contains("custom_overlay_filter"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, newIndex), prefs.getString("custom_overlay_filter", "")!!)
                if (prefs.contains("custom_overlay_color_key"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, newIndex), prefs.getString("custom_overlay_color_key", "")!!)
                remove("custom_overlay_filter")
                remove("custom_overlay_color_key")
                putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + newIndex).sorted().joinToString(","))
            }
        }
        result
    }
    else {
        val customOverlayRegex = "custom_overlay_(\\d+)_".toRegex()
        val indices = getCustomOverlayIndices(prefs).toMutableSet()
        val offset = indices.maxOrNull()?.let { it + 1 } ?: 0
        val newLines = lines.mapNotNull { line ->
            if (line == "overlays") return@mapNotNull null
            line.replace(customOverlayRegex) { result ->
                if (result.groupValues.size <= 1) throw (IllegalStateException())
                val oldIndex = result.groupValues[1].toInt()
                val newIndex = oldIndex + offset
                indices.add(newIndex)
                "custom_overlay_${newIndex}_"
            }
        }
        val result = readToSettings(newLines)
        prefs.edit {
            // update in case of old data
            if (prefs.contains("custom_overlay_filter") || prefs.contains("custom_overlay_color_key")) {
                val oldOverlayIndex = if (indices.contains(offset)) indices.max() + 1 else offset
                indices.add(oldOverlayIndex)
                if (prefs.contains("custom_overlay_filter"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, oldOverlayIndex), prefs.getString("custom_overlay_filter", "")!!)
                if (prefs.contains("custom_overlay_color_key"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, oldOverlayIndex), prefs.getString("custom_overlay_color_key", "")!!)
                remove("custom_overlay_filter")
                remove("custom_overlay_color_key")
            }
            // set updated indices
            putString(Prefs.CUSTOM_OVERLAY_INDICES, indices.sorted().joinToString(","))
        }
        result
    }
}

private fun readToSettings(list: List<String>): Boolean {
    val i = list.iterator()
    val e = Prefs.sharedPreferences.edit()
    try {
        while (i.hasNext()) {
            val next = i.next()
            if (next.isBlank()) continue
            when (next) {
                "boolean settings" -> Json.decodeFromString<Map<String, Boolean>>(i.next()).forEach { e.putBoolean(it.key, it.value) }
                "int settings" -> Json.decodeFromString<Map<String, Int>>(i.next()).forEach { e.putInt(it.key, it.value) }
                "long settings" -> Json.decodeFromString<Map<String, Long>>(i.next()).forEach { e.putLong(it.key, it.value) }
                "float settings" -> Json.decodeFromString<Map<String, Float>>(i.next()).forEach { e.putFloat(it.key, it.value) }
                "string settings" -> Json.decodeFromString<Map<String, String>>(i.next()).forEach { e.putString(it.key, it.value) }
                "string set settings" -> Json.decodeFromString<Map<String, Set<String>>>(i.next()).forEach { e.putStringSet(it.key, it.value) }
            }
        }
        e.apply()
        return true
    } catch (e: Exception) {
        return false
    }
}

private fun importHidden(file: PlatformFile, db: Database, visibleEditTypeController: VisibleEditTypeController, onError: (StringResource) -> Unit) {
    // do not delete existing hidden quests; this can be done manually anyway
    val lines = importLinesAndCheck(file, BACKUP_HIDDEN_OSM_QUESTS, db, onError)
    if (lines.isEmpty()) return

    val quests = mutableListOf<Array<Any?>>()
    val notes = mutableListOf<Array<Any?>>()
    val externalSourceQuests = mutableListOf<Array<Any?>>()
    val added = hashSetOf<String>() // avoid duplicates
    var currentThing = BACKUP_HIDDEN_OSM_QUESTS
    for (line in lines) {
        if (line.isEmpty()) continue
        if (line == BACKUP_HIDDEN_NOTES || line == BACKUP_HIDDEN_OTHER_QUESTS) {
            currentThing = line
            continue
        }
        val split = line.split(",")
        if (split.size < 2) break
        when (currentThing) {
            BACKUP_HIDDEN_OSM_QUESTS -> if (added.add(line)) quests.add(arrayOf(split[0].toLong(), split[1], split[2], split[3].toLong()))
            BACKUP_HIDDEN_NOTES -> if (added.add(line)) notes.add(arrayOf(split[0].toLong(), split[1].toLong()))
            BACKUP_HIDDEN_OTHER_QUESTS -> if (added.add(line)) externalSourceQuests.add(arrayOf(split[0], split[1], split[2].toLong()))
        }
    }

    db.insertMany(OsmQuestsHiddenTable.NAME,
        arrayOf(OsmQuestsHiddenTable.Columns.ELEMENT_ID,
            OsmQuestsHiddenTable.Columns.ELEMENT_TYPE,
            OsmQuestsHiddenTable.Columns.QUEST_TYPE,
            OsmQuestsHiddenTable.Columns.TIMESTAMP),
        quests,
        conflictAlgorithm = ConflictAlgorithm.REPLACE
    )
    db.insertMany(NoteQuestsHiddenTable.NAME,
        arrayOf(NoteQuestsHiddenTable.Columns.NOTE_ID,
            NoteQuestsHiddenTable.Columns.TIMESTAMP),
        notes,
        conflictAlgorithm = ConflictAlgorithm.REPLACE
    )
    db.insertMany(ExternalSourceQuestTables.NAME_HIDDEN,
        arrayOf(ExternalSourceQuestTables.Columns.SOURCE,
            ExternalSourceQuestTables.Columns.ID,
            ExternalSourceQuestTables.Columns.TIMESTAMP),
        externalSourceQuests,
        conflictAlgorithm = ConflictAlgorithm.REPLACE
    )

    // definitely need to reset visible quests
    visibleEditTypeController.onVisibilitiesChanged()
    // imported hidden osmquests are applied, but don't show up in edit history
    // imported other quests are not even applied
}

/** @returns the lines after [checkLine], which is expected to be the second or third line */
private fun importLinesAndCheck(file: PlatformFile, checkLine: String, db: Database, onError: (StringResource) -> Unit): List<String> =
    runCatching { runBlocking { withContext(Dispatchers.IO) { file.source().buffered().use { input ->
        val fileVersion = input.readLine()?.toLongOrNull()
        if (fileVersion == null || (input.readLine() != checkLine && input.readLine() != checkLine)) {
            Log.w(TAG, "import error, file version $fileVersion, checkLine $checkLine")
            onError(Res.string.import_error)
            return@withContext emptyList()
        }
        val dbVersion = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
        if (fileVersion != dbVersion && (fileVersion > LAST_KNOWN_DB_VERSION || dbVersion > LAST_KNOWN_DB_VERSION)) {
            Log.w(TAG, "import error, file version $fileVersion, dbVersion $dbVersion, last known db version $LAST_KNOWN_DB_VERSION")
            onError(Res.string.import_error_db_version)
            return@withContext emptyList()
        }

        var line: String? = input.readLine()
        val lines = mutableListOf<String>()
        while (line != null) {
            lines.add(line.renameUpdatedQuests())
            line = input.readLine()
        }
        lines
    } } } }.getOrNull() ?: emptyList()

// when importing, names should be updated!
private fun List<String>.renameUpdatedQuests() = map { it.renameUpdatedQuests() }

private fun importPresets(lines: List<String>, replaceExistingPresets: Boolean, db: Database, visibleEditTypeController: VisibleEditTypeController) {
    val lines = lines.renameUpdatedQuests()
    val presets = mutableListOf<Array<Any?>>()
    val orders = mutableListOf<Array<Any?>>()
    val visibilities = mutableListOf<Array<Any?>>()
    // set of lines to avoid duplicates that might arise when user has quests of old and new name in the backup
    val presetsSet = hashSetOf<String>()
    val ordersSet = hashSetOf<String>()
    val visibilitiesSet = hashSetOf<String>()
    var currentThing = BACKUP_PRESETS
    val profileIdMap = mutableMapOf(0L to 0L) // "default" is not in the presets section
    val qsRegex = "(\\d+)_qs_".toRegex()
    for (line in lines) { // go through list of presets
        val split = line.split(",")
        if (split.size < 2) break // happens if we come to the next category
        val id = split[0].toLong()
        profileIdMap[id] = id
    }

    if (!replaceExistingPresets) {
        // map profile ids to ids greater than existing maximum
        val max = db.query(EditTypePresetsTable.NAME) { it.getLong(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID) }.maxOrNull() ?: 0L
        val keys = profileIdMap.keys.toList()
        keys.forEachIndexed { i, id ->
            profileIdMap[id] = max + i + 1L
        }
        // consider that profile 0 has no name, as it's the "default"
        presets.add(arrayOf(profileIdMap[0L]!!, "Default"))
    }

    val questSettingsLines = mutableListOf<String>()
    for (line in lines) {
        if (line.isEmpty()) continue // happens if a section is completely empty
        if (line == BACKUP_PRESETS_ORDERS || line == BACKUP_PRESETS_VISIBILITIES) {
            currentThing = line
            continue
        }
        if (line == BACKUP_PRESETS_QUEST_SETTINGS) {
            try {
                // get remaining lines (they must be written if BACKUP_PRESETS_QUEST_SETTINGS is written)
                val l = lines.subList(lines.indexOf(line) + 1, lines.size)
                // replace per-preset quest settings preset ids
                val adjustedLines = l.map { it.replace(qsRegex) { result ->
                    if (result.groupValues.size > 1)
                        "${result.groupValues[1].toLongOrNull()?.let { profileIdMap[it] }}_qs_"
                    else throw (IllegalStateException())
                } }
                questSettingsLines.addAll(adjustedLines)
            } catch (_: Exception){
                // do nothing if lines are broken somehow
            }
            break
        }
        val split = line.split(",")
        if (split.size < 2) break
        val id = profileIdMap[split[0].toLong()]!!
        when (currentThing) {
            BACKUP_PRESETS -> if (presetsSet.add(line)) presets.add(arrayOf(id, split[1]))
            BACKUP_PRESETS_ORDERS -> if (ordersSet.add(line)) orders.add(arrayOf(id, split[1], split[2]))
            BACKUP_PRESETS_VISIBILITIES -> if (visibilitiesSet.add(line)) visibilities.add(arrayOf(id, split[1], split[2].toLong()))
        }
    }

    db.transaction {
        if (replaceExistingPresets) {
            // delete existing data in all tables
            db.delete(EditTypePresetsTable.NAME)
            db.delete(QuestTypeOrderTable.NAME)
            db.delete(VisibleEditTypeTable.NAME)
        }
        db.insertMany(EditTypePresetsTable.NAME,
            arrayOf(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID, EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_NAME),
            presets
        )
        db.insertMany(QuestTypeOrderTable.NAME,
            arrayOf(QuestTypeOrderTable.Columns.EDIT_TYPE_PRESET_ID,
                QuestTypeOrderTable.Columns.BEFORE,
                QuestTypeOrderTable.Columns.AFTER),
            orders
        )
        db.insertMany(VisibleEditTypeTable.NAME,
            arrayOf(VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID,
                VisibleEditTypeTable.Columns.EDIT_TYPE,
                VisibleEditTypeTable.Columns.VISIBILITY),
            visibilities
        )
    }

    // database stuff successful, update preferences
    if (replaceExistingPresets) {
        val prefs = Prefs.sharedPreferences
        prefs.edit {
            // remove all per-preset quest settings for proper replace
            prefs.all.keys.filter { qsRegex.containsMatchIn(it) }.forEach { remove(it) }
            // set selected preset to default, because previously selected may not exist any more
            putLong(Preferences.SELECTED_EDIT_TYPE_PRESET, 0)
        }
    }
    readToSettings(questSettingsLines)

    visibleEditTypeController.setVisibilities(emptyMap()) // reload stuff
}

private fun importSettings(file: PlatformFile, osmoseDao: OsmoseDao, externalSourceQuestController: ExternalSourceQuestController): Boolean {
    if (!file.exists()) return false
    val lines = runBlocking { withContext(Dispatchers.IO) {
        file.source().buffered().use { it.readString().lines().renameUpdatedQuests() }
    } }
    val r = readToSettings(lines)
    osmoseDao.reloadIgnoredItems()
    externalSourceQuestController.invalidate()
    return r
}

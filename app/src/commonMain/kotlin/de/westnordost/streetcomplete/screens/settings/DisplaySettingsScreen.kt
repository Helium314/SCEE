package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.data.importGpx
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun DisplaySettingsScreen(
    onClickBack: () -> Unit,
) {
    val visibleEditTypeController: VisibleEditTypeController = koinInject()
    val prefs: Preferences = koinInject()
    val scope = rememberCoroutineScope()
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showGpxDialog by remember { mutableStateOf(false) }
    var showGeometryDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.pref_screen_display)) },
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
                name = stringResource(Res.string.pref_way_direction),
                description = stringResource(Res.string.pref_way_direction_summary),
                default = false,
                pref = Prefs.SHOW_WAY_DIRECTION
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_quest_geometries_title),
                description = stringResource(Res.string.pref_quest_geometries_summary),
                default = false,
                pref = Prefs.QUEST_GEOMETRIES,
                onCheckedChange = { visibleEditTypeController.onVisibilitiesChanged() }
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_offset_fix_title2),
                description = stringResource(Res.string.pref_offset_fix_summary),
                default = false,
                pref = Prefs.OFFSET_FIX,
                onCheckedChange = {
                    // trigger map update by switching background twice
                    val old = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
                    val new = if (old == "MAP") "AERIAL" else "MAP"
                    prefs.putString(Prefs.THEME_BACKGROUND, new)
                    scope.launch {
                        delay(100)
                        prefs.putString(Prefs.THEME_BACKGROUND, old)
                    }
                }
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_show_solved_animation),
                description = stringResource(Res.string.pref_show_solved_animation_summary),
                default = true,
                pref = Prefs.SHOW_SOLVED_ANIMATION
            )
            Preference(
                name = stringResource(Res.string.pref_background_type_select),
                description = if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP")
                        stringResource(Res.string.background_type_map)
                    else stringResource(Res.string.background_type_aerial_esri),
                onClick = { showBackgroundDialog = true }
            )
            Preference(
                name = stringResource(Res.string.pref_gpx_track_title),
                onClick = { showGpxDialog = true },
            )
            Preference(
                name = stringResource(Res.string.pref_custom_geometry_title),
                onClick = { showGeometryDialog = true },
            )
            if (showBackgroundDialog)
                SimpleListPickerDialog(
                    onDismissRequest = { showBackgroundDialog = false },
                    items = listOf("MAP", "AERIAL"),
                    onItemSelected = { prefs.putString(Prefs.THEME_BACKGROUND, it) },
                    getItemName = {
                        if (it == "MAP") stringResource(Res.string.background_type_map)
                        else stringResource(Res.string.background_type_aerial_esri)
                    },
                    selectedItem = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
                )
            if (showGpxDialog) {
                val ctx = LocalContext.current
                suspend fun getFile() {
                    val file = FileKit.openFilePicker(FileKitType.File(".gpx")) ?: return
                    file.copyTo(gpxFile)
                    gpx_track_changed = true
                    showGpxDialog = false
                    showGpxDialog = true
                }
                val downloadController: DownloadController = koinInject()
                AlertDialog(
                    onDismissRequest = { showGpxDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showGpxDialog = false }) { Text(stringResource(Res.string.close)) }
                    },
                    title = { Text(stringResource(Res.string.pref_gpx_track_title))},
                    text = {
                        Column {
                            Button(
                                onClick = {
                                    val points = loadGpxTrackPoints(ctx, true) ?: return@Button
                                    GlobalScope.launch {
                                        val import = importGpx(points, true, 10.0).getOrNull()
                                        import?.downloadBBoxes?.let {
                                            if (it.isEmpty()) return@launch
                                            Downloader.enqueuedDownloads.addAll(it.drop(1))
                                            downloadController.download(it.first(), false, true)
                                        }
                                    }
                                },
                                enabled = gpxFile.exists()
                            ) { Text(stringResource(Res.string.pref_gpx_track_download), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) { getFile() }
                                }
                            ) { Text(stringResource(Res.string.pref_gpx_track_provide), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                            if (gpxFile.exists())
                                SwitchPreference(
                                    name = stringResource(Res.string.pref_gpx_track_enable),
                                    default = false,
                                    pref = Prefs.SHOW_GPX_TRACK,
                                )
                        }
                    },
                )
            }
            if (showGeometryDialog) {
                suspend fun getFile() {
                    val file = FileKit.openFilePicker(FileKitType.File(".gpx")) ?: return
                    file.copyTo(customGeometryFile)
                    custom_geometry_changed = true
                    showGeometryDialog = false
                    showGeometryDialog = true
                }
                AlertDialog(
                    onDismissRequest = { showGeometryDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showGeometryDialog = false }) { Text(stringResource(Res.string.close)) }
                    },
                    title = { Text(stringResource(Res.string.pref_custom_geometry_title))},
                    text = {
                        Column {
                            Text(stringResource(Res.string.pref_custom_geometry_info))
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) { getFile() }
                                }
                            ) { Text(stringResource(Res.string.file_provide), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                            if (customGeometryFile.exists())
                                SwitchPreference(
                                    name = stringResource(Res.string.quest_enabled),
                                    default = false,
                                    pref = Prefs.SHOW_CUSTOM_GEOMETRY,
                                )
                        }
                    },
                )
            }
        }
    }
}

fun loadGpxTrackPoints(context: Context, complain: Boolean = false): List<LatLon>? {
    // load gpx file as one long track, no matter how it's stored internally (for now)
    // <trkpt lat="..." lon="..."><ele>...</ele></trkpt>
    // <wpt lon="..." lat="...">
    if (!gpxFile.exists()) {
        if (complain)
            context.toast2(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
        return null
    }

    val gpxPoints = runCatching { runBlocking {
        GPXParser().parse(gpxFile.readString().byteInputStream()).tracks.map { track ->
            track.trackSegments.map { segment ->
                segment.trackPoints
            }
        }.flatten().flatten()
            .map { trackPoint ->
                LatLon(
                    latitude = trackPoint.latitude,
                    longitude = trackPoint.longitude
                )
            }
    } }.getOrNull()

    if ((gpxPoints?.size ?: 0) < 2) {
        context.toast2(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
        return null
    }
    return gpxPoints
}

private val gpxFile = PlatformFile(FileKit.filesDir, "display_track.gpx")
val customGeometryFile = PlatformFile(FileKit.filesDir, "customGeometry.geojson")

var gpx_track_changed = false
var custom_geometry_changed = false

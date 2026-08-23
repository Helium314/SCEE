package de.westnordost.streetcomplete.util

import android.content.SharedPreferences
import android.content.res.Resources
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Checkbox
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.settings.toast2
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.ktx.tryOpenUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OverlayCustomizer(
    onDismiss: () -> Unit,
    index: Int,
    onChanged: (Boolean) -> Unit, // true if overlay is currently set custom overlay
    onDeleted: (Boolean) -> Unit, // true if overlay was currently set custom overlay
    questTypeRegistry: QuestTypeRegistry,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showFilterInfo by remember { mutableStateOf(false) }
    var showColorInfo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val prefs: Preferences = koinInject()
    val indices = remember { getCustomOverlayIndices(prefs).sorted() }
    val icons = remember {
        LinkedHashSet<DrawableResource>(questTypeRegistry.size).apply {
            add(Res.drawable.ic_custom_overlay)
            questTypeRegistry.forEach { add(it.icon) }
        }.toList()
    }
    val fullFilter = remember { prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index), "").split(" with ").takeIf { it.size == 2 } }

    var icon by rememberSaveable {
        mutableStateOf(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index), "ic_custom_overlay"))
    }
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), "")))
    }
    var filterNodes by rememberSaveable { mutableStateOf(fullFilter?.get(0)?.contains("nodes") ?: true) }
    var filterWays by rememberSaveable { mutableStateOf(fullFilter?.get(0)?.contains("ways") ?: true) }
    var filterRelations by rememberSaveable { mutableStateOf(fullFilter?.get(0)?.contains("relations") ?: true) }
    var filterText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(fullFilter?.getOrNull(1) ?: "")) }
    var colorKey by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index), "")))
    }
    var dashFilter by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_DASH_FILTER, index), "")))
    }
    var highlightMissingData by rememberSaveable {
        mutableStateOf(prefs.getBoolean(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA, index), true))
    }

    fun fullFilterText(): String {
        val types = listOfNotNull(
            if (filterNodes) "nodes" else null,
            if (filterWays) "ways" else null,
            if (filterRelations) "relations" else null,
        ).joinToString(", ")
        return "$types with ${filterText.text}"
    }
    var enableOk by rememberSaveable { mutableStateOf(false) }
    var toastyJob: Job? by remember { mutableStateOf(null) }
//    var toastMessage by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    fun delayedToast(message: String?) {
        toastyJob?.cancel()
        toastyJob = scope.launch {
            delay(3000)
            withContext(Dispatchers.Main) { ctx.toast2("Error: $message", Toast.LENGTH_LONG) }
//            toastMessage = "Error: $message"
        }
    }
    fun checkIsOK(): Boolean {
        if (colorKey.text.count { it == '(' } != colorKey.text.count { it == ')' }) return false
        if (filterText.text.count { it == '(' } != filterText.text.count { it == ')' }) return false
        if (dashFilter.text.count { it == '(' } != dashFilter.text.count { it == ')' }) return false
        if (filterText.text.isBlank()) return false
        try {
            colorKey.text.toRegex()
        } catch (e: Exception) {
            delayedToast(e.message)
            return false
        }
        if (dashFilter.text.isNotEmpty())
            try {
                "ways with ${dashFilter.text}".toElementFilterExpression()
            } catch (e: Exception) {
                delayedToast(e.message)
                return false
            }
        try {
            fullFilterText().toElementFilterExpression()
        } catch (e: Exception) {
            delayedToast(e.message)
            return false
        }
        toastyJob?.cancel()
        return true
    }
    LaunchedEffect(filterNodes, filterWays, filterRelations, filterText, colorKey, dashFilter) {
        enableOk = checkIsOK()
    }
    val scroll = rememberScrollState()

    ScrollableAlertDialog( // todo: not scrollable for some reason, no matter whether scroll is on dialog or on content column
        onDismissRequest = onDismiss,
        buttonRow = {
            if (index in indices)
                TextButton({ showDeleteConfirmation = true }) { Text(stringResource(Res.string.delete_confirmation)) }
            TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) }
            TextButton(
                onClick = {
                    // update prefs and enable this overlay
                    prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index), fullFilterText())
                    prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index), colorKey.text)
                    prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), name.text)
                    prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index), icon)
                    prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_DASH_FILTER, index), dashFilter.text)
                    prefs.putBoolean(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA, index), highlightMissingData)
                    if (index !in indices) { // add if it's new, and select it immediately
                        prefs.putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + index).joinToString(","))
                        prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, index)
                    }
                    onChanged(index == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0))
                    onDismiss()
                },
                enabled = enableOk
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
        title = { Text(stringResource(Res.string.custom_overlay_title)) },
        content = {
            Column(modifier = Modifier.scrollable(scroll, Orientation.Vertical)) { //
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DropdownButton(
                        items = icons,
                        onSelectedItem = { icon = it.name },
                        itemContent = { Image(painterResource(it), null, modifier = Modifier.width(56.dp)) },
                        selectedItem = icons.firstOrNull { it.name == icon } ?: Res.drawable.ic_custom_overlay,
                    )
                    TextField2(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text(stringResource(Res.string.name_label)) }
                    )
                }
                TextButton({ showFilterInfo = true }) { Text(stringResource(Res.string.custom_overlay_filter_info) + " ℹ️", Modifier.fillMaxWidth()) }
                TextField2(
                    value = filterText,
                    onValueChange = { filterText = it },
                    placeholder = { Text(stringResource(Res.string.element_selection_button)) }
                )
                Row(modifier = Modifier.clickable { filterNodes = !filterNodes }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(filterNodes, { filterNodes = it})
                    Text("nodes")
                }
                Row(modifier = Modifier.clickable { filterWays = !filterWays }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(filterWays, { filterWays = it})
                    Text("ways")
                }
                Row(modifier = Modifier.clickable { filterRelations = !filterRelations }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(filterRelations, { filterRelations = it})
                    Text("relations")
                }
                TextButton({ showColorInfo = true }) { Text(stringResource(Res.string.custom_overlay_color_info) + " ℹ️", Modifier.fillMaxWidth()) }
                TextField2(
                    value = colorKey,
                    onValueChange = { colorKey = it },
                    placeholder = { Text(stringResource(Res.string.custom_overlay_color_hint)) }
                )
                Row(modifier = Modifier.clickable { highlightMissingData = !highlightMissingData }, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.custom_overlay_highlight_missing))
                    Switch(highlightMissingData, { highlightMissingData = it })
                }
                TextField2(
                    value = dashFilter,
                    onValueChange = { dashFilter = it },
                    placeholder = { Text(stringResource(Res.string.custom_overlay_dash_filter_hint)) }
                )
            }
        }
    )

    if (showDeleteConfirmation) {
        val overlayName = prefs.getString(
            getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index),
            stringResource(Res.string.custom_overlay_title)
        )
        ConfirmationDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            onConfirmed = {
                val isActive = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0) == index
                prefs.prefs.keys.forEach { if (it.startsWith("custom_overlay_${index}_")) prefs.prefs.remove(it) }
                prefs.putString(Prefs.CUSTOM_OVERLAY_INDICES, indices.filterNot { it == index }.joinToString(","))
                if (isActive)
                    prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
                onDeleted(isActive)
                onDismiss()
            },
            text = { Text(stringResource(Res.string.custom_overlay_delete, overlayName)) }
        )
    }
    if (showFilterInfo) {
        AlertDialog(
            onDismissRequest = { showFilterInfo = false },
            confirmButton = { TextButton(onClick = { showFilterInfo = false } ) {
                Text(stringResource(Res.string.ok))
            } },
            dismissButton = {
                val uriHandler = LocalUriHandler.current
                TextButton(
                    onClick = {
                        uriHandler.tryOpenUri("https://github.com/Helium314/SCEE/blob/modified/CONTRIBUTING_A_NEW_QUEST.md#element-selection")
                        showFilterInfo = false
                    }
                ) {
                    Text("link")
                }
            },
            text = { Text(stringResource(Res.string.custom_overlay_filter_message)) },
        )
    }
    if (showColorInfo) {
        InfoDialog(
            onDismissRequest = { showColorInfo = false },
            text = { Text(stringResource(Res.string.custom_overlay_color_message)) }
        )
    }
     // the toast appears behind the dialog, so it's completely useless...
//    if (toastMessage != null)
//        ToastPopup({ toastMessage = null }, toastMessage!!)
}

// creates dummy overlays for the custom overlay, so they can be displayed to the user
// title is invalid resId 0
// name and wikiLink are the overlay index as stored in shared preferences
// changesetComment is the overlay title
fun getFakeCustomOverlays(prefs: Preferences, res: Resources, onlyIfExpertMode: Boolean = true): List<Overlay> {
    if (onlyIfExpertMode && !prefs.getBoolean(Prefs.EXPERT_MODE, false)) return emptyList()
    return prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0").split(",").mapNotNull { index ->
        val i = index.toIntOrNull() ?: return@mapNotNull null
        object : Overlay {
            override fun getStyledElements(mapData: MapDataWithGeometry) = emptySequence<Pair<Element, OverlayStyle>>()
            @Composable
            override fun Form(on: (OverlayAction) -> Unit, element: Element?, geometry: ElementGeometry, countryInfo: CountryInfo) {}
            override val changesetComment = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, i), "")
                .ifBlank { res.getString(R.string.custom_overlay_title) } // displayed overlay name
            override val icon = Res.allDrawableResources[prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, i), "ic_custom_overlay")]
                ?: Res.drawable.ic_custom_overlay
            override val title = fakeStringResource // use invalid resource placeholder
            override val name = index // allows to uniquely identify an overlay
            override val wikiLink = index
            override fun equals(other: Any?): Boolean {
                return if (other !is Overlay) false
                    else wikiLink == other.wikiLink && icon == other.icon // index identifies overlay, but we also want a changed icon to trigger some reload
            }
        }
    }
}

fun getIndexedCustomOverlayPref(pref: String, index: Int) = pref.replace("idx", index.toString())
fun getCurrentCustomOverlayPref(pref: String, prefs: ObservableSettings) = getIndexedCustomOverlayPref(pref, prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0))
fun getCustomOverlayIndices(prefs: SharedPreferences) = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")!!
    .split(",").mapNotNull { it.toIntOrNull() }
fun getCustomOverlayIndices(prefs: Preferences) = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")
    .split(",").mapNotNull { it.toIntOrNull() }

@OptIn(InternalResourceApi::class)
val fakeStringResource = StringResource("", "", emptySet())

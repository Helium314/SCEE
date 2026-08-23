package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_poi
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.animateFallDown
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AddPoiForm(
    feature: Feature,
    position: LatLon,
    onAdd: (node: Node) -> Unit,
    onDismiss: () -> Unit,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    showPin: Boolean = true
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val mapDataWithEditsSource: MapDataWithEditsSource = koinInject()
    val levelFilter: LevelFilter = koinInject()
    val featureDictionary: FeatureDictionary = koinInject()

    // show similar elements on map
    LaunchedEffect(Unit) {
        scope.launch {
            val bbox = position.enclosingBoundingBox(50.0)
            val data = mapDataWithEditsSource.getMapDataWithGeometry(bbox)
            val tags = Tags(mapOf()).also { feature.applyTo(it) }
            val elements = if (Node(0L, position, tags).isPlace()) {
                data.filter { it.isPlace() }
            } else {
                val filteredTags = tags.filter {
                    it.key != "name" && !it.key.startsWith("brand") && !it.value.contains(" ")
                }
                val filter = "nodes, ways, relations with ${
                    filteredTags
                        .map { if (it.value == "yes") it.key else it.key + "=" + it.value }
                        .joinToString(" and ")
                }".toElementFilterExpression()
                data.filter { filter.matches(it) }
            }

            val markers = elements.mapNotNull { e ->
                // include only elements that fit with the currently active level filter
                if (!levelFilter.levelAllowed(e)) return@mapNotNull null

                val geometry = data.getGeometry(e.type, e.id) ?: return@mapNotNull null
                val icon = getIcon(featureDictionary, e)
                val title = getTitle(e.tags)
                Marker(geometry, icon, title)
            }

            onSetMapMarkers(markers)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (showPin)
            Pin(
                iconPainter = painterResource(Res.drawable.ic_add_poi),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(Dimensions.getOpenQuestFormMapPadding(LocalWindowInfo.current))
                    .animateFallDown(startDelay = 200.milliseconds)
            )
        val prefs: Preferences = koinInject()
        EditTagsForm(
            onConfirmed = {
                val tags = feature.addTags.toMutableMap()
                it.applyTo(tags)
                val node = Node(0, position, tags)
                onAdd(node)
                if (feature.addTags != tags && !node.isPlace())
                    prefs.putString(Prefs.CREATE_NODE_LAST_TAGS_FOR_FEATURE + feature.id, Json.encodeToString(tags))
                onDismiss()
            },
            onDismiss = onDismiss,
            originalElement = Node(0, position, feature.addTags)
        )
    }
}

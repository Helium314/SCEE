package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.crosshair
import de.westnordost.streetcomplete.resources.insert_node_select_way
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.SwitchMapBackgroundButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.feature.FeatureSearchDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalGetOffsetCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.math.PositionOnCrossingWaySegments
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.PositionOnWaysSegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWaysForInsertNodeForm
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InsertNodeForm(
    position: LatLon,
    onDismiss: () -> Unit,
    highlightGeometries: ((Collection<ElementGeometry>) -> Unit)?,
    onSelectFeature: (Feature, PositionOnWay) -> Unit
) {
    val prefs: Preferences = koinInject()
    val mapDataSource: MapDataWithEditsSource = koinInject()
    val levelFilter: LevelFilter = koinInject()
    val featureDictionary: FeatureDictionary = koinInject()
    val initialBackground = rememberSaveable { prefs.getString(Prefs.THEME_BACKGROUND, "MAP") }
    val metersPerDp = LocalMapMetersPerDp.current
    val maxDistanceToCrosshair = (metersPerDp * 24).dp.toPx().toDouble()
    val snapToVertexDistance = (metersPerDp * 12).dp.toPx().toDouble()
    var mapDataBbox by remember { mutableStateOf(position.enclosingBoundingBox(100.0)) }
    var mapData: MapDataWithGeometry by remember { mutableStateOf(mapDataSource.getMapDataWithGeometry(mapDataBbox)) }
    LaunchedEffect(position) {
        // reload mapData if necessary
        if (mapDataBbox.contains(position)) return@LaunchedEffect
        mapDataBbox = position.enclosingBoundingBox(100.0)
        mapData = mapDataSource.getMapDataWithGeometry(mapDataBbox)
    }
    val ways: List<Pair<Way, List<LatLon>>> = remember(position) {
        mapData.ways.mapNotNull { way ->
            if (!levelFilter.levelAllowed(way)) return@mapNotNull null
            val positions = way.nodeIds.map {
                val node = mapData.getNode(it) ?: throw IllegalStateException("node $it of way ${way.id} not in map data")
                node.position
            }
            way to positions
        }
    }
    BackHandler {
        if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") != initialBackground)
            prefs.putString(Prefs.THEME_BACKGROUND, initialBackground)
        onDismiss()
    }
    val positionOnWay = remember(position, ways) {
        position.getPositionOnWaysForInsertNodeForm(
            ways = ways,
            maxDistance = maxDistanceToCrosshair,
            snapToVertexDistance = snapToVertexDistance
        )
    }
    val connectedWays = remember(positionOnWay) { positionOnWay?.getWays(mapData).orEmpty() }
    var selectedWay: Way? by remember(connectedWays) { mutableStateOf(connectedWays.firstOrNull()) }
    val showMarkers = LocalMapMarkersCallback.current
    LaunchedEffect(connectedWays) {
        showMarkers?.invoke(connectedWays.flatMap { way ->
            way.nodeIds.mapNotNull {
                val node = mapData.getNode(it) ?: return@mapNotNull null
                if (node.tags.isEmpty()) return@mapNotNull null
                Marker(
                    ElementPointGeometry(node.position),
                    getIcon(featureDictionary, node),
                    getTitle(node.tags)
                )
            }
        })
        highlightGeometries?.invoke(connectedWays.mapNotNull { mapData.getWayGeometry(it.id) })
    }
    var showFeatureSearch by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        val offset = LocalGetOffsetCallback.current?.invoke(positionOnWay?.position ?: position) ?: Offset(0f, 0f)
        Icon(
            painter = painterResource(Res.drawable.crosshair),
            contentDescription = null,
            modifier = Modifier
                .align(AbsoluteAlignment.TopLeft)
                .size(72.dp)
                .absoluteOffset(
                    x = offset.x.pxToDp() - 36.dp,
                    y = offset.y.pxToDp() - 36.dp
                ),
            tint = MaterialTheme.colors.onSurface
        )
        BottomSheetFormScaffold(
            content = {
                val featureDictionary: FeatureDictionary = koinInject()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(6.dp)) {
                    connectedWays.forEach {
                        val label = runBlocking { getNameAndLocationLabel(getSystemResourceEnvironment(), LayoutDirection.Ltr, it, featureDictionary, false) }
                        Text(label ?: AnnotatedString("${it.type} ${it.id}"), Modifier.fillMaxWidth().clickable { selectedWay = it })
                    }
                    Divider()
                    Text(
                        selectedWay?.tags?.entries?.joinToString("\n") { "${it.key} = ${it.value}" }
                            ?: stringResource(Res.string.insert_node_select_way),
                        Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())
                    )
                    Divider()
                    Row {
                        TextButton({
                            if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") != initialBackground)
                                prefs.putString(Prefs.THEME_BACKGROUND, initialBackground)
                            onDismiss()
                        }) { Text(stringResource(Res.string.cancel)) }
                        SwitchMapBackgroundButton()
                    }
                }
            },
            fab = {
                if (positionOnWay != null)
                    FloatingOkButton({ showFeatureSearch = true })
            }
        )
        if (showFeatureSearch) {
            val countryBoundaries: CountryBoundaries = koinInject()
            val countryInfos: CountryInfos = koinInject()
            val countryInfo = remember(position) { countryInfos.get(countryBoundaries, position) }
            val defaultFeatureIds = prefs.getString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, "")
                .split("§").filter { it.isNotBlank() }
                .ifEmpty { listOf("amenity/post_box", "barrier/gate", "highway/crossing/unmarked", "highway/crossing/uncontrolled", "highway/traffic_signals", "barrier/bollard", "traffic_calming/table") }
            FeatureSearchDialog(
                onDismissRequest = { showFeatureSearch = false },
                onSelectedFeature = {
                    if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") != initialBackground)
                        prefs.putString(Prefs.THEME_BACKGROUND, initialBackground)
                    val recentFeatureIds = prefs.getString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, "").split("§").toMutableList()
                    if (recentFeatureIds.lastOrNull() != it.id) {
                        recentFeatureIds.remove(it.id)
                        recentFeatureIds.add(it.id)
                        prefs.putString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, recentFeatureIds.takeLast(35).joinToString("§"))
                    }
                    onSelectFeature(it, positionOnWay!!)
                },
                featureDictionary = featureDictionary,
                geometryType = GeometryType.VERTEX,
                countryCode = countryInfo.countryOrSubdivisionCode,
                filterFn = { true },
                codesOfDefaultFeatures = defaultFeatureIds.reversed()
            )
        }
    }
}

private fun PositionOnWay.getWays(mapData: MapData) = when (this) {
    is PositionOnWaySegment -> listOf(mapData.getWay(wayId)!!)
    is VertexOfWay -> wayIds.map { mapData.getWay(it)!! }
    is PositionOnWaysSegment -> insertIntoWaysAt.map { mapData.getWay(it.wayId)!! }
    is PositionOnCrossingWaySegments -> insertIntoWaysAt.map { mapData.getWay(it.wayId)!! }
}

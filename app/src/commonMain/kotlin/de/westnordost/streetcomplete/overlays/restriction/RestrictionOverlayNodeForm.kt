package de.westnordost.streetcomplete.overlays.restriction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.oneway_yes
import de.westnordost.streetcomplete.resources.oneway_yes_reverse
import de.westnordost.streetcomplete.resources.restriction_overlay_direction_text
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.LocalGetOffsetCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.ktx.selectionFrame
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import de.westnordost.streetcomplete.util.math.initialBearingTo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val selectableRestrictionNodeTypes = listOf(
    RestrictionNodeType.GIVE_WAY,
    RestrictionNodeType.STOP,
)

@Composable
fun RestrictionOverlayNodeForm(
    on: (OverlayAction) -> Unit,
    element: Element?,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
) {
    val (originalType, originalDirection) = remember(element) {
        element?.tags?.let { parseRestrictionNode(it) } ?: (null to null)
    }
    var selectedType by rememberSaveable(originalType) {
        mutableStateOf(originalType)
    }
    var direction by rememberSaveable(originalDirection) {
        mutableStateOf(originalDirection)
    }

    val position = if (element == null) geometry.center else null
    val roads = remember<Collection<Pair<Way, List<LatLon>>>?>(position != null) {
        position?.let {
            mapDataWithEditsSource.getRestrictionNodeWays(it.enclosingBoundingBox(100.0))
        }
    }
    val metersPerDp = LocalMapMetersPerDp.current
    val maxDistanceToCrosshair = (metersPerDp * 24).dp.toPx().toDouble()
    val snapToVertexDistance = (metersPerDp * 12).dp.toPx().toDouble()

    val rawPositionOnWay = remember(position, roads, metersPerDp) {
        if (position == null || roads == null) return@remember null
        position.getPositionOnWays(
            ways = roads,
            maxDistance = maxDistanceToCrosshair,
            snapToVertexDistance = snapToVertexDistance,
        )
    }
    val wayCountOnVertex = remember(rawPositionOnWay, roads) {
        val vertex = rawPositionOnWay as? VertexOfWay ?: return@remember null
        val roads = roads ?: return@remember null
        wayCountOnVertex(vertex.nodeId, roads)
    }
    val vertexTags = remember(rawPositionOnWay) {
        val vertex = rawPositionOnWay as? VertexOfWay ?: return@remember null
        mapDataWithEditsSource.getNode(vertex.nodeId)?.tags
    }

    val type = if (element == null) {
        resolveRestrictionNodeType(selectedType, wayCountOnVertex)
    } else {
        selectedType
    }
    val positionOnWay = if (element == null) {
        positionOnWayForRestrictionNode(type, rawPositionOnWay, wayCountOnVertex, vertexTags)
    } else {
        null
    }

    val wayForDirection = remember(element, positionOnWay) {
        mapDataWithEditsSource.wayForRestrictionNode(element, positionOnWay)
    }
    val isMultiWayVertex = (rawPositionOnWay as? VertexOfWay)?.wayIds?.size?.let { it > 1 } == true
        && element == null
    val showWayDirection = type != RestrictionNodeType.ALL_WAY_STOP &&
        shouldShowRestrictionNodeDirection(
            wayTags = wayForDirection?.tags,
            isLeftHandTraffic = countryInfo.isLeftHandTraffic,
            isMultiWayVertex = isMultiWayVertex,
        )
    val wayRotation = remember(element, positionOnWay, wayForDirection) {
        mapDataWithEditsSource.wayRotationForRestrictionNode(element, positionOnWay, wayForDirection)
    }
    val mapRotation = LocalMapRotation.current
    val imageRotation = wayRotation.toFloat() - mapRotation

    val hasChanges = type != originalType || direction != originalDirection
    val isComplete = type != null && (element != null || positionOnWay != null)

    Box(Modifier.fillMaxSize()) {
        if (positionOnWay != null) {
            val offset = LocalGetOffsetCallback.current?.invoke(positionOnWay.position)
            if (offset != null) {
                Pin(
                    iconPainter = painterResource(type?.icon ?: RestrictionNodeType.STOP.icon),
                    modifier = Modifier
                        .align(AbsoluteAlignment.TopLeft)
                        .size(71.dp, 142.dp)
                        .absoluteOffset(
                            x = offset.x.pxToDp() - 36.dp,
                            y = offset.y.pxToDp() - 71.dp
                        )
                )
            }
        }

        OverlayForm(
            on = on,
            isComplete = isComplete,
            hasChanges = hasChanges,
            onClickOk = {
                val type = type ?: return@OverlayForm
                if (element != null) {
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    type.applyTo(tagChanges, direction)
                    if (!tagChanges.hasChanges) return@OverlayForm
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                } else if (positionOnWay != null) {
                    val action = createNodeAction(positionOnWay, mapDataWithEditsSource) {
                        type.applyTo(it, direction)
                    }
                    if (action != null) {
                        on(Edit(action))
                    }
                }
            },
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                DropdownButton(
                    items = selectableRestrictionNodeTypes,
                    onSelectedItem = { selectedType = it },
                    selectedItem = type,
                    itemContent = {
                        ImageWithLabel(
                            painter = painterResource(it.icon),
                            label = stringResource(it.title)
                        )
                    }
                )

                if (showWayDirection) {
                    Text(stringResource(Res.string.restriction_overlay_direction_text))
                    Row {
                        val forwardPainter = painterResource(Res.drawable.oneway_yes)
                        val backwardPainter = painterResource(Res.drawable.oneway_yes_reverse)
                        DirectionChoice(
                            selected = direction == RestrictionNodeDirection.FORWARD,
                            painter = remember(forwardPainter) { ClipCirclePainter(forwardPainter) },
                            imageRotation = imageRotation,
                            onSelect = { direction = RestrictionNodeDirection.FORWARD },
                        )
                        DirectionChoice(
                            selected = direction == RestrictionNodeDirection.BACKWARD,
                            painter = remember(backwardPainter) { ClipCirclePainter(backwardPainter) },
                            imageRotation = imageRotation,
                            onSelect = { direction = RestrictionNodeDirection.BACKWARD },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionChoice(
    selected: Boolean,
    painter: Painter,
    imageRotation: Float,
    onSelect: () -> Unit,
) {
    Box(
        modifier = Modifier
            .selectionFrame(selected)
            .selectable(selected) { onSelect() },
        contentAlignment = Alignment.Center,
    ) {
        ImageWithLabel(
            painter = painter,
            label = null,
            imageRotation = imageRotation,
        )
    }
}

private val restrictionNodeWaysFilter by lazy { """
    ways with
      area != yes
      and (
        highway ~ ${ALL_ROADS.joinToString("|")}|cycleway
        or (
          highway ~ path|footpath|bridleway
          and bicycle ~ yes|designated
        )
      )
""".toElementFilterExpression()
}

private fun MapDataWithEditsSource.getRestrictionNodeWays(
    bbox: BoundingBox,
): Collection<Pair<Way, List<LatLon>>> {
    val data = getMapDataWithGeometry(bbox)
    return data
        .filter(restrictionNodeWaysFilter)
        .filterIsInstance<Way>()
        .map { way ->
            val positions = way.nodeIds.map { data.getNode(it)!!.position }
            way to positions
        }.toList()
}

private fun MapDataWithEditsSource.wayForRestrictionNode(
    element: Element?,
    positionOnWay: PositionOnWay?,
): Way? = when {
    element != null -> getWaysForNode(element.id).firstOrNull { restrictionNodeWaysFilter.matches(it) }
    positionOnWay is VertexOfWay -> getWay(positionOnWay.wayIds.first())
    positionOnWay is PositionOnWaySegment -> getWay(positionOnWay.wayId)
    else -> null
}

private fun MapDataWithEditsSource.wayRotationForRestrictionNode(
    element: Element?,
    positionOnWay: PositionOnWay?,
    way: Way?,
): Double {
    val node = element as? Node
    if (node != null) {
        val way = way ?: return 0.0
        val index = way.nodeIds.indexOf(node.id)
        if (index < 0) return 0.0
        return bearingAlongWayAtIndex(node.position, index, way.nodeIds) { getNode(it)?.position }
    }
    return when (positionOnWay) {
        is PositionOnWaySegment -> positionOnWay.segment.first.initialBearingTo(positionOnWay.segment.second)
        is VertexOfWay -> {
            val way = way ?: getWay(positionOnWay.wayIds.first()) ?: return 0.0
            val index = way.nodeIds.indexOf(positionOnWay.nodeId)
            if (index < 0) return 0.0
            bearingAlongWayAtIndex(positionOnWay.position, index, way.nodeIds) { getNode(it)?.position }
        }
        else -> 0.0
    }
}

internal fun bearingAlongWayAtIndex(
    position: LatLon,
    index: Int,
    nodeIds: List<Long>,
    positionOf: (Long) -> LatLon?,
): Double {
    if (nodeIds.size < 2 || index !in nodeIds.indices) return 0.0
    return if (index != nodeIds.lastIndex) {
        val next = positionOf(nodeIds[index + 1]) ?: return 0.0
        position.initialBearingTo(next)
    } else {
        val previous = positionOf(nodeIds[index - 1]) ?: return 0.0
        previous.initialBearingTo(position)
    }
}

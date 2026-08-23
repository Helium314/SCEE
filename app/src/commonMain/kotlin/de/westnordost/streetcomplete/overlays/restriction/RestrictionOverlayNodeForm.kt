package de.westnordost.streetcomplete.overlays.restriction
/*
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.doOnLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.oneway.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.oneway_yes
import de.westnordost.streetcomplete.resources.oneway_yes_reverse
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.ktx.selectionFrame
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import de.westnordost.streetcomplete.util.math.initialBearingTo
import org.koin.android.ext.android.inject

class RestrictionOverlayNodeForm : AbstractOverlayForm(), IsMapPositionAware, IsMapOrientationAware {

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    private val items = Type.entries
    private val selectableItems = listOf(Type.GIVE_WAY, Type.STOP)

    // state is separate for historic reasons
    private val positionOnWayState: MutableState<PositionOnWay?> = mutableStateOf(null)
    private var positionOnWay: PositionOnWay? = null
        set(value) {
            field = value
            if (value != null) {
                setMarkerPosition(value.position)
                setMarkerVisibility(true)
            } else {
                setMarkerVisibility(false)
                setMarkerPosition(null)
            }
            positionOnWayState.value = value
        }
    private var roads: Collection<Pair<Way, List<LatLon>>>? = null
    private val waysFilter = """
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

    private var data: MapDataWithGeometry? = null
    private var direction: MutableState<Direction?> = mutableStateOf(null)

    // state is separate for historic reasons
    private var typeState: MutableState<Type?> = mutableStateOf(null)
    private var type: Type? = null
        set(value) {
            if (field == value) return
            field = value
            checkCurrentCursorPosition()
            if (element == null) {
                if (type == Type.GIVE_WAY) setMarkerIcon(R.drawable.ic_restriction_give_way)
                    else setMarkerIcon(R.drawable.ic_restriction_stop)
            }
            checkIsFormComplete()
            typeState.value = field
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            var shouldShowWayDirection by remember { mutableStateOf(shouldShowWayDirection()) }
            LaunchedEffect(positionOnWayState.value) {
                shouldShowWayDirection = shouldShowWayDirection()
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                // Sign selection
                DropdownButton(
                    items = selectableItems,
                    onSelectedItem = { type = it },
                    selectedItem = typeState.value,
                    itemContent = {
                        ImageWithLabel(
                            painter = painterResource(it.image),
                            label = stringResource(it.text)
                        )
                    }
                )

                if (typeState.value != Type.ALL_WAY_STOP && shouldShowWayDirection) {
                    // direction chooser
                    Text(stringResource(R.string.restriction_overlay_direction_text))
                    Row {
                        val yes = org.jetbrains.compose.resources.painterResource(Res.drawable.oneway_yes)
                        val reverse = org.jetbrains.compose.resources.painterResource(Res.drawable.oneway_yes_reverse)
                        Box(
                            modifier = Modifier
                                .selectionFrame(direction.value == Direction.FORWARD)
                                .selectable(direction.value == Direction.FORWARD) {
                                    direction.value = Direction.FORWARD
                                    checkIsFormComplete()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            ImageWithLabel(
                                painter = remember(yes) { ClipCirclePainter(yes) },
                                label = null,
                                imageRotation = getWayRotation().toFloat() - mapRotation.floatValue,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .selectionFrame(direction.value == Direction.BACKWARD)
                                .selectable(direction.value == Direction.BACKWARD) {
                                    direction.value = Direction.BACKWARD
                                    checkIsFormComplete()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            ImageWithLabel(
                                painter = remember(reverse) { ClipCirclePainter(reverse) },
                                label = null,
                                imageRotation = getWayRotation().toFloat() - mapRotation.floatValue
                            )
                        }
                    }
                }
            }
        } }

        checkIsFormComplete()

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        if (element == null) {
            view.doOnLayout {
                initCreatingPointOnWay()
                checkCurrentCursorPosition()
            }
            setMarkerIcon(R.drawable.ic_restriction_stop)
            setMarkerVisibility(false)
        } else {
            val td = getTypeAndDirection(element!!.tags)
            type = td.first
            direction.value = td.second
        }
    }

    private fun initCreatingPointOnWay() {
        data = mapDataWithEditsSource.getMapDataWithGeometry(geometry.center.enclosingBoundingBox(100.0))
        val data = data ?: return
        roads = data
            .filter(waysFilter)
            .filterIsInstance<Way>()
            .map { way ->
                val positions = way.nodeIds.map { data.getNode(it)!!.position }
                way to positions
            }.toList()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkCurrentCursorPosition()
    }

    override fun onMapMoved(position: LatLon) {
        if (element != null) return
        checkCurrentCursorPosition()
    }

    private fun shouldShowWayDirection(): Boolean {
        val element = element
        val pow = positionOnWay
        if (pow is VertexOfWay && pow.wayIds.size > 1) return false
        val way = when {
            element != null -> mapDataWithEditsSource.getWaysForNode(element.id).firstOrNull { waysFilter.matches(it) }
            pow is VertexOfWay -> mapDataWithEditsSource.getWay(pow.wayIds.first())
            pow is PositionOnWaySegment -> mapDataWithEditsSource.getWay(pow.wayId)
            else -> null
        }
        if (way == null) return false
        return if (way.tags["highway"] in ALL_ROADS)
            !isOneway(way.tags) || isNotOnewayForCyclists(way.tags, countryInfo.isLeftHandTraffic)
        else // cycleways, though doesn't catch oneway = yes and oneway:bicycle = no
            !isOneway(way.tags) && way.tags["oneway:bicycle"] !in listOf("yes", "-1")
    }

    private fun checkCurrentCursorPosition() {
        val roads = roads ?: return
        val metersPerPixel = metersPerPixel ?: return
        val maxDistance = metersPerPixel * requireContext().resources.dpToPx(24)
        val snapToVertexDistance = metersPerPixel * requireContext().resources.dpToPx(12)
        val pos = geometry.center.getPositionOnWays(roads, maxDistance, snapToVertexDistance)
        if (pos is VertexOfWay) {
            val node = mapDataWithEditsSource.getNode(pos.nodeId)!!
            if (node.tags.containsKey("highway") || node.tags.containsKey("crossing"))
                return
        }
        // get number of roads on this vertex
        // but count only 1 road if count is 2 and it's an end node of both
        val wayCountOnVertex = if (pos !is VertexOfWay) null
        else {
            val r = roads.filter { it.first.nodeIds.contains(pos.nodeId) }
            if (r.size == 2 && r.all { it.first.nodeIds.firstAndLast().contains(pos.nodeId) }) 1
            else r.size
        }
        positionOnWay = when (type) {
            Type.GIVE_WAY -> {
                if (wayCountOnVertex != null && wayCountOnVertex > 1)
                    null // don't allow on more than a single way
                else pos
            }
            Type.STOP -> {
                if (wayCountOnVertex != null && wayCountOnVertex > 1)
                    type = Type.ALL_WAY_STOP // no normal stop if there is more than one way
                pos
            }
            Type.ALL_WAY_STOP -> {
                if (wayCountOnVertex == null || wayCountOnVertex == 1)
                    type = Type.STOP // normal stop if there is only one way
                pos
            }
            else -> pos
        }
        checkIsFormComplete()
    }

    override fun hasChanges(): Boolean {
        val td = element?.let { getTypeAndDirection(it.tags) }
        return td?.first != type || td?.second != direction.value
    }

    override fun isFormComplete(): Boolean = type != null && hasChanges() && (element != null || positionOnWay != null)

    override fun onClickOk() {
        val element = element
        val positionOnWay = positionOnWay
        val direction = direction
        val type = type ?: return
        val editAction = if (element != null) {
            val tagChanges = StringMapChangesBuilder(element.tags)
            applyTo(tagChanges, type, direction.value)
            UpdateElementTagsAction(element, tagChanges.create())
        } else if (positionOnWay != null) {
            createNodeAction(positionOnWay, mapDataWithEditsSource) { applyTo(it, type, direction.value) }
        } else null
        if (editAction != null)
            applyEdit(editAction)
    }

    private fun onLoadInstanceState(inState: Bundle) {
        val selectedIndex = inState.getInt(SELECTED_INDEX)
        type = if (selectedIndex != -1) items[selectedIndex] else null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_INDEX, items.indexOfFirst { it == type })
    }

    private fun getWayRotation(): Double {
        val element = element as? Node
        if (element != null) {
            val way = mapDataWithEditsSource.getWaysForNode(element.id).firstOrNull { waysFilter.matches(it) } ?: return 0.0
            val index = way.nodeIds.indexOf(element.id)
            return if (index != way.nodeIds.lastIndex)
                element.position.initialBearingTo(mapDataWithEditsSource.getNode(way.nodeIds[index + 1])!!.position)
            else
                mapDataWithEditsSource.getNode(way.nodeIds[index - 1])!!.position.initialBearingTo(element.position)
        } else {
            val pow = positionOnWay ?: return 0.0
            if (pow is PositionOnWaySegment) return pow.segment.first.initialBearingTo(pow.segment.second)
            else if (pow is VertexOfWay) {
                val way = mapDataWithEditsSource.getWay(pow.wayIds.first())!!
                val index = way.nodeIds.indexOf(pow.nodeId)
                return if (index != way.nodeIds.lastIndex)
                    pow.position.initialBearingTo(mapDataWithEditsSource.getNode(way.nodeIds[index + 1])!!.position)
                else
                    mapDataWithEditsSource.getNode(way.nodeIds[index - 1])!!.position.initialBearingTo(pow.position)
            }
        }
        return 0.0
    }

    companion object {
        private const val SELECTED_INDEX = "selected_index"
    }
}

private fun applyTo(tagChanges: StringMapChangesBuilder, type: Type, direction: Direction?) {
    val newDirection = direction?.takeIf { type != Type.ALL_WAY_STOP }?.osmValue
    if (tagChanges["direction"] != newDirection) {
        if (newDirection == null)
            tagChanges.remove("direction")
        else tagChanges["direction"] = newDirection
    }
    val newHighway = if (type == Type.GIVE_WAY) "give_way"
        else "stop"
    if (tagChanges["highway"] != newHighway)
        tagChanges["highway"] = newHighway
    if (type == Type.ALL_WAY_STOP) tagChanges["stop"] = "all" // according to wiki, also minor is possible, but it seems that it's not used on intersection nodes
    else if (type == Type.GIVE_WAY) tagChanges.remove("stop")
}

private fun getTypeAndDirection(tags: Map<String, String>): Pair<Type?, Direction?> {
    val type = when {
        tags["highway"] == "give_way" -> Type.GIVE_WAY
        // direction = both seems to be used like stop = all
        tags["highway"] == "stop" && (tags["stop"] == "all" || tags["direction"] == "both") -> Type.ALL_WAY_STOP
        tags["highway"] == "stop" -> Type.STOP
        else -> null
    }
    val direction = when (type) {
        Type.GIVE_WAY, Type.STOP -> tags["direction"]?.let { dir -> Direction.entries.firstOrNull { it.osmValue == dir } }
        else -> null
    }
    return type to direction
}

private enum class Type { GIVE_WAY, STOP, ALL_WAY_STOP }
private val Type.text get() = when (this) {
    Type.GIVE_WAY -> R.string.restriction_overlay_sign_give_way
    Type.STOP -> R.string.restriction_overlay_sign_stop
    Type.ALL_WAY_STOP -> R.string.restriction_overlay_sign_stop_all_way
}
private val Type.image get() = when (this) {
    Type.GIVE_WAY -> R.drawable.ic_restriction_give_way
    Type.STOP -> R.drawable.ic_restriction_stop
    Type.ALL_WAY_STOP -> R.drawable.ic_restriction_stop
}

private enum class Direction(val osmValue: String) { FORWARD("forward"), BACKWARD("backward") }
*/

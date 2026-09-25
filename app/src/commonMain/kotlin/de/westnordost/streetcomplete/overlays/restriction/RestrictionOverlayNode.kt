package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.oneway.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.VertexOfWay
import kotlinx.serialization.Serializable

@Serializable
enum class RestrictionNodeType {
    GIVE_WAY,
    STOP,
    ALL_WAY_STOP,
}

@Serializable
enum class RestrictionNodeDirection(val osmValue: String) {
    FORWARD("forward"),
    BACKWARD("backward"),
}

fun parseRestrictionNode(tags: Map<String, String>): Pair<RestrictionNodeType?, RestrictionNodeDirection?> {
    val type = when {
        tags["highway"] == "give_way" -> RestrictionNodeType.GIVE_WAY
        // direction = both seems to be used like stop = all
        tags["highway"] == "stop" && (tags["stop"] == "all" || tags["direction"] == "both") ->
            RestrictionNodeType.ALL_WAY_STOP
        tags["highway"] == "stop" -> RestrictionNodeType.STOP
        else -> null
    }
    val direction = when (type) {
        RestrictionNodeType.GIVE_WAY, RestrictionNodeType.STOP ->
            tags["direction"]?.let { dir ->
                RestrictionNodeDirection.entries.firstOrNull { it.osmValue == dir }
            }
        else -> null
    }
    return type to direction
}

fun RestrictionNodeType.applyTo(
    tagChanges: StringMapChangesBuilder,
    direction: RestrictionNodeDirection?,
) {
    val newDirection = direction?.takeIf { this != RestrictionNodeType.ALL_WAY_STOP }?.osmValue
    if (tagChanges["direction"] != newDirection) {
        if (newDirection == null) {
            tagChanges.remove("direction")
        } else {
            tagChanges["direction"] = newDirection
        }
    }
    val newHighway = if (this == RestrictionNodeType.GIVE_WAY) "give_way" else "stop"
    if (tagChanges["highway"] != newHighway) {
        tagChanges["highway"] = newHighway
    }
    if (this == RestrictionNodeType.ALL_WAY_STOP) {
        tagChanges["stop"] = "all" // according to wiki, also minor is possible, but it seems that it's not used on intersection nodes
    } else if (this == RestrictionNodeType.GIVE_WAY) {
        tagChanges.remove("stop")
    }
}

/** Auto-switch stop <-> all-way stop when creating a node, matching the historical form. */
fun resolveRestrictionNodeType(
    type: RestrictionNodeType?,
    wayCountOnVertex: Int?,
): RestrictionNodeType? = when (type) {
    RestrictionNodeType.STOP ->
        if (wayCountOnVertex != null && wayCountOnVertex > 1) RestrictionNodeType.ALL_WAY_STOP else type
    RestrictionNodeType.ALL_WAY_STOP ->
        if (wayCountOnVertex == null || wayCountOnVertex == 1) RestrictionNodeType.STOP else type
    else -> type
}

fun isOccupiedTrafficControlVertex(tags: Map<String, String>): Boolean =
    tags.containsKey("highway") || tags.containsKey("crossing")

/** Give-way may not be placed on a vertex shared by more than one eligible way. */
fun positionOnWayForRestrictionNode(
    type: RestrictionNodeType?,
    positionOnWay: PositionOnWay?,
    wayCountOnVertex: Int?,
    vertexTags: Map<String, String>?,
): PositionOnWay? {
    if (positionOnWay == null) return null
    if (positionOnWay is VertexOfWay && (vertexTags == null || isOccupiedTrafficControlVertex(vertexTags))) {
        return null
    }
    if (type == RestrictionNodeType.GIVE_WAY && wayCountOnVertex != null && wayCountOnVertex > 1) {
        return null
    }
    return positionOnWay
}

fun wayCountOnVertex(
    nodeId: Long,
    roads: Collection<Pair<Way, *>>,
): Int {
    val matching = roads.filter { it.first.nodeIds.contains(nodeId) }
    return if (matching.size == 2 && matching.all { it.first.nodeIds.firstAndLast().contains(nodeId) }) {
        1
    } else {
        matching.size
    }
}

fun shouldShowRestrictionNodeDirection(
    wayTags: Map<String, String>?,
    isLeftHandTraffic: Boolean,
    isMultiWayVertex: Boolean,
): Boolean {
    if (isMultiWayVertex) return false
    val tags = wayTags ?: return false
    return if (tags["highway"] in ALL_ROADS) {
        !isOneway(tags) || isNotOnewayForCyclists(tags, isLeftHandTraffic)
    } else {
        // cycleways, though doesn't catch oneway = yes and oneway:bicycle = no
        !isOneway(tags) && tags["oneway:bicycle"] !in listOf("yes", "-1")
    }
}

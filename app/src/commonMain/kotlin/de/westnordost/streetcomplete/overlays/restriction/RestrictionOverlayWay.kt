package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType
import de.westnordost.streetcomplete.quests.max_weight.osmKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_restriction_no_left_turn
import de.westnordost.streetcomplete.resources.ic_restriction_no_right_turn
import de.westnordost.streetcomplete.resources.ic_restriction_no_straight_on
import de.westnordost.streetcomplete.resources.ic_restriction_no_u_turn
import de.westnordost.streetcomplete.resources.ic_restriction_only_left_turn
import de.westnordost.streetcomplete.resources.ic_restriction_only_right_turn
import de.westnordost.streetcomplete.resources.ic_restriction_only_straight_on
import de.westnordost.streetcomplete.resources.ic_restriction_unknown
import de.westnordost.streetcomplete.resources.ic_overlay_restriction
import de.westnordost.streetcomplete.resources.ic_restriction_give_way
import de.westnordost.streetcomplete.resources.ic_restriction_stop
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.math.distanceToArcs
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.finalBearingTo
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource

@Serializable
sealed interface RestrictionOverlayRestriction {
    val element: Element
}

@Serializable
data class TurnRestriction(val relation: Relation) : RestrictionOverlayRestriction {
    override val element get() = relation
}

@Serializable
data class WeightRestriction(
    val way: Way,
    val type: MaxWeightType,
    val weight: String,
) : RestrictionOverlayRestriction {
    override val element get() = way
}

fun getWeightRestrictions(way: Way): List<WeightRestriction> {
    val restrictions = mutableListOf<WeightRestriction>()
    for (type in MaxWeightType.entries) {
        val key = if (way.tags.containsKey(type.osmKey)) type.osmKey
            else way.tags.keys.firstOrNull { it == "${type.osmKey}:conditional" } ?: continue
        val weight = way.tags[key]!!
        restrictions.add(WeightRestriction(way, type, weight))
    }
    return restrictions
}

fun getOriginalRestrictions(
    way: Way,
    mapDataSource: MapDataWithEditsSource,
): List<RestrictionOverlayRestriction> {
    val turnRestrictions = mapDataSource.getRelationsForWay(way.id)
        .filter { it.tags["type"] == "restriction" }
        .map { TurnRestriction(it) }
    return turnRestrictions + getWeightRestrictions(way)
}

fun getInitialRestrictionPreferComplete(
    restrictions: List<RestrictionOverlayRestriction>,
    isRelationComplete: (Relation) -> Boolean,
): RestrictionOverlayRestriction? {
    restrictions
        .firstOrNull {
            it is TurnRestriction
                && it.relation.isSupportedTurnRestriction()
                && isRelationComplete(it.relation)
        }
        ?.let { return it }
    restrictions.firstOrNull { it is WeightRestriction }?.let { return it }
    return restrictions.firstOrNull()
}

fun getInitialRestriction(
    restrictions: List<RestrictionOverlayRestriction>,
): RestrictionOverlayRestriction? =
    getInitialRestrictionPreferComplete(restrictions) { true }

// restriction:* list from wiki
val onlyTurnRestriction = listOf(
    "hgv", "caravan", "motorcar", "bus", "agricultural", "motorcycle", "bicycle", "hazmat"
)

val onlyTurnRestrictionSet = onlyTurnRestriction.toHashSet()

val turnRestrictionTypeList = turnRestrictionTypes.toList()

fun getIconForTurnRestriction(type: String?): DrawableResource = when (type) {
    "no_right_turn" -> Res.drawable.ic_restriction_no_right_turn
    "no_left_turn" -> Res.drawable.ic_restriction_no_left_turn
    "no_u_turn" -> Res.drawable.ic_restriction_no_u_turn
    "no_straight_on" -> Res.drawable.ic_restriction_no_straight_on
    "only_right_turn" -> Res.drawable.ic_restriction_only_right_turn
    "only_left_turn" -> Res.drawable.ic_restriction_only_left_turn
    "only_straight_on" -> Res.drawable.ic_restriction_only_straight_on
    else -> Res.drawable.ic_restriction_unknown
}

/** Compose drawables that Android MapLibre (overlay layer + geometry markers) must resolve. */
fun restrictionOverlayAndroidMapIcons(): List<DrawableResource> = buildList {
    add(Res.drawable.ic_overlay_restriction)
    add(Res.drawable.ic_restriction_stop)
    add(Res.drawable.ic_restriction_give_way)
    for (type in turnRestrictionTypeList) {
        add(getIconForTurnRestriction(type))
    }
    add(getIconForTurnRestriction(null))
}

fun Map<String, String>.getShortRestrictionValue(): String? {
    get("restriction")?.let { return it }
    get("restriction:conditional")?.let { return it.substringBefore("@").trim() }
    entries.firstOrNull { it.key.startsWith("restriction:") }
        ?.let { return it.value.substringBefore("@").trim() }
    return null
}

/** Find a valid neighboring "to" way for creating a turn restriction from [fromWay]. */
fun findEligibleToWay(
    position: LatLon,
    clickAreaSizeInMeters: Double,
    fromWay: Way,
    mapDataSource: MapDataWithEditsSource,
): Pair<Way, Node>? {
    val bbox = position.enclosingBoundingBox(clickAreaSizeInMeters.coerceAtLeast(10.0))
    val data = mapDataSource.getMapDataWithGeometry(bbox)

    val firstAndLastNodes = fromWay.nodeIds.firstAndLast().sorted().filter {
        mapDataSource.getWaysForNode(it).count { way -> way.tags["highway"] in ALL_ROADS } > 2
    }
    if (firstAndLastNodes.isEmpty()) return null

    val eligibleWays = data.ways.mapNotNull { way ->
        if (way.id == fromWay.id || way.isClosed) return@mapNotNull null
        if (way.tags["highway"] !in ALL_ROADS) return@mapNotNull null
        val fl = way.nodeIds.firstAndLast()
        if (!fl.containsAny(firstAndLastNodes)) return@mapNotNull null
        val geometry = data.getWayGeometry(way.id) as? ElementPolylinesGeometry ?: return@mapNotNull null
        way to geometry
    }

    val otherWay = eligibleWays.minByOrNull {
        position.distanceToArcs(it.second.polylines.single())
    }?.first ?: return null

    val viaNodeId = otherWay.nodeIds.firstAndLast()
        .singleOrNull { it in firstAndLastNodes }
        ?: return null
    val viaNode = mapDataSource.getNode(viaNodeId) ?: return null
    return otherWay to viaNode
}

fun createTurnRestrictionRelation(
    fromWay: Way,
    toWay: Way,
    viaNode: Node,
    restrictionType: String,
    signed: Boolean,
    baseTags: Map<String, String> = emptyMap(),
): Relation {
    val newTags = baseTags.toMutableMap()
    newTags["type"] = "restriction"
    // Historical create path wrote explicit=yes when the "signed" switch was unchecked
    if (!signed) newTags["explicit"] = "yes" else newTags.remove("explicit")
    newTags["restriction"] = restrictionType
    return Relation(
        id = 0L,
        members = listOf(
            RelationMember(fromWay.type, fromWay.id, "from"),
            RelationMember(toWay.type, toWay.id, "to"),
            RelationMember(viaNode.type, viaNode.id, "via"),
        ),
        tags = newTags,
    )
}

fun withTurnRestrictionType(relation: Relation, type: String): Relation {
    val newTags = relation.tags.toMutableMap()
    val conditionalKey = if (newTags.containsKey("restriction:conditional")) {
        "restriction:conditional"
    } else {
        newTags.keys.firstOrNull { it.startsWith("restriction:") && it.endsWith(":conditional") }
    }
    if (conditionalKey != null && !newTags.containsKey(conditionalKey.substringBefore(":conditional"))) {
        val old = newTags[conditionalKey]!!.substringBefore("@").trim()
        newTags[conditionalKey] = newTags[conditionalKey]!!.replace(old, type)
    } else {
        if (newTags.containsKey("restriction")) {
            newTags["restriction"] = type
        } else {
            val k = newTags.keys.firstOrNull { key ->
                key.startsWith("restriction:") && onlyTurnRestriction.any { key.endsWith(it) }
            } ?: "restriction"
            newTags[k] = type
        }
    }
    return relation.copy(tags = newTags)
}

fun withImplicitSigned(relation: Relation, signed: Boolean): Relation {
    val newTags = relation.tags.toMutableMap()
    if (signed) {
        newTags.remove("implicit")
    } else {
        newTags["implicit"] = "yes"
    }
    return relation.copy(tags = newTags)
}

fun withSwappedFromTo(relation: Relation): Relation {
    val newMembers = relation.members.map {
        when (it.role) {
            "from" -> it.copy(role = "to")
            "to" -> it.copy(role = "from")
            else -> it
        }
    }
    return relation.copy(members = newMembers)
}

/** Historical UI only enabled from/to swap for newly created draft relations (id == 0). */
fun isTurnRestrictionFromToSwapAllowed(relation: Relation): Boolean =
    relation.id == 0L

fun wayRoleInTurnRestriction(relation: Relation, wayId: Long): String? =
    relation.members.firstOrNull { it.type == ElementType.WAY && it.ref == wayId }?.role

fun withExceptions(relation: Relation, exceptions: List<String>): Relation {
    val newTags = relation.tags.toMutableMap()
    val value = exceptions.joinToString(";")
    if (value.isEmpty()) newTags.remove("except") else newTags["except"] = value
    return relation.copy(tags = newTags)
}

fun withOnlyFor(relation: Relation, onlyFor: String?): Relation {
    val currentOnly = relation.tags.keys
        .firstOrNull {
            it.startsWith("restriction")
                && it.substringAfter("restriction:").substringBefore(":conditional") in onlyTurnRestriction
        }
    val newTags = relation.tags.toMutableMap()
    val switchFrom = currentOnly?.let { ":${it.substringAfter("restriction:").substringBefore(":conditional")}" } ?: ""
    if (onlyFor == null) {
        newTags.remove("restriction$switchFrom")?.let { newTags["restriction"] = it }
        newTags.remove("restriction$switchFrom:conditional")?.let { newTags["restriction:conditional"] = it }
    } else {
        newTags.remove("restriction$switchFrom")?.let { newTags["restriction:$onlyFor"] = it }
        newTags.remove("restriction$switchFrom:conditional")?.let { newTags["restriction:$onlyFor:conditional"] = it }
        // if neither key existed, still set restriction:onlyFor from restriction
        if (!newTags.containsKey("restriction:$onlyFor") && !newTags.containsKey("restriction:$onlyFor:conditional")) {
            newTags.remove("restriction")?.let { newTags["restriction:$onlyFor"] = it }
            newTags.remove("restriction:conditional")?.let { newTags["restriction:$onlyFor:conditional"] = it }
        }
    }
    return relation.copy(tags = newTags)
}

fun currentOnlyFor(relation: Relation): String? =
    relation.tags.keys
        .firstOrNull {
            it.startsWith("restriction")
                && it.substringAfter("restriction:").substringBefore(":conditional") in onlyTurnRestriction
        }
        ?.substringAfter("restriction:")
        ?.substringBefore(":conditional")

fun viaBearingForTurnRestriction(
    relation: Relation,
    mapDataSource: MapDataWithEditsSource,
): Pair<LatLon, Double>? {
    val members = relation.members.map { it.role to (mapDataSource.get(it.type, it.ref) ?: return null) }
    val viaMembers = members.filter { it.first == "via" }.map { it.second }
    val from = members.singleOrNull { it.first == "from" }?.second as? Way ?: return null
    val isFirst = viaMembers.any {
        it is Node && it.id == from.nodeIds.first()
            || it is Way && it.nodeIds.firstAndLast().contains(from.nodeIds.first())
    }
    val isLast = viaMembers.any {
        it is Node && it.id == from.nodeIds.last()
            || it is Way && it.nodeIds.firstAndLast().contains(from.nodeIds.last())
    }
    if (isFirst == isLast) return null
    val nodeIdsForBearing = if (isFirst) from.nodeIds.take(2).reversed() else from.nodeIds.takeLast(2)
    val nodesForBearing = nodeIdsForBearing.map { mapDataSource.getNode(it) ?: return null }
    val bearing = nodesForBearing.first().position.finalBearingTo(nodesForBearing.last().position)
    return nodesForBearing.last().position to bearing
}

fun relationGeometry(
    relation: Relation,
    mapDataSource: MapDataWithEditsSource,
): ElementGeometry? {
    if (relation.id != 0L) {
        return mapDataSource.getGeometry(relation.type, relation.id)
    }
    val ways = relation.members.mapNotNull { if (it.type == ElementType.WAY) it.key else null }
    val geometries = mapDataSource.getGeometries(ways)
        .associate {
            it.elementId to (it.geometry as? ElementPolylinesGeometry)?.polylines?.singleOrNull()
        }
        .filterValues { it != null }
        .mapValues { it.value!! }
    if (geometries.isEmpty()) return null
    return ElementGeometryCreator().create(relation, geometries)
}

fun isRelationComplete(relation: Relation, mapDataSource: MapDataWithEditsSource): Boolean =
    relation.members.all { mapDataSource.get(it.type, it.ref) != null }

/** Exception vehicle types historically offered in the multi-choice dialog. */
val turnRestrictionExceptions = listOf(
    "bicycle", "psv", "bus", "emergency", "agricultural", "hgv", "moped", "destination", "motorcar"
)

fun pointGeometry(position: LatLon): ElementPointGeometry = ElementPointGeometry(position)

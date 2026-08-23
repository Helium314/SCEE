package de.westnordost.streetcomplete.overlays.restriction

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType
import de.westnordost.streetcomplete.quests.max_weight.osmKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.containsAnyKey
import de.westnordost.streetcomplete.util.ktx.isArea

class RestrictionOverlay : Overlay {
    // show restriction icons? will need to add property for rotation / angle
    // but according to tangram docs, angle is a number or string, this would need a function...
    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>> {
        val restrictions = mapData.relations.filter { it.tags["type"] == "restriction" }
        val restrictionsByWayMemberId = HashMap<Long, MutableList<Relation>>(restrictions.size)
        restrictions.forEach { restriction ->
            for (member in restriction.members) {
                if (member.type != ElementType.WAY) continue
                val list = restrictionsByWayMemberId.getOrPut(member.ref) { ArrayList(2) }
                list.add(restriction)
            }
        }

        // don't highlight via nodes... or do they matter?
        //  actually the do matter in some cases, e.g. no-u-turn with from and to being the same way
        //  ideally the via nodes would have the correct icon, and then of course need rotation too
        return mapData.filter("ways with highway ~ ${ALL_ROADS.joinToString("|")}")
            .mapNotNull { way -> getWayStyle(way as Way, restrictionsByWayMemberId)?.let { way to it } } +
            mapData.nodes.mapNotNull { node -> getNodeStyle(node)?.let { node to it } }
    }

    @Composable
    override fun Form(on: (OverlayAction) -> Unit, element: Element?, geometry: ElementGeometry, countryInfo: CountryInfo) {
        TODO("Not yet implemented")
    }
//    override fun createForm(element: Element?): AbstractOverlayForm = RestrictionOverlayNodeForm()
//        if (element is Way) RestrictionOverlayWayForm()
//        else RestrictionOverlayNodeForm() // node or null when inserting

    override val changesetComment: String = "Specify traffic restrictions"
    override val icon = Res.drawable.ic_overlay_restriction
    override val title = Res.string.restriction_overlay_title
    override val wikiLink: String = "Relation:restriction"
    override val isCreateNodeEnabled = true

    // todo: better coloring if there are multiple restrictions on the same way
    //  merge any 2 restrictions?
    //  always take a "first" one?
    //  sth else, like dashed way?
    private fun getWayStyle(way: Way, restrictionsByWayMemberId: Map<Long, List<Relation>>): OverlayStyle? {
        // don't allow selecting areas
        if (way.isArea()) return null
        val relations = restrictionsByWayMemberId[way.id]
        if (relations == null) {
            // no turn restriction, but maybe weight
//            val color = if (way.tags.keys.filter { it.startsWith("max") }.any { key -> maxWeightKeys.any { key.startsWith(it) } })
            val color = if (way.tags.containsAnyKey(*maxWeightKeys))
                OverlayColor.Teal
                else OverlayColor.Invisible
            return OverlayStyle.Polyline(OverlayStyle.Stroke(color))
        }

        // merge colors if we have 2 relations on one way
        val color = if (relations.size == 2) {
            val colors = relations.map { it.getColor(way.id) }
            if (colors.first() == colors.last())
                colors.first()
            else
                Color(ColorUtils.blendARGB(colors.first().toArgb(), colors.last().toArgb(), 0.5f))
        } else
            relations.first().getColor(way.id)
        return OverlayStyle.Polyline(OverlayStyle.Stroke(color))
    }

    private fun getNodeStyle(node: Node): OverlayStyle? {
        val highway = node.tags["highway"] ?: return null
        val icon = when (highway) {
            "stop" -> Res.drawable.ic_restriction_stop
            "give_way" -> Res.drawable.ic_restriction_give_way
            else -> return null
        }
        return OverlayStyle.Point(icon)
    }
}

private fun Relation.getColor(wayId: Long): Color {
    if (!isSupportedTurnRestriction()) return OverlayColor.Black
    val role = members.firstOrNull { it.type == ElementType.WAY && it.ref == wayId }?.role ?: return OverlayColor.Invisible
    return getColor(role, getRestrictionType()!!)
    //.replace("#", "#90") // make it transparent for at least some support of multiple relations on a single way
    // nope, unfortunately we can't simply make it transparent here, because MapLibre doesn't understand colors with alpha channel (update: should work in a different format)
}

private fun getColor(role: String, restriction: String): Color = when {
    restriction.startsWith("no_") && role == "from" -> OverlayColor.Orange
    restriction.startsWith("no_") && role == "to" -> darkerOrange
    restriction.startsWith("only_") && role == "from" -> OverlayColor.Gold
    restriction.startsWith("only_") && role == "to" -> darkerGold
    role == "via" -> OverlayColor.Lime
    else -> OverlayColor.Black
}

// support restrictions with 1 from way, 1 to way, 1 via node or 1+ via ways
// and additionally, ways need to be connected (but that is more complicated, and not checked)
// there are some more restrictions which are not supported currently, e.g. no_entry, stop, give_way
fun Relation.isSupportedTurnRestriction(): Boolean {
    if (tags["type"] != "restriction") return false
    if (getRestrictionType() !in turnRestrictionTypes) return false
    if (members.count { it.type == ElementType.WAY && it.role == "from" } != 1) return false
    if (members.count { it.type == ElementType.WAY && it.role == "to" } != 1) return false
    val viaWayCount = members.count { it.type == ElementType.WAY && it.role == "via" }
    val viaNodeCount = members.count { it.type == ElementType.NODE && it.role == "via" }
    if (viaNodeCount > 1) return false
    if (viaNodeCount != 0 && viaWayCount != 0) return false
    return true
}

fun Relation.getRestrictionType() = tags["restriction"] ?: tags["restriction:conditional"]?.substringBefore("@")?.trim()
    ?: tags.entries.firstOrNull { it.key.substringAfter("restriction:").substringBefore(":conditional") in onlyTurnRestrictionSet }?.value?.substringBefore("@")?.trim()

val turnRestrictionTypes = linkedSetOf(
    "no_right_turn",
    "no_left_turn",
    "no_u_turn",
    "no_straight_on",
    "only_right_turn",
    "only_left_turn",
    "only_straight_on",
)

private val maxWeightKeys = MaxWeightType.entries.map { it.osmKey }.toTypedArray()

private val darkerGold = Color(ColorUtils.blendARGB(OverlayColor.Gold.toArgb(), OverlayColor.Black.toArgb(), 0.75f))
private val darkerOrange = Color(ColorUtils.blendARGB(OverlayColor.Orange.toArgb(), OverlayColor.Black.toArgb(), 0.75f))

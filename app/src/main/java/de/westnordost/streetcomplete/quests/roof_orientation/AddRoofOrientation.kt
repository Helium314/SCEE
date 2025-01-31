package de.westnordost.streetcomplete.quests.roof_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.distanceToArc
import de.westnordost.streetcomplete.util.math.measuredLength
import kotlin.math.abs
import kotlin.math.max

class AddRoofOrientation : OsmElementQuestType<String> {

    private val roofsFilter by lazy { """
        ways with
          roof:shape = gabled
          and !roof:orientation
          and !roof:direction
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Add roof orientation"
    override val wikiLink = "Key:roof:orientation"
    override val icon = R.drawable.ic_quest_roof_orientation
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roof

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofOrientation_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.ways.filter { way ->
            if (!roofsFilter.matches(way)) {
                return@filter false
            }

            val points = way.nodeIds.mapNotNull { mapData.getNode(it)?.position }
            if (points.size < 5 || points.first() != points.last()) {
                return@filter false
            }

            return@filter isRectangularOutline(points.dropLast<LatLon>(1))
        }

    override fun isApplicableTo(element: Element) =
        if (roofsFilter.matches(element)) null else false

    override fun createForm() = AddRoofOrientationForm()

    override fun applyAnswerTo(
        answer: String,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:orientation"] = answer
    }
}

private fun isRectangularOutline(points: List<LatLon>): Boolean {
    val rectangle = findAllQuadrangles(points)
        .filter { isNearlyRectangular(it) }
        .maxByOrNull { it.circumference() }

    if (rectangle == null || isNearlySquare(rectangle)) {
        return false
    }

    // Exclude rectangles that differ too much from the whole outline
    if (rectangle.circumference() < points.circumference() * 0.75) {
        return false
    }

    // Check that all other points lie near the rectangle's sides
    val remainingPoints = points.toSet() - rectangle.toSet()
    if (remainingPoints.isEmpty()) {
        return true
    }

    val sides = rectangle.sides()
    return remainingPoints.all { point ->
        sides.any { side -> point.distanceTo(side) < 0.1 * side.length() }
    }
}

/** Returns all 4-point-subsets that could form a rectangle */
private fun findAllQuadrangles(points: List<LatLon>): Sequence<Quadrangle> = sequence {
    val n = points.size
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            for (k in j + 1 until n) {
                for (l in k + 1 until n) {
                    yield(Quadrangle(points[i], points[j], points[k], points[l]))
                }
            }
        }
    }
}

private fun approximatelyEqual(length1: Double, length2: Double, tolerance: Double): Boolean =
    abs(length1 - length2) <= tolerance

/**
 * Returns true if the four corners of the [quadrangle] form a rectangle within an allowed tolerance.
 */
private fun isNearlyRectangular(quadrangle: Quadrangle): Boolean {
    val (sideA, sideB, sideC, sideD) = quadrangle.sideLengths()

    if (
        !approximatelyEqual(sideA, sideC, 0.1 * max(sideB, sideD)) ||
        !approximatelyEqual(sideB, sideD, 0.1 * max(sideA, sideC))
    ) {
        return false
    }

    val diagonal1 = quadrangle.corner0.distanceTo(quadrangle.corner2)
    val diagonal2 = quadrangle.corner1.distanceTo(quadrangle.corner3)

    return approximatelyEqual(diagonal1, diagonal2, 0.1 * max(diagonal1, diagonal2))
}

/**
 * Returns true if the four corners of the [quadrangle] form a square within an allowed tolerance.
 */
private fun isNearlySquare(quadrangle: Quadrangle): Boolean {
    val (sideA, sideB, sideC, sideD) = quadrangle.sideLengths()

    return approximatelyEqual(
        max(sideA, sideC),
        max(sideB, sideD),
        0.1 * maxOf(sideA, sideB, sideC, sideD)
    )
}

private fun List<LatLon>.circumference() = (this + last()).measuredLength()
private fun Pair<LatLon, LatLon>.length() = first.distanceTo(second)
private fun LatLon.distanceTo(arc: Pair<LatLon, LatLon>) = distanceToArc(arc.first, arc.second)

private data class Quadrangle(val corner0: LatLon, val corner1: LatLon, val corner2: LatLon, val corner3: LatLon)
private data class QuadrangleSides(val sideA: Double, val sideB: Double, val sideC: Double, val sideD: Double)

private fun Quadrangle.toSet() = setOf(corner0, corner1, corner2, corner3)
private fun Quadrangle.circumference() = listOf(corner0, corner1, corner2, corner3).circumference()
private fun Quadrangle.sideLengths() = QuadrangleSides(
    corner0.distanceTo(corner1),
    corner1.distanceTo(corner2),
    corner2.distanceTo(corner3),
    corner3.distanceTo(corner0),
)
private fun Quadrangle.sides() = setOf(
    corner0 to corner1,
    corner1 to corner2,
    corner2 to corner3,
    corner3 to corner0,
)

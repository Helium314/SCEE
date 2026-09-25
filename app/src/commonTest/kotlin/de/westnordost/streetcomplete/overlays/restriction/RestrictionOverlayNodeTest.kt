package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.overlays.restriction.RestrictionNodeDirection.BACKWARD
import de.westnordost.streetcomplete.overlays.restriction.RestrictionNodeDirection.FORWARD
import de.westnordost.streetcomplete.overlays.restriction.RestrictionNodeType.ALL_WAY_STOP
import de.westnordost.streetcomplete.overlays.restriction.RestrictionNodeType.GIVE_WAY
import de.westnordost.streetcomplete.overlays.restriction.RestrictionNodeType.STOP
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestrictionOverlayNodeTest {

    @Test fun `parse give_way with optional direction`() {
        assertEquals(GIVE_WAY to null, parseRestrictionNode(mapOf("highway" to "give_way")))
        assertEquals(
            GIVE_WAY to FORWARD,
            parseRestrictionNode(mapOf("highway" to "give_way", "direction" to "forward"))
        )
        assertEquals(
            GIVE_WAY to BACKWARD,
            parseRestrictionNode(mapOf("highway" to "give_way", "direction" to "backward"))
        )
        assertEquals(
            GIVE_WAY to null,
            parseRestrictionNode(mapOf("highway" to "give_way", "direction" to "both"))
        )
    }

    @Test fun `parse stop`() {
        assertEquals(STOP to null, parseRestrictionNode(mapOf("highway" to "stop")))
        assertEquals(
            STOP to FORWARD,
            parseRestrictionNode(mapOf("highway" to "stop", "direction" to "forward"))
        )
    }

    @Test fun `parse all-way stop`() {
        assertEquals(
            ALL_WAY_STOP to null,
            parseRestrictionNode(mapOf("highway" to "stop", "stop" to "all"))
        )
        assertEquals(
            ALL_WAY_STOP to null,
            parseRestrictionNode(mapOf("highway" to "stop", "direction" to "both"))
        )
        assertEquals(
            ALL_WAY_STOP to null,
            parseRestrictionNode(mapOf("highway" to "stop", "stop" to "all", "direction" to "forward"))
        )
    }

    @Test fun `parse unsupported node`() {
        assertEquals(null to null, parseRestrictionNode(emptyMap()))
        assertEquals(null to null, parseRestrictionNode(mapOf("highway" to "traffic_signals")))
    }

    @Test fun `create give_way tags`() {
        assertEquals(
            setOf(StringMapEntryAdd("highway", "give_way")),
            GIVE_WAY.appliedTo(emptyMap())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("highway", "give_way"),
                StringMapEntryAdd("direction", "forward"),
            ),
            GIVE_WAY.appliedTo(emptyMap(), FORWARD)
        )
    }

    @Test fun `create stop tags`() {
        assertEquals(
            setOf(StringMapEntryAdd("highway", "stop")),
            STOP.appliedTo(emptyMap())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("highway", "stop"),
                StringMapEntryAdd("direction", "backward"),
            ),
            STOP.appliedTo(emptyMap(), BACKWARD)
        )
    }

    @Test fun `create all-way stop tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("highway", "stop"),
                StringMapEntryAdd("stop", "all"),
            ),
            ALL_WAY_STOP.appliedTo(emptyMap())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("highway", "stop"),
                StringMapEntryAdd("stop", "all"),
            ),
            ALL_WAY_STOP.appliedTo(emptyMap(), FORWARD)
        )
    }

    @Test fun `edit give_way to stop keeps direction`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "give_way", "stop")),
            STOP.appliedTo(mapOf("highway" to "give_way", "direction" to "forward"), FORWARD)
        )
    }

    @Test fun `edit stop to give_way removes stop tag`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "stop", "give_way"),
                StringMapEntryDelete("stop", "all"),
            ),
            GIVE_WAY.appliedTo(mapOf("highway" to "stop", "stop" to "all"))
        )
    }

    @Test fun `edit stop to all-way stop removes direction`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("direction", "forward"),
                StringMapEntryAdd("stop", "all"),
            ),
            ALL_WAY_STOP.appliedTo(mapOf("highway" to "stop", "direction" to "forward"))
        )
    }

    @Test fun `edit all-way stop to stop does not remove stop=all`() {
        // historical applyTo only removes stop when switching to give_way
        assertEquals(
            emptySet(),
            STOP.appliedTo(mapOf("highway" to "stop", "stop" to "all"))
        )
    }

    @Test fun `resolve stop to all-way stop on multi-way vertex`() {
        assertEquals(ALL_WAY_STOP, resolveRestrictionNodeType(STOP, 2))
        assertEquals(ALL_WAY_STOP, resolveRestrictionNodeType(STOP, 3))
        assertEquals(STOP, resolveRestrictionNodeType(STOP, 1))
        assertEquals(STOP, resolveRestrictionNodeType(STOP, null))
    }

    @Test fun `resolve all-way stop to stop on single way`() {
        assertEquals(STOP, resolveRestrictionNodeType(ALL_WAY_STOP, 1))
        assertEquals(STOP, resolveRestrictionNodeType(ALL_WAY_STOP, null))
        assertEquals(ALL_WAY_STOP, resolveRestrictionNodeType(ALL_WAY_STOP, 2))
    }

    @Test fun `give_way is not auto-converted at intersections`() {
        assertEquals(GIVE_WAY, resolveRestrictionNodeType(GIVE_WAY, 2))
        assertNull(resolveRestrictionNodeType(null, 2))
    }

    @Test fun `give_way cannot be placed on multi-way vertex`() {
        val pos = VertexOfWay(setOf(1L, 2L), LatLon(0.0, 0.0), 10L)
        assertNull(positionOnWayForRestrictionNode(GIVE_WAY, pos, 2, emptyMap()))
        assertEquals(pos, positionOnWayForRestrictionNode(GIVE_WAY, pos, 1, emptyMap()))
        assertEquals(pos, positionOnWayForRestrictionNode(STOP, pos, 2, emptyMap()))
    }

    @Test fun `cannot snap onto existing highway or crossing vertex`() {
        val pos = VertexOfWay(setOf(1L), LatLon(0.0, 0.0), 10L)
        assertNull(positionOnWayForRestrictionNode(STOP, pos, 1, mapOf("highway" to "crossing")))
        assertNull(positionOnWayForRestrictionNode(STOP, pos, 1, mapOf("crossing" to "unmarked")))
        assertEquals(pos, positionOnWayForRestrictionNode(STOP, pos, 1, emptyMap()))
        assertNull(positionOnWayForRestrictionNode(STOP, pos, 1, null))
        val onSegment = PositionOnWaySegment(1L, LatLon(0.0, 0.0), LatLon(0.0, 0.0) to LatLon(0.001, 0.0))
        assertEquals(onSegment, positionOnWayForRestrictionNode(STOP, onSegment, null, mapOf("highway" to "stop")))
    }

    @Test fun `two ways sharing only an end node count as one way`() {
        val way1 = Way(1, listOf(1, 2, 3))
        val way2 = Way(2, listOf(3, 4, 5))
        val roads = listOf(way1 to emptyList<LatLon>(), way2 to emptyList())
        assertEquals(1, wayCountOnVertex(3, roads))
        assertEquals(1, wayCountOnVertex(1, roads))
        assertEquals(2, wayCountOnVertex(3, listOf(
            Way(1, listOf(1, 2, 3, 4)) to emptyList<LatLon>(),
            Way(2, listOf(10, 3, 20)) to emptyList(),
        )))
    }

    @Test fun `direction chooser hidden on oneway roads unless cyclists are excepted`() {
        assertFalse(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "residential", "oneway" to "yes"),
            isLeftHandTraffic = false,
            isMultiWayVertex = false,
        ))
        assertTrue(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "residential", "oneway" to "yes", "oneway:bicycle" to "no"),
            isLeftHandTraffic = false,
            isMultiWayVertex = false,
        ))
        assertTrue(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "residential"),
            isLeftHandTraffic = false,
            isMultiWayVertex = false,
        ))
        assertFalse(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "residential"),
            isLeftHandTraffic = false,
            isMultiWayVertex = true,
        ))
    }

    @Test fun `direction chooser on cycleways ignores bicycle-contraflow exception`() {
        assertFalse(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "cycleway", "oneway" to "yes"),
            isLeftHandTraffic = false,
            isMultiWayVertex = false,
        ))
        assertFalse(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "cycleway", "oneway:bicycle" to "yes"),
            isLeftHandTraffic = false,
            isMultiWayVertex = false,
        ))
        assertTrue(shouldShowRestrictionNodeDirection(
            wayTags = mapOf("highway" to "cycleway"),
            isLeftHandTraffic = false,
            isMultiWayVertex = false,
        ))
    }

    @Test fun `bearing follows the way direction at a node`() {
        val south = LatLon(0.0, 0.0)
        val north = LatLon(0.001, 0.0)
        val nodeIds = listOf(1L, 2L)
        val positions = mapOf(1L to south, 2L to north)

        val atStart = bearingAlongWayAtIndex(south, 0, nodeIds) { positions[it] }
        val atEnd = bearingAlongWayAtIndex(north, 1, nodeIds) { positions[it] }
        assertEquals(0.0, atStart, 1.0)
        assertEquals(0.0, atEnd, 1.0)
    }
}

private fun RestrictionNodeType.appliedTo(
    tags: Map<String, String>,
    direction: RestrictionNodeDirection? = null,
): Set<StringMapEntryChange> =
    StringMapChangesBuilder(tags).also { applyTo(it, direction) }.create().changes

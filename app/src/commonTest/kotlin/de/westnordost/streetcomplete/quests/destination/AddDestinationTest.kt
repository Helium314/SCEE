package de.westnordost.streetcomplete.quests.destination

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.mockPrefs
import de.westnordost.streetcomplete.testutils.mockPrefs3
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddDestinationTest {

    private lateinit var questType: AddDestination

    @BeforeTest fun setUp() {
        Prefs.sharedPreferences = mockPrefs()
        Prefs.preferences = mockPrefs3()
        questType = AddDestination()
    }

    @Test fun `isApplicableTo is false when destination is already tagged`() {
        val way = way(tags = mapOf("highway" to "primary", "destination" to "Berlin"))
        assertEquals(false, questType.isApplicableTo(way))
    }

    @Test fun `isApplicableTo is false when directional destination is already tagged`() {
        val way = way(tags = mapOf("highway" to "primary", "destination:forward" to "Berlin"))
        assertEquals(false, questType.isApplicableTo(way))
    }

    @Test fun `isApplicableTo is false for roads not in the default selection`() {
        val way = way(tags = mapOf("highway" to "residential"))
        assertEquals(false, questType.isApplicableTo(way))
    }

    @Test fun `isApplicableTo is null when surrounding data is needed`() {
        val way = way(tags = mapOf("highway" to "primary"))
        assertNull(questType.isApplicableTo(way))
    }

    @Test fun `isolated road is not applicable`() {
        val south = p(0.5, 0.1)
        val intersection = p(0.5, 0.5)
        val n1 = node(1, south)
        val n2 = node(2, intersection)
        val incoming = way(1, listOf(1, 2), mapOf("highway" to "primary", "oneway" to "yes"))
        val mapData = TestMapDataWithGeometry(listOf(n1, n2, incoming))
        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(south, intersection)), intersection)
        assertTrue(questType.getApplicableElements(mapData).none())
    }

    @Test fun `destination ways after a fork are applicable`() {
        val mapData = tJunctionMapData()
        val applicableIds = questType.getApplicableElements(mapData).map { it.id }.toSet()
        assertEquals(setOf(2L, 3L), applicableIds)
    }

    @Test fun `already tagged destination way is not applicable`() {
        val mapData = tJunctionMapData(
            destEastTags = mapOf("highway" to "primary", "oneway" to "yes", "destination" to "Berlin")
        )
        val applicableIds = questType.getApplicableElements(mapData).map { it.id }.toSet()
        assertEquals(setOf(3L), applicableIds)
    }

    @Test fun `to-member of destination_sign is not applicable`() {
        val mapData = tJunctionMapData()
        mapData.addAll(listOf(
            rel(
                10,
                listOf(
                    member(ElementType.WAY, 2, "to"),
                    member(ElementType.WAY, 1, "from"),
                ),
                mapOf("type" to "destination_sign")
            )
        ))
        val applicableIds = questType.getApplicableElements(mapData).map { it.id }.toSet()
        assertEquals(setOf(3L), applicableIds)
        assertFalse(2L in applicableIds)
    }

    @Test fun `only restriction at the intersection excludes destination ways`() {
        val mapData = tJunctionMapData()
        mapData.addAll(listOf(
            rel(
                11,
                listOf(
                    member(ElementType.WAY, 1, "from"),
                    member(ElementType.NODE, 2, "via"),
                    member(ElementType.WAY, 2, "to"),
                ),
                mapOf("type" to "restriction", "restriction" to "only_straight_on")
            )
        ))
        assertTrue(questType.getApplicableElements(mapData).none())
    }

    @Test fun `applies forward destination on bidirectional road`() {
        assertEquals(
            setOf(StringMapEntryAdd("destination:forward", "Berlin")),
            questType.answerApplied(DestinationLanes(1).add(1, "Berlin") to null)
        )
    }

    @Test fun `applies backward destination on bidirectional road`() {
        assertEquals(
            setOf(StringMapEntryAdd("destination:backward", "Hamburg")),
            questType.answerApplied(null to DestinationLanes(1).add(1, "Hamburg"))
        )
    }

    @Test fun `applies destination on oneway`() {
        assertEquals(
            setOf(StringMapEntryAdd("destination", "Berlin")),
            questType.answerAppliedTo(
                DestinationLanes(1).add(1, "Berlin") to null,
                mapOf("highway" to "primary", "oneway" to "yes")
            )
        )
    }

    @Test fun `applies destination lanes on oneway`() {
        assertEquals(
            setOf(StringMapEntryAdd("destination:lanes", "Berlin|Hamburg")),
            questType.answerAppliedTo(
                DestinationLanes(2).add(1, "Berlin").add(2, "Hamburg") to null,
                mapOf("highway" to "primary", "oneway" to "yes", "lanes" to "2")
            )
        )
    }

    @Test fun `applies destination lanes forward on bidirectional road`() {
        assertEquals(
            setOf(StringMapEntryAdd("destination:lanes:forward", "Berlin|Hamburg")),
            questType.answerAppliedTo(
                DestinationLanes(2).add(1, "Berlin").add(2, "Hamburg") to null,
                mapOf("highway" to "primary")
            )
        )
    }

    @Test fun `applies both directions on bidirectional road`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("destination:forward", "Berlin"),
                StringMapEntryAdd("destination:backward", "Hamburg"),
            ),
            questType.answerApplied(
                DestinationLanes(1).add(1, "Berlin") to DestinationLanes(1).add(1, "Hamburg")
            )
        )
    }

    private fun tJunctionMapData(
        destEastTags: Map<String, String> = mapOf("highway" to "primary", "oneway" to "yes")
    ): TestMapDataWithGeometry {
        val south = p(0.5, 0.1)
        val intersection = p(0.5, 0.5)
        val east = p(0.8, 0.5)
        val west = p(0.2, 0.5)
        val n1 = node(1, south)
        val n2 = node(2, intersection)
        val n3 = node(3, east)
        val n4 = node(4, west)
        val incoming = way(1, listOf(1, 2), mapOf("highway" to "primary", "oneway" to "yes"))
        val destEast = way(2, listOf(2, 3), destEastTags)
        val destWest = way(3, listOf(2, 4), mapOf("highway" to "primary", "oneway" to "yes"))
        val mapData = TestMapDataWithGeometry(listOf(n1, n2, n3, n4, incoming, destEast, destWest))
        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(south, intersection)), intersection)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(intersection, east)), east)
        mapData.wayGeometriesById[3L] = ElementPolylinesGeometry(listOf(listOf(intersection, west)), west)
        return mapData
    }
}

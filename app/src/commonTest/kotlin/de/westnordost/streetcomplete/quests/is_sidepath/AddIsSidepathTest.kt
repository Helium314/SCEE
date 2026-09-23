package de.westnordost.streetcomplete.quests.is_sidepath

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.createMapData
import de.westnordost.streetcomplete.testutils.mockPrefs
import de.westnordost.streetcomplete.testutils.mockPrefs3
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddIsSidepathTest {
    private lateinit var questType: AddIsSidepath

    @BeforeTest
    fun setUp() {
        Prefs.sharedPreferences = mockPrefs()
        Prefs.preferences = mockPrefs3()
        questType = AddIsSidepath()
    }

    // region tagging

    @Test fun `apply no answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("is_sidepath", "no")),
            questType.answerApplied(IsSidepathAnswer.No)
        )
    }

    @Test fun `apply yes with road name`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("is_sidepath", "yes"),
                StringMapEntryAdd("is_sidepath:of:name", "Hauptstraße"),
            ),
            questType.answerApplied(IsSidepathAnswer.Yes(ofName = "Hauptstraße"))
        )
    }

    @Test fun `apply yes without road name`() {
        assertEquals(
            setOf(StringMapEntryAdd("is_sidepath", "yes")),
            questType.answerApplied(IsSidepathAnswer.Yes(ofName = null))
        )
    }

    @Test fun `apply yes with empty road name writes only is_sidepath`() {
        assertEquals(
            setOf(StringMapEntryAdd("is_sidepath", "yes")),
            questType.answerApplied(IsSidepathAnswer.Yes(ofName = ""))
        )
    }

    @Test fun `apply yes never writes is_sidepath of or of ref`() {
        val changes = questType.answerApplied(IsSidepathAnswer.Yes(ofName = "Hauptstraße"))
        assertTrue(changes.none { it.key == "is_sidepath:of" })
        assertTrue(changes.none { it.key == "is_sidepath:of:ref" })
    }

    @Test fun `apply sidewalk answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("footway", "sidewalk")),
            questType.answerAppliedTo(
                IsSidepathAnswer.IsSidewalk,
                mapOf("highway" to "footway")
            )
        )
    }

    @Test fun `apply crossing answer for footway`() {
        assertEquals(
            setOf(StringMapEntryAdd("footway", "crossing")),
            questType.answerAppliedTo(
                IsSidepathAnswer.IsCrossing,
                mapOf("highway" to "footway")
            )
        )
    }

    @Test fun `apply crossing answer for cycleway`() {
        assertEquals(
            setOf(StringMapEntryAdd("cycleway", "crossing")),
            questType.answerAppliedTo(
                IsSidepathAnswer.IsCrossing,
                mapOf("highway" to "cycleway")
            )
        )
    }

    // endregion

    // region isApplicableTo

    @Test fun `isApplicableTo is true for matching path regardless of nearby roads`() {
        val path = way(tags = mapOf("highway" to "footway"))
        assertTrue(questType.isApplicableTo(path))
    }

    @Test fun `isApplicableTo is false for non-matching element`() {
        val road = way(tags = mapOf("highway" to "residential"))
        assertFalse(questType.isApplicableTo(road))
    }

    @Test fun `isApplicableTo is false for already tagged sidepath`() {
        val path = way(tags = mapOf("highway" to "footway", "is_sidepath" to "yes"))
        assertFalse(questType.isApplicableTo(path))
    }

    // endregion

    // region geometry gating

    @Test fun `applicable to parallel footway 5m from residential`() {
        assertEquals(1, applicableCount(pathAndRoad(pathOffsetMeters = 5.0, roadHighway = "residential")))
    }

    @Test fun `applicable to parallel cycleway about 13m from secondary`() {
        assertEquals(
            1,
            applicableCount(
                pathAndRoad(
                    pathOffsetMeters = 13.0,
                    pathTags = mapOf("highway" to "cycleway"),
                    roadHighway = "secondary",
                )
            )
        )
    }

    @Test fun `applicable to parallel path about 19m away`() {
        assertEquals(
            1,
            applicableCount(
                pathAndRoad(
                    pathOffsetMeters = 19.0,
                    pathTags = mapOf("highway" to "path", "foot" to "yes"),
                )
            )
        )
    }

    @Test fun `not applicable when path is more than 20m away`() {
        assertEquals(0, applicableCount(pathAndRoad(pathOffsetMeters = 21.0)))
    }

    @Test fun `not applicable to isolated path`() {
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val path = way(1, listOf(1, 2), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            path to ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable when only nearby service road`() {
        assertEquals(0, applicableCount(pathAndRoad(pathOffsetMeters = 5.0, roadHighway = "service")))
    }

    @Test fun `not applicable when only nearby track`() {
        assertEquals(0, applicableCount(pathAndRoad(pathOffsetMeters = 5.0, roadHighway = "track")))
    }

    @Test fun `not applicable when only nearby pedestrian road`() {
        assertEquals(0, applicableCount(pathAndRoad(pathOffsetMeters = 5.0, roadHighway = "pedestrian")))
    }

    @Test fun `not applicable to perpendicular crossing`() {
        val roadStart = p(0.0, 0.0)
        val roadEnd = roadStart.translate(50.0, 0.0)
        // Path crosses roughly perpendicular near the middle
        val pathStart = roadStart.translate(25.0, 0.0).translate(10.0, 90.0)
        val pathEnd = roadStart.translate(25.0, 0.0).translate(10.0, 270.0)

        val road = way(1, listOf(1, 2), mapOf("highway" to "residential"))
        val path = way(2, listOf(3, 4), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            road to ElementPolylinesGeometry(listOf(listOf(roadStart, roadEnd)), roadStart),
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable when road is tagged in opposite way direction`() {
        val roadStart = p(0.0, 0.0)
        val roadEnd = roadStart.translate(50.0, 45.0)
        val pathStart = roadStart.translate(5.0, 135.0)
        val pathEnd = pathStart.translate(50.0, 45.0)

        val road = way(1, listOf(1, 2), mapOf("highway" to "residential"))
        val path = way(2, listOf(3, 4), mapOf("highway" to "footway"))
        // Road geometry reversed relative to path direction
        val mapData = createMapData(mapOf(
            road to ElementPolylinesGeometry(listOf(listOf(roadEnd, roadStart)), roadEnd),
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to short legitimate parallel sidepath`() {
        val roadStart = p(0.0, 0.0)
        val roadEnd = roadStart.translate(12.0, 45.0)
        val pathStart = roadStart.translate(5.0, 135.0)
        val pathEnd = pathStart.translate(12.0, 45.0)

        val road = way(1, listOf(1, 2), mapOf("highway" to "residential"))
        val path = way(2, listOf(3, 4), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            road to ElementPolylinesGeometry(listOf(listOf(roadStart, roadEnd)), roadStart),
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to curved road and curved sidepath`() {
        val r0 = p(0.0, 0.0)
        val r1 = r0.translate(30.0, 45.0)
        val r2 = r1.translate(30.0, 60.0)
        val p0 = r0.translate(5.0, 135.0)
        val p1 = r1.translate(5.0, 150.0)
        val p2 = r2.translate(5.0, 150.0)

        val road = way(1, listOf(1, 2, 3), mapOf("highway" to "residential"))
        val path = way(2, listOf(4, 5, 6), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            road to ElementPolylinesGeometry(listOf(listOf(r0, r1, r2)), r1),
            path to ElementPolylinesGeometry(listOf(listOf(p0, p1, p2)), p1),
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable with dual carriageway nearby`() {
        val roadStart = p(0.0, 0.0)
        val roadEnd = roadStart.translate(50.0, 45.0)
        val road2Start = roadStart.translate(8.0, 135.0)
        val road2End = road2Start.translate(50.0, 45.0)
        val pathStart = roadStart.translate(4.0, 135.0)
        val pathEnd = pathStart.translate(50.0, 45.0)

        val road1 = way(1, listOf(1, 2), mapOf("highway" to "primary", "name" to "Hauptstraße", "oneway" to "yes"))
        val road2 = way(2, listOf(3, 4), mapOf("highway" to "primary", "name" to "Hauptstraße", "oneway" to "yes"))
        val path = way(3, listOf(5, 6), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            road1 to ElementPolylinesGeometry(listOf(listOf(roadStart, roadEnd)), roadStart),
            road2 to ElementPolylinesGeometry(listOf(listOf(road2Start, road2End)), road2Start),
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `tag exclusions win despite nearby parallel road`() {
        assertEquals(
            0,
            applicableCount(
                pathAndRoad(
                    pathOffsetMeters = 5.0,
                    pathTags = mapOf("highway" to "footway", "footway" to "sidewalk"),
                )
            )
        )
        assertEquals(
            0,
            applicableCount(
                pathAndRoad(
                    pathOffsetMeters = 5.0,
                    pathTags = mapOf("highway" to "footway", "footway" to "crossing"),
                )
            )
        )
        assertEquals(
            0,
            applicableCount(
                pathAndRoad(
                    pathOffsetMeters = 5.0,
                    pathTags = mapOf("highway" to "cycleway", "cycleway" to "link"),
                )
            )
        )
    }

    @Test fun `missing path geometry is not applicable and does not crash`() {
        val roadStart = p(0.0, 0.0)
        val roadEnd = roadStart.translate(50.0, 45.0)
        val road = way(1, listOf(1, 2), mapOf("highway" to "residential"))
        val path = way(2, listOf(3, 4), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            road to ElementPolylinesGeometry(listOf(listOf(roadStart, roadEnd)), roadStart),
            path to null,
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `boundary distance 20m is applicable`() {
        assertEquals(1, applicableCount(pathAndRoad(pathOffsetMeters = 20.0)))
    }

    // endregion

    private fun applicableCount(mapData: Map<de.westnordost.streetcomplete.data.osm.mapdata.Element, de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry?>): Int =
        questType.getApplicableElements(createMapData(mapData)).toList().size

    private fun pathAndRoad(
        pathOffsetMeters: Double,
        pathTags: Map<String, String> = mapOf("highway" to "footway"),
        roadHighway: String = "residential",
        roadLengthMeters: Double = 50.0,
        bearing: Double = 45.0,
    ): Map<de.westnordost.streetcomplete.data.osm.mapdata.Element, de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry?> {
        val roadStart = p(0.0, 0.0)
        val roadEnd = roadStart.translate(roadLengthMeters, bearing)
        val pathStart = roadStart.translate(pathOffsetMeters, bearing + 90.0)
        val pathEnd = pathStart.translate(roadLengthMeters, bearing)

        val road = way(1, listOf(1, 2), mapOf("highway" to roadHighway))
        val path = way(2, listOf(3, 4), pathTags)
        return mapOf(
            road to ElementPolylinesGeometry(listOf(listOf(roadStart, roadEnd)), roadStart),
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
        )
    }
}

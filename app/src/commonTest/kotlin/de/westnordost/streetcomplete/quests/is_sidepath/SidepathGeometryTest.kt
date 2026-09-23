package de.westnordost.streetcomplete.quests.is_sidepath

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.testutils.createMapData
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SidepathGeometryTest {

    @Test fun `alignment diff treats opposite bearings as parallel`() {
        assertEquals(0.0, alignmentDiffDegrees(10.0, 190.0), 1e-9)
        assertEquals(0.0, alignmentDiffDegrees(0.0, 180.0), 1e-9)
        assertEquals(30.0, alignmentDiffDegrees(0.0, 30.0), 1e-9)
        assertEquals(30.0, alignmentDiffDegrees(0.0, 150.0), 1e-9)
    }

    @Test fun `candidates ordered by distance then alignment then id`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(50.0, 45.0)
        val path = way(10, listOf(100, 101), mapOf("highway" to "footway"))

        // farther but better aligned
        val farStart = pathStart.translate(12.0, 135.0)
        val farEnd = farStart.translate(50.0, 45.0)
        val farRoad = way(2, listOf(1, 2), mapOf("highway" to "residential", "name" to "Far"))

        // nearer
        val nearStart = pathStart.translate(5.0, 135.0)
        val nearEnd = nearStart.translate(50.0, 45.0)
        val nearRoad = way(3, listOf(3, 4), mapOf("highway" to "residential", "name" to "Near"))

        // same distance band as near, slightly worse alignment, smaller id than another twin
        val near2Start = pathStart.translate(5.2, 135.0)
        val near2End = near2Start.translate(50.0, 50.0)
        val near2Road = way(1, listOf(5, 6), mapOf("highway" to "residential", "name" to "Nearish"))

        val mapData = createMapData(mapOf(
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
            farRoad to ElementPolylinesGeometry(listOf(listOf(farStart, farEnd)), farStart),
            nearRoad to ElementPolylinesGeometry(listOf(listOf(nearStart, nearEnd)), nearStart),
            near2Road to ElementPolylinesGeometry(listOf(listOf(near2Start, near2End)), near2Start),
        ))

        val candidates = findCandidateRoads(
            mapData.getWayGeometry(path.id)!!,
            mapData,
        )
        assertEquals(listOf(3L, 1L, 2L), candidates.map { it.element.id })
        assertTrue(candidates[0].distance <= candidates[1].distance)
        assertTrue(candidates[1].distance <= candidates[2].distance)
    }

    @Test fun `named and unnamed roads are both returned`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(40.0, 0.0)
        val path = way(1, listOf(1, 2), mapOf("highway" to "footway"))

        val namedStart = pathStart.translate(5.0, 90.0)
        val namedEnd = namedStart.translate(40.0, 0.0)
        val named = way(2, listOf(3, 4), mapOf("highway" to "residential", "name" to "Bahnhofstraße"))

        val unnamedStart = pathStart.translate(8.0, 270.0)
        val unnamedEnd = unnamedStart.translate(40.0, 0.0)
        val unnamed = way(3, listOf(5, 6), mapOf("highway" to "residential"))

        val mapData = createMapData(mapOf(
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
            named to ElementPolylinesGeometry(listOf(listOf(namedStart, namedEnd)), namedStart),
            unnamed to ElementPolylinesGeometry(listOf(listOf(unnamedStart, unnamedEnd)), unnamedStart),
        ))

        val candidates = findCandidateRoads(mapData.getWayGeometry(path.id)!!, mapData)
        assertEquals(2, candidates.size)
        assertEquals("Bahnhofstraße", candidates.first { it.element.id == 2L }.name)
        assertNull(candidates.first { it.element.id == 3L }.name)
    }

    @Test fun `multiple differently named roads all remain candidates`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(40.0, 0.0)
        val path = way(1, listOf(1, 2), mapOf("highway" to "footway"))

        val aStart = pathStart.translate(5.0, 90.0)
        val aEnd = aStart.translate(40.0, 0.0)
        val a = way(2, listOf(3, 4), mapOf("highway" to "residential", "name" to "Hauptstraße"))

        val bStart = pathStart.translate(6.0, 270.0)
        val bEnd = bStart.translate(40.0, 0.0)
        val b = way(3, listOf(5, 6), mapOf("highway" to "secondary", "name" to "Bahnhofstraße"))

        val mapData = createMapData(mapOf(
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
            a to ElementPolylinesGeometry(listOf(listOf(aStart, aEnd)), aStart),
            b to ElementPolylinesGeometry(listOf(listOf(bStart, bEnd)), bStart),
        ))

        val candidates = findCandidateRoads(mapData.getWayGeometry(path.id)!!, mapData)
        assertEquals(setOf("Hauptstraße", "Bahnhofstraße"), candidates.mapNotNull { it.name }.toSet())
    }

    @Test fun `dual carriageway same name keeps both ways as candidates`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(50.0, 45.0)
        val path = way(1, listOf(1, 2), mapOf("highway" to "footway"))

        val r1Start = pathStart.translate(4.0, 135.0)
        val r1End = r1Start.translate(50.0, 45.0)
        val r1 = way(2, listOf(3, 4), mapOf("highway" to "primary", "name" to "Hauptstraße"))

        val r2Start = pathStart.translate(10.0, 135.0)
        val r2End = r2Start.translate(50.0, 45.0)
        val r2 = way(3, listOf(5, 6), mapOf("highway" to "primary", "name" to "Hauptstraße"))

        val mapData = createMapData(mapOf(
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
            r1 to ElementPolylinesGeometry(listOf(listOf(r1Start, r1End)), r1Start),
            r2 to ElementPolylinesGeometry(listOf(listOf(r2Start, r2End)), r2Start),
        ))

        val candidates = findCandidateRoads(mapData.getWayGeometry(path.id)!!, mapData)
        assertEquals(2, candidates.size)
        assertTrue(candidates.all { it.name == "Hauptstraße" })
    }

    @Test fun `no candidates when nothing nearby`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(40.0, 0.0)
        val path = way(1, listOf(1, 2), mapOf("highway" to "footway"))
        val mapData = createMapData(mapOf(
            path to ElementPolylinesGeometry(listOf(listOf(pathStart, pathEnd)), pathStart),
        ))
        assertEquals(emptyList(), findCandidateRoads(mapData.getWayGeometry(path.id)!!, mapData))
    }

    @Test fun `alignment boundary of 30 degrees is accepted`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(40.0, 0.0)
        val roadStart = pathStart.translate(5.0, 90.0)
        val roadEnd = roadStart.translate(40.0, 30.0)

        val match = assertNotNull(
            bestNearAndAlignedPair(
                listOf(pathStart, pathEnd),
                listOf(roadStart, roadEnd),
                MAX_SIDEPATH_DISTANCE_METERS,
                MAX_SIDEPATH_ALIGNMENT_DEGREES,
            )
        )
        assertTrue(abs(match.second - 30.0) < 1.0)
    }

    @Test fun `alignment beyond 30 degrees is rejected`() {
        val pathStart = p(0.0, 0.0)
        val pathEnd = pathStart.translate(40.0, 0.0)
        val roadStart = pathStart.translate(5.0, 90.0)
        val roadEnd = roadStart.translate(40.0, 40.0)

        assertNull(
            bestNearAndAlignedPair(
                listOf(pathStart, pathEnd),
                listOf(roadStart, roadEnd),
                MAX_SIDEPATH_DISTANCE_METERS,
                MAX_SIDEPATH_ALIGNMENT_DEGREES,
            )
        )
    }
}

package de.westnordost.streetcomplete.quests.destination

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.oneway.isOneway
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DestinationLanesTest {

    @Test fun `rejects non-positive lane count`() {
        assertFailsWith<IllegalArgumentException> { DestinationLanes(0) }
    }

    @Test fun `applies destination on oneway`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary", "oneway" to "yes"))
        DestinationLanes(1).add(1, "Berlin").applyTo(tags, false)
        assertEquals("Berlin", tags["destination"])
        assertNull(tags["destination:forward"])
    }

    @Test fun `applies multiple destinations on oneway`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary", "oneway" to "yes"))
        DestinationLanes(1).add(1, "Berlin").add(1, "Hamburg").applyTo(tags, false)
        assertEquals("Berlin;Hamburg", tags["destination"])
    }

    @Test fun `applies destination forward on bidirectional road`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary"))
        DestinationLanes(1).add(1, "Berlin").applyTo(tags, false)
        assertEquals("Berlin", tags["destination:forward"])
        assertNull(tags["destination"])
    }

    @Test fun `applies destination backward on bidirectional road`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary"))
        DestinationLanes(1).add(1, "Hamburg").applyTo(tags, true)
        assertEquals("Hamburg", tags["destination:backward"])
    }

    @Test fun `applies destination lanes on oneway`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary", "oneway" to "yes"))
        DestinationLanes(2)
            .add(1, "Berlin")
            .add(2, "Hamburg")
            .add(2, "Kiel")
            .applyTo(tags, false)
        assertEquals("Berlin|Hamburg;Kiel", tags["destination:lanes"])
        assertNull(tags["destination"])
    }

    @Test fun `applies destination lanes forward on bidirectional road`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary"))
        DestinationLanes(2)
            .add(1, "A")
            .add(2, "B")
            .applyTo(tags, false)
        assertEquals("A|B", tags["destination:lanes:forward"])
    }

    @Test fun `does not apply incomplete lanes`() {
        val tags = StringMapChangesBuilder(mapOf("highway" to "primary", "oneway" to "yes"))
        assertFailsWith<IllegalStateException> {
            DestinationLanes(2).add(1, "Berlin").applyTo(tags, false)
        }
    }

    @Test fun `isComplete and isCompleteExcept`() {
        val lanes = DestinationLanes(2).add(1, "Berlin")
        assertFalse(lanes.isComplete)
        assertTrue(lanes.isCompleteExcept(2))
        assertFalse(lanes.isCompleteExcept(1))
        assertTrue(lanes.add(2, "Hamburg").isComplete)
    }

    @Test fun `laneCountInDirection uses directional tags then half of lanes`() {
        assertEquals(1, laneCountInDirection(mapOf("oneway" to "yes"), false))
        assertEquals(3, laneCountInDirection(mapOf("oneway" to "yes", "lanes" to "3"), false))
        assertEquals(2, laneCountInDirection(mapOf("lanes:forward" to "2"), false))
        assertEquals(3, laneCountInDirection(mapOf("lanes:backward" to "3"), true))
        assertEquals(1, laneCountInDirection(mapOf("lanes" to "2"), false))
        assertEquals(1, laneCountInDirection(emptyMap(), false))
        assertFalse(isOneway(mapOf("highway" to "primary")))
    }
}

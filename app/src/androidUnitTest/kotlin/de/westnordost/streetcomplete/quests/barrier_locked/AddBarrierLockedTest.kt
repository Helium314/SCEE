package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddBarrierLockedTest {

    private val questType = AddBarrierLocked()

    @Test
    fun `no quest for barrier node with one restricted and one unrestricted connected way`() {
        val barrierNode = node(
            1,
            tags = mapOf(
                "barrier" to "gate",
                "locked" to "yes"
            ),
        )

        val privateWay = way(
            1,
            listOf(1, 2),
            mapOf(
                "highway" to "service",
                "access" to "private",
            ),
        )

        val publicWay = way(
            2,
            listOf(1, 3),
            mapOf(
                "highway" to "service",
            ),
        )

        val mapData = TestMapDataWithGeometry(
            listOf(
                barrierNode,
                node(2),
                node(3),
                privateWay,
                publicWay,
            ),
        )

        val applicable = questType.getApplicableElements(mapData).toList()
        assertEquals(emptyList(), applicable)
    }
}

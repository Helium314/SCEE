package de.westnordost.streetcomplete.quests.kerb_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AddKerbTypeTest {

    private val questType = AddKerbType()

    @Test fun `applicable to barrier kerb ways without kerb key`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(tags = mapOf("barrier" to "kerb"))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to barrier kerb ways with kerb key already`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(tags = mapOf(
                "barrier" to "kerb",
                "kerb" to "raised"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `apply regular kerb answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("kerb", "yes")),
            questType.answerApplied(KerbType.REGULAR)
        )
    }

    @Test fun `disabled by default in SCEE`() {
        assertNotNull(questType.defaultDisabledMessage)
    }
}

package de.westnordost.streetcomplete.quests.kerb_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
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

    @Test fun `apply no kerb answer removes barrier and adds no-barrier tag`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(tags = mapOf("barrier" to "kerb"))
        ))
        val element = mapData.ways.first()
        val changes = StringMapChangesBuilder(element.tags).apply {
            questType.applyAnswerTo(KerbType.NO_KERB, this, mapData.getGeometry(element.type, element.id)!!, 0)
        }.create().changes

        assertEquals(
            setOf(
                StringMapEntryAdd("kerb", "no"),
                StringMapEntryDelete("barrier", "kerb"),
                StringMapEntryAdd("no:barrier", "kerb")
            ),
            changes
        )
    }

    @Test fun `disabled by default in SCEE`() {
        assertNotNull(questType.defaultDisabledMessage)
    }
}

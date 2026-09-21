package de.westnordost.streetcomplete.quests.crossing_markings

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mockPrefs
import de.westnordost.streetcomplete.testutils.node
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddCrossingMarkingsTest {

    @Test fun `extended filter is not applicable to unmarked crossings`() {
        val questType = questType(extended = true)
        val crossing = node(tags = mapOf(
            "highway" to "crossing",
            "crossing" to "unmarked"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(crossing))
    }

    @Test fun `extended filter is applicable to marked crossings without marking type`() {
        val questType = questType(extended = true)
        val crossing = node(tags = mapOf(
            "highway" to "crossing",
            "crossing" to "marked"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `apply no markings replaces crossing marked with unmarked`() {
        val questType = questType()
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:markings", "no"),
                StringMapEntryModify("crossing", "marked", "unmarked")
            ),
            questType.answerAppliedTo(
                setOf(CrossingMarkings.NO),
                mapOf("highway" to "crossing", "crossing" to "marked")
            )
        )
    }

    @Test fun `apply no markings replaces crossing uncontrolled with unmarked`() {
        val questType = questType()
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:markings", "no"),
                StringMapEntryModify("crossing", "uncontrolled", "unmarked")
            ),
            questType.answerAppliedTo(
                setOf(CrossingMarkings.NO),
                mapOf("highway" to "crossing", "crossing" to "uncontrolled")
            )
        )
    }
}

private fun questType(extended: Boolean = false): AddCrossingMarkings {
    val settings: ObservableSettings = mock()
    every { settings.getBoolean(any(), true) } returns true
    every { settings.getBoolean(any(), false) } returns false
    every { settings.getBoolean("qs_AddCrossingMarkings_extended", false) } returns extended
    Prefs.sharedPreferences = mockPrefs()
    Prefs.preferences = Preferences(settings)
    return AddCrossingMarkings()
}

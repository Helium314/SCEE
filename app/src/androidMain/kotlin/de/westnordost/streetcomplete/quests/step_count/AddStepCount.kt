package de.westnordost.streetcomplete.quests.step_count

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.NumberSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.util.math.measuredLength

class AddStepCount : OsmElementQuestType<Int>, AndroidQuest {

    val elementFilter by lazy { """
        nodes, ways with
        (
          (
            highway = steps
            and (!indoor or indoor = no)
            and (!conveying or conveying = no)
          )
          or man_made = tower and access ~ yes|customers and tower:type ~ observation|watchtower
        )
        and access !~ private|no
        and !step_count
    """.toElementFilterExpression() }
    override val changesetComment = "Specify step counts"
    override val wikiLink = "Key:step_count"
    override val icon = R.drawable.quest_steps_count
    override val title = Res.string.quest_step_count_title
    // because the user needs to start counting at the start of the steps
    override val hasMarkersAtEnds = true
    override val achievements = listOf(PEDESTRIAN)

    override fun isApplicableTo(element: Element): Boolean? {
        if (!elementFilter.matches(element)) return false
        return null
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.filter { element ->
            if (!elementFilter.matches(element)) return@filter false
            val geometry = mapData.getWayGeometry(element.id) as? ElementPolylinesGeometry
            val totalLength = geometry?.polylines?.sumOf { it.measuredLength() } ?: return@filter true
            totalLength <= prefs.getInt(questPrefix(prefs) + PREF_MAX_STEPS_LENGTH, 999)
        }
    }

    override fun createForm() = AddStepCountForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["step_count"] = answer.toString()
    }

    override val hasQuestSettings = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        NumberSelectionDialog(prefs, questPrefix(prefs) + PREF_MAX_STEPS_LENGTH, 999, R.string.quest_settings_max_steps_length, onDismissRequest)
    }
}

private const val PREF_MAX_STEPS_LENGTH = "qs_AddStepCount_max_length"

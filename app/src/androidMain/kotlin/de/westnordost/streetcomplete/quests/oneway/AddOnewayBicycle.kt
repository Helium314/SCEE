package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee

class AddOnewayBicycle :
    OsmElementQuestType<OnewayAnswer>,
    AndroidQuest {

    /** default element selection (user editable via settings) */
    private val elementFilter = """
    ways with
      (
        highway = cycleway
        or (highway ~ path|footway and bicycle ~ yes|designated)
      )
      and !oneway
      and !oneway:bicycle
      and area != yes
      and junction != roundabout
      and access !~ private|no
""".trimIndent()

    private val filter by lazy {
        prefs
            .getString(getPrefixedFullElementSelectionPref(prefs), elementFilter)
            .toElementFilterExpression()
    }

    override val changesetComment = "Specify whether bicycle ways are one-ways"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.quest_bicycleway_oneway
    override val hasMarkersAtEnds = true
    override val achievements = listOf(EditTypeAchievement.BICYCLIST)
    override val hint = R.string.quest_arrow_tutorial
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_onewayBicycle_title

    override val hasQuestSettings: Boolean = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        FullElementSelectionDialog(
            prefs,
            getPrefixedFullElementSelectionPref(prefs),
            R.string.quest_settings_element_selection,
            elementFilter,
            onDismissRequest
        )
    }

    override fun getApplicableElements(
        mapData: MapDataWithGeometry
    ): Iterable<Element> =
        mapData.ways.filter { filter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? =
        if (filter.matches(element)) null else false

    override fun createForm() = AddOnewayForm()

    override fun applyAnswerTo(
        answer: OnewayAnswer,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        val key =
            if (tags["highway"] == "cycleway") "oneway"
            else "oneway:bicycle"

        tags[key] = when (answer) {
            FORWARD -> "yes"
            BACKWARD -> "-1"
            NO_ONEWAY -> "no"
        }
    }
}

package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
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
        (highway = cycleway and !oneway)
        or
        (
          (highway = path or highway = footway)
          and bicycle ~ yes|designated
          and !oneway
          and !oneway:bicycle
        )
      )
      and area != yes
      and junction != roundabout
      and access !~ private|no
""".trimIndent()

    private val filter by lazy {
        prefs
            .getString(getPrefixedFullElementSelectionPref(prefs), elementFilter)
            .toElementFilterExpression()
    }

    /** broader filter for connectivity calculation */
    private val allBikeWaysFilter by lazy {
        """
            ways with
              (
                highway = cycleway
                or (highway = path and bicycle ~ yes|designated)
                or (highway = footway and bicycle ~ yes|designated)
              )
              and area != yes
        """.trimIndent().toElementFilterExpression()
    }

    override val changesetComment = "Specify whether bicycle ways are one-ways"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.quest_bicycleway_oneway
    override val hasMarkersAtEnds = true
    override val achievements = listOf(EditTypeAchievement.BICYCLIST)
    override val hint = R.string.quest_arrow_tutorial
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override fun getTitle(tags: Map<String, String>) = R.string.quest_onewayBicycle_title

    /* ---------- settings ---------- */

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

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val bikeWays = mapData.ways.filter {
            allBikeWaysFilter.matches(it) && it.nodeIds.size >= 2
        }

        val connectionCountByNodeIds = mutableMapOf<Long, Int>()
        val onewayCandidates = mutableListOf<Way>()

        for (way in bikeWays) {
            for (nodeId in way.nodeIds) {
                connectionCountByNodeIds[nodeId] =
                    (connectionCountByNodeIds[nodeId] ?: 0) + 1
            }
            if (filter.matches(way)) {
                onewayCandidates.add(way)
            }
        }

        // bicycle infrastructure: one connected end is sufficient
        return onewayCandidates.filter { way ->
            val firstConnected =
                (connectionCountByNodeIds[way.nodeIds.first()] ?: 0) > 1
            val lastConnected =
                (connectionCountByNodeIds[way.nodeIds.last()] ?: 0) > 1
            firstConnected || lastConnected
        }
    }

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

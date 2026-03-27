package de.westnordost.streetcomplete.quests.valves

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_valves_title

class AddValves : OsmFilterQuestType<Set<Valves>>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          (compressed_air = yes
          or service:bicycle:pump = yes
          or amenity = compressed_air)
          and access !~ private|no
          and !valves
    """
    override val changesetComment = "Specify valves types for air pumps or compressed air"
    override val wikiLink = "Key:valves"
    override val icon = R.drawable.ic_quest_valve
    override val title = Res.string.quest_valves_title
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun createForm() = AddValvesForm()

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = compressed_air or service:bicycle:pump = yes or compressed_air = yes")

    override fun applyAnswerTo(answer: Set<Valves>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["valves"] = answer.joinToString(";") { it.osmValue }
    }
}

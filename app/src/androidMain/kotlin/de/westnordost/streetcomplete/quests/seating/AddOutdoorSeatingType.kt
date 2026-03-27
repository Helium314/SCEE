package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_seasonal
import de.westnordost.streetcomplete.resources.quest_outdoor_seating_name_title

class AddOutdoorSeatingType : OsmFilterQuestType<String>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          outdoor_seating = yes
    """
    override val changesetComment = "Add outdoor seating info"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_seasonal
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.ic_quest_seating_type
    override val title = Res.string.quest_outdoor_seating_name_title
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    override fun createForm() = AddOutdoorSeatingTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["outdoor_seating"] = answer
    }
}


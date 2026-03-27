package de.westnordost.streetcomplete.quests.building_colour

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_buildingColour_title
import de.westnordost.streetcomplete.resources.quest_buildingPartColour_title

class AddBuildingColour : OsmFilterQuestType<BuildingColour>(), AndroidQuest {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction|roof|carport)
          or (building:part and building:part !~ no|construction|roof|carport))
          and !building:colour
          and (!indoor or indoor = no)
          and wall !~ no
          and location != underground
    """
    override val changesetComment = "Specify building colour"
    override val wikiLink = "Key:building:colour"
    override val title = Res.string.quest_buildingColour_title
    override val icon = R.drawable.ic_quest_building_colour
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> Res.string.quest_buildingPartColour_title
        else -> Res.string.quest_buildingColour_title
    }

    override fun createForm() = AddBuildingColourForm()

    override fun applyAnswerTo(
        answer: BuildingColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:colour"] = answer.osmValue
    }
}


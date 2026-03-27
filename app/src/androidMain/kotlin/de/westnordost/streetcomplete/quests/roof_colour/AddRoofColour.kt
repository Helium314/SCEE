package de.westnordost.streetcomplete.quests.roof_colour

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_roof
import de.westnordost.streetcomplete.resources.quest_roofColour_title

class AddRoofColour : OsmFilterQuestType<RoofColour>(), AndroidQuest {

    override val elementFilter = """
        ways, relations with
          roof:shape
          and roof:shape != flat
          and !roof:colour
          and building
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """
    override val changesetComment = "Specify roof colour"
    override val wikiLink = "Key:roof:colour"
    override val icon = R.drawable.ic_quest_roof_colour
    override val title = Res.string.quest_roofColour_title
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_roof

    override fun createForm() = AddRoofColourForm()

    override fun applyAnswerTo(
        answer: RoofColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:colour"] = answer.osmValue
    }
}


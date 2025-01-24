package de.westnordost.streetcomplete.quests.roof_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddRoofOrientation : OsmFilterQuestType<String>() {

    override val elementFilter = """
        ways with
          roof:shape = gabled
          and !roof:orientation
          and !roof:direction
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """
    override val changesetComment = "Add roof orientation"
    override val wikiLink = "Key:roof:orientation"
    override val icon = R.drawable.ic_quest_roof_orientation
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roof

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofOrientation_title

    override fun createForm() = AddRoofOrientationForm()

    override fun applyAnswerTo(
        answer: String,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:orientation"] = answer
    }
}

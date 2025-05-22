package de.westnordost.streetcomplete.quests.roof_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddRoofMaterial : OsmFilterQuestType<RoofMaterial>() {

    override val elementFilter = """
        ways, relations with
          roof:shape
          and !roof:material
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """
    override val changesetComment = "Specify roof material"
    override val wikiLink = "Key:roof:material"
    override val icon = R.drawable.ic_quest_roof_material
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roofMaterial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofMaterial_title

    override fun createForm() = AddRoofMaterialForm()

    override fun applyAnswerTo(
        answer: RoofMaterial,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:material"] = answer.osmValue
    }
}

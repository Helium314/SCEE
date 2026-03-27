package de.westnordost.streetcomplete.quests.building_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_difficult_and_time_consuming
import de.westnordost.streetcomplete.resources.quest_buildingMaterial_title
import de.westnordost.streetcomplete.resources.quest_buildingPartMaterial_title

class AddBuildingMaterial : OsmFilterQuestType<BuildingMaterial>(), AndroidQuest {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction|roof|carport)
          or (building:part and building:part !~ no|construction|roof|carport))
          and !building:material
          and indoor != no
          and wall != no
    """
    override val changesetComment = "Specify building material"
    override val wikiLink = "Key:building:material"
    override val icon = R.drawable.ic_quest_building_material
    override val title = Res.string.quest_buildingMaterial_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> Res.string.quest_buildingPartMaterial_title
        else -> Res.string.quest_buildingMaterial_title
    }

    override fun createForm() = AddBuildingMaterialForm()

    override fun applyAnswerTo(
        answer: BuildingMaterial,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:material"] = answer.osmValue
    }
}

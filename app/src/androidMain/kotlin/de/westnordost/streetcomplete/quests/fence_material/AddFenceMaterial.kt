package de.westnordost.streetcomplete.quests.fence_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_fence_material_title

class AddFenceMaterial : OsmFilterQuestType<FenceMaterial>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with
          barrier = fence
          and !material
          and fence_type != wire
    """

    override val changesetComment = "Specify fence material"
    override val wikiLink = "Key:material"
    override val title = Res.string.quest_fence_material_title
    override val icon = R.drawable.ic_quest_fence_material

    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun createForm() = AddFenceMaterialForm()

    override fun applyAnswerTo(
        answer: FenceMaterial,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        answer.materialValue?.let { tags["material"] = it }
        answer.fenceTypeValue?.let { tags["fence_type"] = it }
    }
}

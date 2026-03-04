package de.westnordost.streetcomplete.quests.paving_stones_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_difficult_and_time_consuming

class AddPavingStonesMaterial : OsmFilterQuestType<PavingStonesMaterialAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
          surface=paving_stones
          and !paving_stones:material
    """
    override val changesetComment = "Specify paving stones material"
    override val wikiLink = "Key:paving_stones:material"
    override val icon = R.drawable.quest_paving_stones_material

    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_pavingStonesMaterial_title

    override fun createForm() = AddPavingStonesMaterialForm()

    override fun applyAnswerTo(
        answer: PavingStonesMaterialAnswer,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        when (answer) {
            is PavingStonesMaterial -> tags["paving_stones:material"] = answer.osmValue
            SurfaceIsNotPavingStones -> {
                tags.remove("surface")
                tags.remove("paving_stones:material")
            }
        }
    }
}

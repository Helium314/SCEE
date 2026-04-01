package de.westnordost.streetcomplete.quests.toilets_disposal

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_toilets_disposal_title

class AddToiletsDisposal : OsmFilterQuestType<ToiletsDisposalType>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          amenity = toilets
          and !toilets:disposal
          and (!seasonal or seasonal = no)
          and (!fee or fee = no)
    """

    override val changesetComment = "Add toilets disposal type"
    override val wikiLink = "Key:toilets:disposal"
    override val icon = R.drawable.quest_toilets_disposal
    override val title = Res.string.quest_toilets_disposal_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val achievements = listOf(CITIZEN)

    override fun createForm() = AddToiletsDisposalForm()

    override fun applyAnswerTo(
        answer: ToiletsDisposalType,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        tags["toilets:disposal"] = answer.osmValue
    }
}

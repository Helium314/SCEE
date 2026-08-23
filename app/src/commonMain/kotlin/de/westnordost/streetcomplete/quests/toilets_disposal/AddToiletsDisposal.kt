package de.westnordost.streetcomplete.quests.toilets_disposal

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddToiletsDisposal : OsmFilterQuestType<ToiletsDisposalType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = toilets
          and !toilets:disposal
          and (!seasonal or seasonal = no)
          and (!fee or fee = no)
    """

    override val changesetComment = "Add toilets disposal type"
    override val wikiLink = "Key:toilets:disposal"
    override val icon = Res.drawable.quest_toilets_disposal
    override val title = Res.string.quest_toilets_disposal_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<ToiletsDisposalType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on,
            answers = ToiletsDisposalType.entries.map { AnswerItem(stringResource(it.title)) { on(Answer(it)) } }
        )
    }

    override fun applyAnswerTo(
        answer: ToiletsDisposalType,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        tags["toilets:disposal"] = answer.osmValue
    }
}

package de.westnordost.streetcomplete.quests.guidepost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.TextInputForm
import org.jetbrains.compose.resources.stringResource

class AddGuidepostName : OsmFilterQuestType<GuidepostNameAnswer>() {

    override val elementFilter = """
        nodes with
        (information = guidepost or guidepost) and guidepost != simple
        and !name and noname != yes and !~"name:.*"
        and hiking = yes
    """
    override val changesetComment = "Specify guidepost name"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = Res.drawable.ic_quest_guidepost_name
    override val title = Res.string.quest_guidepostName_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with information ~ guidepost|map")


    override val defaultDisabledMessage = Res.string.quest_guidepost_disabled_msg

    override val highlightedElementsRadius: Double get() = 200.0

    @Composable
    override fun Form(on: (QuestAction<GuidepostNameAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        var confirmNoName by remember { mutableStateOf(false) }
        TextInputForm(
            on,
            stringToAnswer = { GuidepostName(it) },
            otherAnswers = {
                listOf(AnswerItem(stringResource(Res.string.quest_placeName_no_name_answer)) { confirmNoName = true })
            }
        )
        AreYouSureDialog(
            onDismissRequest = { confirmNoName = false },
            onConfirmed = { on(Answer(NoVisibleGuidepostName)) },
            titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
            confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
        )
    }

    override fun applyAnswerTo(answer: GuidepostNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleGuidepostName -> tags["name:signed"] = "no"
            is GuidepostName ->          tags["name"] = answer.name
        }
    }
}

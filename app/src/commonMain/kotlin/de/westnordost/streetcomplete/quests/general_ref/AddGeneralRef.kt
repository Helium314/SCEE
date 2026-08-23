package de.westnordost.streetcomplete.quests.general_ref

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

class AddGeneralRef : OsmFilterQuestType<GeneralRefAnswer>() {

    override val elementFilter = """
        nodes, ways with
          (
            (information = guidepost or guidepost) and guidepost != simple and hiking = yes
            or railway = subway_entrance and highway != elevator
            or building ~ service|transformer_tower and power = substation
            or man_made = street_cabinet
            or highway = street_lamp
            or golf = hole
          )
          and !ref
          and noref != yes
          and ref:signed != no
          and !~"ref:.*"
    """
    override val changesetComment = "Specify refs"
    override val wikiLink = "Key:ref"
    override val icon = Res.drawable.ic_quest_general_ref
    override val title = Res.string.quest_genericRef_title
    override val achievements = listOf(OUTDOORS)

    // substation buildings are not highlighted because those are usually far apart
    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes with
              information ~ guidepost|map
              or railway = subway_entrance
              or man_made = street_cabinet
              or highway = street_lamp
        """)
    override val highlightedElementsRadius: Double get() = 200.0

    override val defaultDisabledMessage = Res.string.quest_generalRef_disabled_msg

    @Composable
    override fun Form(on: (QuestAction<GeneralRefAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        var confirmNoName by remember { mutableStateOf(false) }
        TextInputForm(
            on,
            stringToAnswer = { GeneralRef(it) },
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_ref_answer_noRef)) { confirmNoName = true }) },
            hintText = if (element.tags.containsKey("guidepost") || element.tags["information"] == "guidepost")
                stringResource(Res.string.quest_guidepostRef_hint)
            else
                stringResource(Res.string.quest_generalRef_hint)
        )
        if (confirmNoName)
            AreYouSureDialog(
                onDismissRequest = { confirmNoName = false },
                onConfirmed = { on(Answer(NoVisibleGeneralRef)) },
                titleText = stringResource(Res.string.quest_generic_confirmation_title),
                confirmButtonText = stringResource(Res.string.quest_generic_confirmation_yes),
            )
    }

    override fun applyAnswerTo(answer: GeneralRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleGeneralRef -> tags["ref:signed"] = "no"
            is GeneralRef ->          tags["ref"] = answer.ref
        }
    }
}

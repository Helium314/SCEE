package de.westnordost.streetcomplete.quests.tree

import androidx.compose.runtime.Composable
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
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddTreeDenotation : OsmFilterQuestType<TreeDenotationAnswer>() {
    override val elementFilter = "nodes with natural = tree and !denotation"
    override val changesetComment = "Specify tree denotation"
    override val wikiLink = "Key:denotation"
    override val icon = Res.drawable.quest_tree_denotation
    override val title = Res.string.quest_tree_denotation_title
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee


    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with natural = tree")

    @Composable
    override fun Form(on: (QuestAction<TreeDenotationAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            on,
            items = TreeDenotation.entries,
            itemContent = {
                ImageWithDescription(
                    painter = null,
                    title = stringResource(it.title),
                    description = stringResource(it.description),
                )
            },
            otherAnswers = {
                listOf(AnswerItem(stringResource(Res.string.quest_leafType_tree_is_just_a_stump)) { on(Answer(NotTreeButStump))})
            }
        )
    }

    override fun applyAnswerTo(answer: TreeDenotationAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is TreeDenotation -> tags["denotation"] = answer.osmValue
            NotTreeButStump -> tags["natural"] = "tree_stump"
        }
    }
}

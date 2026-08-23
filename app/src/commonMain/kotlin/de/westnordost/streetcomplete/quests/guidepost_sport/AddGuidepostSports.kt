package de.westnordost.streetcomplete.quests.guidepost_sport

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddGuidepostSports : OsmFilterQuestType<Set<GuidepostSportsAnswer>>() {

    override val elementFilter =
        """
        nodes with
          tourism = information
          and information ~ guidepost|route_marker
          and !hiking and !bicycle and !mtb and !climbing and !horse and !nordic_walking and !ski and !inline_skates and !running
          and !disused
          and !guidepost
    """

    override val changesetComment = "Specify what kind of guidepost"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = Res.drawable.ic_quest_guidepost_sport
    override val title = Res.string.quest_guidepost_sports_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val hint = Res.string.quest_guidepost_sports_note

    @Composable
    override fun Form(on: (QuestAction<Set<GuidepostSportsAnswer>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemsSelectQuestForm(
            on = on,
            items = GuidepostSport.entries,
            itemContent = { ImageWithLabel(painterResource(it.iconResId), stringResource(it.titleResId)) },
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_guidepost_sports_answer_simple_description)) {
                on(Answer(setOf(IsSimpleGuidepost))) })
            }
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with tourism = information and information ~ guidepost|route_marker")

    override fun applyAnswerTo(answer: Set<GuidepostSportsAnswer>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.forEach {
            if (it is IsSimpleGuidepost) {
                applySimpleGuidepostAnswer(tags)
            } else if (it is GuidepostSport) {
                tags[it.key] = "yes"
            }
        }
    }

    private fun applySimpleGuidepostAnswer(tags: Tags) {
        tags["guidepost"] = "simple"
    }
}

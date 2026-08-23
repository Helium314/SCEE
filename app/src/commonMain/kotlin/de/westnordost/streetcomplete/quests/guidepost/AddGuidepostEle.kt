package de.westnordost.streetcomplete.quests.guidepost

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_quest_guidepost_ele
import de.westnordost.streetcomplete.resources.quest_guidepostEle_hint
import de.westnordost.streetcomplete.resources.quest_guidepostEle_title
import de.westnordost.streetcomplete.resources.quest_guidepost_disabled_msg
import de.westnordost.streetcomplete.ui.common.quest.TextInputForm

class AddGuidepostEle : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes with
        (information = guidepost or guidepost) and guidepost != simple
        and !ele and !~"ele:.*"
        and hiking = yes
    """
    override val changesetComment = "Specify guidepost elevation"
    override val wikiLink = "Tag:information=guidepost"
    override val hint = Res.string.quest_guidepostEle_hint
    override val icon = Res.drawable.ic_quest_guidepost_ele
    override val title = Res.string.quest_guidepostEle_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with information = guidepost")

    override val highlightedElementsRadius: Double get() = 200.0
    override val defaultDisabledMessage = Res.string.quest_guidepost_disabled_msg

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        TextInputForm(on, keyboardType = KeyboardType.Decimal)
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["ele"] = answer
    }
}

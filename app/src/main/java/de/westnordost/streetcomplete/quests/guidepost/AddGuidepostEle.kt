package de.westnordost.streetcomplete.quests.guidepost

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddGuidepostEle : OsmFilterQuestType<GuidepostEleAnswer>() {

    override val elementFilter = """
        nodes with
        information ~ guidepost|map
        and !ele and !ele:signed and !~"ele:.*"
        and hiking = yes
    """
    override val changesetComment = "Specify guidepost/map elevation"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = R.drawable.ic_quest_guidepost_ele
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_guidepostEle_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with information ~ guidepost|map")

    override val highlightedElementsRadius: Double get() = 200.0
    override val defaultDisabledMessage: Int = R.string.quest_guidepost_disabled_msg

    override fun createForm() = AddGuidepostEleForm()

    override fun applyAnswerTo(answer: GuidepostEleAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleGuidepostEle -> tags["ele:signed"] = "no"
            is GuidepostEle ->          tags["ele"] = answer.ele.toString()
        }
    }
}

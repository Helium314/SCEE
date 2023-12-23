package de.westnordost.streetcomplete.quests.guidepost_sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddGuidepostSports : OsmElementQuestType<GuidepostSportsAnswer> {

    private val filter by lazy {
        """
        nodes with
          tourism = information
          and information ~ guidepost|route_marker
          and !hiking and !bicycle and !mtb and !climbing and !horse and !nordic_walking and !ski and !inline_skates and !running
          and !disused
          and !guidepost
    """.toElementFilterExpression()
    }

    override val changesetComment = "Specify what kind of guidepost"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = R.drawable.ic_quest_guidepost_sport
    override val isDeleteElementEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_guidepost_sports_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) == true }

    override fun isApplicableTo(element: Element): Boolean? =
        filter.matches(element)

    override fun createForm() = AddGuidepostSportsForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with tourism = information and information ~ guidepost|route_marker")

    override fun applyAnswerTo(answer: GuidepostSportsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer is IsSimpleGuidepost) {
            applySimpleGuidepostAnswer(tags)
        } else {
            answer.selectedSports.forEach { sport ->
                tags[sport.key] = "yes"
            }
        }
    }
    private fun applySimpleGuidepostAnswer(tags: Tags) {
        tags["guidepost"] = "simple"
    }
}

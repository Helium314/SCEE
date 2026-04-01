package de.westnordost.streetcomplete.quests.guidepost_sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_guidepost_sports_note
import de.westnordost.streetcomplete.resources.quest_guidepost_sports_title

class AddGuidepostSports : OsmFilterQuestType<Set<GuidepostSportsAnswer>>(), AndroidQuest {

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
    override val icon = R.drawable.ic_quest_guidepost_sport
    override val title = Res.string.quest_guidepost_sports_title
    override val isDeleteElementEnabled = true
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val hint = Res.string.quest_guidepost_sports_note

    override fun createForm() = AddGuidepostSportsForm()

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

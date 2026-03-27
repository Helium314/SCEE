package de.westnordost.streetcomplete.quests.swimming_pool_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_swimmingPoolAvailability_title

class AddSwimmingPoolAvailability : OsmFilterQuestType<SwimmingPoolAvailability>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
         (
           leisure = resort
           or (leisure = sports_hall and sport = swimming)
           or tourism ~ camp_site|hotel
         )
         and !swimming_pool
    """
    override val changesetComment = "Survey whether places have a swimming pool"
    override val wikiLink = "Key:swimming_pool"
    override val title = Res.string.quest_swimmingPoolAvailability_title
    override val icon = R.drawable.ic_quest_swimming_pool
    override val isReplacePlaceEnabled = true
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
                nodes, ways with
                (
                   leisure ~ resort|swimming_pool
                   or (leisure = sports_hall and sport = swimming)
                   or tourism ~ camp_site|hotel
                 )
            """)

    override fun createForm() = AddSwimmingPoolAvailabilityForm()

    override fun applyAnswerTo(answer: SwimmingPoolAvailability, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("swimming_pool", answer.osmValue)
    }
}

package de.westnordost.streetcomplete.quests.swimming_pool_availability

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm

class AddSwimmingPoolAvailability : OsmFilterQuestType<SwimmingPoolAvailability>() {

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
    override val icon = Res.drawable.ic_quest_swimming_pool
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

    @Composable
    override fun Form(
        on: (QuestAction<SwimmingPoolAvailability>) -> Unit,
        element: Element,
        geometry: ElementGeometry,
        countryInfo: CountryInfo
    ) {
        RadioGroupQuestForm(
            on,
            items = SwimmingPoolAvailability.entries,
            itemContent = { Text(stringResource(when (it) {
                SwimmingPoolAvailability.INDOOR_AND_OUTDOOR -> R.string.quest_swimming_pool_indoor_and_outdoor
                SwimmingPoolAvailability.ONLY_INDOOR -> R.string.quest_swimming_pool_indoor_only
                SwimmingPoolAvailability.ONLY_OUTDOOR -> R.string.quest_swimming_pool_outdoor_only
                SwimmingPoolAvailability.NO -> R.string.quest_swimming_pool_no
            }))}
        )
    }

    override fun applyAnswerTo(answer: SwimmingPoolAvailability, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("swimming_pool", answer.osmValue)
    }
}

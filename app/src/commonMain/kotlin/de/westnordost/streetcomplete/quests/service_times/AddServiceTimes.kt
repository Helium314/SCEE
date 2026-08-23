package de.westnordost.streetcomplete.quests.service_times

import androidx.compose.runtime.Composable
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.isSupported
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimesForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimes
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimesAnswer

class AddServiceTimes : OsmElementQuestType<CollectionTimesAnswer> {

    private val filter by lazy { """
        nodes, ways, relations with amenity=place_of_worship
          and service_times:signed != no
          and (!service_times or service_times older today -4 years)
    """.toElementFilterExpression() }

    /* Don't ask again for places without signed service times. This is very unlikely to
     * change and problematic to tag clearly with the check date scheme */

    override val changesetComment = "Survey religious service times"
    override val wikiLink = "Key:collection_times"
    override val icon = Res.drawable.quest_religion_service_times
    override val title = Res.string.quest_service_times_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean {
        if (!filter.matches(element)) return false
        val tags = element.tags
        // no service_times yet -> new survey
        val ct = tags["service_times"] ?: return true
        // invalid service_times rules -> applicable because we want to ask for opening hours again
        // be strict
        val oh = ct.toOpeningHoursOrNull(lenient = false) ?: return true
        // only display supported rules, or ambiguous rules that should be corrected
        return oh.isSupported(allowTimePoints = true, allowAmbiguity = true)
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways, relations with amenity=place_of_worship")

    @Composable
    override fun Form(on: (QuestAction<CollectionTimesAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddPostboxCollectionTimesForm(on, element, countryInfo, "service_times", Res.string.quest_service_times_resurvey_title)
    }

    override fun applyAnswerTo(answer: CollectionTimesAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is CollectionTimesAnswer.NoSign -> {
                tags["service_times:signed"] = "no"
            }
            is CollectionTimes -> {
                tags.updateWithCheckDate("service_times", answer.times.toOpeningHours().toString())
            }
        }
    }
}

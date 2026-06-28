package de.westnordost.streetcomplete.quests.service_times

import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.isSupported
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_service_times_title
import de.westnordost.streetcomplete.resources.quest_service_times_resurvey_title
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimes
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimesAnswer
import de.westnordost.streetcomplete.quests.postbox_collection_times.NoCollectionTimesSign

class AddServiceTimes : OsmElementQuestType<CollectionTimesAnswer>, AndroidQuest {

    private val filter by lazy { """
        nodes, ways, relations with amenity=place_of_worship
          and service_times:signed != no
          and (!service_times or service_times older today -4 years)
    """.toElementFilterExpression() }

    /* Don't ask again for places without signed service times. This is very unlikely to
     * change and problematic to tag clearly with the check date scheme */

    override val changesetComment = "Survey postbox collection times"
    override val wikiLink = "Key:collection_times"
    override val icon = R.drawable.religion_service_times
    override val title = Res.string.quest_service_times_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>): StringResource {
        /* treat invalid service times like it is not set at all. Any service are
           legal tagging for service times, even though they are not supported in
           this app, i.e. are never asked again */
        val oh = tags["service_times"]?.toOpeningHoursOrNull(lenient = true)
        val hasSupportedServiceTimes = oh != null && oh.isSupported(allowTimePoints = true)
        return if (hasSupportedServiceTimes) {
            Res.string.quest_service_times_resurvey_title
        } else {
            Res.string.quest_service_times_title
        }
    }

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

    override fun createForm() = AddServiceTimesForm()

    override fun applyAnswerTo(answer: CollectionTimesAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoCollectionTimesSign -> {
                tags["service_times:signed"] = "no"
            }
            is CollectionTimes -> {
                tags.updateWithCheckDate("service_times", answer.times.toOpeningHours().toString())
            }
        }
    }
}

package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class LGBTQAccessQuest : OsmFilterQuestType<LGBTQAccessAnswer>() {
    override val elementFilter = """

        nodes, ways with (
          amenity ~ swingerclub|nightclub|bar|pub|cafe|restaurant|place_of_worship|community_centre|library|doctors|social_facility
          or shop ~ erotic|books
          or leisure ~ sauna|nightclub|nightlife
        )
        and (!seasonal or seasonal = no)
        and !brand and !wikipedia:brand and !wikidata:brand
        and !memorial and !historic
        and !lgbtq
    """

    // countries that are listed here ban lgbtq people
    override val enabledInCountries = LGBTExcludedCountries

    override val changesetComment = "Survey lgbtq access"
    override val wikiLink = "Key:lgbtq"
    // TODO: replace me
    override val icon = R.drawable.ic_quest_shop
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lgbtq_access

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean = filter.matches(element)
    override fun createForm() = LGBTQAccessForm()

    override fun applyAnswerTo(answer: LGBTQAccessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer.access.osmValue != null) {
            tags["lgbtq"] = answer.access.osmValue
        }
    }
}

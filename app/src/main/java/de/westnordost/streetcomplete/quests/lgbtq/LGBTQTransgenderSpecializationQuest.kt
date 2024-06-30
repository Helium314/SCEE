package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class LGBTQTransgenderSpecializationQuest : OsmFilterQuestType<LGBTQTransgenderSpecialization>() {
    override val elementFilter = """
        nodes, ways with lgbtq ~ primary|only and !lgbtq:trans
        and !memorial and !historic
    """

    // countries that are listed here ban lgbtq people
    override val enabledInCountries = LGBTExcludedCountries

    override val changesetComment = "Survey lgbtq transgender specialization"
    override val wikiLink = "Key:lgbtq"
    // TODO: replace me
    override val icon = R.drawable.ic_quest_shop
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lgbtq_transgender_specialization

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean = filter.matches(element)

    override fun applyAnswerTo(answer: LGBTQTransgenderSpecialization, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            LGBTQTransgenderSpecialization.YES -> tags["lgbtq:trans"] = "primary"
            LGBTQTransgenderSpecialization.NO -> tags["lgbtq:trans"] = "welcome"
            LGBTQTransgenderSpecialization.BANNED -> tags["lgbtq:trans"] = "no"
        }
    }

    override fun createForm() = LGBTQTransgenderSpecializationForm()
}

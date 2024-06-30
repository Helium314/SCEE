package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class LGBTQGenderSpecializationQuest : OsmFilterQuestType<LGBTQGenderSpecialization>() {
    override val elementFilter = """
        nodes with lgbtq ~ primary|only and !(lgbtq:men or lgbtq:woman or lgbtq:non_binary)
        and !memorial and !historic
    """

    // countries that are listed here ban lgbtq people
    override val enabledInCountries = LGBTExcludedCountries

    override val changesetComment = "Survey lgbtq specificity"
    override val wikiLink = "Key:lgbtq"
    // TODO: replace me
    override val icon = R.drawable.ic_quest_shop
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lgbtq_gender_specialization

    override fun createForm() = LGBTQGenderSpecializationForm()

    override fun applyAnswerTo(answer: LGBTQGenderSpecialization, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) =
        when (answer) {
            LGBTQGenderSpecialization.NONE -> {
                tags["lgbtq:men"] = "welcome"
                tags["lgbtq:woman"] = "welcome"
                tags["lgbtq:non_binary"] = "welcome"
            }
            LGBTQGenderSpecialization.MEN -> {
                tags["lgbtq:men"] = tags["lgbtq"] ?: "primary"
            }
            LGBTQGenderSpecialization.WOMAN -> {
                tags["lgbtq:woman"] = tags["lgbtq"] ?: "primary"
            }
            LGBTQGenderSpecialization.NON_BINARY -> {
                tags["lgbtq:non_binary"] = tags["lgbtq"] ?: "primary"
            }
        }
}

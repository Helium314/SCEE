package de.westnordost.streetcomplete.quests.contact

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_contact_phone
import de.westnordost.streetcomplete.resources.quest_phone
import de.westnordost.streetcomplete.ui.common.quest.TextInputForm

class AddContactPhone : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or craft
         or healthcare
         or """.trimIndent() +
         PLACES_FOR_CONTACT_QUESTS +
        "\n) and !phone and !contact:phone and !contact:mobile and !brand and (name or operator)"

    override val changesetComment = "Add phone number"
    override val wikiLink = "Key:phone"
    override val icon = Res.drawable.quest_phone
    override val title = Res.string.quest_contact_phone
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        TextInputForm(on, keyboardType = KeyboardType.Number, initialValue = prefill, isOk = { it != prefill })
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        prefill = if (answer.contains(" ") && answer.substringBefore(" ").length <= 5)
            answer.substringBefore(" ") + " "
        else
            "+"
        tags["phone"] = answer
    }
}

private var prefill = "+"

val PLACES_FOR_CONTACT_QUESTS = mapOf(
    "amenity" to arrayOf(
        "restaurant", "cafe", "internet_cafe",
        "cinema", "townhall", "embassy", "community_centre", "youth_centre", "library",
        "dentist", "doctors", "clinic", "veterinary", "animal_shelter",
        "arts_centre", "ferry_terminal", "prep_school", "dojo"
    ),
    "leisure" to arrayOf("fitness_centre", "bowling_alley", "sports_centre", "escape_game"),
    "office" to arrayOf(
        "insurance", "government", "travel_agent", "tax_advisor", "religion", "employment_agency",
        "lawyer", "estate_agent", "therapist", "notary"
    ),
    "shop" to arrayOf(
        "beauty", "massage", "hairdresser", "wool", "tattoo", "electrical", "glaziery", "tailor",
        "computer", "electronics", "hifi", "bicycle", "outdoor", "sports", "art", "craft", "model",
        "musical_instrument", "camera", "books", "travel_agency", "cheese", "chocolate", "coffee", "health_food"
    ),
    "tourism" to arrayOf("zoo", "aquarium", "gallery", "museum", "alpine_hut", "camp_site", "caravan_site"),
).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ")

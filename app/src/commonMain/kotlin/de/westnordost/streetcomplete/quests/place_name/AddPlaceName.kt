package de.westnordost.streetcomplete.quests.place_name

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.osm.localized_name.parseLocalizedNames
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class AddPlaceName(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<PlaceNameAnswer> {

    private val filter by lazy { ("""
        nodes, ways with
        (
          shop and shop !~ no|vacant
          or office and office !~ no|vacant
          or craft
          or amenity = recycling and recycling_type = centre
          or amenity = shelter and shelter_type = basic_hut
          or tourism = information and information ~ office|visitor_centre
          or natural = cave_entrance and fee = yes
          or """ +

        // The common list is shared by the opening hours quest and the wheelchair quest.
        // It is also mostly shared by the name quest, that has some wildcards (for say craft and office)
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can be added in the "name only" tab.

        prefs.getString(questPrefix(prefs) + PREF_ELEMENTS, NAME_PLACES)+ "\n" + """
        )
        and (
            (
                !name
                and !brand
                and noname != yes
            )
            or ~fixme|FIXME ~ name|name\?|Name|Name\?
        )
        and name:signed != no
    """).toElementFilterExpression() }

    override val changesetComment = "Determine place names"
    override val wikiLink = "Key:name"
    override val icon = Res.drawable.quest_label
    override val title = Res.string.quest_placeName_title
    override val achievements = listOf(CITIZEN)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && getFeature(element) != null

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(on: (QuestAction<PlaceNameAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        var showBrandNameDialog by remember { mutableStateOf(false) }
        LocalizedNameQuestForm(
            on = {
                on(when (it) {
                    is Answer<List<LocalizedName>> -> Answer(PlaceName(it.value))
                    is Action -> it
                })
            },
            countryInfo = countryInfo,
            initialLocalizedNames = parseLocalizedNames(element.tags),
            otherAnswers = { listOfNotNull(
                if (!element.tags.containsKey("shop") && !element.tags.containsKey("amenity")
                    && !element.tags.containsKey("leisure") && !element.tags.containsKey("tourism")) null
                else AnswerItem(stringResource(Res.string.quest_name_brand)) { showBrandNameDialog = true }
            ) }
        )
        if (showBrandNameDialog) {
            var brand by remember { mutableStateOf(TextFieldValue()) }
            var suggestions by remember { mutableStateOf(listOf<Feature>()) }
            val featureDictionary: FeatureDictionary = koinInject()
            LaunchedEffect(brand) {
                suggestions = featureDictionary.getByTerm(
                    search = brand.text,
                    languages = getLanguagesForFeatureDictionary(),
                    country = countryInfo.countryOrSubdivisionCode,
                    geometry = GeometryType.POINT
                ).filter {
                    it.addTags.containsKey("brand") && when {
                        element.tags.containsKey("amenity") -> it.addTags["amenity"] == element.tags["amenity"]
                        element.tags.containsKey("shop") -> it.addTags["shop"] == element.tags["shop"]
                        element.tags.containsKey("leisure") -> it.addTags["leisure"] == element.tags["leisure"]
                        element.tags.containsKey("tourism") -> it.addTags["tourism"] == element.tags["tourism"]
                        else -> false
                    } }.toList()
            }
            AlertDialog(
                onDismissRequest = { showBrandNameDialog = false },
                buttonRow = {
                    TextButton({ showBrandNameDialog = false }) { Text(stringResource(Res.string.cancel)) }
                    TextButton({
                        val feature = suggestions.firstOrNull { it.name == brand.text } // hope that we don't have 2 features with the same name...
                        if (feature == null) on(Answer(PlaceNameAnswer.BrandName(brand.text)))
                        else on(Answer(PlaceNameAnswer.FeatureName(feature)))
                    }) { Text(stringResource(Res.string.ok)) }
                },
                title = { Text(stringResource(Res.string.quest_name_brand)) },
                text = {
                    AutoCompleteTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        // avoid annoyingly showing a single suggestion blocking the buttons
                        suggestions = suggestions.takeIf { it.size > 1 || it.singleOrNull()?.name != brand.text }.orEmpty().map { it.name }
                    )
                }
            )
        }
    }

    override fun applyAnswerTo(answer: PlaceNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is PlaceName -> {
                if (answer.localizedNames.isEmpty()) {
                    tags["name:signed"] = "no"
                } else {
                    answer.localizedNames.applyTo(tags)
                }
            }
            is PlaceNameAnswer.FeatureName -> {
                for (addTag in answer.feature.addTags)
                    tags[addTag.key] = addTag.value
            }
            is PlaceNameAnswer.BrandName -> {
                tags["brand"] = answer.name
                tags["name"] = answer.name
            }
        }
    }

    override val hasQuestSettings = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        FullElementSelectionDialog(prefs, questPrefix(prefs) + PREF_ELEMENTS, Res.string.quest_settings_element_selection, NAME_PLACES, onDismissRequest)
    }
}

private val NAME_PLACES = mapOf(
    "amenity" to arrayOf(
        // common
        "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten",         // eat & drink
        "food_court", "nightclub", "hookah_lounge",
        "cinema", "planetarium", "casino",                                                  // amenities
        "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",
        "ranger_station",                                                                   // civic
        "driving_school", "music_school", "prep_school", "language_school", "dive_centre",  // learning
        "dancing_school", "ski_school", "flight_school", "surf_school", "sailing_school",
        "cooking_school",
        "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace",         // commercial
        "internet_cafe", "payment_centre",
        "car_wash", "car_rental", "fuel",                                                   // car stuff
        "dentist", "doctors", "clinic", "pharmacy", "veterinary", "veterinary_pharmacy",    // health
        "animal_boarding", "animal_shelter", "animal_breeding",                             // animals
        "coworking_space",                                                                  // work

        // name & opening hours
        "boat_rental", "vehicle_inspection", "motorcycle_rental", "crematorium",
        "public_bath", "traffic_park",

        // name & wheelchair
        "theatre",                                        // culture
        "conference_centre", "arts_centre",               // events
        "police",                                         // civic
        "ferry_terminal",                                 // transport
        "place_of_worship",                               // religious
        "hospital",                                       // health care
        "brothel", "gambling", "love_hotel", "stripclub", // bad stuff

        // name only
        "studio",                                                                // culture
        "events_venue", "exhibition_centre", "music_venue", "funeral_hall",      // events
        "prison", "fire_station", "bus_station", "refugee_site",                 // civic
        "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
        "monastery",                                                             // religious
        "kindergarten", "school", "college", "university", "research_institute", // education
        "dojo",                                                                  // sport
    ),
    "tourism" to arrayOf(
        // common
        "zoo", "aquarium", "theme_park", "gallery", "museum",

        // name & wheelchair
        "attraction",
        "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet", // accommodations

        // and tourism = information, see above
    ),
    "leisure" to arrayOf(
        // common
        "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
        "amusement_arcade", "adult_gaming_centre", "tanning_salon", "sauna",
        "indoor_play",

        // name & wheelchair
        "sports_centre", "stadium",

        // name & opening hours
        "trampoline_park",

        // name only
        "dance", "nature_reserve", "marina", "horse_riding",
        "bathing_place", "escape_game", "beach_resort", "summer_camp", "marina"
    ),
    "landuse" to arrayOf(
        "cemetery", "allotments"
    ),
    "military" to arrayOf(
        "airfield", "barracks", "training_area", "base",
    ),
    "healthcare" to arrayOf(
        // common
        "pharmacy", "doctor", "clinic", "dentist", "centre", "physiotherapist",
        "laboratory", "alternative", "psychotherapist", "optometrist", "podiatrist",
        "nurse", "counselling", "speech_therapist", "blood_donation", "sample_collection",
        "occupational_therapist", "dialysis", "vaccination_centre", "audiologist",
        "blood_bank", "nutrition_counselling",

        // name & wheelchair
        "rehabilitation", "hospice", "midwife", "birthing_centre"
    ),
    "historic" to arrayOf(
        // name only
        "castle", "church", "farm", "fort", "manor", "monument", "mosque", "temple",
        "ship",
    ),
    "waterway" to arrayOf(
        // name & opening hours
        "fuel",
    ),
).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ")

private const val PREF_ELEMENTS = "qs_AddPlaceName_element_selection"

sealed interface PlaceNameAnswer {
    data class FeatureName(val feature: Feature) : PlaceNameAnswer
    data class BrandName(val name: String) : PlaceNameAnswer
}

data class PlaceName(val localizedNames: List<LocalizedName>) : PlaceNameAnswer

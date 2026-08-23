package de.westnordost.streetcomplete.quests.healthcare_speciality

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeForm
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeFormOption
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.FeatureSaver
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.koin.compose.koinInject

class AddHealthcareSpeciality : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
         amenity = doctors
         and name and !healthcare:speciality
    """
    override val changesetComment = "Add healthcare specialities"
    override val wikiLink = "Key:healthcare:speciality"
    override val icon = Res.drawable.ic_quest_healthcare_speciality
    override val title = Res.string.quest_healthcare_speciality_title
    override val defaultDisabledMessage = Res.string.quest_healthcare_speciality_disabled_message

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        // todo: this is only the "new" form, but we might also want to have the old form available for multiple values
        val featureDictionary: FeatureDictionary = koinInject()
        var feature by rememberSaveable(stateSaver = FeatureSaver(featureDictionary)) {
            mutableStateOf<Feature?>(null)
        }
        var option by remember { mutableStateOf<ShopTypeFormOption?>(null) }

        QuestForm(
            on = on,
            isComplete = when (option) {
                null -> false
                ShopTypeFormOption.FEATURE -> feature != null
                else -> true
            },
            onClickOk = {
                on(Answer(feature!!.addTags["healthcare:speciality"]!!))
            },
        ) {
            ShopTypeForm(
                feature = feature,
                selectedOption = option,
                onSelectedFeature = { feature = it },
                onSelectedOption = { option = it },
                featureDictionary = featureDictionary,
                geometryType = element.geometryType,
                countryCode = countryInfo.countryOrSubdivisionCode,
                filterFn = ::filterOnlySpecialitiesOfMedicalDoctors,
                codesOfDefaultFeatures = getSuggestions()
            )
        }
    }

    private fun filterOnlySpecialitiesOfMedicalDoctors(feature: Feature): Boolean {
        if (!feature.tags.containsKey("healthcare:speciality")) {
            return false
        }
        return feature.tags["amenity"] == "doctors"
    }

    private fun getSuggestions(): List<String> {
//        if (lastPickedAnswers.size >= 12) return lastPickedAnswers
        return (/*lastPickedAnswers +*/ listOf(
            // based on https://taginfo.openstreetmap.org/keys/healthcare%3Aspeciality#values
            // with alternative medicine skipped
            "amenity/doctors/general",
            // chiropractic - skipped (alternative medicine)
            "amenity/doctors/ophthalmology",
            "amenity/doctors/paediatrics",
            "amenity/doctors/gynaecology",
            //biology skipped as that is value for laboratory
            // "amenity/dentist", would require changes in SCEE
            // psychiatry - https://github.com/openstreetmap/id-tagging-schema/issues/778
            "amenity/doctors/orthopaedics",
            "amenity/doctors/internal",
            // "healthcare/dentist/orthodontics", may require changes in SCEE
            "amenity/doctors/dermatology",
            // osteopathy - skipped (alternative medicine)
            "amenity/doctors/otolaryngology",
            "amenity/doctors/radiology",
            // vaccination? that is tagged differently, right? TODO
            "amenity/doctors/cardiology",
            "amenity/doctors/surgery", // TODO? really for doctors? Maybe that is used primarily for hospitals?
            // physiotherapy
            // urology
            // emergency
            // dialysis
        )).distinct().take(12)
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["healthcare:speciality"] = answer
    }
}

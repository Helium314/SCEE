package de.westnordost.streetcomplete.quests.healthcare_speciality

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AMultiValueQuestForm
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeForm
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeFormOption
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.takeFavorites

class AddHealthcareSpecialityForm : AMultiValueQuestForm<String>() {

    override fun stringToAnswer(answerString: String) = answerString

    // the hacky UI switch breaks when using tag editor...
    override val otherAnswers get() = if (TagEditor.showingTagEditor) emptyList() else listOf(AnswerItem(R.string.quest_healthcare_speciality_switch_ui) {
        val f = MedicalSpecialityTypeForm()
        if (f.arguments == null) f.arguments = bundleOf()
        val args = createArguments(questKey, questType, geometry, 0.0, 0.0)
        f.requireArguments().putAll(args)
        val osmArgs = createArguments(element)
        f.requireArguments().putAll(osmArgs)
        activity?.currentFocus?.hideKeyboard()
        parentFragmentManager.commit {
            replace(id, f, "bottom_sheet")
            addToBackStack("bottom_sheet")
        }
    })

    override val onlyAllowSuggestions = true

    override val addAnotherValueResId = R.string.quest_healthcare_speciality_add_more

    override fun getConstantSuggestions() =
        (healthcareSpecialityFromWiki.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        } + healthcareSpecialityValuesFromTaginfo.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        }).toSet()

}


class MedicalSpecialityTypeForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val feature: MutableState<Feature?> = mutableStateOf(null)
    private val option: MutableState<ShopTypeFormOption?> = mutableStateOf(null)

    // the hacky UI switch breaks when using tag editor...
    override val otherAnswers = if (TagEditor.showingTagEditor) emptyList() else listOf(AnswerItem(R.string.quest_healthcare_speciality_switch_ui) {
        val f = AddHealthcareSpecialityForm()
        if (f.arguments == null) f.arguments = bundleOf()
        val args = createArguments(questKey, questType, geometry, 0.0, 0.0)
        f.requireArguments().putAll(args)
        val osmArgs = createArguments(element)
        f.requireArguments().putAll(osmArgs)
        activity?.currentFocus?.hideKeyboard()
        parentFragmentManager.commit {
            replace(id, f, "bottom_sheet")
            addToBackStack("bottom_sheet")
        }
    })

    private val lastPickedAnswers by lazy {
        prefs.getLastPicked<String>(this::class.simpleName!!).takeFavorites(12, 50, 1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            ShopTypeForm(
                feature = feature.value,
                option = option.value,
                onSelectedFeature = {
                    feature.value = it
                    checkIsFormComplete()
                },
                onSelectedOption = {
                    option.value = it
                    checkIsFormComplete()
                },
                featureDictionary = featureDictionary,
                geometryType = element.geometryType,
                countryCode = countryOrSubdivisionCode,
                filterFn = ::filterOnlySpecialitiesOfMedicalDoctors,
                codesOfDefaultFeatures = getSuggestions()
            )
        } }
    }

    private fun filterOnlySpecialitiesOfMedicalDoctors(feature: Feature): Boolean {
        if (!feature.tags.containsKey("healthcare:speciality")) {
            return false
        }
        return feature.tags["amenity"] == "doctors"
    }

    override fun onClickOk() {
        when (option.value) {
            ShopTypeFormOption.FEATURE -> {
                val feature = feature.value!!
                applyAnswer(feature.addTags["healthcare:speciality"]!!)
                prefs.addLastPicked(this::class.simpleName!!, feature.id)
            }
            ShopTypeFormOption.VACANT -> composeNote()
            ShopTypeFormOption.LEAVE_NOTE -> composeNote()
            null -> { }
        }
    }

    override fun isFormComplete() = when (option.value) {
        null -> false
        ShopTypeFormOption.FEATURE -> feature.value != null
        else -> true
    }

    private fun getSuggestions(): List<String> {
        if (lastPickedAnswers.size >= 12) return lastPickedAnswers
        return (lastPickedAnswers + listOf(
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
                )
            ).distinct().take(12)
    }
}


const val healthcareSpecialityFromWiki = """
allergology
anaesthetics
cardiology
cardiothoracic_surgery
child_psychiatry
community
dermatology
dermatovenereology
diagnostic_radiology
emergency
endocrinology
gastroenterology
general
geriatrics
gynaecology
haematology
hepatology
infectious_diseases
intensive
internal
maxillofacial_surgery
nephrology
neurology
neuropsychiatry
neurosurgery
nuclear
occupational
oncology
ophthalmology
orthodontics
orthopaedics
otolaryngology
paediatric_surgery
paediatrics
palliative
pathology
physiatry
plastic_surgery
podiatry
proctology
psychiatry
pulmonology
radiology
radiotherapy
rheumatology
stomatology
surgery
transplant
trauma
tropical
urology
vascular_surgery
"""

const val healthcareSpecialityValuesFromTaginfo = """
general
chiropractic
ophthalmology
paediatrics
biology
gynaecology
psychiatry
dentist
orthopaedics
internal
dermatology
orthodontics
vaccination
osteopathy
otolaryngology
radiology
surgery
cardiology
urology
physiotherapy
dentistry
emergency
dialysis
covid19
community
neurology
acupuncture
plastic_surgery
traditional_chinese_medicine
weight_loss
intensive
naturopathy
oncology
physiatry
homeopathy
clinic
blood_check
occupational
gastroenterology
child_psychiatry
dental_oral_maxillo_facial_surgery
podiatry
maternity
pulmonology
optometry
fertility
endocrinology
massage_therapy
dermatovenereology
stomatology
psychotherapist
family_medicine
diagnostic_radiology
general;emergency
kinesitherapy
pathology
trauma
nephrology
behavior
psychology
geriatrics
ayurveda
anaesthetics
otorhinolaryngology
"""

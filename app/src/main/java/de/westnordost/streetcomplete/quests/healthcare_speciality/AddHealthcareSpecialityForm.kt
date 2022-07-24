package de.westnordost.streetcomplete.quests.healthcare_speciality

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestCuisineSuggestionBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.cuisine.AddCuisineForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.mostCommonWithin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddHealthcareSpecialityForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    private val binding by contentViewBinding(QuestCuisineSuggestionBinding::bind)

    val specialities = mutableListOf<String>()

    val speciality get() = binding.cuisineInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cuisineInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                (lastPickedAnswers + suggestions).distinct(),
            )
        )

        binding.cuisineInput.doAfterTextChanged { checkIsFormComplete() }

        binding.addCuisineButton.setOnClickListener {
            if (isFormComplete() && binding.cuisineInput.text.isNotBlank()) {
                specialities.add(speciality)
                binding.currentCuisines.text = specialities.joinToString(";")
                binding.cuisineInput.text.clear()
            }
            viewLifecycleScope.launch {
                delay(20) // delay, because otherwise dropdown disappears immediately
                binding.cuisineInput.showDropDown()
            }
        }
        binding.addCuisineButton.setText(R.string.quest_healthcare_speciality_add_more)
        binding.cuisineInput.showDropDown()
    }

    override fun onClickOk() {
        specialities.removeAll { it.isBlank() }
        if (specialities.isNotEmpty()) favs.add(specialities)
        if (speciality.isNotBlank()) favs.add(speciality)
        if (speciality.isBlank())
            applyAnswer(specialities.joinToString(";"))
        else
            applyAnswer((specialities + listOf(speciality)).joinToString(";"))
    }

    override fun isFormComplete() = (speciality.isNotBlank() || specialities.isNotEmpty())
        && !specialities.contains(speciality)
        && (speciality.isEmpty() || suggestions.contains(speciality))
        && specialities.all { suggestions.contains(it) }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
            maxEntries = 10
        )
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 10, historyCount = 10, first = 1)
            .toList()
    }

    companion object {
        private val suggestions = (healthcareSpecialityFromWiki.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        } + healthcareSpecialityValuesFromTaginfo.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        }).toSet().toTypedArray()
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
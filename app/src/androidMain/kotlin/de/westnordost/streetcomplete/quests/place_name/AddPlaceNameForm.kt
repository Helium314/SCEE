package de.westnordost.streetcomplete.quests.place_name

import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.confirmNoName
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo
import de.westnordost.streetcomplete.util.SearchAdapter
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary

class AddPlaceNameForm : AAddLocalizedNameForm<PlaceNameAnswer>() {

    override val otherAnswers = listOfNotNull(
        AnswerItem(R.string.quest_placeName_no_name_answer) {
            confirmNoName(requireContext()) { applyAnswer(PlaceNameAnswer.NoNameSign) }
        },
        AnswerItem(R.string.quest_streetName_answer_cantType) {
            showKeyboardInfo(requireContext())
        },
        createBrandAnswer()
    )

    private fun createBrandAnswer(): AnswerItem? {
        val ctx = context ?: return null
        if (!element.tags.containsKey("shop") && !element.tags.containsKey("amenity")
            && !element.tags.containsKey("leisure") && !element.tags.containsKey("tourism")) return null
        return AnswerItem(R.string.quest_name_brand) {
            val languages = getLanguagesForFeatureDictionary()
            val searchAdapter = SearchAdapter(ctx, { search ->
                featureDictionary.getByTerm(
                    search = search,
                    languages = languages,
                    country = countryOrSubdivisionCode,
                    geometry = GeometryType.POINT
                ).filter {
                    it.addTags.containsKey("brand") && when {
                        element.tags.containsKey("amenity") -> it.addTags["amenity"] == element.tags["amenity"]
                        element.tags.containsKey("shop") -> it.addTags["shop"] == element.tags["shop"]
                        element.tags.containsKey("leisure") -> it.addTags["leisure"] == element.tags["leisure"]
                        element.tags.containsKey("tourism") -> it.addTags["tourism"] == element.tags["tourism"]
                        else -> false
                    } }.toList()
            }, { it.name })
            var feature: Feature? = null
            var dialog: AlertDialog? = null
            val textField = layoutInflater.inflate(R.layout.quest_name_suggestion, null) as AutoCompleteTextView
            textField.setAdapter(searchAdapter)
            textField.doAfterTextChanged {
                dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = !it?.toString().isNullOrBlank()
            }
            textField.setOnItemClickListener { _, _, i, _ -> feature = searchAdapter.getItem(i) }
            dialog = AlertDialog.Builder(ctx)
                .setTitle(R.string.quest_name_brand)
                .setView(textField)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val f = feature
                    val text = textField.text.toString()
                    if (text == f?.name)
                        applyAnswer(PlaceNameAnswer.FeatureName(f))
                    else
                        applyAnswer(PlaceNameAnswer.BrandName(text))
                }
                .create()
            dialog.setOnShowListener {
                textField.requestFocus()
                textField.showKeyboard()
            }
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
    }

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(PlaceName(names))
    }
}

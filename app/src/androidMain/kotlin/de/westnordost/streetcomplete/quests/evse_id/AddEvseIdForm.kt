package de.westnordost.streetcomplete.quests.evse_id

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.text.InputFilter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AMultiValueQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddEvseIdForm : AMultiValueQuestForm<String>() {

    override fun stringToAnswer(answerString: String) =
        answerString.trim()

    override fun getConstantSuggestions() = emptyList<String>()

    override val addAnotherValueResId =
        R.string.quest_evse_id_add_more

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) {
            applyAnswer("ref:signed=no")
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<AutoCompleteTextView>(R.id.valueInput)
            ?: return

        // Force uppercase input
        input.filters += InputFilter.AllCaps()

        // Example hint
        input.hint = getString(R.string.quest_evse_id_hint)
    }

    override fun isFormComplete(): Boolean =
        super.isFormComplete() && (value.isBlank() || EVSE_REGEX.matches(value))

    companion object {
        private val EVSE_REGEX =
            Regex("(?i)^[A-Z]{2}\\*?[A-Z0-9]{3}\\*?E(?!\\*)[A-Z0-9*]{1,31}$")
    }
}

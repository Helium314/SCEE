package de.westnordost.streetcomplete.quests.contact

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.widget.doAfterTextChanged

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.databinding.QuestContactBinding


class AddContactPhoneForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_contact
    private val binding by contentViewBinding(QuestContactBinding::bind)

    private val contact get() = binding.nameInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.inputType = InputType.TYPE_CLASS_PHONE

        binding.nameInput.setText(PREFILL)
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(contact)
    }


    override fun isFormComplete() = contact.isNotEmpty() && contact != PREFILL
}

private const val PREFILL = "+"
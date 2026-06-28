package de.westnordost.streetcomplete.quests.guidepost

import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.quests.TextInputForm

class AddGuidepostEleForm : TextInputForm<String>() {
    override val keyboardType = KeyboardType.Decimal

    override fun onClickOk() {
        applyAnswer(text.value)
    }

    override fun isFormComplete() = text.value.toDoubleOrNull() != null
}

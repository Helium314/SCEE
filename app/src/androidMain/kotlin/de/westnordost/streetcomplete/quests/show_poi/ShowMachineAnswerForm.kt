package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

class ShowMachineAnswerForm : AbstractOsmQuestForm<Boolean>() {
    @Composable
    override fun getSubtitle(): AnnotatedString? {
        if (element.tags["amenity"] == "vending_machine" && element.tags.contains("vending")) {
            return (super.getSubtitle() ?: AnnotatedString("")) + AnnotatedString(" ${element.tags["vending"]}")
        }
        return super.getSubtitle()
    }
}

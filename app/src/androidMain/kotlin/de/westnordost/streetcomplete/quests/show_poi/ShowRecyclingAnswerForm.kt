package de.westnordost.streetcomplete.quests.show_poi

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowRecyclingAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (element.tags["amenity"] == "waste_basket")
            buttonPanelAnswers.add(AnswerItem(R.string.quest_recycling_excrement_bag_dispenser) { applyAnswer(true) })
    }

    @Composable
    override fun getSubtitle(): AnnotatedString? {
        val recycling = element.tags.mapNotNull {
            if (it.value == "yes" && it.key.startsWith("recycling:"))
                it.key.substringAfter("recycling:")
            else null
        }.sorted().joinToString(", ")
        if (recycling.isNotEmpty()) {
            return (super.getSubtitle() ?: AnnotatedString("")) + AnnotatedString(" $recycling")
        }
        return super.getSubtitle()
    }
}

package de.westnordost.streetcomplete.quests.show_poi

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowTrafficStuffAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (element.tags["traffic_calming"] == null && element.tags["crossing"] != null)
            buttonPanelAnswers.add(AnswerItem(R.string.quest_traffic_stuff_raised) { applyAnswer(true) })
    }

    @Composable
    override fun getSubtitle(): AnnotatedString? {
        if ((!element.tags["crossing"].isNullOrBlank() && !element.tags["traffic_calming"].isNullOrBlank())
            || element.tags["type"] == "restriction"
            || element.tags["highway"] == "elevator") {
            return (super.getSubtitle() ?: AnnotatedString("")) + AnnotatedString(" ${element.tags.entries}")
        }
        return super.getSubtitle()
    }
}

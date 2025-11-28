package de.westnordost.streetcomplete.quests.show_poi

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowFixmeAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_fixme_remove) { applyAnswer(false) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (element.tags["fixme"] ?: element.tags["FIXME"])
            ?.let { setTitle(resources.getString((questType as OsmElementQuestType<*>).getTitle(element.tags)) + " ($it)") }
    }

}

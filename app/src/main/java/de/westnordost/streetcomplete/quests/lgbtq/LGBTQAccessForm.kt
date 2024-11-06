package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextItem

class LGBTQAccessForm : AListQuestForm<String?>() {

    override val items get() = listOf<TextItem<String?>>(
        TextItem("no", R.string.quest_lgbtq_access_no),
        TextItem("welcome", R.string.quest_lgbtq_access_welcome),
        TextItem("primary", R.string.quest_lgbtq_access_primary),
        TextItem("only", R.string.quest_lgbtq_access_only),
        TextItem(null, R.string.quest_lgbtq_access_not_marked),
    )

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_lgbtq_access_hide_forever) {
            hideQuest()
        }
    )
}

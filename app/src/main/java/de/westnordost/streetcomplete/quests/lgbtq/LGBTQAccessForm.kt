package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class LGBTQAccessForm : AListQuestForm<LGBTQAccess>() {

    override val items get() = listOf(
        LGBTQAccess.NO,
        LGBTQAccess.WELCOME,
        LGBTQAccess.PRIMARY,
        LGBTQAccess.ONLY,
        LGBTQAccess.UNKNOWN,
    ).toItems()

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_lgbtq_access_hide_forever) {
            hideQuest()
        }
    )
}

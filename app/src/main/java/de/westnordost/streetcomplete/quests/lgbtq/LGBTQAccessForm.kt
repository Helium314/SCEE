package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class LGBTQAccessForm : AImageListQuestForm<LGTBQAccess, LGBTQAccessAnswer>() {

    override val items get() = listOf(
        LGTBQAccess.NO,
        LGTBQAccess.WELCOME,
        LGTBQAccess.PRIMARY,
        LGTBQAccess.ONLY,
        LGTBQAccess.UNKNOWN,
    ).toItems()

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_lgbtq_access_hide_forever) {
            hideQuest()
        }
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<LGTBQAccess>) {
        val value = selectedItems.single()
        if (value.osmValue == null) {
            hideOsmQuestController.tempHide(questKey as OsmQuestKey)
            return
        }
        applyAnswer(LGBTQAccessAnswer(value))
    }
}

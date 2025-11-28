package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class LGBTQTransgenderSpecializationForm : AbstractOsmQuestForm<LGBTQTransgenderSpecialization>() {
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) {
            applyAnswer(
                LGBTQTransgenderSpecialization.NO
            )
        },
        AnswerItem(R.string.quest_generic_hasFeature_yes) {
            applyAnswer(
                LGBTQTransgenderSpecialization.YES
            )
        },
        AnswerItem(R.string.quest_lgbtq_transgender_specialization_banned) {
            applyAnswer(
                LGBTQTransgenderSpecialization.BANNED
            )
        },
    )
}

package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class LGBTQGenderSpecializationForm : AbstractOsmQuestForm<LGBTQGenderSpecialization>() {
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_lgbtq_gender_specialization_men) { applyAnswer(LGBTQGenderSpecialization.MEN) },
        AnswerItem(R.string.quest_lgbtq_gender_specialization_woman) { applyAnswer(LGBTQGenderSpecialization.WOMAN) },
        AnswerItem(R.string.quest_lgbtq_gender_specialization_non_binary) { applyAnswer(LGBTQGenderSpecialization.NON_BINARY) },
        AnswerItem(R.string.quest_lgbtq_gender_specialization_none) { applyAnswer(LGBTQGenderSpecialization.NONE) },
    )
}

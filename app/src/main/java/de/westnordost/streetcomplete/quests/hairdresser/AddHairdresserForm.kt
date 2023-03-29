package de.westnordost.streetcomplete.quests.hairdresser

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.hairdresser.Hairdresser.MALE_AND_FEMALE
import de.westnordost.streetcomplete.quests.hairdresser.Hairdresser.NOT_SIGNED
import de.westnordost.streetcomplete.quests.hairdresser.Hairdresser.ONLY_FEMALE
import de.westnordost.streetcomplete.quests.hairdresser.Hairdresser.ONLY_MALE

class AddHairdresserForm : AListQuestForm<Hairdresser>() {
    override val items = listOf(
        TextItem(MALE_AND_FEMALE, R.string.quest_hairdresser_male_and_female),
        TextItem(ONLY_FEMALE, R.string.quest_hairdresser_female_only),
        TextItem(ONLY_MALE, R.string.quest_hairdresser_male_only),
        TextItem(NOT_SIGNED, R.string.quest_hairdresser_not_signed),
    )
}

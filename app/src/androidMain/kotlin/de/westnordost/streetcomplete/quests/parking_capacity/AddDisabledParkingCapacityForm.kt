package de.westnordost.streetcomplete.quests.parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.wheelchair_sign

class AddDisabledParkingCapacityForm : AAddCountInput() {
    override val icon = Res.drawable.wheelchair_sign
    override val otherAnswers get() = if (element.tags["capacity:disabled"] != "yes")
        listOf(AnswerItem(R.string.quest_parking_capacity_disabled_answer_yes) {
            applyAnswer(-1)
        })
    else emptyList()

    override fun isFormComplete() = count.value?.let { it >= 0 } == true
}

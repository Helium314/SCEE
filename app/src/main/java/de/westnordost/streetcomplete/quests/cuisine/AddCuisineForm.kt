package de.westnordost.streetcomplete.quests.cuisine

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AMultiValueQuestForm


class AddCuisineForm : AMultiValueQuestForm() {

    override fun getConstantSuggestions() =
        requireContext().assets.open("cuisine/cuisineSuggestions.txt").bufferedReader().readLines()

    override val addAnotherValueResId = R.string.quest_cuisine_add_more
}

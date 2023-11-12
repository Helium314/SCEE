package de.westnordost.streetcomplete.quests.piste_difficulty

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddPisteDifficultyForm : AImageListQuestForm<PisteDifficulty, PisteDifficulty>() {

    override val items = PisteDifficulty.values().map { it.asItem() }
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.imageView)
            .setImageResource(getRegionalPisteDifficultyResId(countryInfo.countryCode))
    }

    override fun onClickOk(selectedItems: List<PisteDifficulty>) {
        applyAnswer(selectedItems.single())
    }
}

private fun getRegionalPisteDifficultyResId(countryCode: String): Int = when (countryCode) {
    "AT" -> R.drawable.ic_quest_piste_difficulty_novice
    "JP" -> R.drawable.ic_quest_piste_difficulty_easy
    else -> R.drawable.ic_quest_piste_difficulty_intermediate
}

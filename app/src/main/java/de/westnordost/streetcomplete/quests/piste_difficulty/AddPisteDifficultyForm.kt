package de.westnordost.streetcomplete.quests.piste_difficulty

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddPisteDifficultyForm : AImageListQuestForm<PisteDifficulty, List<PisteDifficulty>>() {

    override val items = PisteDifficulty.values().map { it.asItem() }
    override val itemsPerRow = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(selectedItems: List<PisteDifficulty>) {
        applyAnswer(selectedItems)
    }
}

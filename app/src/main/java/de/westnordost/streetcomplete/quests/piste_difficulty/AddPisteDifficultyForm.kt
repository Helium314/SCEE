package de.westnordost.streetcomplete.quests.piste_difficulty

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.Item

class AddPisteDifficultyForm : AImageListQuestForm<PisteDifficulty, PisteDifficulty>() {

    override val items get() = PisteDifficulty.values().mapNotNull { it.asItem(countryInfo.countryCode) }
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(selectedItems: List<PisteDifficulty>) {
        applyAnswer(selectedItems.single())
    }
}

fun PisteDifficulty.asItem(countryCode: String) = if (this == PisteDifficulty.NOVICE && countryCode in listOf("JP", "US", "CA", "NZ", "AU")) null
else if (this == PisteDifficulty.EXPERT && countryCode == "JP") null
else if (this == PisteDifficulty.FREERIDE && countryCode == "JP") null
else if (this == PisteDifficulty.EXTREME && countryCode == "JP") null
else  Item(this, getIconResId(countryCode), titleResId)

private val PisteDifficulty.titleResId: Int get() = when (this) {
    PisteDifficulty.NOVICE -> R.string.quest_piste_difficulty_novice
    PisteDifficulty.EASY -> R.string.quest_piste_difficulty_easy
    PisteDifficulty.INTERMEDIATE -> R.string.quest_piste_difficulty_intermediate
    PisteDifficulty.ADVANCED -> R.string.quest_piste_difficulty_advanced
    PisteDifficulty.EXPERT -> R.string.quest_piste_difficulty_expert
    PisteDifficulty.FREERIDE -> R.string.quest_piste_difficulty_freeride
    PisteDifficulty.EXTREME -> R.string.quest_piste_difficulty_extreme
}

private fun PisteDifficulty.getIconResId(countryCode: String): Int = when (this) {
    PisteDifficulty.NOVICE -> R.drawable.ic_quest_piste_difficulty_novice
    PisteDifficulty.EASY ->    if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) R.drawable.ic_quest_piste_difficulty_novice else R.drawable.ic_quest_piste_difficulty_easy
    PisteDifficulty.INTERMEDIATE ->    if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) R.drawable.ic_quest_piste_difficulty_blue_square else R.drawable.ic_quest_piste_difficulty_intermediate
    PisteDifficulty.ADVANCED ->    if (countryCode in listOf("US", "CA", "NZ", "AU", "FI", "SE", "NO")) R.drawable.ic_quest_piste_difficulty_black_diamond else R.drawable.ic_quest_piste_difficulty_advanced
    PisteDifficulty.EXPERT ->    if (countryCode in listOf("US", "CA", "NZ", "AU", "FI", "SE", "NO")) R.drawable.ic_quest_piste_difficulty_double_black_diamond else R.drawable.ic_quest_piste_difficulty_expert
    PisteDifficulty.FREERIDE ->    if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) R.drawable.ic_quest_piste_difficulty_orange_oval else R.drawable.ic_quest_piste_difficulty_freeride
    PisteDifficulty.EXTREME ->    R.drawable.ic_quest_piste_difficulty_extreme
}

package de.westnordost.streetcomplete.quests.piste_difficulty

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddPisteDifficultyForm : AImageListQuestForm<PisteDifficulty, PisteDifficulty>() {

    override val items get() = values().mapNotNull { it.asItem(countryInfo.countryCode) }
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

fun PisteDifficulty.asItem(countryCode: String) = if (this == NOVICE && countryCode in listOf("JP", "US", "CA", "NZ", "AU")) null
else if (this == EXPERT && countryCode == "JP") null
else if (this == FREERIDE && countryCode == "JP") null
else if (this == EXTREME && countryCode == "JP") null
else  Item(this, getIconResId(countryCode), titleResId)

private val PisteDifficulty.titleResId: Int get() = when (this) {
    NOVICE -> R.string.quest_piste_difficulty_novice
    EASY -> R.string.quest_piste_difficulty_easy
    INTERMEDIATE -> R.string.quest_piste_difficulty_intermediate
    ADVANCED -> R.string.quest_piste_difficulty_advanced
    EXPERT -> R.string.quest_piste_difficulty_expert
    FREERIDE -> R.string.quest_piste_difficulty_freeride
    EXTREME -> R.string.quest_piste_difficulty_extreme
}

private fun PisteDifficulty.getIconResId(countryCode: String): Int = when (this) {
    NOVICE -> R.drawable.ic_quest_piste_difficulty_novice
    EASY ->    if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) R.drawable.ic_quest_piste_difficulty_novice else R.drawable.ic_quest_piste_difficulty_easy
    INTERMEDIATE ->    if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) R.drawable.ic_quest_piste_difficulty_blue_square else R.drawable.ic_quest_piste_difficulty_intermediate
    ADVANCED ->    if (countryCode in listOf("US", "CA", "NZ", "AU", "FI", "SE", "NO")) R.drawable.ic_quest_piste_difficulty_black_diamond else R.drawable.ic_quest_piste_difficulty_advanced
    EXPERT ->    if (countryCode in listOf("US", "CA", "NZ", "AU", "FI", "SE", "NO")) R.drawable.ic_quest_piste_difficulty_double_black_diamond else R.drawable.ic_quest_piste_difficulty_expert
    FREERIDE ->    if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) R.drawable.ic_quest_piste_difficulty_orange_oval else R.drawable.ic_quest_piste_difficulty_freeride
    EXTREME ->    R.drawable.ic_quest_piste_difficulty_extreme
}

package de.westnordost.streetcomplete.quests.piste_difficulty

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item

fun PisteDifficulty.asItem() = Item(this, iconResId, titleResId)

private val PisteDifficulty.titleResId: Int get() = when (this) {
    PisteDifficulty.NOVICE -> R.string.quest_piste_difficulty_novice
    PisteDifficulty.EASY -> R.string.quest_piste_difficulty_easy
    PisteDifficulty.INTERMEDIATE -> R.string.quest_piste_difficulty_intermediate
    PisteDifficulty.ADVANCED -> R.string.quest_piste_difficulty_advanced
    PisteDifficulty.EXPERT -> R.string.quest_piste_difficulty_expert
    PisteDifficulty.FREERIDE -> R.string.quest_piste_difficulty_freeride
    PisteDifficulty.EXTREME -> R.string.quest_piste_difficulty_extreme
}

private val PisteDifficulty.iconResId: Int get() = when (this) {
    PisteDifficulty.NOVICE -> R.drawable.ic_quest_piste_difficulty_novice
    PisteDifficulty.EASY ->    R.drawable.ic_quest_piste_difficulty_easy
    PisteDifficulty.INTERMEDIATE ->    R.drawable.ic_quest_piste_difficulty_intermediate
    PisteDifficulty.ADVANCED ->    R.drawable.ic_quest_piste_difficulty_advanced
    PisteDifficulty.EXPERT ->    R.drawable.ic_quest_piste_difficulty_expert
    PisteDifficulty.FREERIDE ->    R.drawable.ic_quest_piste_difficulty_freeride
    PisteDifficulty.EXTREME ->    R.drawable.ic_quest_piste_difficulty_extreme
}

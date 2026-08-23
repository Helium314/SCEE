package de.westnordost.streetcomplete.quests.piste_difficulty

import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.ADVANCED
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.EASY
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.EXPERT
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.EXTREME
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.FREERIDE
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.INTERMEDIATE
import de.westnordost.streetcomplete.quests.piste_difficulty.PisteDifficulty.NOVICE
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

enum class PisteDifficulty(val osmValue: String) {
    NOVICE("novice"),
    EASY("easy"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced"),
    EXPERT("expert"),
    FREERIDE("freeride"),
    EXTREME("extreme")
}

fun PisteDifficulty.isAvailable(countryCode: String) = when {
    this == NOVICE && countryCode in listOf("JP", "US", "CA", "NZ", "AU") -> false
    this == EXPERT && countryCode == "JP" -> false
    this == FREERIDE && countryCode == "JP" -> false
    this == EXTREME && countryCode == "JP" -> false
    else -> true
}

val PisteDifficulty.title get() = when (this) {
    NOVICE -> Res.string.quest_piste_difficulty_novice
    EASY -> Res.string.quest_piste_difficulty_easy
    INTERMEDIATE -> Res.string.quest_piste_difficulty_intermediate
    ADVANCED -> Res.string.quest_piste_difficulty_advanced
    EXPERT -> Res.string.quest_piste_difficulty_expert
    FREERIDE -> Res.string.quest_piste_difficulty_freeride
    EXTREME -> Res.string.quest_piste_difficulty_extreme
}

fun PisteDifficulty.getIcon(countryCode: String): DrawableResource = when (this) {
    NOVICE ->       Res.drawable.ic_quest_piste_difficulty_novice
    EASY ->         if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) Res.drawable.ic_quest_piste_difficulty_novice
    else Res.drawable.ic_quest_piste_difficulty_easy
    INTERMEDIATE -> if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) Res.drawable.ic_quest_piste_difficulty_blue_square
    else Res.drawable.ic_quest_piste_difficulty_intermediate
    ADVANCED ->     if (countryCode in listOf("US", "CA", "NZ", "AU", "FI", "SE", "NO")) Res.drawable.ic_quest_piste_difficulty_black_diamond
    else Res.drawable.ic_quest_piste_difficulty_advanced
    EXPERT ->       if (countryCode in listOf("US", "CA", "NZ", "AU", "FI", "SE", "NO")) Res.drawable.ic_quest_piste_difficulty_double_black_diamond
    else Res.drawable.ic_quest_piste_difficulty_expert
    FREERIDE ->     if (countryCode in listOf("JP", "US", "CA", "NZ", "AU")) Res.drawable.ic_quest_piste_difficulty_orange_oval
    else Res.drawable.ic_quest_piste_difficulty_freeride
    EXTREME ->      Res.drawable.ic_quest_piste_difficulty_extreme
}

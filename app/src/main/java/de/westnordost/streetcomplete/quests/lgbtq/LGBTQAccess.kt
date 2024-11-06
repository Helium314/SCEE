package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.TextItem

enum class LGBTQAccess(
    val osmValue: String?,
    val titleId: Int,
) {
    NO("no", R.string.quest_lgbtq_access_no),
    WELCOME("welcome", R.string.quest_lgbtq_access_welcome),
    PRIMARY("primary", R.string.quest_lgbtq_access_primary),
    ONLY("only", R.string.quest_lgbtq_access_only),
    UNKNOWN(null, R.string.quest_lgbtq_access_not_marked),
}

fun List<LGBTQAccess>.toItems() = this.map { it.asItem() }
fun LGBTQAccess.asItem(): TextItem<LGBTQAccess> = TextItem(this, this.titleId)

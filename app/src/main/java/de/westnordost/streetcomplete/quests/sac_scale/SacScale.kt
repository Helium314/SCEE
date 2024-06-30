package de.westnordost.streetcomplete.quests.sac_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.sac_scale.SacScale.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class SacScale(val osmValue: String) {
    HIKING("hiking"),
    MOUNTAIN_HIKING("mountain_hiking"),
    DEMANDING_MOUNTAIN_HIKING("demanding_mountain_hiking"),
    ALPINE_HIKING("alpine_hiking"),
    DEMANDING_ALPINE_HIKING("demanding_alpine_hiking"),
    DIFFICULT_ALPINE_HIKING("difficult_alpine_hiking")
}
fun Collection<SacScale>.toItems() = map { it.asItem() }

fun SacScale.asItem(): GroupableDisplayItem<SacScale> {
    return Item(this, null, titleResId, descriptionResId)
}

private val SacScale.titleResId: Int get() = when (this) {
    HIKING -> R.string.quest_sacScale_one
    MOUNTAIN_HIKING -> R.string.quest_sacScale_two
    DEMANDING_MOUNTAIN_HIKING -> R.string.quest_sacScale_three
    ALPINE_HIKING -> R.string.quest_sacScale_four
    DEMANDING_ALPINE_HIKING -> R.string.quest_sacScale_five
    DIFFICULT_ALPINE_HIKING -> R.string.quest_sacScale_six
}

private val SacScale.descriptionResId: Int get() = when (this) {
    HIKING -> R.string.quest_sacScale_one_description
    MOUNTAIN_HIKING -> R.string.quest_sacScale_two_description
    DEMANDING_MOUNTAIN_HIKING -> R.string.quest_sacScale_three_description
    ALPINE_HIKING -> R.string.quest_sacScale_four_description
    DEMANDING_ALPINE_HIKING -> R.string.quest_sacScale_five_description
    DIFFICULT_ALPINE_HIKING -> R.string.quest_sacScale_six_description
}

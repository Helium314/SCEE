package de.westnordost.streetcomplete.quests.mtb_scale

import de.westnordost.streetcomplete.R

import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.*

enum class MtbScale(val osmValue: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6")
}

fun Collection<MtbScale>.toItems() = map { it.asItem() }

fun MtbScale.asItem(): GroupableDisplayItem<MtbScale> {
    return Item(
        this,
        drawableId = imageResId,
        titleId = titleResId,
        descriptionId = descriptionResId
    )
}

private val MtbScale.imageResId: Int?
    get() = when (this) {
        ZERO -> R.drawable.mtb_scale_0
        ONE -> R.drawable.mtb_scale_1
        TWO -> R.drawable.mtb_scale_2
        THREE -> R.drawable.mtb_scale_3
        FOUR -> R.drawable.mtb_scale_4
        FIVE -> R.drawable.mtb_scale_5
        SIX -> null
    }

private val MtbScale.titleResId: Int
    get() = when (this) {
        ZERO -> R.string.quest_mtbScale_zero
        ONE -> R.string.quest_mtbScale_one
        TWO -> R.string.quest_mtbScale_two
        THREE -> R.string.quest_mtbScale_three
        FOUR -> R.string.quest_mtbScale_four
        FIVE -> R.string.quest_mtbScale_five
        SIX -> R.string.quest_mtbScale_six
    }

private val MtbScale.descriptionResId: Int
    get() = when (this) {
        ZERO -> R.string.quest_mtbScale_zero_description
        ONE -> R.string.quest_mtbScale_one_description
        TWO -> R.string.quest_mtbScale_two_description
        THREE -> R.string.quest_mtbScale_three_description
        FOUR -> R.string.quest_mtbScale_four_description
        FIVE -> R.string.quest_mtbScale_five_description
        SIX -> R.string.quest_mtbScale_six_description
    }

package de.westnordost.streetcomplete.quests.guidepost_sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.guidepost_sport.GuidepostSport.*
import de.westnordost.streetcomplete.view.image_select.Item

fun GuidepostSport.asItem(): Item<List<GuidepostSport>> =
    Item(listOf(this), iconResId, titleResId)

fun List<GuidepostSport>.asItem(): Item<List<GuidepostSport>> =
    Item(this, iconResId, titleResId)

private val GuidepostSport.iconResId: Int get() = when (this) {
    HIKING ->          R.drawable.ic_recycling_glass_bottles
    BICYCLE ->         R.drawable.ic_recycling_glass
    MTB ->             R.drawable.ic_recycling_paper
    CLIMBING ->        R.drawable.ic_recycling_plastic
    HORSE ->           R.drawable.ic_recycling_plastic_packaging
    NORDIC_WALKING ->  R.drawable.ic_recycling_plastic_bottles
    SKI ->             R.drawable.ic_recycling_pet
    INLINE_SKATING ->  R.drawable.ic_recycling_beverage_cartons
    RUNNING ->         R.drawable.ic_recycling_cans
    WINTER_HIKING ->   R.drawable.ic_recycling_cans
}

private val GuidepostSport.titleResId: Int get() = when (this) {
    HIKING ->          R.string.quest_guidepost_sports_hiking
    BICYCLE ->         R.string.quest_guidepost_sports_bicycle
    MTB ->             R.string.quest_guidepost_sports_mtb
    CLIMBING ->        R.string.quest_guidepost_sports_climbing
    HORSE ->           R.string.quest_guidepost_sports_horse
    NORDIC_WALKING ->  R.string.quest_guidepost_sports_nordic_walking
    SKI ->             R.string.quest_guidepost_sports_ski
    INLINE_SKATING ->  R.string.quest_guidepost_sports_inline_skating
    RUNNING ->         R.string.quest_guidepost_sports_running
    WINTER_HIKING ->   R.string.quest_guidepost_sports_winter_hiking
}

private val List<GuidepostSport>.iconResId: Int get() = when (this) {
    listOf(HIKING, BICYCLE) -> R.drawable.ic_recycling_plastic_bottles_and_cartons
    else -> first().iconResId
}

private val List<GuidepostSport>.titleResId: Int get() = when (this) {
    listOf(HIKING, BICYCLE) -> R.string.quest_recycling_type_plastic_bottles_and_cartons
    else -> first().titleResId
}

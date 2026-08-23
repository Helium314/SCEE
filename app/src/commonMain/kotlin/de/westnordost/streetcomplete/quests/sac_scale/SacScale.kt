package de.westnordost.streetcomplete.quests.sac_scale

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class SacScale(
    val osmValue: String,
    val imageResId: DrawableResource,
    val titleResId: StringResource,
    val descriptionResId: StringResource,
) {
    STROLLING(
        osmValue = "strolling",
        imageResId = Res.drawable.sac_scale_strolling,
        titleResId = Res.string.quest_sacScale_strolling,
        descriptionResId = Res.string.quest_sacScale_strolling_description
    ),
    HIKING(
        osmValue = "hiking",
        imageResId = Res.drawable.sac_scale_t1,
        titleResId = Res.string.quest_sacScale_one,
        descriptionResId = Res.string.quest_sacScale_one_description
    ),
    MOUNTAIN_HIKING(
        osmValue = "mountain_hiking",
        imageResId = Res.drawable.sac_scale_t2,
        titleResId = Res.string.quest_sacScale_two,
        descriptionResId = Res.string.quest_sacScale_two_description
    ),
    DEMANDING_MOUNTAIN_HIKING(
        osmValue = "demanding_mountain_hiking",
        imageResId = Res.drawable.sac_scale_t3,
        titleResId = Res.string.quest_sacScale_three,
        descriptionResId = Res.string.quest_sacScale_three_description
    ),
    ALPINE_HIKING(
        osmValue = "alpine_hiking",
        imageResId = Res.drawable.sac_scale_t4,
        titleResId = Res.string.quest_sacScale_four,
        descriptionResId = Res.string.quest_sacScale_four_description
    ),
    DEMANDING_ALPINE_HIKING(
        osmValue = "demanding_alpine_hiking",
        imageResId = Res.drawable.sac_scale_t5,
        titleResId = Res.string.quest_sacScale_five,
        descriptionResId = Res.string.quest_sacScale_five_description
    ),
    DIFFICULT_ALPINE_HIKING(
        osmValue = "difficult_alpine_hiking",
        imageResId = Res.drawable.sac_scale_t6,
        titleResId = Res.string.quest_sacScale_six,
        descriptionResId = Res.string.quest_sacScale_six_description
    )
}

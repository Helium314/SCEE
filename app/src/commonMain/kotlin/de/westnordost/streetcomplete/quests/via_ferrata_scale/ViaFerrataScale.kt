package de.westnordost.streetcomplete.quests.via_ferrata_scale

import de.westnordost.streetcomplete.quests.via_ferrata_scale.ViaFerrataScale.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

enum class ViaFerrataScale(val osmValue: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6")
}

val ViaFerrataScale.imageRes get() = when (this) {
    ZERO -> Res.drawable.via_ferrata_scale_0
    ONE -> Res.drawable.via_ferrata_scale_1
    TWO -> Res.drawable.via_ferrata_scale_2
    THREE -> Res.drawable.via_ferrata_scale_3
    FOUR -> Res.drawable.via_ferrata_scale_4
    FIVE -> Res.drawable.via_ferrata_scale_5
    SIX -> Res.drawable.via_ferrata_scale_6
}

val ViaFerrataScale.titleRes get() = when (this) {
    ZERO -> Res.string.quest_viaFerrataScale_zero
    ONE -> Res.string.quest_viaFerrataScale_one
    TWO -> Res.string.quest_viaFerrataScale_two
    THREE -> Res.string.quest_viaFerrataScale_three
    FOUR -> Res.string.quest_viaFerrataScale_four
    FIVE -> Res.string.quest_viaFerrataScale_five
    SIX -> Res.string.quest_viaFerrataScale_six
}

val ViaFerrataScale.descriptionRes get() = when (this) {
    ZERO -> Res.string.quest_viaFerrataScale_zero_description
    ONE -> Res.string.quest_viaFerrataScale_one_description
    TWO -> Res.string.quest_viaFerrataScale_two_description
    THREE -> Res.string.quest_viaFerrataScale_three_description
    FOUR -> Res.string.quest_viaFerrataScale_four_description
    FIVE -> Res.string.quest_viaFerrataScale_five_description
    SIX -> Res.string.quest_viaFerrataScale_six_description
}

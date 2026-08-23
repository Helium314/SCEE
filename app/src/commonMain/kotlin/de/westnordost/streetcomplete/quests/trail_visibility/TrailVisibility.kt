package de.westnordost.streetcomplete.quests.trail_visibility

import de.westnordost.streetcomplete.quests.trail_visibility.TrailVisibility.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

enum class TrailVisibility(val osmValue: String) {
    EXCELLENT("excellent"),
    GOOD("good"),
    INTERMEDIATE("intermediate"),
    BAD("bad"),
    HORRIBLE("horrible"),
    NO("no")
}

val TrailVisibility.titleRes get() = when (this) {
    EXCELLENT -> Res.string.quest_trail_visibility_excellent
    GOOD -> Res.string.quest_trail_visibility_good
    INTERMEDIATE -> Res.string.quest_trail_visibility_intermediate
    BAD -> Res.string.quest_trail_visibility_bad
    HORRIBLE -> Res.string.quest_trail_visibility_horrible
    NO -> Res.string.quest_trail_visibility_no
}

val TrailVisibility.descriptionRes get() = when (this) {
    EXCELLENT -> Res.string.quest_trail_visibility_excellent_description
    GOOD -> Res.string.quest_trail_visibility_good_description
    INTERMEDIATE -> Res.string.quest_trail_visibility_intermediate_description
    BAD -> Res.string.quest_trail_visibility_bad_description
    HORRIBLE -> Res.string.quest_trail_visibility_horrible_description
    NO -> Res.string.quest_trail_visibility_no_description
}

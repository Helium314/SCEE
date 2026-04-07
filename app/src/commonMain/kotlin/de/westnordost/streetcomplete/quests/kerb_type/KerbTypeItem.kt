package de.westnordost.streetcomplete.quests.kerb_type

import de.westnordost.streetcomplete.quests.kerb_type.KerbType.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val KerbType.title: StringResource get() = when (this) {
    RAISED -> Res.string.quest_kerb_type_raised
    LOWERED -> Res.string.quest_kerb_type_lowered
    FLUSH -> Res.string.quest_kerb_type_flush
    REGULAR -> Res.string.quest_kerb_type_regular_unspecified
}

val KerbType.icon: DrawableResource get() = when (this) {
    RAISED -> Res.drawable.kerb_height_raised
    LOWERED -> Res.drawable.kerb_height_lowered
    FLUSH -> Res.drawable.kerb_height_flush
    REGULAR -> Res.drawable.kerb_height_no
}

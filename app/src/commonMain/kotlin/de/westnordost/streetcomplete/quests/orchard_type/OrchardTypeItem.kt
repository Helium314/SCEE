package de.westnordost.streetcomplete.quests.orchard_type

import de.westnordost.streetcomplete.quests.orchard_type.OrchardType.MEADOW_ORCHARD
import de.westnordost.streetcomplete.quests.orchard_type.OrchardType.PLANTATION
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val OrchardType.title: StringResource get() = when (this) {
    MEADOW_ORCHARD -> Res.string.quest_orchard_type_meadow_orchard
    PLANTATION ->     Res.string.quest_orchard_type_plantation
}

val OrchardType.description: StringResource get() = when (this) {
    MEADOW_ORCHARD -> Res.string.quest_orchard_type_meadow_orchard_description
    PLANTATION ->     Res.string.quest_orchard_type_plantation_description
}

val OrchardType.icon: DrawableResource get() = when (this) {
    MEADOW_ORCHARD -> Res.drawable.orchard_type_meadow_orchard
    PLANTATION ->     Res.drawable.orchard_type_plantation
}

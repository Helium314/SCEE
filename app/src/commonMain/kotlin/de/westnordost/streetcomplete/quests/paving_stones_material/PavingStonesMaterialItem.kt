package de.westnordost.streetcomplete.quests.paving_stones_material

import de.westnordost.streetcomplete.quests.paving_stones_material.PavingStonesMaterial.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.paving_stones_brick
import de.westnordost.streetcomplete.resources.paving_stones_concrete
import de.westnordost.streetcomplete.resources.paving_stones_stone
import de.westnordost.streetcomplete.resources.quest_material_brick
import de.westnordost.streetcomplete.resources.quest_material_concrete
import de.westnordost.streetcomplete.resources.quest_material_stone
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val PavingStonesMaterial.title: StringResource get() = when (this) {
    BRICK ->    Res.string.quest_material_brick
    CONCRETE -> Res.string.quest_material_concrete
    STONE ->    Res.string.quest_material_stone
}

val PavingStonesMaterial.icon: DrawableResource get() = when (this) {
    BRICK ->    Res.drawable.paving_stones_brick
    CONCRETE -> Res.drawable.paving_stones_concrete
    STONE ->    Res.drawable.paving_stones_stone
}

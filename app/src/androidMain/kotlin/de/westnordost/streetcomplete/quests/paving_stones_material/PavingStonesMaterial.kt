package de.westnordost.streetcomplete.quests.paving_stones_material

import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.paving_stones_brick
import de.westnordost.streetcomplete.resources.paving_stones_concrete
import de.westnordost.streetcomplete.resources.paving_stones_stone
import org.jetbrains.compose.resources.DrawableResource

enum class PavingStonesMaterial(
    val osmValue: String,
    val imageResId: DrawableResource,
    @StringRes val titleResId: Int,
) {
    BRICK(
        osmValue = "brick",
        imageResId = Res.drawable.paving_stones_brick,
        titleResId = R.string.quest_material_brick
    ),
    CONCRETE(
        osmValue = "concrete",
        imageResId = Res.drawable.paving_stones_concrete,
        titleResId = R.string.quest_material_concrete
    ),
    STONE(
        osmValue = "stone",
        imageResId = Res.drawable.paving_stones_stone,
        titleResId = R.string.quest_material_stone
    )
}

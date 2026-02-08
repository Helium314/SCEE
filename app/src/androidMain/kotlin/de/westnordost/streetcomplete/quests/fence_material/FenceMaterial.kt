package de.westnordost.streetcomplete.quests.fence_material

import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.fence_material_brick
import de.westnordost.streetcomplete.resources.fence_material_concrete
import de.westnordost.streetcomplete.resources.fence_material_glass
import de.westnordost.streetcomplete.resources.fence_material_metal
import de.westnordost.streetcomplete.resources.fence_material_plastic
import de.westnordost.streetcomplete.resources.fence_material_stone
import de.westnordost.streetcomplete.resources.fence_material_wire
import de.westnordost.streetcomplete.resources.fence_material_wood
import org.jetbrains.compose.resources.DrawableResource

enum class FenceMaterial(
    val osmValue: String?,
    val imageResId: DrawableResource,
    @StringRes val titleResId: Int,
) {
    WOOD(
        osmValue = "wood",
        imageResId = Res.drawable.fence_material_wood,
        titleResId = R.string.quest_material_wood
    ),
    METAL(
        osmValue = "metal",
        imageResId = Res.drawable.fence_material_metal,
        titleResId = R.string.quest_material_metal
    ),
    CONCRETE(
        osmValue = "concrete",
        imageResId = Res.drawable.fence_material_concrete,
        titleResId = R.string.quest_material_concrete
    ),
    STONE(
        osmValue = "stone",
        imageResId = Res.drawable.fence_material_stone,
        titleResId = R.string.quest_material_stone
    ),
    BRICK(
        osmValue = "brick",
        imageResId = Res.drawable.fence_material_brick,
        titleResId = R.string.quest_material_brick
    ),
    PLASTIC(
        osmValue = "plastic",
        imageResId = Res.drawable.fence_material_plastic,
        titleResId = R.string.quest_material_plastic
    ),
    WIRE(
        osmValue = null, // special case
        imageResId = Res.drawable.fence_material_wire,
        titleResId = R.string.quest_fence_material_wire
    ),
    GLASS(
        osmValue = "glass",
        imageResId = Res.drawable.fence_material_glass,
        titleResId = R.string.quest_material_glass
    )
}

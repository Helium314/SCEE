package de.westnordost.streetcomplete.quests.fence_material

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class FenceMaterial(
    val materialValue: String?,
    val fenceTypeValue: String?,
    val imageResId: DrawableResource,
    val titleResId: StringResource
) {
    WOOD(
        materialValue = "wood",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_wood,
        titleResId = Res.string.quest_material_wood
    ),
    METAL(
        materialValue = "metal",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_metal,
        titleResId = Res.string.quest_material_metal
    ),
    CONCRETE(
        materialValue = "concrete",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_concrete,
        titleResId = Res.string.quest_material_concrete
    ),
    STONE(
        materialValue = "stone",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_stone,
        titleResId = Res.string.quest_material_stone
    ),
    BRICK(
        materialValue = "brick",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_brick,
        titleResId = Res.string.quest_material_brick
    ),
    PLASTIC(
        materialValue = "plastic",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_plastic,
        titleResId = Res.string.quest_material_plastic
    ),
    WIRE(
        materialValue = "metal",
        fenceTypeValue = "wire",
        imageResId = Res.drawable.fence_material_wire,
        titleResId = Res.string.quest_fence_material_wire
    ),
    GLASS(
        materialValue = "glass",
        fenceTypeValue = null,
        imageResId = Res.drawable.fence_material_glass,
        titleResId = Res.string.quest_material_glass
    )
}

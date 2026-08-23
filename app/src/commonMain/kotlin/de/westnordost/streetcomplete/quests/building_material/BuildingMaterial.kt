package de.westnordost.streetcomplete.quests.building_material

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class BuildingMaterial(
    val osmValue: String,
    val imageRes: DrawableResource,
    val titleRes: StringResource,
) {
    CEMENT_BLOCK(
        osmValue = "cement_block",
        imageRes = Res.drawable.building_material_cement_block,
        titleRes = Res.string.quest_material_cement_block
    ),
    BRICK(
        osmValue = "brick",
        imageRes = Res.drawable.building_material_brick,
        titleRes = Res.string.quest_material_brick
    ),
    PLASTER(
        osmValue = "plaster",
        imageRes = Res.drawable.building_material_plaster,
        titleRes = Res.string.quest_material_plaster
    ),
    WOOD(
        osmValue = "wood",
        imageRes = Res.drawable.building_material_wood,
        titleRes = Res.string.quest_material_wood
    ),
    CONCRETE(
        osmValue = "concrete",
        imageRes = Res.drawable.building_material_concrete,
        titleRes = Res.string.quest_material_concrete
    ),
    METAL(
        osmValue = "metal",
        imageRes = Res.drawable.building_material_metal,
        titleRes = Res.string.quest_material_metal
    ),
    STONE(
        osmValue = "stone",
        imageRes = Res.drawable.building_material_stone,
        titleRes = Res.string.quest_material_stone
    ),
    GLASS(
        osmValue = "glass",
        imageRes = Res.drawable.building_material_glass,
        titleRes = Res.string.quest_material_glass
    ),
    MIRROR(
        osmValue = "mirror",
        imageRes = Res.drawable.building_material_mirror,
        titleRes = Res.string.quest_material_mirror
    ),
    MUD(
        osmValue = "mud",
        imageRes = Res.drawable.building_material_mud,
        titleRes = Res.string.quest_material_mud
    ),
    PLASTIC(
        osmValue = "plastic",
        imageRes = Res.drawable.building_material_plastic,
        titleRes = Res.string.quest_material_plastic
    ),
    TIMBER_FRAMING(
        osmValue = "timber_framing",
        imageRes = Res.drawable.building_material_timber_framing,
        titleRes = Res.string.quest_material_timber_framing
    ),
    SANDSTONE(
        osmValue = "sandstone",
        imageRes = Res.drawable.building_material_sandstone,
        titleRes = Res.string.quest_material_sandstone
    ),
    CLAY(
        osmValue = "clay",
        imageRes = Res.drawable.building_material_clay,
        titleRes = Res.string.quest_material_clay
    ),
    REED(
        osmValue = "reed",
        imageRes = Res.drawable.building_material_reed,
        titleRes = Res.string.quest_material_reed
    ),
    LOAM(
        osmValue = "loam",
        imageRes = Res.drawable.building_material_loam,
        titleRes = Res.string.quest_material_loam
    ),
    MARBLE(
        osmValue = "marble",
        imageRes = Res.drawable.building_material_marble,
        titleRes = Res.string.quest_material_marble
    ),
    SLATE(
        osmValue = "slate",
        imageRes = Res.drawable.building_material_slate,
        titleRes = Res.string.quest_material_slate
    ),
    VINYL(
        osmValue = "vinyl",
        imageRes = Res.drawable.building_material_vinyl,
        titleRes = Res.string.quest_material_vinyl
    ),
    LIMESTONE(
        osmValue = "limestone",
        imageRes = Res.drawable.building_material_limestone,
        titleRes = Res.string.quest_material_limestone
    ),
    TILES(
        osmValue = "tiles",
        imageRes = Res.drawable.building_material_tiles,
        titleRes = Res.string.quest_material_tiles
    ),
    BAMBOO(
        osmValue = "bamboo",
        imageRes = Res.drawable.building_material_bamboo,
        titleRes = Res.string.quest_material_bamboo
    ),
    ADOBE(
        osmValue = "adobe",
        imageRes = Res.drawable.building_material_adobe,
        titleRes = Res.string.quest_material_adobe
    ),
    PEBBLEDASH(
        osmValue = "pebbledash",
        imageRes = Res.drawable.building_material_pebbledash,
        titleRes = Res.string.quest_material_pebbledash
    )
}

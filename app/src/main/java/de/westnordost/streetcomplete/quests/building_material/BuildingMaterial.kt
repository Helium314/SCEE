package de.westnordost.streetcomplete.quests.building_material

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class BuildingMaterial(
    val osmValue: String,
    @DrawableRes val imageResId: Int,
    @StringRes val titleResId: Int,
) {
    CEMENT_BLOCK(
        osmValue = "cement_block",
        imageResId = R.drawable.building_material_cement_block,
        titleResId = R.string.quest_building_material_value_cement_block
    ),
    BRICK(
        osmValue = "brick",
        imageResId = R.drawable.building_material_brick,
        titleResId = R.string.quest_building_material_value_brick
    ),
    PLASTER(
        osmValue = "plaster",
        imageResId = R.drawable.building_material_plaster,
        titleResId = R.string.quest_building_material_value_plaster
    ),
    WOOD(
        osmValue = "wood",
        imageResId = R.drawable.building_material_wood,
        titleResId = R.string.quest_building_material_value_wood
    ),
    CONCRETE(
        osmValue = "concrete",
        imageResId = R.drawable.building_material_concrete,
        titleResId = R.string.quest_building_material_value_concrete
    ),
    METAL(
        osmValue = "metal",
        imageResId = R.drawable.building_material_metal,
        titleResId = R.string.quest_building_material_value_metal
    ),
    STEEL(
        osmValue = "steel",
        imageResId = R.drawable.building_material_steel,
        titleResId = R.string.quest_building_material_value_steel
    ),
    STONE(
        osmValue = "stone",
        imageResId = R.drawable.building_material_stone,
        titleResId = R.string.quest_building_material_value_stone
    ),
    GLASS(
        osmValue = "glass",
        imageResId = R.drawable.building_material_glass,
        titleResId = R.string.quest_building_material_value_glass
    ),
    MIRROR(
        osmValue = "mirror",
        imageResId = R.drawable.building_material_mirror,
        titleResId = R.string.quest_building_material_value_mirror
    ),
    MUD(
        osmValue = "mud",
        imageResId = R.drawable.building_material_mud,
        titleResId = R.string.quest_building_material_value_mud
    ),
    TIN(
        osmValue = "tin",
        imageResId = R.drawable.building_material_tin,
        titleResId = R.string.quest_building_material_value_tin
    ),
    PLASTIC(
        osmValue = "plastic",
        imageResId = R.drawable.building_material_plastic,
        titleResId = R.string.quest_building_material_value_plastic
    ),
    TIMBER_FRAMING(
        osmValue = "timber_framing",
        imageResId = R.drawable.building_material_timber_framing,
        titleResId = R.string.quest_building_material_value_timber_framing
    ),
    SANDSTONE(
        osmValue = "sandstone",
        imageResId = R.drawable.building_material_sandstone,
        titleResId = R.string.quest_building_material_value_sandstone
    ),
    CLAY(
        osmValue = "clay",
        imageResId = R.drawable.building_material_clay,
        titleResId = R.string.quest_building_material_value_clay
    ),
    REED(
        osmValue = "reed",
        imageResId = R.drawable.building_material_reed,
        titleResId = R.string.quest_building_material_value_reed
    ),
    LOAM(
        osmValue = "loam",
        imageResId = R.drawable.building_material_loam,
        titleResId = R.string.quest_building_material_value_loam
    ),
    MARBLE(
        osmValue = "marble",
        imageResId = R.drawable.building_material_marble,
        titleResId = R.string.quest_building_material_value_marble
    ),
    COPPER(
        osmValue = "copper",
        imageResId = R.drawable.building_material_copper,
        titleResId = R.string.quest_building_material_value_copper
    ),
    SLATE(
        osmValue = "slate",
        imageResId = R.drawable.building_material_slate,
        titleResId = R.string.quest_building_material_value_slate
    ),
    VINYL(
        osmValue = "vinyl",
        imageResId = R.drawable.building_material_vinyl,
        titleResId = R.string.quest_building_material_value_vinyl
    ),
    LIMESTONE(
        osmValue = "limestone",
        imageResId = R.drawable.building_material_limestone,
        titleResId = R.string.quest_building_material_value_limestone
    ),
    TILES(
        osmValue = "tiles",
        imageResId = R.drawable.building_material_tiles,
        titleResId = R.string.quest_building_material_value_tiles
    ),
    METAL_PLATES(
        osmValue = "metal_plates",
        imageResId = R.drawable.building_material_metal_plates,
        titleResId = R.string.quest_building_material_value_metal_plates
    ),
    BAMBOO(
        osmValue = "bamboo",
        imageResId = R.drawable.building_material_bamboo,
        titleResId = R.string.quest_building_material_value_bamboo
    ),
    ADOBE(
        osmValue = "adobe",
        imageResId = R.drawable.building_material_adobe,
        titleResId = R.string.quest_building_material_value_adobe
    )
}

fun Collection<BuildingMaterial>.toItems() = map { it.asItem() }

fun BuildingMaterial.asItem(): DisplayItem<BuildingMaterial> = Item(this, imageResId, titleResId)

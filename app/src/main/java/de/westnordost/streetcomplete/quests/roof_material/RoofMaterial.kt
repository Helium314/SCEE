package de.westnordost.streetcomplete.quests.roof_material

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class RoofMaterial(
    val osmValue: String,
    @DrawableRes val imageResId: Int,
    @StringRes val titleResId: Int,
) {
    ROOF_TILES(
        osmValue = "roof_tiles",
        imageResId = R.drawable.roof_material_roof_tiles,
        titleResId = R.string.quest_roof_material_value_roof_tiles
    ),
    METAL_SHEET(
        osmValue = "metal_sheet",
        imageResId = R.drawable.roof_material_metal_sheet,
        titleResId = R.string.quest_roof_material_value_metal_sheet
    ),
    CONCRETE(
        osmValue = "concrete",
        imageResId = R.drawable.roof_material_concrete,
        titleResId = R.string.quest_roof_material_value_concrete
    ),
    TAR_PAPER(
        osmValue = "tar_paper",
        imageResId = R.drawable.roof_material_tar_paper,
        titleResId = R.string.quest_roof_material_value_tar_paper
    ),
    ETERNIT(
        osmValue = "eternit",
        imageResId = R.drawable.roof_material_eternit,
        titleResId = R.string.quest_roof_material_value_eternit
    ),
    SLATE(
        osmValue = "slate",
        imageResId = R.drawable.roof_material_slate,
        titleResId = R.string.quest_roof_material_value_slate
    ),
    GLASS(
        osmValue = "glass",
        imageResId = R.drawable.roof_material_glass,
        titleResId = R.string.quest_roof_material_value_glass
    ),
    ACRYLIC_GLASS(
        osmValue = "acrylic_glass",
        imageResId = R.drawable.roof_material_acrylic_glass,
        titleResId = R.string.quest_roof_material_value_acrylic_glass
    ),
    TIN(
        osmValue = "tin",
        imageResId = R.drawable.roof_material_tin,
        titleResId = R.string.quest_roof_material_value_tin
    ),
    GRASS(
        osmValue = "grass",
        imageResId = R.drawable.roof_material_grass,
        titleResId = R.string.quest_roof_material_value_grass
    ),
    COPPER(
        osmValue = "copper",
        imageResId = R.drawable.roof_material_copper,
        titleResId = R.string.quest_roof_material_value_copper
    ),
    THATCH(
        osmValue = "thatch",
        imageResId = R.drawable.roof_material_thatch,
        titleResId = R.string.quest_roof_material_value_thatch
    ),
    GRAVEL(
        osmValue = "gravel",
        imageResId = R.drawable.roof_material_gravel,
        titleResId = R.string.quest_roof_material_value_gravel
    ),
    STONE(
        osmValue = "stone",
        imageResId = R.drawable.roof_material_stone,
        titleResId = R.string.quest_roof_material_value_stone
    ),
    WOOD(
        osmValue = "wood",
        imageResId = R.drawable.roof_material_wood,
        titleResId = R.string.quest_roof_material_value_wood
    ),
    PLASTIC(
        osmValue = "plastic",
        imageResId = R.drawable.roof_material_plastic,
        titleResId = R.string.quest_roof_material_value_plastic
    ),
    ASPHALT(
        osmValue = "asphalt",
        imageResId = R.drawable.roof_material_asphalt,
        titleResId = R.string.quest_roof_material_value_asphalt
    ),
    ASPHALT_SHINGLE(
        osmValue = "asphalt_shingle",
        imageResId = R.drawable.roof_material_asphalt_shingle,
        titleResId = R.string.quest_roof_material_value_asphalt_shingle
    ),
    ZINC(
        osmValue = "zinc",
        imageResId = R.drawable.roof_material_zinc,
        titleResId = R.string.quest_roof_material_value_zinc
    ),
    SANDSTONE(
        osmValue = "sandstone",
        imageResId = R.drawable.roof_material_sandstone,
        titleResId = R.string.quest_roof_material_value_sandstone
    ),
    BAMBOO(
        osmValue = "bamboo",
        imageResId = R.drawable.roof_material_bamboo,
        titleResId = R.string.quest_roof_material_value_bamboo
    ),
    PALM_LEAVES(
        osmValue = "palm_leaves",
        imageResId = R.drawable.roof_material_palm_leaves,
        titleResId = R.string.quest_roof_material_value_palm_leaves
    ),
    SOLAR_PANELS(
        osmValue = "solar_panels",
        imageResId = R.drawable.roof_material_solar_panels,
        titleResId = R.string.quest_roof_material_value_solar_panels
    ),
}

fun Collection<RoofMaterial>.toItems() = map { it.asItem() }

fun RoofMaterial.asItem(): DisplayItem<RoofMaterial> = Item(this, imageResId, titleResId)

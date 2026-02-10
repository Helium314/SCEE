package de.westnordost.streetcomplete.quests.building_material

import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.building_material_adobe
import de.westnordost.streetcomplete.resources.building_material_bamboo
import de.westnordost.streetcomplete.resources.building_material_brick
import de.westnordost.streetcomplete.resources.building_material_cement_block
import de.westnordost.streetcomplete.resources.building_material_clay
import de.westnordost.streetcomplete.resources.building_material_concrete
import de.westnordost.streetcomplete.resources.building_material_glass
import de.westnordost.streetcomplete.resources.building_material_limestone
import de.westnordost.streetcomplete.resources.building_material_loam
import de.westnordost.streetcomplete.resources.building_material_marble
import de.westnordost.streetcomplete.resources.building_material_metal
import de.westnordost.streetcomplete.resources.building_material_mirror
import de.westnordost.streetcomplete.resources.building_material_mud
import de.westnordost.streetcomplete.resources.building_material_pebbledash
import de.westnordost.streetcomplete.resources.building_material_plaster
import de.westnordost.streetcomplete.resources.building_material_plastic
import de.westnordost.streetcomplete.resources.building_material_reed
import de.westnordost.streetcomplete.resources.building_material_sandstone
import de.westnordost.streetcomplete.resources.building_material_slate
import de.westnordost.streetcomplete.resources.building_material_stone
import de.westnordost.streetcomplete.resources.building_material_tiles
import de.westnordost.streetcomplete.resources.building_material_timber_framing
import de.westnordost.streetcomplete.resources.building_material_vinyl
import de.westnordost.streetcomplete.resources.building_material_wood
import org.jetbrains.compose.resources.DrawableResource

enum class BuildingMaterial(
    val osmValue: String,
    val imageResId: DrawableResource,
    @StringRes val titleResId: Int,
) {
    CEMENT_BLOCK(
        osmValue = "cement_block",
        imageResId = Res.drawable.building_material_cement_block,
        titleResId = R.string.quest_material_cement_block
    ),
    BRICK(
        osmValue = "brick",
        imageResId = Res.drawable.building_material_brick,
        titleResId = R.string.quest_material_brick
    ),
    PLASTER(
        osmValue = "plaster",
        imageResId = Res.drawable.building_material_plaster,
        titleResId = R.string.quest_material_plaster
    ),
    WOOD(
        osmValue = "wood",
        imageResId = Res.drawable.building_material_wood,
        titleResId = R.string.quest_material_wood
    ),
    CONCRETE(
        osmValue = "concrete",
        imageResId = Res.drawable.building_material_concrete,
        titleResId = R.string.quest_material_concrete
    ),
    METAL(
        osmValue = "metal",
        imageResId = Res.drawable.building_material_metal,
        titleResId = R.string.quest_material_metal
    ),
    STONE(
        osmValue = "stone",
        imageResId = Res.drawable.building_material_stone,
        titleResId = R.string.quest_material_stone
    ),
    GLASS(
        osmValue = "glass",
        imageResId = Res.drawable.building_material_glass,
        titleResId = R.string.quest_material_glass
    ),
    MIRROR(
        osmValue = "mirror",
        imageResId = Res.drawable.building_material_mirror,
        titleResId = R.string.quest_material_mirror
    ),
    MUD(
        osmValue = "mud",
        imageResId = Res.drawable.building_material_mud,
        titleResId = R.string.quest_material_mud
    ),
    PLASTIC(
        osmValue = "plastic",
        imageResId = Res.drawable.building_material_plastic,
        titleResId = R.string.quest_material_plastic
    ),
    TIMBER_FRAMING(
        osmValue = "timber_framing",
        imageResId = Res.drawable.building_material_timber_framing,
        titleResId = R.string.quest_material_timber_framing
    ),
    SANDSTONE(
        osmValue = "sandstone",
        imageResId = Res.drawable.building_material_sandstone,
        titleResId = R.string.quest_material_sandstone
    ),
    CLAY(
        osmValue = "clay",
        imageResId = Res.drawable.building_material_clay,
        titleResId = R.string.quest_material_clay
    ),
    REED(
        osmValue = "reed",
        imageResId = Res.drawable.building_material_reed,
        titleResId = R.string.quest_material_reed
    ),
    LOAM(
        osmValue = "loam",
        imageResId = Res.drawable.building_material_loam,
        titleResId = R.string.quest_material_loam
    ),
    MARBLE(
        osmValue = "marble",
        imageResId = Res.drawable.building_material_marble,
        titleResId = R.string.quest_material_marble
    ),
    SLATE(
        osmValue = "slate",
        imageResId = Res.drawable.building_material_slate,
        titleResId = R.string.quest_material_slate
    ),
    VINYL(
        osmValue = "vinyl",
        imageResId = Res.drawable.building_material_vinyl,
        titleResId = R.string.quest_material_vinyl
    ),
    LIMESTONE(
        osmValue = "limestone",
        imageResId = Res.drawable.building_material_limestone,
        titleResId = R.string.quest_material_limestone
    ),
    TILES(
        osmValue = "tiles",
        imageResId = Res.drawable.building_material_tiles,
        titleResId = R.string.quest_material_tiles
    ),
    BAMBOO(
        osmValue = "bamboo",
        imageResId = Res.drawable.building_material_bamboo,
        titleResId = R.string.quest_material_bamboo
    ),
    ADOBE(
        osmValue = "adobe",
        imageResId = Res.drawable.building_material_adobe,
        titleResId = R.string.quest_material_adobe
    ),
    PEBBLEDASH(
        osmValue = "pebbledash",
        imageResId = Res.drawable.building_material_pebbledash,
        titleResId = R.string.quest_material_pebbledash
    )
}

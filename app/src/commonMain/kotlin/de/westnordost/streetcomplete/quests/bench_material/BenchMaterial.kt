package de.westnordost.streetcomplete.quests.bench_material

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.bench_brick
import de.westnordost.streetcomplete.resources.bench_concrete
import de.westnordost.streetcomplete.resources.bench_metal
import de.westnordost.streetcomplete.resources.bench_plastic
import de.westnordost.streetcomplete.resources.bench_stone
import de.westnordost.streetcomplete.resources.bench_wood
import de.westnordost.streetcomplete.resources.quest_material_brick
import de.westnordost.streetcomplete.resources.quest_material_concrete
import de.westnordost.streetcomplete.resources.quest_material_metal
import de.westnordost.streetcomplete.resources.quest_material_plastic
import de.westnordost.streetcomplete.resources.quest_material_stone
import de.westnordost.streetcomplete.resources.quest_material_wood

enum class BenchMaterial(val osmValue: String) {
    WOOD("wood"),
    METAL("metal"),
    PLASTIC("plastic"),
    CONCRETE("concrete"),
    STONE("stone"),
    BRICK("brick"),
    PICNIC("")
}

val BenchMaterial.title get() = when (this) {
    BenchMaterial.WOOD -> Res.string.quest_material_wood
    BenchMaterial.METAL -> Res.string.quest_material_metal
    BenchMaterial.PLASTIC -> Res.string.quest_material_plastic
    BenchMaterial.CONCRETE -> Res.string.quest_material_concrete
    BenchMaterial.STONE -> Res.string.quest_material_stone
    BenchMaterial.BRICK -> Res.string.quest_material_brick
    BenchMaterial.PICNIC -> null
}

val BenchMaterial.icon get() = when (this) {
    BenchMaterial.WOOD -> Res.drawable.bench_wood
    BenchMaterial.METAL -> Res.drawable.bench_metal
    BenchMaterial.PLASTIC -> Res.drawable.bench_plastic
    BenchMaterial.CONCRETE -> Res.drawable.bench_concrete
    BenchMaterial.STONE -> Res.drawable.bench_stone
    BenchMaterial.BRICK -> Res.drawable.bench_brick
    BenchMaterial.PICNIC -> null
}

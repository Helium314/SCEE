package de.westnordost.streetcomplete.quests.paving_stones_material

sealed interface PavingStonesMaterialAnswer

enum class PavingStonesMaterial(val osmValue: String) : PavingStonesMaterialAnswer {
    BRICK("brick"),
    CONCRETE("concrete"),
    STONE("stone"),
}

data object SurfaceIsNotPavingStones : PavingStonesMaterialAnswer

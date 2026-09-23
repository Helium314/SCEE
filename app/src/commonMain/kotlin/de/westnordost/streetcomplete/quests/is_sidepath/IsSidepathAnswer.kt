package de.westnordost.streetcomplete.quests.is_sidepath

sealed interface IsSidepathAnswer {
    /** Sidepath of a road. [ofName] is the selected road's `name=*` when available. */
    data class Yes(val ofName: String?) : IsSidepathAnswer
    data object No : IsSidepathAnswer
    data object IsSidewalk : IsSidepathAnswer
    data object IsCrossing : IsSidepathAnswer
}

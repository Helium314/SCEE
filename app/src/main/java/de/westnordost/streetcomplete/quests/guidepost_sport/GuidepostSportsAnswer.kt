package de.westnordost.streetcomplete.quests.guidepost_sport

sealed interface GuidepostSportsAnswer {
    val selectedSports: List<GuidepostSport>
}

data class SelectedGuidepostSports(
    override val selectedSports: List<GuidepostSport>
) : GuidepostSportsAnswer

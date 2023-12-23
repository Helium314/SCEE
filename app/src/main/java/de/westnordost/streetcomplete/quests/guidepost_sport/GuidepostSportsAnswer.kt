package de.westnordost.streetcomplete.quests.guidepost_sport


sealed interface GuidepostSportsAnswer {
    val selectedSports: List<GuidepostSport>
}

object IsSimpleGuidepost : GuidepostSportsAnswer

data class SelectedGuidepostSports(
    override val selectedSports: List<GuidepostSport>
) : GuidepostSportsAnswer

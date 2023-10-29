package de.westnordost.streetcomplete.quests.guidepost

sealed interface GuidepostEleAnswer

data class GuidepostEle(val ele: String) : GuidepostEleAnswer
object NoVisibleGuidepostEle : GuidepostEleAnswer

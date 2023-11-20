package de.westnordost.streetcomplete.quests.guidepost_sport

sealed interface RecyclingContainerMaterialsAnswer

object IsWasteContainer : RecyclingContainerMaterialsAnswer
data class RecyclingMaterials(val materials: List<GuidepostSport>) : RecyclingContainerMaterialsAnswer

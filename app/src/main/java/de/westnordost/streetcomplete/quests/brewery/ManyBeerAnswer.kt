package de.westnordost.streetcomplete.quests.brewery

sealed interface ManyBeerAnswer

data class BreweryAnswer(val brewery: String) : ManyBeerAnswer
object isManyBeerAnswer : ManyBeerAnswer

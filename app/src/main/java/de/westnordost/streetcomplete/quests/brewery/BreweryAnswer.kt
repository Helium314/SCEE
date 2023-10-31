package de.westnordost.streetcomplete.quests.brewery

sealed interface BreweryAnswer

data class BreweryStringAnswer(val brewery: String) : BreweryAnswer
object ManyBeerAnswer : BreweryAnswer
object NoBeerAnswer : BreweryAnswer

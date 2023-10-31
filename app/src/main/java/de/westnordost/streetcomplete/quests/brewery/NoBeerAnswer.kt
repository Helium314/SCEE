package de.westnordost.streetcomplete.quests.brewery

sealed interface NoBeerAnswer

data class BreweryAnswer(val brewery: String) : NoBeerAnswer
object isNoBeerAnswer : NoBeerAnswer

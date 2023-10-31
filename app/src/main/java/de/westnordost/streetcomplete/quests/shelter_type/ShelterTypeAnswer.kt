package de.westnordost.streetcomplete.quests.shelter_type

sealed interface ShelterTypeAnswer

data class ShelterTypeAnswer(val value: ShelterType) : ShelterTypeAnswer
object WeatherShelterAnswer : ShelterTypeAnswer

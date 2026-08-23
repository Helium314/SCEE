package de.westnordost.streetcomplete.quests.tree

sealed interface TreeDenotationAnswer

// ordered from most specific to broadest
enum class TreeDenotation(val osmValue: String) : TreeDenotationAnswer {
    LANDMARK("landmark"),
    NATURAL_MONUMENT("natural_monument"),
    AGRICULTURAL("agricultural"),
    PARK("park"),
    GARDEN("garden"),
    AVENUE("avenue"),
    URBAN("urban"),
    NATURAL("natural"),
}

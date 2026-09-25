package de.westnordost.streetcomplete.quests.destination

import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.oneway.isOneway
import kotlinx.serialization.Serializable

@Serializable
data class DestinationLanes(
    val count: Int,
    private val destinationsByLane: Map<Int, List<String>> = emptyMap()
) {
    init { require(count > 0) { "count $count must be positive" } }

    fun get(lane: Int): List<String> {
        checkLane(lane)
        return destinationsByLane[lane].orEmpty()
    }

    fun set(lane: Int, destinations: List<String>): DestinationLanes {
        checkLane(lane)
        return copy(destinationsByLane = destinationsByLane + (lane to destinations.filter { it.isNotBlank() }.distinct()))
    }

    fun add(lane: Int, destination: String): DestinationLanes {
        val dest = destination.trim()
        if (dest.isBlank()) return this
        return set(lane, get(lane) + dest)
    }

    fun getDestinations() = (1..count).flatMap { get(it) }

    val isEmpty get() = (1..count).all { destinationsByLane[it].isNullOrEmpty() }
    val isComplete get() = (1..count).none { destinationsByLane[it].isNullOrEmpty() }
    fun isCompleteExcept(lane: Int) = (1..count).filterNot { it == lane }.none { destinationsByLane[it].isNullOrEmpty() }

    fun laneString(): String? {
        if (!isComplete) return null
        return (1..count).joinToString("|") { get(it).joinToString(";") }
    }

    fun isTooLong(): Boolean =
        (laneString()?.length ?: 0) > MAX_OSM_TAG_VALUE_LENGTH

    // todo: for lane count also cycleways need to be considered
    //  but careful about sides!
    // anyway, currently such cases are simply ignored by the filter
    fun applyTo(tags: Tags, isBackward: Boolean) {
        if (!isComplete) throw IllegalStateException("cannot apply an incomplete destination answer")
        val tag = if (count > 1) "destination:lanes" else "destination"
        if (isOneway(tags)) {
            tags[tag] = laneString()!!
            return
        }
        val forwardBackward = if (isBackward) ":backward" else ":forward"
        tags[tag + forwardBackward] = laneString()!!
    }

    private fun checkLane(lane: Int) =
        require(lane in 1..count) { "tried to access lane $lane outside laneCount $count" }
}

fun laneCountInDirection(tags: Map<String, String>, isBackward: Boolean): Int {
    if (isBackward) tags["lanes:backward"]?.toIntOrNull()?.let { return it }
    else tags["lanes:forward"]?.toIntOrNull()?.let { return it }
    val lanes = tags["lanes"]?.toIntOrNull()
    if (isOneway(tags)) return lanes ?: 1
    return ((lanes ?: 2) / 2).coerceAtLeast(1)
}

package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface BarrierLockedAnswer

object Locked : BarrierLockedAnswer
object NotLocked : BarrierLockedAnswer
data class LockedAtHours(val timeRestriction: TimeRestriction?) : BarrierLockedAnswer

fun BarrierLockedAnswer.applyTo(tags: Tags) {
    when (this) {
        is Locked -> {
            tags.updateWithCheckDate("locked", "yes")
            tags.remove("locked:conditional")
        }
        is NotLocked -> {
            tags.updateWithCheckDate("locked", "no")
            tags.remove("locked:conditional")
        }
        is LockedAtHours -> {
            when (timeRestriction?.mode) {
                TimeRestriction.Mode.ONLY_AT_HOURS -> {
                    tags.updateWithCheckDate("locked", "no")
                    tags["locked:conditional"] = "yes @ (${timeRestriction.hours.toOpeningHours()})"
                }
                TimeRestriction.Mode.EXCEPT_AT_HOURS -> {
                    tags.updateWithCheckDate("locked", "yes")
                    tags["locked:conditional"] = "no @ (${timeRestriction.hours.toOpeningHours()})"
                }
                null -> {}
            }
        }
    }
}

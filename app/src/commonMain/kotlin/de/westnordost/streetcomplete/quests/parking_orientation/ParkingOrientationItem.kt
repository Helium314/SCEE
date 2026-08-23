package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

val ParkingOrientation.title get() = when (this) {
        PARALLEL ->      Res.string.street_parking_parallel
        DIAGONAL ->      Res.string.street_parking_diagonal
        PERPENDICULAR -> Res.string.street_parking_perpendicular
    }

val ParkingOrientation.osmValue get() = when (this) {
    PARALLEL ->      "parallel"
    DIAGONAL ->      "diagonal"
    PERPENDICULAR -> "perpendicular"
}


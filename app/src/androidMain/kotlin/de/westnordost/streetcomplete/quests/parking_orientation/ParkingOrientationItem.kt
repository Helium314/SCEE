package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR

val ParkingOrientation.title: Int get() = when (this) {
        PARALLEL ->      R.string.street_parking_parallel
        DIAGONAL ->      R.string.street_parking_diagonal
        PERPENDICULAR -> R.string.street_parking_perpendicular
    }

val ParkingOrientation.osmValue get() = when (this) {
    PARALLEL ->      "parallel"
    DIAGONAL ->      "diagonal"
    PERPENDICULAR -> "perpendicular"
}


package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.view.image_select.Item

fun ParkingOrientation.asItem() = Item(this, iconResId, titleResId)

private val ParkingOrientation.titleResId: Int get() = when (this) {
    PARALLEL ->      R.string.quest_parking_orientation_answer_parallel
    DIAGONAL ->      R.string.quest_parking_orientation_answer_diagonal
    PERPENDICULAR -> R.string.quest_parking_orientation_answer_perpendicular
}

private val ParkingOrientation.iconResId: Int get() = when (this) {
    PARALLEL ->      R.drawable.ic_parking_orientation_parallel
    DIAGONAL ->      R.drawable.ic_parking_orientation_diagonal
    PERPENDICULAR -> R.drawable.ic_parking_orientation_perpendicular
}

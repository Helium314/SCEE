package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PERPENDICULAR


class AddParkingOrientationForm : AListQuestForm<ParkingOrientation>() {

    override val items = listOf(
        TextItem(PARALLEL, R.string.quest_parking_orientation_answer_parallel),
        TextItem(DIAGONAL, R.string.quest_parking_orientation_answer_diagonal),
        TextItem(PERPENDICULAR, R.string.quest_parking_orientation_answer_perpendicular),
    )
}

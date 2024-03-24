package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddParkingOrientationForm : AImageListQuestForm<ParkingOrientation, ParkingOrientation>() {

    // get the context and save it in the variable ctx
    override val items = ParkingOrientation.values().map { it.asItem(requireContext()) }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ParkingOrientation>) {
        applyAnswer(selectedItems.single())
    }
}

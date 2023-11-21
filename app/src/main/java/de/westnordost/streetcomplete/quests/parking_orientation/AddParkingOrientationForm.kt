package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddParkingOrientationForm : AImageListQuestForm<ParkingOrientation, ParkingOrientation>() {

    override val items = ParkingOrientation.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ParkingOrientation>) {
        applyAnswer(selectedItems.single())
    }
}

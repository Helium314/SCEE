package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddStreetCabinetTypeForm : AImageListQuestForm<StreetCabinetType, StreetCabinetType>() {

    override val items = StreetCabinetType.values().map { it.asItem() }
    override val itemsPerRow = 4
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<StreetCabinetType>) {
        applyAnswer(selectedItems.single())
    }
}

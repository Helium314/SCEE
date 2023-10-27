package de.westnordost.streetcomplete.quests.street_cabinet

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestForm

class AddStreetCabinetTypeForm : AGroupedImageListQuestForm<StreetCabinetType, StreetCabinetType>() {

    override val topItems = listOf(
        StreetCabinetType.POWER,
        StreetCabinetType.WATER,
        StreetCabinetType.TELECOM,
        StreetCabinetType.POSTAL_SERVICE,
        StreetCabinetType.GAS,
    ).toItems()

    override val allItems = StreetCabinetTypeCategory.values().toItems()

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(value: StreetCabinetType) {
        applyAnswer(value)
    }
}

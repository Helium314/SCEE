package de.westnordost.streetcomplete.quests.service_building

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestForm

class AddServiceBuildingTypeForm : AGroupedImageListQuestForm<ServiceBuildingType, ServiceBuildingType>() {

    override val topItems = listOf(ServiceBuildingType.MINOR_SUBSTATION, ServiceBuildingType.GAS_PRESSURE_REGULATION).toItems()

    override val allItems = ServiceBuildingTypeCategory.values().toItems()

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(value: ServiceBuildingType) {
        applyAnswer(value)
    }
}

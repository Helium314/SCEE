package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem

class AddServiceBuildingTypeForm : AListQuestAnswerFragment<String>() {

    override val items = listOf(
        TextItem("gas", R.string.quest_service_building_type_pressure),
        TextItem("substation", R.string.quest_service_building_type_substation),
        TextItem("well", R.string.quest_service_building_type_well),
        TextItem("reservoir_covered", R.string.quest_service_building_type_reservoir),
        TextItem("pumping_station", R.string.quest_service_building_type_pump),
    )
}

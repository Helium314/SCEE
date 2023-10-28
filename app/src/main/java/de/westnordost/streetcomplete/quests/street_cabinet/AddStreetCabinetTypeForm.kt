package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.POWER
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TELECOM
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.POSTAL_SERVICE
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TRAFFIC_CONTROL
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TRAFFIC_MONITORING
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TRANSPORT_MANAGEMENT
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.WASTE
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TELEVISION
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.GAS
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.STREET_LIGHTING
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.WATER
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.SEWERAGE
class AddStreetCabinetTypeForm : AListQuestForm<StreetCabinetType>() {
    override val items = listOf(
        TextItem(POWER, R.string.quest_utility_power),
        TextItem(TELECOM, R.string.quest_utility_telecom),
        TextItem(POSTAL_SERVICE, R.string.quest_street_cabinet_postal_service),
        TextItem(TRAFFIC_CONTROL, R.string.quest_street_cabinet_traffic_control),
        TextItem(TRAFFIC_MONITORING, R.string.quest_street_cabinet_traffic_monitoring),
        TextItem(TRANSPORT_MANAGEMENT, R.string.quest_street_cabinet_transport_management),
        TextItem(WASTE, R.string.quest_street_cabinet_waste),
        TextItem(TELEVISION, R.string.quest_street_cabinet_television),
        TextItem(GAS, R.string.quest_utility_gas),
        TextItem(STREET_LIGHTING, R.string.quest_street_cabinet_street_lighting),
        TextItem(WATER, R.string.quest_utility_water),
        TextItem(SEWERAGE, R.string.quest_utility_sewerage),
    )
}

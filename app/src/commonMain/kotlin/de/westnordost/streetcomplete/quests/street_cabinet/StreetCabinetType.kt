package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.GAS
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.POSTAL_SERVICE
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.POWER
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.SEWERAGE
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.STREET_LIGHTING
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TELECOM
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TELEVISION
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TRAFFIC_CONTROL
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TRAFFIC_MONITORING
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.TRANSPORT_MANAGEMENT
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.WASTE
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.WATER
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

enum class StreetCabinetType(val osmKey: String, val osmValue: String) {
    POWER("utility", "power"),
    TELECOM("utility", "telecom"),
    TRAFFIC_CONTROL("street_cabinet", "traffic_control"),
    POSTAL_SERVICE("street_cabinet", "postal_service"),
    GAS("utility", "gas"),
    STREET_LIGHTING("utility", "street_lighting"),
    TRANSPORT_MANAGEMENT("street_cabinet", "transport_management"),
    TRAFFIC_MONITORING("street_cabinet", "traffic_monitoring"),
    WASTE("street_cabinet", "waste"),
    TELEVISION("utility", "television"),
    WATER("utility", "water"),
    SEWERAGE("utility", "sewerage");
}

val StreetCabinetType.title get() = when (this) {
    POWER ->                Res.string.quest_utility_power
    TELECOM ->              Res.string.quest_utility_telecom
    POSTAL_SERVICE ->       Res.string.quest_street_cabinet_postal_service
    TRAFFIC_CONTROL ->      Res.string.quest_street_cabinet_traffic_control
    TRAFFIC_MONITORING ->   Res.string.quest_street_cabinet_traffic_monitoring
    TRANSPORT_MANAGEMENT -> Res.string.quest_street_cabinet_transport_management
    WASTE ->                Res.string.quest_street_cabinet_waste
    TELEVISION ->           Res.string.quest_street_cabinet_television
    GAS ->                  Res.string.quest_utility_gas
    STREET_LIGHTING ->      Res.string.quest_street_cabinet_street_lighting
    WATER ->                Res.string.quest_utility_water
    SEWERAGE ->             Res.string.quest_utility_sewerage
}

val StreetCabinetType.icon get() = when (this) {
    POWER ->                Res.drawable.quest_street_cabinet_power
    TELECOM ->              Res.drawable.quest_street_cabinet_telecom
    POSTAL_SERVICE ->       Res.drawable.quest_street_cabinet_postal_service
    TRAFFIC_CONTROL ->      Res.drawable.quest_street_cabinet_traffic_control
    TRAFFIC_MONITORING ->   Res.drawable.quest_street_cabinet_traffic_monitoring
    TRANSPORT_MANAGEMENT -> Res.drawable.quest_street_cabinet_transport_management
    WASTE ->                Res.drawable.quest_street_cabinet_waste
    TELEVISION ->           Res.drawable.quest_street_cabinet_television
    GAS ->                  Res.drawable.quest_street_cabinet_gas
    STREET_LIGHTING ->      Res.drawable.quest_street_cabinet_street_lighting
    WATER ->                Res.drawable.quest_street_cabinet_water
    SEWERAGE ->             Res.drawable.quest_street_cabinet_sewerage
}

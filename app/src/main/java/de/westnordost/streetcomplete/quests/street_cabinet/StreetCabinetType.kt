package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class StreetCabinetType(val tags: List<Pair<String, String>>) {
    POSTAL_SERVICE(listOf("street_cabinet" to "postal_service")),
    TRAFFIC_CONTROL(listOf("street_cabinet" to "traffic_control")),
    TRAFFIC_MONITORING(listOf("street_cabinet" to "traffic_monitoring")),
    TRANSPORT_MANAGEMENT(listOf("street_cabinet" to "transport_management")),
    WASTE(listOf("street_cabinet" to "waste")),
    TELEVISION(listOf("utility" to "television")),
    GAS(listOf("utility" to "gas")),
    POWER(listOf("utility" to "power")),
    STREET_LIGHTING(listOf("utility" to "street_lighting")),
    TELECOM(listOf("utility" to "telecom")),
    WATER(listOf("utility" to "water")),
    SEWERAGE(listOf("utility" to "sewerage"))
}

enum class StreetCabinetTypeCategory(val type: StreetCabinetType?, val subTypes: List<StreetCabinetType>) {
    TRAFFIC(StreetCabinetType.TRAFFIC_CONTROL, listOf(TRAFFIC_CONTROL, TRAFFIC_MONITORING)),
    OTHER(StreetCabinetType.GAS, listOf(POWER, TELECOM, STREET_LIGHTING, TELECOM, POSTAL_SERVICE, TRANSPORT_MANAGEMENT, WASTE, TELEVISION, GAS, WATER, SEWERAGE)),
}

fun Collection<StreetCabinetType>.toItems() = map { it.asItem() }
fun Array<StreetCabinetTypeCategory>.toItems() = map { it.asItem() }

fun StreetCabinetType.asItem(): GroupableDisplayItem<StreetCabinetType> {
    return Item(this, iconResId, titleResId, descriptionResId)
}

fun StreetCabinetTypeCategory.asItem(): GroupableDisplayItem<StreetCabinetType> {
    //val descriptionResId = null // could be added similar to StreetCabinetType.descriptionResId
    return Item(type, iconResId, titleResId, null, subTypes.toItems())
}

private val StreetCabinetType.titleResId: Int get() = when (this) {
    POSTAL_SERVICE -> R.string.quest_street_cabinet_postal_service
    TRAFFIC_CONTROL -> R.string.quest_street_cabinet_traffic_control
    TRAFFIC_MONITORING -> R.string.quest_street_cabinet_traffic_monitoring
    TRANSPORT_MANAGEMENT -> R.string.quest_street_cabinet_transport_management
    WASTE -> R.string.quest_street_cabinet_waste
    TELEVISION -> R.string.quest_street_cabinet_television
    GAS -> R.string.quest_street_cabinet_gas
    POWER -> R.string.quest_street_cabinet_power
    STREET_LIGHTING -> R.string.quest_street_cabinet_street_lighting
    WATER -> R.string.quest_street_cabinet_water
    TELECOM -> R.string.quest_street_cabinet_telecom
    SEWERAGE -> R.string.quest_street_cabinet_sewerage
}

private val StreetCabinetType.descriptionResId: Int? get() = when (this) {
    POSTAL_SERVICE -> R.string.quest_street_cabinet_postal_service_description
    else -> null
}

private val StreetCabinetTypeCategory.titleResId: Int get() = when (this) {
    StreetCabinetTypeCategory.TRAFFIC -> R.string.quest_street_cabinet_traffic
    StreetCabinetTypeCategory.OTHER -> R.string.quest_street_cabinet_other
}

private val StreetCabinetTypeCategory.iconResId: Int get() = when (this) {
    StreetCabinetTypeCategory.TRAFFIC -> R.drawable.ic_quest_service_building
    StreetCabinetTypeCategory.OTHER -> R.drawable.ic_quest_service_building
}

private val StreetCabinetType.iconResId: Int get() = when (this) {
    StreetCabinetType.POSTAL_SERVICE -> R.drawable.ic_quest_service_building
    StreetCabinetType.TRAFFIC_CONTROL ->    R.drawable.ic_quest_service_building
    StreetCabinetType.TRAFFIC_MONITORING ->    R.drawable.ic_quest_service_building
    StreetCabinetType.TRANSPORT_MANAGEMENT ->    R.drawable.ic_quest_service_building
    StreetCabinetType.WASTE ->    R.drawable.ic_quest_service_building
    StreetCabinetType.TELEVISION ->    R.drawable.ic_quest_service_building
    StreetCabinetType.TELECOM ->    R.drawable.ic_quest_service_building_telecom
    StreetCabinetType.GAS ->    R.drawable.ic_quest_building_service_gas
    StreetCabinetType.SEWERAGE ->    R.drawable.ic_quest_service_building_sewerage
    StreetCabinetType.POWER ->    R.drawable.ic_quest_service_building_power
    StreetCabinetType.STREET_LIGHTING ->    R.drawable.ic_quest_service_building
    StreetCabinetType.WATER ->    R.drawable.ic_quest_service_building_water
}

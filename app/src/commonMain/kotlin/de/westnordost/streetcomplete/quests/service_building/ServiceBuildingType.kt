package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.quests.service_building.ServiceBuildingType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.Group
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class ServiceBuildingType(val tags: List<Pair<String, String>>) {
    POWER(listOf("utility" to "power")),
    TELECOM(listOf("utility" to "telecom")),
    WATER(listOf("utility" to "water")),
    GAS(listOf("utility" to "gas")),
    SEWERAGE(listOf("utility" to "sewerage", "substance" to "sewage")), // can be pumping stations or treatment plants
    HEATING(listOf("utility" to "heating")),
    VENTILATION_SHAFT(listOf("man_made" to "ventilation")), // building tag removed in AddServiceBuildingType.applyAnswerTo
    MONITORING_STATION(listOf("man_made" to "monitoring_station")),
    // POWER
    MINOR_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "minor_distribution")),
    SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "distribution")),
    INDUSTRIAL_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "industrial")),
    TRACTION_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "traction")),
    SWITCHGEAR(listOf("utility" to "power", "power" to "switchgear")),
    PLANT(listOf("utility" to "power", "power" to "plant")),
    //GAS
    GAS_PRESSURE_REGULATION(listOf("utility" to "gas", "pipeline" to "substation", "substation" to "distribution", "substance" to "gas")),
    GAS_PUMPING_STATION(listOf("utility" to "gas", "man_made" to "pumping_station", "substance" to "gas")),
    // WATER
    WATER_WELL(listOf("utility" to "water", "man_made" to "water_well", "substance" to "water")),
    COVERED_RESERVOIR(listOf("utility" to "water", "man_made" to "reservoir_covered", "substance" to "water")),
    WATER_PUMPING_STATION(listOf("utility" to "water", "man_made" to "pumping_station", "substance" to "water")),
    // OIL
    OIL_PUMPING_STATION(listOf("utility" to "oil", "man_made" to "pumping_station", "substance" to "oil")),
    // RAILWAY
    RAILWAY_VENTILATION_SHAFT(listOf("service" to "ventilation", "railway" to "ventilation_shaft")),
    RAILWAY_SIGNAL_BOX(listOf("building" to "industrial", "railway" to "signal_box")),
    RAILWAY_ENGINE_SHED(listOf("building" to "industrial", "railway" to "engine_shed")),
    RAILWAY_WASH(listOf("building" to "industrial", "railway" to "wash")),
    // TELECOM
    INTERNET_EXCHANGE(listOf("utility" to "communication", "telecom" to "internet_exchange")),
    TELECOM_EXCHANGE(listOf("utility" to "communication", "telecom" to "exchange")),
    // DISUSED
    DISUSED(listOf("disused" to "yes")),
}

enum class ServiceBuildingTypeCategory(
    override val item: ServiceBuildingType?,
    override val children: List<ServiceBuildingType>
) : Group<ServiceBuildingType> {
    POWER(ServiceBuildingType.POWER, listOf(MINOR_SUBSTATION, SUBSTATION, INDUSTRIAL_SUBSTATION, TRACTION_SUBSTATION, SWITCHGEAR, PLANT)),
    WATER(ServiceBuildingType.WATER, listOf(WATER_WELL, COVERED_RESERVOIR, WATER_PUMPING_STATION)),
    GAS(ServiceBuildingType.GAS, listOf(GAS_PUMPING_STATION, GAS_PRESSURE_REGULATION)),
    TELECOM(ServiceBuildingType.TELECOM, listOf(TELECOM_EXCHANGE, INTERNET_EXCHANGE)),
    RAILWAY(null, listOf(RAILWAY_VENTILATION_SHAFT, RAILWAY_SIGNAL_BOX, RAILWAY_ENGINE_SHED, RAILWAY_WASH)),
    OTHER_SERVICE(null, listOf(OIL_PUMPING_STATION, SEWERAGE, HEATING, VENTILATION_SHAFT, MONITORING_STATION)),
}

val ServiceBuildingType.titleRes: StringResource get() = when (this) {
    POWER -> Res.string.quest_utility_power
    MINOR_SUBSTATION -> Res.string.quest_service_building_type_minor_substation
    SUBSTATION -> Res.string.quest_service_building_type_substation
    INDUSTRIAL_SUBSTATION -> Res.string.quest_service_building_type_industrial_substation
    TRACTION_SUBSTATION -> Res.string.quest_service_building_type_traction_substation
    SWITCHGEAR -> Res.string.quest_service_building_type_switchgear
    PLANT -> Res.string.quest_service_building_type_plant
    WATER -> Res.string.quest_utility_water
    WATER_WELL -> Res.string.quest_service_building_type_well
    COVERED_RESERVOIR -> Res.string.quest_service_building_type_reservoir
    WATER_PUMPING_STATION -> Res.string.quest_service_building_type_pump
    SEWERAGE -> Res.string.quest_utility_sewerage
    OIL_PUMPING_STATION -> Res.string.quest_service_building_oil_pumping_station
    GAS -> Res.string.quest_utility_gas
    GAS_PRESSURE_REGULATION -> Res.string.quest_service_building_type_pressure
    GAS_PUMPING_STATION -> Res.string.quest_service_building_gas_pumping_station
    TELECOM -> Res.string.quest_utility_telecom
    TELECOM_EXCHANGE -> Res.string.quest_service_building_telecom_exchange
    INTERNET_EXCHANGE -> Res.string.quest_service_building_internet_exchange
    RAILWAY_VENTILATION_SHAFT -> Res.string.quest_service_building_railway_ventilation_shaft
    RAILWAY_SIGNAL_BOX -> Res.string.quest_service_building_railway_signal_box
    RAILWAY_ENGINE_SHED -> Res.string.quest_service_building_railway_engine_shed
    RAILWAY_WASH -> Res.string.quest_service_building_railway_wash
    VENTILATION_SHAFT -> Res.string.quest_service_building_ventilation
    HEATING -> Res.string.quest_service_building_heating
    MONITORING_STATION -> Res.string.quest_service_building_monitoring_station
    DISUSED -> Res.string.quest_disused
}

val ServiceBuildingType.descriptionRes: StringResource? get() = when (this) {
    MINOR_SUBSTATION -> Res.string.quest_service_building_type_minor_substation_description
    SUBSTATION -> Res.string.quest_service_building_type_substation_description
    INDUSTRIAL_SUBSTATION -> Res.string.quest_service_building_type_industrial_substation_description
    TRACTION_SUBSTATION -> Res.string.quest_service_building_type_traction_substation_description
    SWITCHGEAR -> Res.string.quest_service_building_type_switchgear_description
    WATER_WELL -> Res.string.quest_service_building_type_well_description
    COVERED_RESERVOIR -> Res.string.quest_service_building_type_reservoir_description
    WATER_PUMPING_STATION -> Res.string.quest_service_building_type_pump_description
    SEWERAGE -> Res.string.quest_service_building_sewerage_description
    OIL_PUMPING_STATION -> Res.string.quest_service_building_oil_pumping_station_description
    GAS_PRESSURE_REGULATION -> Res.string.quest_service_building_type_pressure_description
    GAS_PUMPING_STATION -> Res.string.quest_service_building_gas_pumping_station_description
    TELECOM_EXCHANGE -> Res.string.quest_service_building_telecom_exchange_description
    INTERNET_EXCHANGE -> Res.string.quest_service_building_internet_exchange_description
    RAILWAY_VENTILATION_SHAFT -> Res.string.quest_service_building_railway_ventilation_shaft_description
    RAILWAY_SIGNAL_BOX -> Res.string.quest_service_building_railway_signal_box_description
    RAILWAY_ENGINE_SHED -> Res.string.quest_service_building_railway_engine_shed_description
    RAILWAY_WASH -> Res.string.quest_service_building_railway_wash_description
    VENTILATION_SHAFT -> Res.string.quest_service_building_ventilation_description
    HEATING -> Res.string.quest_service_building_heating_description
    MONITORING_STATION -> Res.string.quest_service_building_monitoring_station_description
    else -> null
}

val ServiceBuildingType.iconRes: DrawableResource get() = when (this) {
    POWER -> Res.drawable.ic_quest_service_building_power
    WATER ->    Res.drawable.ic_quest_service_building_water
    TELECOM ->    Res.drawable.ic_quest_service_building_telecom
    GAS ->    Res.drawable.ic_quest_building_service_gas
    SEWERAGE ->    Res.drawable.ic_quest_service_building_sewerage
    MINOR_SUBSTATION ->    Res.drawable.ic_quest_service_building_minor_substation
    SUBSTATION ->    Res.drawable.ic_quest_service_building_substation
    INDUSTRIAL_SUBSTATION ->    Res.drawable.ic_quest_service_building_industrial_substation
    TRACTION_SUBSTATION ->    Res.drawable.ic_quest_service_building_traction_substation
    SWITCHGEAR ->    Res.drawable.ic_quest_service_building_switchgear
    PLANT ->    Res.drawable.ic_quest_service_building_power_plant
    GAS_PRESSURE_REGULATION ->    Res.drawable.ic_quest_building_service_gas_pressure
    GAS_PUMPING_STATION ->    Res.drawable.ic_quest_building_service_gas_pump
    WATER_WELL ->    Res.drawable.ic_quest_service_building_water_well
    COVERED_RESERVOIR ->    Res.drawable.ic_quest_service_reservoir_covered
    WATER_PUMPING_STATION ->    Res.drawable.ic_quest_service_building_water_pump
    OIL_PUMPING_STATION ->    Res.drawable.ic_quest_service_building_oil_pump
    RAILWAY_VENTILATION_SHAFT ->    Res.drawable.ic_quest_service_building_railway_ventilation
    RAILWAY_SIGNAL_BOX ->    Res.drawable.ic_quest_service_building_railway_signal_box
    RAILWAY_ENGINE_SHED ->    Res.drawable.ic_quest_service_building_railway_engine_shed
    RAILWAY_WASH ->    Res.drawable.ic_quest_service_building_railway_wash
    HEATING ->    Res.drawable.ic_quest_service_building_heating
    VENTILATION_SHAFT ->    Res.drawable.ic_quest_service_building_ventilation
    TELECOM_EXCHANGE ->    Res.drawable.ic_quest_service_building_telecom_exchange
    INTERNET_EXCHANGE ->    Res.drawable.ic_quest_service_building_internet_exchange
    MONITORING_STATION ->    Res.drawable.ic_quest_service_building_monitoring
    DISUSED -> Res.drawable.ic_quest_service_building
}

val ServiceBuildingTypeCategory.titleRes: StringResource get() = when (this) {
    ServiceBuildingTypeCategory.POWER -> Res.string.quest_utility_power
    ServiceBuildingTypeCategory.WATER -> Res.string.quest_utility_water
    ServiceBuildingTypeCategory.GAS -> Res.string.quest_utility_gas
    ServiceBuildingTypeCategory.TELECOM -> Res.string.quest_utility_telecom
    ServiceBuildingTypeCategory.RAILWAY -> Res.string.quest_service_building_railway
    ServiceBuildingTypeCategory.OTHER_SERVICE -> Res.string.quest_service_building_other
}

val ServiceBuildingTypeCategory.iconRes: DrawableResource get() = when (this) {
    ServiceBuildingTypeCategory.POWER -> Res.drawable.ic_quest_service_building_power
    ServiceBuildingTypeCategory.WATER -> Res.drawable.ic_quest_service_building_water
    ServiceBuildingTypeCategory.GAS -> Res.drawable.ic_quest_building_service_gas
    ServiceBuildingTypeCategory.TELECOM -> Res.drawable.ic_quest_service_building_telecom
    ServiceBuildingTypeCategory.RAILWAY -> Res.drawable.ic_quest_service_building_railway
    ServiceBuildingTypeCategory.OTHER_SERVICE -> Res.drawable.ic_quest_service_building_other
}


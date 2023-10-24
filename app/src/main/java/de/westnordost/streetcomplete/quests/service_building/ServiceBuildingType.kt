package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.quests.service_building.ServiceBuildingType.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

// todo: UtilityType instead? that way the quest could also extend stuff to street_cabinet
//  just have different Categories then
//  possibly extend to marker? utility_pole? man_hole?
// CAREFUL: utility is what it's for, water pumping station can be sewage or power
// https://wiki.openstreetmap.org/wiki/Key:utility
// https://wiki.openstreetmap.org/wiki/Tag:building%3Dservice
// https://wiki.openstreetmap.org/wiki/Tag:man_made%3Dpumping_station

// like BuildingType
enum class ServiceBuildingType(val tags: List<Pair<String, String>>) {
    POWER(listOf("utility" to "power")), // also street cabinet
    TELECOM(listOf("utility" to "telecom")), // also street cabinet
    WATER(listOf("utility" to "water")),
    GAS(listOf("utility" to "gas")),
    OIL(listOf("utility" to "oil")),
    RAILWAY(listOf()),
    OTHER_SERVICE(listOf()),
    SEWERAGE(listOf("utility" to "sewerage", "substance" to "sewage")), // can be pumping stations or treatment plants
    // https://commons.wikimedia.org/wiki/Category:Abwasserpumpstation_(Rosenstra%C3%9Fe,_Saarbr%C3%BCcken)
    HEATING(listOf("utility" to "heating", "substance" to "hot_water")),
    // https://commons.wikimedia.org/wiki/File:Cornell_University_Central_Heating_Plant.jpg
    VENTILATION_SHAFT(listOf("man_made" to "ventilation")), // building tag removed in AddServiceBuildingType.applyAnswerTo
    // https://commons.wikimedia.org/wiki/File:Maryina_Roshcha_ventilation_shaft_in_Moscow.jpg
    MONITORING_STATION(listOf("man_made" to "monitoring_station")),
    // https://commons.wikimedia.org/wiki/File:River_monitoring_station,_Coytrahen_-_geograph.org.uk_-_4224388.jpg

    // POWER
    MINOR_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "minor_distribution")),
    // https://commons.wikimedia.org/wiki/File:Trafostation_%22Station_hinter_dem_Weinbergweg%22.jpg
    SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "distribution")),
    // https://commons.wikimedia.org/wiki/Category:Umspannwerk_Stangenm%C3%BChle
    INDUSTRIAL_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "industrial")),
    TRACTION_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "traction")), // kinda related with railway
    // https://commons.wikimedia.org/wiki/File:Woburn_rail_traction_substation,_Lower_Hutt,_New_Zealand.JPG
    SWITCHGEAR(listOf("utility" to "power", "power" to "switchgear")),
    // outside: https://commons.wikimedia.org/wiki/File:Painted_switchgear_in_Minsk_near_Janki_Kupaly_museum_2.jpg; inside: https://commons.wikimedia.org/wiki/File:Factory_Salasel_electric_services_switchgear_made_in_oman.jpg
    PLANT(listOf("utility" to "power", "power" to "plant")),
    // https://commons.wikimedia.org/wiki/File:Dunamenti_Power_Plant,_gate_building,_2020_Sz%C3%A1zhalombatta.jpg
    //GAS
    GAS_PRESSURE_REGULATION(listOf("utility" to "gas", "pipeline" to "substation", "substation" to "distribution", "substance" to "gas")),
    // https://commons.wikimedia.org/wiki/File:Bellah%C3%B8j_gas_pressure_regulator_building-3.jpg
    GAS_PUMPING_STATION(listOf("utility" to "gas", "man_made" to "pumping_station", "substance" to "gas")),
    // https://commons.wikimedia.org/wiki/File:Gas_pipeline_pumping_station_-_geograph.org.uk_-_4991046.jpg
    // WATER
    WATER_WELL(listOf("utility" to "water", "man_made" to "water_well", "substance" to "water")),
    // https://commons.wikimedia.org/wiki/File:Well_house_Ringsaker.jpg
    COVERED_RESERVOIR(listOf("utility" to "water", "man_made" to "reservoir_covered", "substance" to "water")),
    // https://commons.wikimedia.org/wiki/File:Pai_Tau_Hang_Fresh_Water_Service_Reservoir_03.jpg
    WATER_PUMPING_STATION(listOf("utility" to "water", "man_made" to "pumping_station", "substance" to "water")),
    // https://commons.wikimedia.org/wiki/File:Hoxton_Park_Water_Pumping_Station_05.jpg
    // OIL
    OIL_PUMPING_STATION(listOf("utility" to "oil", "man_made" to "pumping_station", "substance" to "oil")),
    // https://commons.wikimedia.org/wiki/File:Oil_Pump_North_Dakota.jpg
    // RAILWAY
    RAILWAY_VENTILATION_SHAFT(listOf("service" to "ventilation", "railway" to "ventilation_shaft")), // 374x in use
    // https://commons.wikimedia.org/wiki/File:Ventilation_shaft_of_Sovetskaya_metro_station_(%D0%92%D0%B5%D0%BD%D1%82%D0%BA%D0%B8%D0%BE%D1%81%D0%BA_%D1%81%D1%82%D0%B0%D0%BD%D1%86%D0%B8%D0%B8_%D0%A1%D0%BE%D0%B2%D0%B5%D1%82%D1%81%D0%BA%D0%B0%D1%8F)_(6937492132).jpg
    RAILWAY_SIGNAL_BOX(listOf("railway" to "signal_box")), // there is no service-tag; DE: Elektronisches Stellwerk; 562x in use
    // https://commons.wikimedia.org/wiki/File:Kwinana_Signal_Box,_March_2020_05.jpg
    RAILWAY_ENGINE_SHED(listOf("railway" to "engine_shed")), // there is no service-tag; 13x in use
    // https://commons.wikimedia.org/wiki/File:Former_engine_shed_sited_on_a_shipyard_at_Whitby.jpg
    RAILWAY_WASH(listOf("railway" to "wash")), // there is no service-tag; DE: Zug-Waschanlage; 5x in use
    // https://commons.wikimedia.org/wiki/File:Train_wash,_Perth_Station_-_geograph.org.uk_-_4724498.jpg
    // TELECOM
    //INTERNET_EXCHANGE(listOf("utility" to "communication", "telecom" to "internet_exchange")), --> only 6x; but documented: https://wiki.openstreetmap.org/wiki/Telecoms#Internet_Exchanges
    TELECOM_EXCHANGE(listOf("utility" to "communication", "telecom" to "exchange")),
    // https://commons.wikimedia.org/wiki/File:Telecom_Exchange_-_geograph.org.uk_-_5360464.jpg
}

// like BuildingTypeCategory
enum class ServiceBuildingTypeCategory(val type: ServiceBuildingType?, val subTypes: List<ServiceBuildingType>) {
    POWER(ServiceBuildingType.POWER, listOf(MINOR_SUBSTATION, SUBSTATION, INDUSTRIAL_SUBSTATION, TRACTION_SUBSTATION, SWITCHGEAR, PLANT)),
    WATER(ServiceBuildingType.WATER, listOf(WATER_WELL, COVERED_RESERVOIR, WATER_PUMPING_STATION)),
    OIL(ServiceBuildingType.OIL, listOf(OIL_PUMPING_STATION)),
    GAS(ServiceBuildingType.GAS, listOf(GAS_PUMPING_STATION, GAS_PRESSURE_REGULATION)),
    TELECOM(ServiceBuildingType.TELECOM, listOf(TELECOM_EXCHANGE)),
    RAILWAY(ServiceBuildingType.RAILWAY, listOf(RAILWAY_VENTILATION_SHAFT, RAILWAY_SIGNAL_BOX, RAILWAY_ENGINE_SHED, RAILWAY_WASH)), // todo: this should be an unselectable category (here the ok button should disappear, but that's same for building type quest)
    OTHER_SERVICE(ServiceBuildingType.OTHER_SERVICE, listOf(SEWERAGE, HEATING, VENTILATION_SHAFT, MONITORING_STATION)), // todo: this should be an unselectable category (here the ok button should disappear, but that's same for building type quest)
}

fun Collection<ServiceBuildingType>.toItems() = map { it.asItem() }
fun Array<ServiceBuildingTypeCategory>.toItems() = map { it.asItem() }

fun ServiceBuildingType.asItem(): GroupableDisplayItem<ServiceBuildingType> {
    return Item(this, iconResId, titleResId, descriptionResId)
}

fun ServiceBuildingTypeCategory.asItem(): GroupableDisplayItem<ServiceBuildingType> {
    //val descriptionResId = null // could be added similar to ServiceBuildingType.descriptionResId
    return Item(type, iconResId, titleResId, null, subTypes.toItems())
}

private val ServiceBuildingType.titleResId: Int get() = when (this) {
    POWER -> R.string.quest_service_building_power
    MINOR_SUBSTATION -> R.string.quest_service_building_type_minor_substation
    SUBSTATION -> R.string.quest_service_building_type_substation
    INDUSTRIAL_SUBSTATION -> R.string.quest_service_building_type_industrial_substation
    TRACTION_SUBSTATION -> R.string.quest_service_building_type_traction_substation
    SWITCHGEAR -> R.string.quest_service_building_type_switchgear
    PLANT -> R.string.quest_service_building_type_plant
    WATER -> R.string.quest_service_building_water
    WATER_WELL -> R.string.quest_service_building_type_well
    COVERED_RESERVOIR -> R.string.quest_service_building_type_reservoir
    WATER_PUMPING_STATION -> R.string.quest_service_building_type_pump
    SEWERAGE -> R.string.quest_service_building_sewerage
    OIL -> R.string.quest_service_building_oil
    OIL_PUMPING_STATION -> R.string.quest_service_building_oil_pumping_station
    GAS -> R.string.quest_service_building_gas
    GAS_PRESSURE_REGULATION -> R.string.quest_service_building_type_pressure
    GAS_PUMPING_STATION -> R.string.quest_service_building_gas_pumping_station
    TELECOM -> R.string.quest_service_building_telecom
    TELECOM_EXCHANGE -> R.string.quest_service_building_telecom_exchange
    RAILWAY -> R.string.quest_service_building_railway
    RAILWAY_VENTILATION_SHAFT -> R.string.quest_service_building_railway_ventilation_shaft
    RAILWAY_SIGNAL_BOX -> R.string.quest_service_building_railway_signal_box
    RAILWAY_ENGINE_SHED -> R.string.quest_service_building_railway_engine_shed
    RAILWAY_WASH -> R.string.quest_service_building_railway_wash
    VENTILATION_SHAFT -> R.string.quest_service_building_ventilation
    HEATING -> R.string.quest_service_building_heating
    MONITORING_STATION -> R.string.quest_service_building_monitoring_station
    OTHER_SERVICE -> R.string.quest_service_building_other
}

private val ServiceBuildingType.descriptionResId: Int? get() = when (this) {
    POWER -> R.string.quest_service_building_power_description
    MINOR_SUBSTATION -> R.string.quest_service_building_type_minor_substation_description
    SUBSTATION -> R.string.quest_service_building_type_substation_description
    INDUSTRIAL_SUBSTATION -> R.string.quest_service_building_type_industrial_substation_description
    TRACTION_SUBSTATION -> R.string.quest_service_building_type_traction_substation_description
    SWITCHGEAR -> R.string.quest_service_building_type_switchgear_description
    //PLANT -> R.string.quest_service_building_type_plant_description
    //WATER -> R.string.quest_service_building_water_description
    WATER_WELL -> R.string.quest_service_building_type_well_description
    COVERED_RESERVOIR -> R.string.quest_service_building_type_reservoir_description
    WATER_PUMPING_STATION -> R.string.quest_service_building_type_pump_description
    SEWERAGE -> R.string.quest_service_building_sewerage_description
    OIL_PUMPING_STATION -> R.string.quest_service_building_oil_pumping_station_description
    GAS_PRESSURE_REGULATION -> R.string.quest_service_building_type_pressure_description
    GAS_PUMPING_STATION -> R.string.quest_service_building_gas_pumping_station_description
    //TELECOM -> R.string.quest_service_building_telecom_description
    TELECOM_EXCHANGE -> R.string.quest_service_building_telecom_exchange_description
    //RAILWAY -> R.string.quest_service_building_railway_description
    RAILWAY_VENTILATION_SHAFT -> R.string.quest_service_building_railway_ventilation_shaft_description
    RAILWAY_SIGNAL_BOX -> R.string.quest_service_building_railway_signal_box_description
    RAILWAY_ENGINE_SHED -> R.string.quest_service_building_railway_engine_shed_description
    RAILWAY_WASH -> R.string.quest_service_building_railway_wash_description
    VENTILATION_SHAFT -> R.string.quest_service_building_ventilation_description
    HEATING -> R.string.quest_service_building_heating_description
    MONITORING_STATION -> R.string.quest_service_building_monitoring_station_description
    //OTHER_SERVICE -> R.string.quest_service_building_other_description
    else -> null
}

private val ServiceBuildingTypeCategory.titleResId: Int get() = when (this) {
    ServiceBuildingTypeCategory.POWER -> R.string.quest_service_building_power
    ServiceBuildingTypeCategory.WATER -> R.string.quest_service_building_water
    ServiceBuildingTypeCategory.OIL -> R.string.quest_service_building_oil
    ServiceBuildingTypeCategory.GAS -> R.string.quest_service_building_gas
    ServiceBuildingTypeCategory.TELECOM -> R.string.quest_service_building_telecom
    ServiceBuildingTypeCategory.RAILWAY -> R.string.quest_service_building_railway
    ServiceBuildingTypeCategory.OTHER_SERVICE -> R.string.quest_service_building_other
}

private val ServiceBuildingTypeCategory.iconResId: Int? get() = when (this) {
    ServiceBuildingTypeCategory.POWER -> R.drawable.ic_building_service
    ServiceBuildingTypeCategory.WATER -> R.drawable.ic_quest_service_building_water
    ServiceBuildingTypeCategory.OIL -> R.drawable.ic_building_service
    ServiceBuildingTypeCategory.GAS -> R.drawable.ic_quest_building_service_gas
    ServiceBuildingTypeCategory.TELECOM -> R.drawable.ic_building_service
    ServiceBuildingTypeCategory.RAILWAY -> R.drawable.ic_building_service
    ServiceBuildingTypeCategory.OTHER_SERVICE -> R.drawable.ic_building_service
}

private val ServiceBuildingType.iconResId: Int? get() = when (this) {
    ServiceBuildingType.POWER -> R.drawable.ic_building_service
    ServiceBuildingType.WATER ->    R.drawable.ic_quest_service_building_water
    ServiceBuildingType.TELECOM ->    R.drawable.ic_building_service
    ServiceBuildingType.GAS ->    R.drawable.ic_quest_building_service_gas
    ServiceBuildingType.OIL ->    R.drawable.ic_building_service
    ServiceBuildingType.RAILWAY ->    R.drawable.ic_building_service
    ServiceBuildingType.SEWERAGE ->    R.drawable.ic_building_service
    ServiceBuildingType.MINOR_SUBSTATION ->    R.drawable.ic_building_service
    ServiceBuildingType.SUBSTATION ->    R.drawable.ic_building_service
    ServiceBuildingType.INDUSTRIAL_SUBSTATION ->    R.drawable.ic_building_service
    ServiceBuildingType.TRACTION_SUBSTATION ->    R.drawable.ic_building_service
    ServiceBuildingType.SWITCHGEAR ->    R.drawable.ic_building_service
    ServiceBuildingType.PLANT ->    R.drawable.ic_building_service
    ServiceBuildingType.GAS_PRESSURE_REGULATION ->    R.drawable.ic_building_service
    ServiceBuildingType.GAS_PUMPING_STATION ->    R.drawable.ic_building_service
    ServiceBuildingType.WATER_WELL ->    R.drawable.ic_quest_service_building_water
    ServiceBuildingType.COVERED_RESERVOIR ->    R.drawable.ic_quest_service_building_water
    ServiceBuildingType.WATER_PUMPING_STATION ->    R.drawable.ic_quest_service_building_water
    ServiceBuildingType.OIL_PUMPING_STATION ->    R.drawable.ic_building_service
    ServiceBuildingType.RAILWAY_VENTILATION_SHAFT ->    R.drawable.ic_building_service
    ServiceBuildingType.RAILWAY_SIGNAL_BOX ->    R.drawable.ic_building_service
    ServiceBuildingType.RAILWAY_ENGINE_SHED ->    R.drawable.ic_building_service
    ServiceBuildingType.RAILWAY_WASH ->    R.drawable.ic_building_service
    ServiceBuildingType.HEATING ->    R.drawable.ic_building_service
    ServiceBuildingType.VENTILATION_SHAFT ->    R.drawable.ic_building_service
    ServiceBuildingType.TELECOM_EXCHANGE ->    R.drawable.ic_building_service
    ServiceBuildingType.MONITORING_STATION ->    R.drawable.ic_building_service
    ServiceBuildingType.OTHER_SERVICE ->    R.drawable.ic_building_service
}

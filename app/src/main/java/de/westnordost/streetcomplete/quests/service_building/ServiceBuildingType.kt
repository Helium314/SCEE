package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.quests.service_building.ServiceBuildingType.*
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item3

// todo: UtilityType instead? that way the quest could also extend stuff to street_cabinet
//  just have different Categories then
//  possibly extend to marker? utility_pole? man_hole?
// CAREFUL: utility is what it's for, water pumping station can be sewage or power
// https://wiki.openstreetmap.org/wiki/Key:utility
// https://wiki.openstreetmap.org/wiki/Tag:building%3Dservice
// https://wiki.openstreetmap.org/wiki/Tag:man_made%3Dpumping_station

// like BuildingType
enum class ServiceBuildingType(val tags: List<Pair<String, String>>) {
    // todo: check these whether they actually apply to service buildings
    POWER(listOf("utility" to "power")), // also street cabinet
    TELECOM(listOf("utility" to "telecom")), // also street cabinet
    WATER(listOf("utility" to "water")),
    GAS(listOf("utility" to "gas")),
    OIL(listOf("utility" to "oil")),
    RAILWAY(listOf()),
    // below is not really for service buildings
//    CHEMICAL(listOf("utility" to "chemical")),

    SEWERAGE(listOf("utility" to "sewerage", "substance" to "sewage")), // can be pumping stations or treatment plants
    // https://commons.wikimedia.org/wiki/Category:Abwasserpumpstation_(Rosenstra%C3%9Fe,_Saarbr%C3%BCcken)

//    HYDRANT(listOf("utility" to "hydrant")),
//    STREET_LIGHTING(listOf("utility" to "street_lighting")), // only street cabinet

    // POWER
    MINOR_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "minor_distribution")),
    // https://commons.wikimedia.org/wiki/File:Trafostation_%22Station_hinter_dem_Weinbergweg%22.jpg
    SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "distribution")),
    // https://commons.wikimedia.org/wiki/Category:Umspannwerk_Stangenm%C3%BChle
    INDUSTRIAL_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "industrial")),
    TRACTION_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "traction")), // kinda related with railway
    // https://commons.wikimedia.org/wiki/File:Woburn_rail_traction_substation,_Lower_Hutt,_New_Zealand.JPG
    SWITCHGEAR(listOf("power" to "switchgear")),
    // outside: https://commons.wikimedia.org/wiki/File:Painted_switchgear_in_Minsk_near_Janki_Kupaly_museum_2.jpg; inside: https://commons.wikimedia.org/wiki/File:Factory_Salasel_electric_services_switchgear_made_in_oman.jpg
    PLANT(listOf("power" to "plant")),
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

    // HEATING
    HEATING(listOf("utility" to "heating", "substance" to "hot_water")),
    // https://commons.wikimedia.org/wiki/File:Cornell_University_Central_Heating_Plant.jpg

    VENTILATION_SHAFT(listOf("man_made" to "ventilation")), // todo: should remove building tag (handle in applyAnswerTo, not here)
    // https://commons.wikimedia.org/wiki/File:Maryina_Roshcha_ventilation_shaft_in_Moscow.jpg
    // TELECOM
    COMMUNICATION(listOf("utility" to "communication")),
    INTERNET_EXCHANGE(listOf("telecom" to "internet_exchange")),
    TELECOM_EXCHANGE(listOf("telecom" to "exchange")),
    // https://commons.wikimedia.org/wiki/File:Telecom_Exchange_-_geograph.org.uk_-_5360464.jpg

    // MONITORING
    MONITORING_STATION(listOf("man_made" to "monitoring_station")),
    // https://commons.wikimedia.org/wiki/File:River_monitoring_station,_Coytrahen_-_geograph.org.uk_-_4224388.jpg

}

// like BuildingTypeCategory
enum class ServiceBuildingTypeCategory(val type: ServiceBuildingType?, val subTypes: List<ServiceBuildingType>) {
    POWER(ServiceBuildingType.POWER, listOf(MINOR_SUBSTATION)),
    WATER(ServiceBuildingType.WATER, listOf(WATER_WELL, COVERED_RESERVOIR, WATER_PUMPING_STATION)),
    OIL(ServiceBuildingType.OIL, listOf(OIL_PUMPING_STATION)),
    GAS(ServiceBuildingType.GAS, listOf(GAS_PUMPING_STATION, GAS_PRESSURE_REGULATION)),
    TELECOM(ServiceBuildingType.TELECOM, listOf(ServiceBuildingType.TELECOM, ServiceBuildingType.COMMUNICATION, ServiceBuildingType.INTERNET_EXCHANGE, ServiceBuildingType.TELECOM_EXCHANGE)), // this is an unselectable category (here the ok button should disappear, but that's same for building type quest)
    RAILWAY(ServiceBuildingType.RAILWAY, listOf(RAILWAY_VENTILATION_SHAFT, RAILWAY_SIGNAL_BOX, RAILWAY_ENGINE_SHED, RAILWAY_WORKSHOP, RAILWAY_WASH)),
    VENTILATION_SHAFT(ServiceBuildingType.VENTILATION_SHAFT, emptyList()) // here is a category without entries -> this would be better than above, but looks like entries of category above...
}

fun Collection<ServiceBuildingType>.toItems() = map { it.asItem() }
fun Array<ServiceBuildingTypeCategory>.toItems() = map { it.asItem() }

fun ServiceBuildingType.asItem(): GroupableDisplayItem<ServiceBuildingType> {
    val iconResId = null // looks strange without icons, but icons are work
//    val titleResId = titleResId -> later
    val title = CharSequenceText(tags.toString())
    return Item3(this, iconResId, title, null)
}

fun ServiceBuildingTypeCategory.asItem(): GroupableDisplayItem<ServiceBuildingType> {
    val iconResId = null // always or just for now?
//    val titleResId = titleResId ?: return null
    val title = CharSequenceText(type.toString()) // todo: actual name
    return Item3(type, iconResId, title, null, subTypes.toItems())
}

// works as a simple list with missing icons (ugly)
// categories not working

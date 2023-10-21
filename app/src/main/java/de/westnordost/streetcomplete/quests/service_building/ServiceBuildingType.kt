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
//    HYDRANT(listOf("utility" to "hydrant")),
//    STREET_LIGHTING(listOf("utility" to "street_lighting")), // only street cabinet

    // POWER
    MINOR_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "minor_distribution")),
    SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "distribution")),
    INDUSTRIAL_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "industrial")),
    TRACTION_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "traction")), // kinda related with railway
    SWITCHGEAR(listOf("power" to "switchgear")),
    PLANT(listOf("power" to "plant")),
    //GAS
    GAS_PRESSURE_REGULATION(listOf("utility" to "gas", "pipeline" to "substation", "substation" to "distribution", "substance" to "gas")),
    // WATER
    WATER_WELL(listOf("utility" to "water", "man_made" to "water_well", "substance" to "water")),
    COVERED_RESERVOIR(listOf("utility" to "water", "man_made" to "reservoir_covered", "substance" to "water")),
    WATER_PUMPING_STATION(listOf("utility" to "water", "man_made" to "pumping_station", "substance" to "water")),
    // OIL
    OIL_PUMPING_STATION(listOf("utility" to "oil", "man_made" to "pumping_station", "substance" to "oil")),
    OIL_VALVE(listOf("utility" to "oil", "pipeline" to "substation", "substation" to "valve", "substance" to "oil")), // in reality mostly without building
    GAS_PUMPING_STATION(listOf("utility" to "gas", "man_made" to "pumping_station", "substane" to "gas")),
    // RAILWAY
    RAILWAY_VENTILATION_SHAFT(listOf("service" to "ventilation", "railway" to "ventilation_shaft")), // 374x in use
    RAILWAY_SIGNAL_BOX(listOf("railway" to "signal_box")), // there is no service-tag; DE: Elektronisches Stellwerk; 562x in use
    RAILWAY_ENGINE_SHED(listOf("railway" to "engine_shed")), // there is no service-tag; 13x in use
    RAILWAY_WORKSHOP(listOf("railway" to "workshop")), // there is no service-tag; 4x in use
    RAILWAY_WASH(listOf("railway" to "wash")), // there is no service-tag; DE: Zug-Waschanlage; 5x in use

    // HEATING
    HEATING(listOf("utility" to "heating", "substance" to "hot_water")),

    VENTILATION_SHAFT(listOf("man_made" to "ventilation")), // todo: should remove building tag (handle in applyAnswerTo, not here)
    // TELECOM
    COMMUNICATION(listOf("utility" to "communication")),
    INTERNET_EXCHANGE(listOf("telecom" to "internet_exchange")),
    TELECOM_EXCHANGE(listOf("telecom" to "exchange")),

    // MONITORING
    MONITORING_STATION(listOf("man_made" to "monitoring_station")),

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

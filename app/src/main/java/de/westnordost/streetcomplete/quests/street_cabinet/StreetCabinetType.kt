package de.westnordost.streetcomplete.quests.street_cabinet

sealed interface StreetCabinetTypeAnswer
enum class StreetCabinetType(val osmKey: String, val osmValue: String) : StreetCabinetTypeAnswer {
    POSTAL_SERVICE("street_cabinet", "postal_service"),
    TRAFFIC_CONTROL("street_cabinet", "traffic_control"),
    TRAFFIC_MONITORING("street_cabinet", "traffic_monitoring"),
    TRANSPORT_MANAGEMENT("street_cabinet", "transport_management"),
    WASTE("street_cabinet", "waste"),
    TELEVISION("utility", "television"),
    GAS("utility", "gas"),
    POWER("utility", "power"),
    STREET_LIGHTING("utility", "street_lighting"),
    TELECOM("utility", "telecom"),
    WATER("utility", "water"),
    SEWERAGE("utility", "sewerage");
}

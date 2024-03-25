package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*

enum class BuildingTypeCategory(val type: BuildingType?, val subTypes: List<BuildingType>) {
    RESIDENTIAL(
        BuildingType.RESIDENTIAL, listOf(
        DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, HOUSE, FARM, HUT, BUNGALOW, STATIC_CARAVAN,
        DORMITORY, HOUSEBOAT,
    )),
    COMMERCIAL(
        BuildingType.COMMERCIAL, listOf(
        OFFICE, RETAIL, KIOSK, INDUSTRIAL, WAREHOUSE, HOTEL, STORAGE_TANK, PARKING, HANGAR,
    )),
    CIVIC(
        BuildingType.CIVIC, listOf(
        SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, SPORTS_HALL, TRAIN_STATION, TRANSPORTATION,
        TRANSIT_SHELTER, COLLEGE, GOVERNMENT, TOILETS, STADIUM, FIRE_STATION, OFFICE, GRANDSTAND
    )),
    OUTBUILDING(
        BuildingType.OUTBUILDING, listOf(
        SHED, GARAGE, GARAGES, CARPORT, SERVICE, TRANSFORMER_TOWER, ALLOTMENT_HOUSE, GREENHOUSE, ROOF,
        BOATHOUSE, CONTAINER, TENT, GUARDHOUSE,
    )),
    RELIGIOUS(
        BuildingType.RELIGIOUS, listOf(
        CHURCH, CATHEDRAL, CHAPEL, PRESBYTERY, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE
    )),
    FOR_FARMS(null, listOf(
        FARM, FARM_AUXILIARY, SILO, GREENHOUSE, STORAGE_TANK, SHED, BARN, COWSHED, STABLE, STY
    )),
    OTHER(null, listOf(
        ROOF, TOWER, BUNKER, BRIDGE, RIDING_HALL, ELEVATOR, DIGESTER, HISTORIC, ABANDONED, RUINS, TOMB
    )),
}
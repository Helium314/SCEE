package de.westnordost.streetcomplete.data.osm.osmquests

object OsmQuestTable {
    const val NAME = "osm_quests"

    object Columns {
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val ID = "id"
        const val LATITUDE = "latitude"
        const val LATITUDE_MAX = "latitude_max"
        const val LONGITUDE = "longitude"
        const val LONGITUDE_MAX = "longitude_max"
    }

    const val CREATE = """
        CREATE VIRTUAL TABLE $NAME USING rtree(
            ${Columns.ID},
            ${Columns.LATITUDE},
            ${Columns.LATITUDE_MAX},
            ${Columns.LONGITUDE},
            ${Columns.LONGITUDE_MAX},
            +${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            +${Columns.ELEMENT_ID} int NOT NULL,
            +${Columns.ELEMENT_TYPE} varchar(255) NOT NULL
        );
    """
}

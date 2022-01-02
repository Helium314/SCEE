package de.westnordost.streetcomplete.data.osm.osmquests

import android.os.SystemClock
import android.util.Log
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LATITUDE_MAX
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LONGITUDE_MAX
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME
import de.westnordost.streetcomplete.data.queryIn
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import javax.inject.Inject

/** Persists OsmQuest objects, or more specifically, OsmQuestEntry objects */
class OsmQuestDao @Inject constructor(private val db: Database) {

    fun put(quest: OsmQuestDaoEntry) {
        db.replace(NAME, quest.toPairs())
    }

    fun get(key: OsmQuestKey): OsmQuestDaoEntry? =
        db.queryOne(NAME,
            //where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            //args = arrayOf(key.elementType.name, key.elementId, key.questTypeName)
            // why don't the lines above find anything, but the line below does?
//            where = "$ELEMENT_TYPE = '${key.elementType.name}' AND $ELEMENT_ID = ${key.elementId} AND $QUEST_TYPE = '${key.questTypeName}'"
            where = "$ID = '${key.questIndex()}'"
        ) { it.toOsmQuestEntry() }

    fun delete(key: OsmQuestKey): Boolean =
        db.delete(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(key.elementType.name, key.elementId, key.questTypeName)
        ) == 1

    fun putAll(quests: Collection<OsmQuestDaoEntry>) {
        if (quests.isEmpty()) return
        // replace because even if the quest already exists in DB, the center position might have changed
        db.replaceMany(NAME,
            arrayOf(ID, LATITUDE, LATITUDE_MAX, LONGITUDE, LONGITUDE_MAX, QUEST_TYPE, ELEMENT_TYPE, ELEMENT_ID),
            quests.map { arrayOf(
                it.questIndex(),
                it.position.latitude,
                it.position.latitude,
                it.position.longitude,
                it.position.longitude,
                it.questTypeName,
                it.elementType.name,
                it.elementId
            ) }
        )
    }

    fun getAllForElements(keys: Collection<ElementKey>): List<OsmQuestDaoEntry> {
        if (keys.isEmpty()) return emptyList()
        return db.queryIn(NAME,
            whereColumns = arrayOf(ELEMENT_TYPE, ELEMENT_ID),
            whereArgs = keys.map { arrayOf(it.type.name, it.id) }
        ) { it.toOsmQuestEntry() }
    }

    fun getAllInBBox(bounds: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuestDaoEntry> {
        var builder = inBoundsSql(bounds)
        if (questTypes != null) {
            if (questTypes.isEmpty()) return emptyList()
            val questTypesStr = questTypes.joinToString(",") { "'$it'" }
            builder += " AND $QUEST_TYPE IN (${questTypesStr})"
        }
        return db.query(NAME, where = builder) { it.toOsmQuestEntry() }
    }

    fun deleteAll(keys: Collection<OsmQuestKey>) {
        if (keys.isEmpty()) return
        db.transaction {
            for(key in keys) {
                delete(key)
            }
        }
    }

    fun clear() {
        db.delete(NAME)
    }
}

private fun inBoundsSql(bbox: BoundingBox) = """
    $LATITUDE <= ${bbox.max.latitude} AND
    $LATITUDE_MAX >= ${bbox.min.latitude} AND
    $LONGITUDE <= ${bbox.max.longitude} AND
    $LONGITUDE_MAX >= ${bbox.min.longitude}
""".trimIndent()

private fun CursorPosition.toOsmQuestEntry(): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(
    ElementType.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    getString(QUEST_TYPE),
    LatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
)

private fun OsmQuestDaoEntry.toPairs() = listOf(
    ID to questIndex(),
    LATITUDE to position.latitude,
    LATITUDE_MAX to position.latitude,
    LONGITUDE to position.longitude,
    LONGITUDE_MAX to position.longitude,
    QUEST_TYPE to questTypeName,
    ELEMENT_TYPE to elementType.name,
    ELEMENT_ID to elementId
)

private fun OsmQuestKey.questIndex() =
    (elementId.toString() + questTypeName + elementType.name).hashCode() // should better be long, not int

private fun OsmQuestDaoEntry.questIndex() =
    (elementId.toString() + questTypeName + elementType.name).hashCode() // should better be long, not int

data class BasicOsmQuestDaoEntry(
    override val elementType: ElementType,
    override val elementId: Long,
    override val questTypeName: String,
    override val position: LatLon
) : OsmQuestDaoEntry

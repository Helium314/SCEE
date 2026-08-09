package de.westnordost.streetcomplete.quests.osmose

import android.content.res.Resources
import android.database.sqlite.SQLiteException
import android.os.LocaleList
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.CLASS
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.ELEMENTS
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.ANSWERED
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.ITEM
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.LATITUDE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.LEVEL
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.SUBTITLE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.TITLE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.UUID
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.NAME
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.measuredMultiPolygonArea
import de.westnordost.streetcomplete.util.Mockable
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.util.Locale

@Mockable
class OsmoseDao(
    private val db: Database,
    private val prefs: Preferences,
) : KoinComponent {
    private val client by lazy { HttpClient() }

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()

    private val ignoredItems = hashSetOf<Int>()
    private val ignoredItemClassCombinations = hashSetOf<String>()
    private val ignoredSubtitles = hashSetOf<String>()
    private val allowedLevels = hashSetOf<Int>()
    init { reloadIgnoredItems() }
    fun reloadIgnoredItems() {
        val ignored = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_ITEMS, OSMOSE_DEFAULT_IGNORED_ITEMS).split("§§")
        ignoredItems.clear()
        ignoredItemClassCombinations.clear()
        ignoredSubtitles.clear()
        val itemClassRegex = "\\d+/\\d+".toRegex()
        ignored.forEach {
            val i = it.trim().ifEmpty { return@forEach }
            when {
                i.toIntOrNull() != null -> ignoredItems.add(i.toInt())
                i.contains('/') && i.matches(itemClassRegex) -> ignoredItemClassCombinations.add(i)
                else -> ignoredSubtitles.add(i)
            }
        }
        allowedLevels.clear()
        allowedLevels.addAll(
            prefs.getString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, "").split("%2C").mapNotNull { it.toIntOrNull() }
        )
    }

    suspend fun download(bbox: BoundingBox): List<ExternalSourceQuest> {
        // https://osmose.openstreetmap.fr/api/0.3/issues.csv?zoom=18&item=xxxx&level=1&limit=500&bbox=16.412324309349064%2C48.18403988244578%2C16.41940534114838%2C48.1871908341706
        // replace bbox
        val csvUrl = "https://osmose.openstreetmap.fr/api/0.3/issues.csv"
        val zoom = 16 // what is the use?
        val level = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, "")
        if (level.isEmpty()) return emptyList()
        val url = "$csvUrl?zoom=$zoom&item=xxxx&level=$level&limit=500&bbox=${bbox.min.longitude}%2C${bbox.min.latitude}%2C${bbox.max.longitude}%2C${bbox.max.latitude}"
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.url(url)
        requestBuilder.header("User-Agent", USER_AGENT)
        if (prefs.getBoolean(PREF_OSMOSE_APP_LANGUAGE, false)) {
            val locale = getSelectedLocales(prefs)[0]
            if (locale != null)
                requestBuilder.header("Accept-Language", locale.toString())
        }
        Log.d(TAG, "downloading for bbox: $bbox using request $url")
        val issues = mutableListOf<OsmoseIssue>()
        try {
            val response = client.get(requestBuilder)
            val body: String = response.body() ?: return emptyList()
            // drop first, it's just column names
            // drop last, it's an empty line
            // trim each line because there was some additional newline in logs (maybe windows line endings?)
            val bodylines = body.split("\n").drop(1).dropLast(1)
            Log.d(TAG, "got ${bodylines.size} problems")

            val downloadTimestamp = nowAsEpochMilliseconds()
            db.replaceMany(NAME,
                arrayOf(UUID, ITEM, CLASS, LEVEL, TITLE, SUBTITLE, LATITUDE, LONGITUDE, ELEMENTS, ANSWERED, TIMESTAMP),
                bodylines.mapNotNull {
                    val split = it.trim().split(splitRegex) // from https://stackoverflow.com/questions/53997728/parse-csv-to-kotlin-list
                    if (split.size != 14) {
                        Log.i(TAG, "skip line, not split into 14 items: $split")
                        null
                    } else {
                        val item = split[2].toIntOrNull()
                        val itemClass = split[3].toIntOrNull()
                        val itemLevel = split[4].toIntOrNull()
                        val lat = split[11].toDoubleOrNull()
                        val lon = split[12].toDoubleOrNull()
                        if (item == null || itemClass == null || itemLevel == null || lat == null || lon == null) {
                            Log.i(TAG, "skip line, could not parse some numbers: $split")
                            return@mapNotNull null
                        }
                        issues.add(OsmoseIssue(
                            split[0], item, itemClass, itemLevel, split[5], split[6], LatLon(lat, lon), parseElementKeys(split[13])
                        ))
                        arrayOf(split[0], item, itemClass, itemLevel, split[5], split[6], lat, lon, split[13], 0, downloadTimestamp)
                    }
                }
            )
            db.delete(NAME, where = "${inBoundsSql(bbox)} AND $TIMESTAMP < $downloadTimestamp") // delete old issues inside the bbox
        } catch (e: Exception) {
            Log.e(TAG, "error while downloading / inserting: ${e.message}", e)
        }
        return issues.mapNotNull { it.toQuest() }
    }

    fun getQuest(uuid: String): ExternalSourceQuest? =
        db.queryOne(NAME, where = "$UUID = '$uuid' AND $ANSWERED = 0") { it.toOsmoseIssue().toQuest() }

    fun getIssue(uuid: String): OsmoseIssue? =
        db.queryOne(NAME, where = "$UUID = '$uuid' AND $ANSWERED = 0") { c -> c.toOsmoseIssue().takeIf { !it.isIgnored() } }

    fun getAllQuests(bbox: BoundingBox): List<ExternalSourceQuest> =
        db.query(NAME, where = "${inBoundsSql(bbox)} AND $ANSWERED = 0") {
            it.toOsmoseIssue()
        }.mapNotNull { it.toQuest() }

    private fun OsmoseIssue.toQuest(): ExternalSourceQuest? =
        if (isIgnored()) null
        else
            ExternalSourceQuest(
                uuid,
                if (elements.size == 1) mapDataWithEditsSource.getGeometry(elements.single().type, elements.single().id) ?: ElementPointGeometry(position)
                else ElementPointGeometry(position),
                questTypeRegistry.getByName("OsmoseQuest") as ExternalSourceQuestType,
                position
        ).apply { if (elements.size == 1) elementKey = elements.single() }
                // same area limitation as AddForestLeafType
                .takeIf { ((it.geometry as? ElementPolygonsGeometry)?.polygons?.measuredMultiPolygonArea() ?: 0.0) < 10000 }

    suspend fun reportChange(uuid: String, falsePositive: Boolean) {
        val url = "https://osmose.openstreetmap.fr/api/0.3/issue/$uuid/" +
            if (falsePositive) "false"
            else "done"
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.header("User-Agent", USER_AGENT)
        requestBuilder.url(url)
        try {
            client.get(requestBuilder)
            db.delete(NAME, where = "$UUID = '$uuid'")
        } catch (e: IOException) {
            // just do nothing, so it's later tried again (hopefully...)
            Log.i(TAG, "error while uploading: ${e.message} to $url")
        }
    }

    // no need to report done here, as each "done" should be connected to an element edit
    suspend fun reportFalsePositives() {
        try {
            db.query(NAME, where = "$ANSWERED = 1") {
                Pair(it.getString(UUID), it.getInt(ANSWERED) == 1)
            }.forEach { reportChange(it.first, it.second) }
        } catch (e: SQLiteException) {
            // SQLiteException: no such table: osmose_issues_v2 (code 1): , while compiling: SELECT * FROM osmose_issues_v2 WHERE answered = 1
            // user didn't even enable osmose quest -> in this case unused osmose quest should not cause a crash
            // but actually: why isn't table created? it's in database helper init!
            Log.w(TAG, "Osmose table not found when trying to report false positives")
        }
    }

    // assume it exists if it's unclear
    suspend fun doesIssueStillExist(uuid: String): Boolean {
        val url = "https://osmose.openstreetmap.fr/api/0.3/issue/$uuid"
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.header("User-Agent", USER_AGENT)
        requestBuilder.url(url)
        return try {
            val r = client.get(requestBuilder)
            val body: String = r.body()
            if (body.contains("not a valid uuid") || body.contains("not present in database")) {
                db.delete(NAME, where = "$UUID = '$uuid'")
                false
            } else true
        } catch (e: IOException) {
            // just do nothing, so it's later tried again (hopefully...)
            Log.i(TAG, "error checking existence of $uuid: ${e.message}")
            true
        }
    }

    fun setAsFalsePositive(uuid: String) {
        db.update(NAME, values = listOf(ANSWERED to 1), where = "$UUID = '$uuid'",
            conflictAlgorithm = ConflictAlgorithm.IGNORE
        )
    }

    fun setDone(uuid: String) {
        db.update(NAME, values = listOf(ANSWERED to -1), where = "$UUID = '$uuid'",
            conflictAlgorithm = ConflictAlgorithm.IGNORE
        )
    }

    fun setNotAnswered(uuid: String) {
        db.update(NAME, values = listOf(ANSWERED to 0), where = "$UUID = '$uuid'",
            conflictAlgorithm = ConflictAlgorithm.IGNORE
        )
    }

    fun delete(uuid: String) = db.delete(NAME, where = "$UUID = '$uuid'") > 0

    fun deleteOlderThan(timestamp: Long) {
        db.delete(NAME, where = "$TIMESTAMP < $timestamp")
    }

    fun clear() {
        db.delete(NAME)
    }

    private fun OsmoseIssue.isIgnored() = item in ignoredItems
        || level !in allowedLevels
        || subtitle in ignoredSubtitles
        || "$item/$itemClass" in ignoredItemClassCombinations

}

private const val TAG = "OsmoseDao"

data class OsmoseIssue(
    val uuid: String,
    val item: Int,
    val itemClass: Int,
    val level: Int,
    val title: String,
    val subtitle: String,
    val position: LatLon,
    val elements: List<ElementKey>,
)

private fun CursorPosition.toOsmoseIssue() = OsmoseIssue(
    getString(UUID),
    getInt(ITEM),
    getInt(CLASS),
    getInt(LEVEL),
    getString(TITLE).intern(),
    getString(SUBTITLE).intern(),
    LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
    parseElementKeys(getString(ELEMENTS))
)

private fun parseElementKeys(elementString: String): List<ElementKey> {
    return try {
        elementString.split("_").mapNotNull { e ->
            when {
                e.startsWith("node") -> e.substringAfter("node").toLongOrNull()?.let { ElementKey(ElementType.NODE, it) }
                e.startsWith("way") -> e.substringAfter("way").toLongOrNull()?.let { ElementKey(ElementType.WAY, it) }
                e.startsWith("relation") -> e.substringAfter("relation").toLongOrNull()?.let { ElementKey(ElementType.RELATION, it) }
                else -> null
            }
        }
    } catch (e: Exception) {
        Log.w(TAG, "could not parse element string: $elementString")
        emptyList()
    }
}

object OsmoseTable {
    const val NAME = "osmose_issues_v2"
    private const val NAME_INDEX = "osmose_issues_spatial_index"

    object Columns {
        const val UUID = "uuid"
        const val ITEM = "item"
        const val CLASS = "class"
        const val LEVEL = "level"
        const val TITLE = "title"
        const val SUBTITLE = "subtitle"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val ELEMENTS = "elements"
        const val ANSWERED = "answered"
        const val TIMESTAMP = "download_timestamp"
    }

    const val CREATE_IF_NOT_EXISTS = """
        CREATE TABLE IF NOT EXISTS $NAME (
            $UUID varchar(255) PRIMARY KEY NOT NULL,
            $ITEM int NOT NULL,
            $CLASS int NOT NULL,
            $LEVEL int NOT NULL,
            $TITLE text,
            $SUBTITLE text,
            $LATITUDE float NOT NULL,
            $LONGITUDE float NOT NULL,
            $ELEMENTS text,
            $ANSWERED int NOT NULL,
            $TIMESTAMP int NOT NULL
        );
    """

    const val CREATE_SPATIAL_INDEX_IF_NOT_EXISTS = """
        CREATE INDEX IF NOT EXISTS $NAME_INDEX ON $NAME (
            $LATITUDE,
            $LONGITUDE
        );
    """

}

private val splitRegex = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

private fun inBoundsSql(bbox: BoundingBox): String = """
    ($LATITUDE BETWEEN ${bbox.min.latitude} AND ${bbox.max.latitude}) AND
    ($LONGITUDE BETWEEN ${bbox.min.longitude} AND ${bbox.max.longitude})
""".trimIndent()


const val PREF_OSMOSE_ITEMS = "qs_OsmoseQuest_blocked_items"
const val PREF_OSMOSE_LEVEL = "qs_OsmoseQuest_level"
const val PREF_OSMOSE_APP_LANGUAGE = "qs_OsmoseQuest_app_language" // do not use the quest settings prefix here, as it doesn't make sense for language

// items that have associated SC quests/overlays are disabled by default
// same for issues related to ignored relation types
// §§ is used as separator
const val OSMOSE_DEFAULT_IGNORED_ITEMS =
    "3230/32301" + "§§" + // "Probably only for bottles, not any type of glass"
        "4061/40610" + "§§" + // "object needs review" (fixme poi "quest")
        "7130/71301" + "§§" + // "Missing maxheight tag"
        "2060/1" + "§§" + // "addr:housenumber or addr:housename without addr:street, addr:district, addr:neighbourhood, addr:quarter, addr:suburb, addr:place or addr:hamlet must be in a associatedStreet relation"
        "3250" + "§§" + // "Invalid Opening Hours" (will be not be asked immediately, but frequently re-surveyed, at least of the option is on)
        "shop=yes is unspecific. Please replace ''yes'' by a specific value." + "§§" +
//    alternative for all languages: 9002/9002007 and contains "shop=yes" or "shop = yes" (thanks, translator)
        "barrier=yes is unspecific. Please replace ''yes'' by a specific value." + "§§" +
        "traffic_calming=yes is unspecific. Please replace ''yes'' by a specific value" + "§§" +
        "amenity=recycling without recycling:*" + "§§" +
//    alternative for all languages: 9001/9001001 and contains "recycling:*"
        "amenity=recycling without recycling_type=container or recycling_type=centre" + "§§" +
//    alternative for all languages: 9001/9001001 and contains all 3 tags
        "emergency=fire_hydrant without fire_hydrant:type" + "§§" +
//    alternative for all languages: 9001/9001001 and contains "emergency=fire_hydrant" and "fire_hydrant:type"
        "Combined foot- and cycleway without segregated." + "§§" +
//    alternative for all languages: 9001/9001001 and contains "segregated"
        "leisure=pitch without sport" + "§§" +
//    alternative for all languages and types: 9001/9001001 and contains "leisure=pitch" and "sport"
        "The tag `parking:lane:both` is deprecated in favour of `parking:both`" + "§§" +
        "The tag `parking:lane:left` is deprecated in favour of `parking:left`" + "§§" +
        "The tag `parking:lane:right` is deprecated in favour of `parking:right`" + "§§" +
//    alternative for all languages and types: 4010 and contains "parking:lane:*" and "parking:<same>"
        "The tag `parking:orientation` is deprecated in favour of `orientation`" + "§§" +
        "Same value of cycleway:left and cycleway:right" + "§§" + // there is no quest, but SC may cause this and does not understand the "fix"
//    alternative for all languages: 9001 and contains "cycleway:left" and "cycleway:right"
// "tracktype=grade4 together with surface=asphalt" -> how to do it properly? current system won't work, or needs blacklisting all combinations
//    alternative for all languages and types: 9001/9001001 and contains "tracktype=" and "surface="
        "female=yes together with male=yes" + "§§" + // this is not necessarily the same as unisex
        // relation-related stuff below
        "1260" + "§§" + // Osmosis_Relation_Public_Transport
        "2140" + "§§" + // missing tags on public transport relations / stops
        "1140" + "§§" + // missing tag or role
        "1200" + "§§" + //  1-member relation
        "9007" + "§§" // various relation related issues, usually missing tags

// copy so it compiles
private fun getSelectedLocale(prefs: Preferences): Locale? {
    val languageTag = prefs.language ?: ""
    return if (languageTag.isEmpty()) null else Locale.forLanguageTag(languageTag)
}

private fun getSelectedLocales(prefs: Preferences): LocaleList {
    val locale = getSelectedLocale(prefs)
    val systemLocales = getSystemLocales()
    return if (locale == null) systemLocales else systemLocales.addedToFront(locale)
}

private fun getSystemLocales(): LocaleList =
    Resources.getSystem().configuration.locales

private fun LocaleList.addedToFront(locale: Locale): LocaleList {
    val currentList = toList().filterNot { it == locale }.toTypedArray()
    return LocaleList(locale, *currentList)
}

private fun LocaleList.toList(): List<Locale> = (0 until size()).map { i -> this[i]!! }

package de.westnordost.streetcomplete.quests.atpsync

import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.atpsync.AtpsyncTable.Columns.ID
import de.westnordost.streetcomplete.quests.atpsync.AtpsyncTable.Columns.LATITUDE
import de.westnordost.streetcomplete.quests.atpsync.AtpsyncTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.quests.atpsync.AtpsyncTable.Columns.TAGS
import de.westnordost.streetcomplete.quests.atpsync.AtpsyncTable.NAME
import de.westnordost.streetcomplete.util.logs.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException

class AtpsyncDao(
    private val db: Database,
) : KoinComponent {
    private val client by lazy { OkHttpClient() }

    private val questTypeRegistry: QuestTypeRegistry by inject()

    fun download(bbox: BoundingBox): List<ExternalSourceQuest> {
        val url = "https://atpsync.vfosnar.cz/scee_quests_v1.csv"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
        Log.d(TAG, "downloading using request $url")
        val issues = mutableListOf<AtpsyncIssue>()
        try {
            val response = client.newCall(request.build()).execute()
            val body = response.body() ?: return emptyList()
            // drop first, it's just column names
            // drop last, it's an empty line
            // trim each line because there was some additional newline in logs (maybe windows line endings?)
            val bodylines = body.string().split("\n").drop(1).dropLast(1)
            Log.d(TAG, "got ${bodylines.size} missing locations")

            db.delete(NAME);
            db.insertMany(NAME,
                arrayOf(ID, LATITUDE, LONGITUDE, TAGS),
                bodylines.mapNotNull {
                    val split = it.trim().split(',')
                    val id = split[0]
                    val lat = split[1].toDouble()
                    val lon = split[2].toDouble()
                    val tags = split.subList(3, split.size).joinToString(",")
                    Log.d(TAG, "tags: $tags")
                    issues.add(AtpsyncIssue(id, LatLon(lat, lon), tags))
                    arrayOf(id, lat, lon, tags)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "error while downloading / inserting: ${e.message}", e)
        }
        return issues.mapNotNull { it.toQuest() }
    }

    fun getQuest(id: String): ExternalSourceQuest? =
        db.queryOne(NAME, where = "$ID = '$id'") { it.toAtpsyncIssue().toQuest() }

    fun getIssue(id: String): AtpsyncIssue? =
        db.queryOne(NAME, where = "$ID = '$id'") { c -> c.toAtpsyncIssue() }

    fun getAllQuests(bbox: BoundingBox): List<ExternalSourceQuest> =
        db.query(NAME, where = "${inBoundsSql(bbox)}") {
            it.toAtpsyncIssue()
        }.mapNotNull { it.toQuest() }

    private fun AtpsyncIssue.toQuest(): ExternalSourceQuest? =
        ExternalSourceQuest(
            id,
            ElementPointGeometry(position),
            questTypeRegistry.getByName(AtpsyncQuest::class.simpleName!!) as ExternalSourceQuestType,
            position
        )

    fun reportChange(id: String) {
        // val url = "https://osmose.openstreetmap.fr/api/0.3/issue/$uuid/" +
        //     if (falsePositive) "false"
        //     else "done"
        // val request = Request.Builder().header("User-Agent", USER_AGENT).url(url).build()
        try {
            // client.newCall(request).execute()
            db.delete(NAME, where = "$ID = '$id'")
        } catch (e: IOException) {
            // just do nothing, so it's later tried again (hopefully...)
            Log.i(TAG, "error while uploading: ${e.message}")
        }
    }

    fun delete(id: String) = db.delete(NAME, where = "$ID = '$id'") > 0

    fun clear() {
        db.delete(NAME)
    }
}

private const val TAG = "AtpsyncDao"

data class AtpsyncIssue(
    val id: String,
    val position: LatLon,
    val tags: String,
)

private fun CursorPosition.toAtpsyncIssue() = AtpsyncIssue(
    getString(ID),
    LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
    getString(TAGS)
)


object AtpsyncTable {
    const val NAME = "atpsync_issues_v1"
    private const val NAME_INDEX = "atpsync_issues_v1_issues_spatial_index"

    object Columns {
        const val ID = "id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val TAGS = "tags"
    }

    const val CREATE_IF_NOT_EXISTS = """
        CREATE TABLE IF NOT EXISTS $NAME (
            $ID varchar(1023) PRIMARY KEY NOT NULL,
            $LATITUDE float NOT NULL,
            $LONGITUDE float NOT NULL,
            $TAGS text
        );
    """

    const val CREATE_SPATIAL_INDEX_IF_NOT_EXISTS = """
        CREATE INDEX IF NOT EXISTS $NAME_INDEX ON $NAME (
            $LATITUDE,
            $LONGITUDE
        );
    """

}

private fun inBoundsSql(bbox: BoundingBox): String = """
    ($LATITUDE BETWEEN ${bbox.min.latitude} AND ${bbox.max.latitude}) AND
    ($LONGITUDE BETWEEN ${bbox.min.longitude} AND ${bbox.max.longitude})
""".trimIndent()

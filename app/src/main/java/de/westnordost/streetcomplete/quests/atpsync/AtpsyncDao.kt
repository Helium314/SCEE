package de.westnordost.streetcomplete.quests.atpsync

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException

class AtpsyncDao(
    private val db: Database,
) : KoinComponent {
    private val client by lazy { OkHttpClient() }

    private val questTypeRegistry: QuestTypeRegistry by inject()

    fun download(bbox: BoundingBox): List<ExternalSourceQuest> {
        val url =
            "${BASE_URL}/api/1/scee/quests?lon_min=${bbox.min.longitude}&lon_max=${bbox.max.longitude}&lat_min=${bbox.min.latitude}&lat_max=${bbox.max.latitude}"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
        Log.d(TAG, "downloading for bbox: $bbox using request $url")
        val missingLocations = mutableListOf<AtpsyncMissingLocation>()
        try {
            val response = client.newCall(request.build()).execute()
            val body = response.body() ?: return emptyList()
            val questDtos = missingLocationJsonAdapter.fromJson(body.string())!!

            // there is no better way to check if the data have been removed from the server than
            // to purge locally stored data every time
            db.delete(NAME)
            db.insertMany(NAME,
                arrayOf(ID, LATITUDE, LONGITUDE, TAGS),
                questDtos.map {
                    missingLocations.add(
                        AtpsyncMissingLocation(
                            it.id,
                            LatLon(it.lat.toDouble(), it.lon.toDouble()),
                            it.tags
                        )
                    )
                    arrayOf(
                        stringHashmapJsonAdapter.toJson(it.id),
                        it.lat,
                        it.lon,
                        stringHashmapJsonAdapter.toJson(it.tags)
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "error while downloading / inserting: ${e.message}", e)
        }
        return missingLocations.mapNotNull { it.toQuest() }
    }

    fun getQuest(id: String): ExternalSourceQuest? =
        db.queryOne(NAME, where = "$ID = '$id'") { it.toAtpsyncIssue().toQuest() }

    fun getMissingLocation(id: String): AtpsyncMissingLocation? =
        db.queryOne(NAME, where = "$ID = '$id'") { c -> c.toAtpsyncIssue() }

    fun getAllQuests(bbox: BoundingBox): List<ExternalSourceQuest> =
        db.query(NAME, where = inBoundsSql(bbox)) {
            it.toAtpsyncIssue()
        }.mapNotNull { it.toQuest() }

    private fun AtpsyncMissingLocation.toQuest(): ExternalSourceQuest? =
        ExternalSourceQuest(
            stringHashmapJsonAdapter.toJson(id),
            ElementPointGeometry(position),
            questTypeRegistry.getByName(AtpsyncQuest::class.simpleName!!) as ExternalSourceQuestType,
            position
        )

    fun reportChange(id: String) {
        val url = "$BASE_URL/api/1/scee/quest"
        val request = Request.Builder()
            .header("User-Agent", USER_AGENT)
            .url(url)
            .delete(RequestBody.create(MediaType.get("application/json"), id))
            .build()
        try {
            client.newCall(request).execute()
            db.delete(NAME, where = "$ID = '$id'")
        } catch (e: IOException) {
            Log.i(TAG, "error while uploading: ${e.message}")
        }
    }

    fun delete(id: String) = db.delete(NAME, where = "$ID = '$id'") > 0

    fun clear() {
        db.delete(NAME)
    }
}

const val TAG = "AtpsyncDao"

private const val BASE_URL = "https://atpsync.dev.vfosnar.cz"

private val stringHashmapJsonAdapter: JsonAdapter<Map<String, String>> = Moshi.Builder().build()
    .adapter(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

private val missingLocationJsonAdapter: JsonAdapter<List<AtpsyncMissingLocationDto>> =
    Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        .adapter(
            Types.newParameterizedType(
                List::class.java,
                AtpsyncMissingLocationDto::class.java
            )
        )

class AtpsyncMissingLocationDto(
    val id: Map<String, String>,
    val lat: Float,
    val lon: Float,
    val tags: Map<String, String>,
)

data class AtpsyncMissingLocation(
    val id: Map<String, String>,
    val position: LatLon,
    val tags: Map<String, String>,
)

private fun CursorPosition.toAtpsyncIssue() = AtpsyncMissingLocation(
    stringHashmapJsonAdapter.fromJson(getString(ID))!!,
    LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
    stringHashmapJsonAdapter.fromJson(getString(TAGS))!!
)

object AtpsyncTable {
    const val NAME = "atpsync_missing_locations_v1"
    private const val NAME_INDEX = "atpsync_missing_locations_v1_spatial_index"

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

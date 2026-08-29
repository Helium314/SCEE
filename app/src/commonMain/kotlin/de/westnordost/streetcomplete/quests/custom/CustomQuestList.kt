package de.westnordost.streetcomplete.quests.custom

import android.net.Uri
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.Mockable
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.writeString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.Exception

@Mockable
class CustomQuestList : KoinComponent {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val entriesById by lazy {
        // need to load by lazy, because there is a problem if mapDataWithEditsSource is accessed early
        val m = hashMapOf<String, CustomQuestEntry>()
        load(m)
        m
    }

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val questController: ExternalSourceQuestController by inject()

    fun reload() = load(entriesById)

    fun readFromUri(uri: Uri) {
        runBlocking { PlatformFile(uri).copyTo(customQuestFile) }
        reload()
    }

    fun load(m: MutableMap<String, CustomQuestEntry>) {
        m.clear()
        if (!customQuestFile.exists()) {
            try {
                customQuestFile.parent()?.createDirectories()
                runBlocking { customQuestFile.writeString("") }
            } catch (_: Exception) {
                // sometimes can't be created, don't show an error message in this case
                return
            }
        }
        runBlocking {
            m.putAll(customQuestFile.readString().lines().asReversed().mapNotNull { line ->
                val rawText = line.substringAfter(',').substringAfter(',')
                val text = if (rawText.endsWith(",solved"))
                        rawText.substringBeforeLast(',')
                    else rawText
                val id = line.getId()
                if (id == null) null
                else
                    id to CustomQuestEntry(id).also {
                        it.text = text
                        it.solved = rawText.endsWith(",solved")
                    }
            })
        }
    }

    fun addEntry(element: Element, message: String) {
        val id = "${element.type},${element.id}".getId() ?: return
        if (entriesById.containsKey(id)) return
        val entry = CustomQuestEntry(id).apply { text = message }
        entriesById[id] = entry
        customQuestFile.sink(true).buffered().writeString("\n$id,$message")
        getQuest(id)?.let { questController.addQuests(listOf(it)) }
    }

    fun getEntry(id: String) = entriesById[id]

    fun getQuest(id: String): ExternalSourceQuest? {
        val entry = getEntry(id) ?: return null
        if (entry.solved) return null
        val geometry = entry.elementKey?.let { mapDataWithEditsSource.getGeometry(it.type, it.id) }
            ?: entry.position?.let { ElementPointGeometry(it) } ?: return null
        return ExternalSourceQuest(
            id,
            geometry,
            questTypeRegistry.getByName(/*CustomQuest::class.simpleName!!*/"CustomQuest") as ExternalSourceQuestType, // todo
            geometry.center
        ).apply { entry.elementKey?.let { elementKey = it } }
    }

    fun get(bbox: BoundingBox): List<ExternalSourceQuest> {
        val type = questTypeRegistry.getByName(/*CustomQuest::class.simpleName!!*/"CustomQuest") as ExternalSourceQuestType
        return entriesById.values.mapNotNull { entry ->
            if (entry.solved) return@mapNotNull null
            val geometry = entry.elementKey?.let { mapDataWithEditsSource.getGeometry(it.type, it.id) }
                ?: entry.position?.let { ElementPointGeometry(it) } ?: return@mapNotNull null
            if (geometry.center !in bbox) return@mapNotNull null
            ExternalSourceQuest(entry.id, geometry, type, geometry.center)
        }
    }

    fun markSolved(id: String, solved: Boolean = true) {
        if (entriesById[id]?.solved == solved) return
        entriesById[id]?.solved = solved
        scope.launch {
            val lines = customQuestFile.readString().lines().toMutableList()
            var lineToChange = -1
            for (i in lines.indices) {
                if (lines[i].getId() == id
                    && ((solved && !lines[i].endsWith(",solved"))
                        || !solved && lines[i].endsWith(",solved"))
                ) {
                    lineToChange = i
                    break
                }
            }
            if (lineToChange == -1) return@launch // should not happen, but crashes also should not happen
            lines[lineToChange] = if (solved) lines[lineToChange] + ",solved"
            else lines[lineToChange].substringBeforeLast(',')
            customQuestFile.writeString(lines.joinToString("\n"))
        }
    }


    fun deleteSolved() { delete(entriesById.filterValues { it.solved }.map { it.key }) }

    fun delete(id: String) = delete(listOf(id))

    fun delete(idList: List<String>): Boolean {
        if (idList.isEmpty()) return false
        val ids = idList.toMutableSet()
        val deletedAny = entriesById.keys.removeAll(ids)
        val lines = runBlocking { customQuestFile.readString() }.lines().toMutableList()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val id = iterator.next().getId()
            if (id in ids) {
                iterator.remove()
                ids.remove(id)
                if (ids.isEmpty()) break
            }
        }
        runBlocking { customQuestFile.writeString(lines.joinToString("\n")) }
        return deletedAny
    }
}

private fun String.getId(): String? {
    val first = substringBefore(',').trim()
    val second = substringAfter(',').substringBefore(',').trim()
    return if ((first.matches(nodeWayRelation) && second.toLongOrNull() != null) || (first.toDoubleOrNull() != null && second.toDoubleOrNull() != null))
        "${first.uppercase()},${second.uppercase()}"
    else null
}

data class CustomQuestEntry(val id: String ) {
    val elementKey = try {
        ElementKey(ElementType.valueOf(id.substringBefore(',').uppercase()),
            id.substringAfter(',').substringBefore(',').toLong())
    } catch (e: Exception) {
        null
    }
    val position = try {
        LatLon(id.substringBefore(',').toDouble(), id.substringAfter(',').substringBefore(',').toDouble())
    } catch (e: Exception) {
        null
    }
    var text: String = ""
    var solved: Boolean = false
}

val customQuestFile = PlatformFile(FileKit.filesDir, "custom_quest.csv")

private val nodeWayRelation = "node|way|relation".toRegex(RegexOption.IGNORE_CASE)

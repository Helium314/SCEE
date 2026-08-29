package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.screens.settings.gpxNotesDir
import de.westnordost.streetcomplete.screens.settings.gpxNotesFile
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class NoteEditsControllerImpl(
    private val editsDB: NoteEditsDao,
    private val fileSystem: FileSystem
) : NoteEditsController {

    private val listeners = Listeners<NoteEditsSource.Listener>()

    private val lock = ReentrantLock()
    val scope = CoroutineScope(Dispatchers.IO)

    override fun add(
        noteId: Long,
        action: NoteEditAction,
        position: LatLon,
        text: String?,
        imagePaths: List<String>,
        track: List<Trackpoint>?,
        isGpxNote: Boolean,
    ) {
        val edit = NoteEdit(
            0,
            noteId,
            position,
            action,
            text,
            imagePaths,
            nowAsEpochMilliseconds(),
            false,
            imagePaths.isNotEmpty(),
            track,
        )
        if (isGpxNote) {
            scope.launch { createGpxNote(text ?: "", imagePaths, position, track) }
        } else {
            lock.withLock { editsDB.add(edit) }
            onAddedEdit(edit)
        }
    }

    override fun get(id: Long): NoteEdit? =
        editsDB.get(id)

    override fun getAllUnsynced(): List<NoteEdit> =
        editsDB.getAllUnsynced()

    override fun getAll(): List<NoteEdit> =
        editsDB.getAll()

    override fun getOldestUnsynced(): NoteEdit? =
        editsDB.getOldestUnsynced()

    override fun getUnsyncedCount(): Int =
        editsDB.getUnsyncedCount()

    override fun getAllUnsyncedForNote(noteId: Long): List<NoteEdit> =
        editsDB.getAllUnsyncedForNote(noteId)

    override fun getAllUnsyncedForNotes(noteIds: Collection<Long>): List<NoteEdit> =
        editsDB.getAllUnsyncedForNotes(noteIds)

    override fun getAllUnsynced(bbox: BoundingBox): List<NoteEdit> =
        editsDB.getAllUnsynced(bbox)

    override fun getAllUnsyncedPositions(bbox: BoundingBox): List<LatLon> =
        editsDB.getAllUnsyncedPositions(bbox)

    override fun getOldestNeedingImagesActivation(): NoteEdit? =
        editsDB.getOldestNeedingImagesActivation()

    override fun markImagesActivated(id: Long): Boolean =
        lock.withLock { editsDB.markImagesActivated(id) }

    override fun markSynced(edit: NoteEdit, note: Note) {
        var markSyncedSuccess = false
        for (imagePath in edit.imagePaths) {
            fileSystem.delete(Path(imagePath), mustExist = false)
        }
        lock.withLock {
            if (edit.noteId != note.id) {
                editsDB.updateNoteId(edit.noteId, note.id)
            }
            markSyncedSuccess = editsDB.markSynced(edit.id)
        }

        if (markSyncedSuccess) {
            onSyncedEdit(edit.copy(isSynced = true))
        }
    }

    override fun markSyncFailed(edit: NoteEdit): Boolean =
        delete(edit)

    override fun undo(edit: NoteEdit): Boolean =
        delete(edit)

    override fun deleteSyncedOlderThan(timestamp: Long): Int {
        var deletedCount = 0
        var deleteEdits = listOf<NoteEdit>()
        lock.withLock {
            deleteEdits = editsDB.getSyncedOlderThan(timestamp)
            if (deleteEdits.isEmpty()) return 0
            deletedCount = editsDB.deleteAll(deleteEdits.map { it.id })
        }
        onDeletedEdits(deleteEdits)
        return deletedCount
    }

    private fun delete(edit: NoteEdit): Boolean {
        for (imagePath in edit.imagePaths) {
            fileSystem.delete(Path(imagePath), mustExist = false)
        }
        val deleteSuccess = lock.withLock { editsDB.delete(edit.id) }
        if (deleteSuccess) {
            onDeletedEdits(listOf(edit))
            return false
        }
        return true
    }

    override fun updateElementIds(idUpdates: Collection<ElementIdUpdate>) {
        for (idUpdate in idUpdates) {
            val elementType = idUpdate.elementType.name.lowercase()
            editsDB.replaceTextInUnsynced(
                "osm.org/$elementType/${idUpdate.oldElementId} ",
                "osm.org/$elementType/${idUpdate.newElementId} ",
            )
        }
    }

    // there is some xmlwriter, and even gpxTrackWriter
    // maybe use this instead of the current ugly things, probably less prone to bugs caused by weird characters
    private suspend fun createGpxNote(note: String, imagePaths: List<String>, position: LatLon, recordedTrack: List<Trackpoint>?) {
        gpxNotesDir.createDirectories()
        if (!gpxNotesFile.exists())
            gpxNotesFile.writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx \n" +
                " xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                "</gpx>")
        // now delete the last 6 characters, which is <\gpx>
        val oldText = gpxNotesFile.readString().dropLast(6)
        // save image file names (this is not nice, but better than not keeping any reference to them
        val imageText = if (imagePaths.isEmpty()) "" else
            "\n images used: ${imagePaths.joinToString(", ") { it.substringAfterLast(File.separator) }}"
        val trackFile: PlatformFile?
        if (!recordedTrack.isNullOrEmpty()) {
            var i = 1
            while (PlatformFile(gpxNotesDir, "track_$i.gpx").exists()) {
                i += 1
            }
            trackFile = PlatformFile(gpxNotesDir, "track_$i.gpx")
            val formatter = DateTimeFormatter
                .ofPattern("yyyy_MM_dd'T'HH_mm_ss.SSSSSS'Z'")
                .withZone(ZoneOffset.UTC)
            val trackText = recordedTrack.map {
                "     <trkpt lon=\"${it.position.longitude}\" lat=\"${it.position.latitude}\">\n" +
                    "       <time>\"${formatter.format(Instant.ofEpochMilli(it.time))}\"</time>\n" +
                    if (it.elevation == 0.0f)
                        ""
                    else {
                        "       <ele>\"${it.elevation}\"</ele>\n" +
                            "       <hdop>\"${it.accuracy}\"</hdop>\n"
                    } +
                    "     </trkpt>"
            }
            trackFile.writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx \n" +
                " xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                "  <trk>\n" +
                "    <name>${trackFile.name.substringBefore(".gpx")}</name>\n" +
                "    <trkseg>\n" +
                trackText.joinToString("\n") + "\n" +
                "    </trkseg>\n" +
                "  </trk>\n" +
                "</gpx>")
        } else trackFile = null
        val trackText = if (trackFile == null) "" else
            "\n attached track: ${trackFile.name}"
        gpxNotesFile.writeString(oldText +" <wpt lon=\"" + position.longitude + "\" lat=\"" + position.latitude + "\">\n" +
            "  <name>" + (note + trackText + imageText).replace("&","&amp;")
            .replace("<","&lt;")
            .replace(">","&gt;")
            .replace("\"","&quot;")
            .replace("'","&apos;") + "</name>\n" +
            " </wpt>\n" +
            "</gpx>")
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: NoteEditsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: NoteEditsSource.Listener) {
        listeners.remove(listener)
    }

    private fun onAddedEdit(edit: NoteEdit) {
        listeners.forEach { it.onAddedEdit(edit) }
    }

    private fun onSyncedEdit(edit: NoteEdit) {
        listeners.forEach { it.onSyncedEdit(edit) }
    }

    private fun onDeletedEdits(edits: List<NoteEdit>) {
        listeners.forEach { it.onDeletedEdits(edits) }
    }
}

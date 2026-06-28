package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/** Manages access to the notes storage */
interface NoteController : NoteSource {
    /** Replace all notes in the given bounding box with the given notes */
    fun putAllForBBox(bbox: BoundingBox, notes: Collection<Note>)

    /** delete a note because the note does not exist anymore on OSM (has been closed) */
    fun delete(noteId: Long)

    /** put a note because the note has been created/changed on OSM */
    fun put(note: Note)

    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int

    fun clear()
}

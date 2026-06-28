package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CLOSE
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.COMMENT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.SpatialCache

interface NotesWithEditsSource {
    /** Interface to be notified of new notes, updated notes and notes that have been deleted,
    this includes not yet synced answers in addition to what NoteController would report
     */
    interface Listener {
        fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>)

        fun onCleared()
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)


    fun get(noteId: Long): Note?

    fun getAllPositions(bbox: BoundingBox): List<LatLon>

    fun getAll(bbox: BoundingBox): Collection<Note>

    fun getAll(noteIds: Collection<Long>): Collection<Note>
}

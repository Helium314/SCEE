package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

interface ElementEditsController : ElementEditsSource, AddElementEditsController {

    /** Add new unsynced edit to the to-be-uploaded queue */
    override fun add(
        type: ElementEditType,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction,
        isNearUserLocation: Boolean,
        key: QuestKey?
    )

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedOlderThan(timestamp: Long): Int

    fun markSynced(edit: ElementEdit, elementUpdates: MapDataUpdates)

    fun markSyncFailed(edit: ElementEdit)

    /** Undo edit with the given id. If unsynced yet, will delete the edit if it is undoable. If
     *  already synced, will add a revert of that edit as a new edit, if possible */
    fun undo(edit: ElementEdit): Boolean
}

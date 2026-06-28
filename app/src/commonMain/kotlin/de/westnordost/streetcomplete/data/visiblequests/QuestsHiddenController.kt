package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.externalsource.ExternalSourceHiddenDao
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/** Controller for managing which quests have been hidden by user interaction. */
interface QuestsHiddenController : QuestsHiddenSource, HideQuestController {
    /** Mark the quest as hidden by user interaction */
    override fun hide(key: QuestKey)

    /** Un-hide the given quest. Returns whether it was hid before */
    fun unhide(key: QuestKey): Boolean

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int
}

package de.westnordost.streetcomplete.quests.atpsync

import androidx.appcompat.app.AlertDialog
import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept

class AtpsyncQuest(private val atpsyncDao: AtpsyncDao) : ExternalSourceQuestType {

    override var downloadEnabled: Boolean
        get() = true
        set(value) {}


    override fun getTitle(tags: Map<String, String>) = R.string.quest_atpsync_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf("")

    override fun download(bbox: BoundingBox) = atpsyncDao.download(bbox)

    override fun upload() {}

    override fun deleteMetadataOlderThan(timestamp: Long) {}

    override fun getQuests(bbox: BoundingBox) = atpsyncDao.getAllQuests(bbox)

    override fun get(id: String): ExternalSourceQuest? = atpsyncDao.getQuest(id)

    override fun deleteQuest(id: String): Boolean = atpsyncDao.delete(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) {
        Thread {
            atpsyncDao.reportChange(id)
        }.start()
    }

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {}

    override fun onSyncEditFailed(edit: ElementEdit, id: String?) {
        if (id != null) atpsyncDao.delete(id)
    }

    override suspend fun onUpload(edit: ElementEdit, id: String?): Boolean {
        return true
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        // TODO for some reason never gets called
        if (id != null)
            atpsyncDao.reportChange(id)
    }

    override val enabledInCountries: Countries
        get() = NoCountriesExcept("CZ")

    override val changesetComment = "Add missing atpsync locations"
    override val wikiLink = "atpsync"
    override val icon = R.drawable.ic_quest_atpsync
    override val defaultDisabledMessage = R.string.quest_atpsync_message
    override val source = "atpsync"

    override fun createForm() = AtpsyncForm()

    override fun getQuestSettingsDialog(context: Context): AlertDialog? = null
}

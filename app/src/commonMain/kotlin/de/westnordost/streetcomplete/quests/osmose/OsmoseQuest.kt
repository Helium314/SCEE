package de.westnordost.streetcomplete.quests.osmose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.quests.questPrefix
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import de.westnordost.streetcomplete.quests.ResetCancelOk
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference

class OsmoseQuest(private val osmoseDao: OsmoseDao) : ExternalSourceQuestType {

    override val title = Res.string.quest_osmose_title

    override suspend fun download(bbox: BoundingBox) = osmoseDao.download(bbox)

    override suspend fun upload() = osmoseDao.reportFalsePositives()

    override fun deleteMetadataOlderThan(timestamp: Long) = osmoseDao.deleteOlderThan(timestamp)

    override fun getQuests(bbox: BoundingBox) = osmoseDao.getAllQuests(bbox)

    override fun get(id: String): ExternalSourceQuest? = osmoseDao.getQuest(id)

    override fun deleteQuest(id: String): Boolean = osmoseDao.delete(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) = osmoseDao.setDone(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (edit.isSynced) return // already reported as done
        if (id != null)
            osmoseDao.setNotAnswered(id)
    }

    override fun onSyncEditFailed(edit: ElementEdit, id: String?) {
        if (id != null) osmoseDao.delete(id)
    }

    override suspend fun onUpload(edit: ElementEdit, id: String?): Boolean {
        // check whether issue still exists before uploading
        if (id == null) return true // if we don't have an id, assume it's ok
        return osmoseDao.doesIssueStillExist(id)
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        if (id != null)
            GlobalScope.launch { osmoseDao.reportChange(id, false) } // edits are never false positive
    }

    override val enabledInCountries get() = super.enabledInCountries

    override val changesetComment = "Fix osmose issues"
    override val wikiLink = "Osmose"
    override val icon = Res.drawable.ic_quest_osmose
    override val defaultDisabledMessage = Res.string.quest_osmose_message
    override val source = "osmose"

    @Composable
    override fun Form(on: (Action) -> Unit, quest: ExternalSourceQuest, countryInfo: CountryInfo) {
        OsmoseForm(on, quest)
    }

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        val levels = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, "").split("%2C").mapNotNull { it.toIntOrNull() }
        var high by remember { mutableStateOf(levels.contains(1)) }
        var medium by remember { mutableStateOf(levels.contains(2)) }
        var low by remember { mutableStateOf(levels.contains(3)) }
        var showTypeEditDialog by remember { mutableStateOf(false) }
        ConfirmationDialog(
            onDismissRequest = onDismissRequest,
            onConfirmed = {
                val levelString = listOfNotNull(
                    if (high) 1 else null,
                    if (medium) 2 else null,
                    if (low) 3 else null,
                ).takeIf { it.isNotEmpty() }?.joinToString("%2C") ?: ""
                if (levelString != prefs.getString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, OSMOSE_DEFAULT_IGNORED_ITEMS)) {
                    prefs.putString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, levelString)
                    downloadEnabled = levelString != ""
                    osmoseDao.reloadIgnoredItems()
                    OsmQuestController.reloadQuestTypes() // actually this is doing a bit more than necessary, but whatever
                }
            },
            title = { Text(stringResource(R.string.quest_osmose_title)) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { high = !high }) {
                        Checkbox(high, { high = it })
                        Text(stringResource(R.string.quest_settings_osmose_level_high))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { medium = !medium }) {
                        Checkbox(medium, { medium = it })
                        Text(stringResource(R.string.quest_settings_osmose_level_medium))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { low = !low }) {
                        Checkbox(low, { low = it })
                        Text(stringResource(R.string.quest_settings_osmose_level_low))
                    }
                    Button({ showTypeEditDialog = true }, Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.quest_osmose_settings_items))
                    }
                    SwitchPreference(
                        name = stringResource(R.string.quest_osmose_use_app_language),
                        pref = PREF_OSMOSE_APP_LANGUAGE,
                        default = false,
                        description = stringResource(R.string.quest_osmose_use_app_language_information),
                    )
                }
            }
        )
        if (showTypeEditDialog) {
            val pref = questPrefix(prefs) + PREF_OSMOSE_ITEMS
            val items = prefs.getString(pref, OSMOSE_DEFAULT_IGNORED_ITEMS).split("§§").filter { it.isNotEmpty() }.toTypedArray()
            var itemsForRemoval by remember { mutableStateOf(setOf<String>()) }
            ScrollableAlertDialog(
                onDismissRequest = { showTypeEditDialog = false },
                buttonRow = {
                    ResetCancelOk(
                        onDismissRequest = { showTypeEditDialog = false },
                        resetEnabled = prefs.contains(pref),
                        onReset = {
                            prefs.remove(pref)
                            osmoseDao.reloadIgnoredItems()
                            OsmQuestController.reloadQuestTypes()
                        },
                        okEnabled = itemsForRemoval.isNotEmpty(),
                        onOk = {
                            prefs.putString(pref, items.filterNot { it in itemsForRemoval }.joinToString("§§"))
                            osmoseDao.reloadIgnoredItems()
                            OsmQuestController.reloadQuestTypes()
                        }
                    )
                },
                content = {
                    val scroll = rememberScrollState()
                    Column(Modifier.verticalScroll(scroll)) {
                        items.forEach { item ->
                            var checked by remember { mutableStateOf(false) }
                            LaunchedEffect(checked) {
                                if (checked) itemsForRemoval += item else itemsForRemoval -= item
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { checked = !checked }) {
                                Checkbox(checked, { checked = it })
                                Text(item)
                            }
                        }
                    }
                }
            )
        }
    }
}

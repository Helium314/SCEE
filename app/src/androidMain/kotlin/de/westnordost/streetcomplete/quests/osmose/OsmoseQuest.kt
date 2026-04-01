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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.quests.questPrefix
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.quests.ResetCancelOk
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_osmose_message
import de.westnordost.streetcomplete.resources.quest_osmose_title
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference

class OsmoseQuest(private val osmoseDao: OsmoseDao) : ExternalSourceQuestType, AndroidQuest {

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

    override val enabledInCountries: Countries
        get() = super.enabledInCountries

    override val changesetComment = "Fix osmose issues"
    override val wikiLink = "Osmose"
    override val icon = R.drawable.ic_quest_osmose
    override val defaultDisabledMessage = Res.string.quest_osmose_message
    override val source = "osmose"

    override fun createForm() = OsmoseForm()

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
                buttons = {
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

const val PREF_OSMOSE_ITEMS = "qs_OsmoseQuest_blocked_items"
const val PREF_OSMOSE_LEVEL = "qs_OsmoseQuest_level"
const val PREF_OSMOSE_APP_LANGUAGE = "qs_OsmoseQuest_app_language" // do not use the quest settings prefix here, as it doesn't make sense for language

// items that have associated SC quests/overlays are disabled by default
// same for issues related to ignored relation types
// §§ is used as separator
const val OSMOSE_DEFAULT_IGNORED_ITEMS =
    "3230/32301" + "§§" + // "Probably only for bottles, not any type of glass"
    "4061/40610" + "§§" + // "object needs review" (fixme poi "quest")
    "7130/71301" + "§§" + // "Missing maxheight tag"
    "2060/1" + "§§" + // "addr:housenumber or addr:housename without addr:street, addr:district, addr:neighbourhood, addr:quarter, addr:suburb, addr:place or addr:hamlet must be in a associatedStreet relation"
    "3250" + "§§" + // "Invalid Opening Hours" (will be not be asked immediately, but frequently re-surveyed, at least of the option is on)
    "shop=yes is unspecific. Please replace ''yes'' by a specific value." + "§§" +
//    alternative for all languages: 9002/9002007 and contains "shop=yes" or "shop = yes" (thanks, translator)
    "barrier=yes is unspecific. Please replace ''yes'' by a specific value." + "§§" +
    "traffic_calming=yes is unspecific. Please replace ''yes'' by a specific value" + "§§" +
    "amenity=recycling without recycling:*" + "§§" +
//    alternative for all languages: 9001/9001001 and contains "recycling:*"
    "amenity=recycling without recycling_type=container or recycling_type=centre" + "§§" +
//    alternative for all languages: 9001/9001001 and contains all 3 tags
    "emergency=fire_hydrant without fire_hydrant:type" + "§§" +
//    alternative for all languages: 9001/9001001 and contains "emergency=fire_hydrant" and "fire_hydrant:type"
    "Combined foot- and cycleway without segregated." + "§§" +
//    alternative for all languages: 9001/9001001 and contains "segregated"
    "leisure=pitch without sport" + "§§" +
//    alternative for all languages and types: 9001/9001001 and contains "leisure=pitch" and "sport"
    "The tag `parking:lane:both` is deprecated in favour of `parking:both`" + "§§" +
    "The tag `parking:lane:left` is deprecated in favour of `parking:left`" + "§§" +
    "The tag `parking:lane:right` is deprecated in favour of `parking:right`" + "§§" +
//    alternative for all languages and types: 4010 and contains "parking:lane:*" and "parking:<same>"
    "The tag `parking:orientation` is deprecated in favour of `orientation`" + "§§" +
    "Same value of cycleway:left and cycleway:right" + "§§" + // there is no quest, but SC may cause this and does not understand the "fix"
//    alternative for all languages: 9001 and contains "cycleway:left" and "cycleway:right"
// "tracktype=grade4 together with surface=asphalt" -> how to do it properly? current system won't work, or needs blacklisting all combinations
//    alternative for all languages and types: 9001/9001001 and contains "tracktype=" and "surface="
    "female=yes together with male=yes" + "§§" + // this is not necessarily the same as unisex
    // relation-related stuff below
    "1260" + "§§" + // Osmosis_Relation_Public_Transport
    "2140" + "§§" + // missing tags on public transport relations / stops
    "1140" + "§§" + // missing tag or role
    "1200" + "§§" + //  1-member relation
    "9007" + "§§" // various relation related issues, usually missing tags

